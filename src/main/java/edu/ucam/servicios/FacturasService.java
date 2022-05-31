package edu.ucam.servicios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucam.pojos.Factura;
import edu.ucam.pojos.Producto;

@Path("/")
public class FacturasService extends MetodosJson{
	/**
	 * las facturas solo van a mostrarse en facturas HTML
	 * La funcionalidad es la siguiente en referencia a las facturas:
	 * C -> Create -> se crea la factura en index.html, su creacion es similar a la de una "cesta de la compra"
	 * 		A la hora de pulsar el boton de "generar factura" la factura actual se inserta en la HT 
	 * R -> Read -> las facturas se muestran en facturas.html, la factura actual se muestra en index.html
	 * 		Ambas se muestran automaticamente cuadno se carga la pagina (Document.Ready)
	 * U -> Update -> En sí las facturas no se pueden modificar, solo la Factura actual (la "cesta de la compra"
	 * 		Esto puede verse como una medida de seguridad para que los clientes no modifiquen una factura ya creada
	 * D -> Delete -> Las facturas se eliminan del HT desde facturas.html, se saca la factura del HT
	 */ 		
	 
	/**TODO Al crear el ID de factura en funcion del tamaño hay que cambiar la forma de crearlo para que no haya colisiones*/ 
	 
	static Hashtable<Integer,Factura> facturasHT = new Hashtable<Integer, Factura>();

	
	/*
	 * realmente el objeto JSon generado es igual que el de produto salvo que tiene dos campos más llamados id y precio->
	 * productosJson -> {Productos:[{id:1,nombre:tomate,precio:1.5},{id:2,nombre:patata,precio:2}]}
	 * facturaJson ->{id:1,precio:3.5,Productos:[{id:1,nombre:tomate,precio:1.5},{id:2,nombre:patata,precio:2}]}
	 */
	
	static JSONObject facturasJson;
		
	/*
	 * la factura actual nos sirve como "cesta de la compra" hasta que no se pulsa el boton "Generar Factura" no se guarda dentro del HT
	 */
	static Factura facturaActual;
	

	
	/**
	 * Metodo que se ejecuta cada vez que se carga la pagina index.html, envía los datos de la factura actual mostrarlos en el index.
	 * @return envío de todos los facturas guardados en forma de json
	 */
	@GET
	@Path("/getFacturaActual") 
	@Produces(MediaType.APPLICATION_JSON)
	public Response enviarFacturaActual() {
		iniciarFacturaActual();//
		return  Response.status(200).entity(FacturaToJson(facturaActual).toString()).build();
	
	}
	
	@GET
	@Path("/getFacturas") 
	@Produces(MediaType.APPLICATION_JSON)
	public Response enviarFacturas() {
		iniciarFacturas();//
		return  Response.status(200).entity(facturasJson.toString()).build();
	
	}
	
	private void iniciarFacturas() {
		facturasJson = leerJson("facturas.json"); // se carga el fichero json
		facturasHT= jsonToHTfacturas(facturasJson);//se convierte el json a HT
	}
	
	/**
	 * Metodo para iniciar la factura actual
	 */
	private void iniciarFacturaActual() {
		if(facturaActual==null) {// si la factura actual no existe hay que crearla
			facturasJson = leerJson("facturas.json"); // se carga el fichero json
			int id=0;//Si no hay facturas el primer id será 0
			
			if(facturasJson.has("facturas")) {//si hay facturas en el fichero json
				facturasHT= jsonToHTfacturas(facturasJson);//se convierte el json a HT
				int i=0;
				for(i=0;i<facturasHT.size();i++) {//se recorre todo el hashtable por si hay un hueco y se usa i como id
					if(!facturasHT.containsKey(i)) {
						break;
					}
				}
				id=i;//para mantener el numero de facturas actualizado;
			}
			facturaActual=new Factura(id);
		}
	}
	
