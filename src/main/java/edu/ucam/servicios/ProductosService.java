package edu.ucam.servicios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucam.pojos.Producto;

@Path("/")
public class ProductosService extends MetodosJson{
	/**
	 *  Si podemos guardar todos los productos dentro de un hashtable estatico,
	 *  Solo nos haría falta cargar y guardar el fichero en determinados casos 
	 *  Como cuando se enciende y apaga el servidor o cuando el Usuario lo requiera.
	 */
	static Hashtable<Integer,Producto> productosHT; 
	/**
	 * Lo mismo ocurre si podemos guardarlo como JSON estatico
	 * Incluso sería mejor puesto que no haría falta el transformar de uno a otro.
	 */
	static JSONObject productosJson;
	
	
	/**
	 * Metodo que se ejecuta cada vez que se carga la pagina, envía todos los productos para mostrarlos en el index.
	 * @return envío de todos los productos guardados en forma de json
	 */
	@GET
	@Path("/getProductos") 
	@Produces(MediaType.APPLICATION_JSON)
	public Response enviarProductos() {
		iniciarProductos();//
		return  Response.status(200).entity(productosJson.toString()).build(); //y lo envía 
	
	}
	/**
	 * Metodo para iniciar el JsonObject statico productosJson y el ht statico productos
	 */
	private void iniciarProductos() {
		if(productosJson == null){//si no se han iniciado ya los productos
			System.out.println("vamos a cargar el fichero productos");
			productosJson= leerJson("productos.json"); // carga el fichero Json 
			//de esta forma solo carga el fichero al iniciar el sistema
		}
		if(!productosJson.has("productos")) { // si a la hora de haber borrado se han eliminado todos los productos se produce un error
			JSONObject producto = new JSONObject(); //así que tenemos que añadir un producto basico (prevencion de errores)
			producto.put("id", 1);
			producto.put("nombre", "Tomate");
			producto.put("precio", 1.5);
			productosJson.append("productos", producto);
		}
		productosHT= jsonToHTProductos(productosJson);
	}
	
	/**
	 * Metodo para añadir un producto al ht y al JsonObject en funcion de un json envíado por el usuario
	 * @param incomingData
	 * @return
	 */
	@POST	
	@Path("/addProducto")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addProducto(InputStream incomingData) {
		
		iniciarProductos();
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
		int id = producto.getInt("id");
		
		if(productosHT.containsKey(id)) { // si existe el id (si el producto ha sido añadido anteriormente)
			jsonRespuesta.append("resultado", "El id del producto ya existe");	
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
			
		String nombre = producto.getString("nombre");
		double precio = producto.getDouble("precio");
		Producto p = new Producto(nombre,precio,id);
		productosHT.put(id, p);//crea el producto y lo añade al ht
		
		productosJson= htProductosToJson(productosHT);//combierte de nuevo el ht a Json para poder enviarlo cuando sea necesario
		jsonRespuesta.append("resultado", "El Producto se ha añadido correctamente");
		//cada vez que se modifica el JsonObject se guarda en un fichero, de esta forma siempre está la "bbdd" actualizada
		jsonRespuesta.append("resultado",guardarJson(productosJson,"productos.json")?"Se ha guardado el fichero correctamente": "Ha habido un error guardando el fichero" );
	
		return Response.status(200).entity(jsonRespuesta.toString()).build();
	}
	
	/**
	 * Metodo para borrar un producto en funcion de un id envíado por el usuario
	 * @param incomingData
	 * @return
	 */
	@POST	
	@Path("/deleteProducto")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteProducto(InputStream incomingData) {
			
		iniciarProductos();
		//Recuperamos el String correspondiente al JSON que nos envía el navegador
		StringBuilder sb = recuperarStringNavegador(incomingData);
		
		//Construimos un objeto JSON en base al recibido como cadena 
		JSONObject jsonRecibido = new JSONObject(sb.toString());
		JSONObject jsonRespuesta  = new JSONObject();
		
		//si el tamaño de alguno de los dos es menor o igual que 1 no deja eliminar el producto por motivos de seguridad
		if(productosHT.size()<=1 || productosJson.getJSONArray("productos").length()<=1) {
			jsonRespuesta.append("resultado", "No hay suficientes productos como para eliminar");
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		
		if(!jsonRecibido.has("id")) {//por si acaso comprobamos si el objeto recibido contiene una id
			jsonRespuesta.append("resultado", "Error al enviar o recibir el producto");		
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		int id= jsonRecibido.getInt("id");
			
		if(!productosHT.containsKey(id)) {//si no existe el id
			jsonRespuesta.append("resultado", "No existe el producto");
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
	
		productosHT.remove(id); // se elimina sin problemas cuando es un ht
		jsonRespuesta.append("resultado", "Se ha eliminado el producto");
		productosJson= htProductosToJson(productosHT);
		
		jsonRespuesta.append("resultado",guardarJson(productosJson,"productos.json")?"Se ha guardado el fichero correctamente": "Ha habido un error guardando el fichero" );
		
		return Response.status(200).entity(jsonRespuesta.toString()).build();
	}
	
	/**
	 * Metodo para modificar un producto enviado por el usuario, no se podrá modificar el ID por motivos de seguridad
	 * @param incomingData
	 * @return
	 */
	@POST	
	@Path("/modProducto")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modProducto(InputStream incomingData) {
			
		iniciarProductos();
		//Recuperamos el String correspondiente al JSON que nos envía el navegador
		StringBuilder sb = recuperarStringNavegador(incomingData);
		
		//Construimos un objeto JSON en base al recibido como cadena 
		JSONObject jsonRecibido = new JSONObject(sb.toString());
		JSONObject jsonRespuesta  = new JSONObject();
		
		/*
		 * si no hay productos dentro de la hashtable no hace falta continuar
		 * Esto es una medida de seguridad en caso de que algo haya salido mal a la hora de guardar/cargar el json
		 */
		if(productosHT.isEmpty()) {
			jsonRespuesta.append("resultado", "No existen productos");
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		
		if(!jsonRecibido.has("producto")) {//por si acaso comprobamos si el objeto recibido contiene un producto
			jsonRespuesta.append("resultado", "Error al enviar o recibir el producto");		
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
		JSONObject producto = jsonRecibido.getJSONObject("producto");
		int id = producto.getInt("id");
		
		if(!productosHT.containsKey(id)) { // si existe el id (si el producto ha sido añadido anteriormente)
			jsonRespuesta.append("resultado", "El id del producto no existe");	
			return Response.status(200).entity(jsonRespuesta.toString()).build();
		}
			
		String nombre = producto.getString("nombre");
		double precio = producto.getDouble("precio");
		Producto p = new Producto(nombre,precio,id);
		productosHT.put(id, p);//crea el producto y lo añade al ht
		jsonRespuesta.append("resultado", "Se ha modificado el producto");
		productosJson= htProductosToJson(productosHT);
		
		jsonRespuesta.append("resultado",guardarJson(productosJson,"productos.json")?"Se ha guardado el fichero correctamente": "Ha habido un error guardando el fichero" );
		
		return Response.status(200).entity(jsonRespuesta.toString()).build();
	}	
}