	/**
	 * Metodo para añadir un producto al ht y al JsonObject en funcion de un json envíado por el usuario
	 * @param incomingData
	 * @return
	 */
	@POST	
	@Path("/addProductoToFactura")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addProductoToFactura(InputStream incomingData) {
		
		iniciarFacturaActual();
		//Recuperamos el String correspondiente al JSON que nos envía el navegador
		StringBuilder sb = recuperarStringNavegador(incomingData);
		
		//Construimos un objeto JSON en base al recibido como cadena 
		JSONObject jsonRecibido = new JSONObject(sb.toString());
		JSONObject jsonRespuesta  = new JSONObject();
		
		
		if(!jsonRecibido.has("producto")) {//por si acaso comprobamos si el objeto recibido es un producto
		jsonRespuesta.append("resultado", "Error al enviar o recibir producto");
		return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		
		JSONObject producto = jsonRecibido.getJSONObject("producto");
	
		int id =producto.getInt("id");
		
	
		String nombre = producto.getString("nombre");
		double precio = producto.getDouble("precio");
		Producto p = new Producto(nombre,precio,id);
	
		facturaActual.addProducto(p);
		
		jsonRespuesta.append("resultado", "El Producto se ha añadido correctamente");
	
		return Response.status(200).entity(jsonRespuesta.toString()).build();
	}
	
	/**
	 * Metodo para borrar un producto en funcion de un id envíado por el usuario
	 * @param incomingData
	 * @return
	 */
	@POST	
	@Path("/guardarFactura")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response safeFactura(InputStream incomingData) {
			
		iniciarFacturaActual();
		iniciarFacturas();
	
		JSONObject jsonRespuesta  = new JSONObject();
		
		if(facturaActual.getProductos().isEmpty()) {
			jsonRespuesta.append("resultado", "No hay productos en la factura");
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		
		
		facturasHT.put(facturaActual.getId(), facturaActual);
		facturasJson = htFacturasToJson(facturasHT);
		jsonRespuesta.append("resultado",guardarJson(facturasJson,"facturas.json")?"Se ha guardado el fichero correctamente": "Ha habido un error guardando el fichero" );
		facturaActual=null;
		
		jsonRespuesta.append("resultado", "Se ha guardado la factura");
		//facturasJson= htfacturasToJson(facturasHT);

		
		return Response.status(200).entity(jsonRespuesta.toString()).build();
	}
	
	
	/**
	 * Metodo para borrar un producto en funcion de un id envíado por el usuario
	 * @param incomingData
	 * @return
	 */
	@POST	
	@Path("/delProdFactura")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delProdFactura(InputStream incomingData) {
			
		iniciarFacturaActual();
		//Recuperamos el String correspondiente al JSON que nos envía el navegador
		StringBuilder sb = recuperarStringNavegador(incomingData);
		
		//Construimos un objeto JSON en base al recibido como cadena 
		JSONObject jsonRecibido = new JSONObject(sb.toString());
		JSONObject jsonRespuesta  = new JSONObject();
		
		//si el tamaño de alguno de los dos es menor o igual que 1 no deja eliminar el producto por motivos de seguridad
		if(facturaActual.getProductos().size()<1) {
			jsonRespuesta.append("resultado", "No hay suficientes facturas como para eliminar");
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		
		if(!jsonRecibido.has("id")) {//por si acaso comprobamos si el objeto recibido contiene una id
			jsonRespuesta.append("resultado", "Error al enviar o recibir el producto");		
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		int id= jsonRecibido.getInt("id");
			
		if((facturaActual.getProductos().size()<id)) {//si no existe el id
			jsonRespuesta.append("resultado", "No existe el producto");
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
	
		facturaActual.removeProducto(id); // se elimina sin problemas cuando es un ht
		jsonRespuesta.append("resultado", "Se ha eliminado el producto");
		//facturasJson= htfacturasToJson(facturasHT);
		
		
		
		return Response.status(200).entity(jsonRespuesta.toString()).build();
	}
	
	
	/**
	 * Metodo para borrar un producto en funcion de un id envíado por el usuario
	 * @param incomingData
	 * @return
	 */
	@POST	
	@Path("/deleteFactura")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteFactura(InputStream incomingData) {
			
		iniciarFacturas();
		//Recuperamos el String correspondiente al JSON que nos envía el navegador
		StringBuilder sb = recuperarStringNavegador(incomingData);
		
		//Construimos un objeto JSON en base al recibido como cadena 
		JSONObject jsonRecibido = new JSONObject(sb.toString());
		JSONObject jsonRespuesta  = new JSONObject();
		
		//si no hay facturas no se elimina
		if(facturasHT.size()<0) {
			jsonRespuesta.append("resultado", "No hay facturas para eliminar");
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		
		if(!jsonRecibido.has("id")) {//por si acaso comprobamos si el objeto recibido contiene una id
			jsonRespuesta.append("resultado", "Error al enviar o recibir la factura");		
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		int id= jsonRecibido.getInt("id");
		facturasHT.remove(id);
		
		jsonRespuesta.append("resultado", "Se ha eliminado la factura");
		//facturasJson= htfacturasToJson(facturasHT);
		
		facturasJson= htFacturasToJson(facturasHT);
		
		jsonRespuesta.append("resultado",guardarJson(facturasJson,"facturas.json")?"Se ha guardado el fichero correctamente": "Ha habido un error guardando el fichero" );
		
		
		return Response.status(200).entity(jsonRespuesta.toString()).build();
	}
	
	
	

	
	
}