package edu.ucam.servicios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucam.pojos.Factura;
import edu.ucam.pojos.Producto;

public abstract class MetodosJson {
	
	/*
	 *Con el objetivo de modularizar aun más el programa hemos extraido los metodos comunes entre los dos servicios 
	 *Esto sirve para mantener sus clases limpias con solo lo necesario para el servicio concreto 
	 *Ahora cualquier servicio que extienda esta clase abstracta dispondrá de estos metodos como están o podrán darle su propia implementación
	 */

	/**
	 * Metodo para leer JSON desdde fichero propio
	 * @param fichero ruta del fichero a leer
	 * @return objeto JsonLeido
	 */
	
	public JSONObject leerJson(String fichero) {
		//Recuperamos el String correspondiente al JSON que queremos desde la ruta del fichero
				StringBuilder sb = new StringBuilder();//creamos un string builder para ir montando el JSon
				try {
					FileInputStream file = new FileInputStream(fichero); 
					BufferedReader in = new BufferedReader(new InputStreamReader(file));
					String line = null;
					while ((line = in.readLine()) != null) {//leemos todo el fichero y lo añadimos a la linea
						sb.append(line);
					}
					in.close();
					return new JSONObject(sb.toString()); //finalmente devolvemos el objeto Json
				} catch (Exception e) {
					e.printStackTrace();
				}
		
		return new JSONObject(); //en caso de dar fallo se envía un objeto vacío para que no haya problemas de null pointer
	}
	/**
	 * Metodo para guardar un objeto Json en un Fichero
	 * @param objetoJson objeto Json a guardar dentro del fichero
	 * @param fichero ruta del fichero que se va a crear
	 * @return true en caso de guardarse correctamente, false en caso contrario
	 */
	public boolean guardarJson(JSONObject objetoJson,String fichero) {
		 try {
			 File file = new File (fichero); 
			 file.createNewFile();//en caso de no existir el fichero debería crearse, este metodo es por si acaso eso no sucede
			 FileWriter fileWriter = new FileWriter(fichero);
			 PrintWriter out = new PrintWriter(fileWriter);
	            out.write(objetoJson.toString());
	            out.close();
	            return true;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }

	}
	
	/**
	 * Metodo extraido para guardar el String que envía el navegador 
	 * Se extrae por motivos de modularidad pues el mismo metodo se usará en cada uno de los otros metodos
	 * @param incomingData 
	 * @return string builder con todos los datos json enviados para que se convierta correctamente a JSONObject
	 */
	public StringBuilder recuperarStringNavegador(InputStream incomingData) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			in.close();
		} catch (Exception e) {
			System.out.println("Error Parsing: Data sent not good ");
		}
		return sb;
	}
	
	
	/**
	 * Metodo para transformar de un Json de facturas a un Hashtable de facturas
	 * @param json objeto json a transformar
	 * @return devuelve la hashtable formada con los campos ID, factura 
	 */
	public  Hashtable<Integer, Factura> jsonToHTfacturas(JSONObject json){
	    Hashtable<Integer, Factura> ht = new Hashtable<Integer, Factura>();
	    if(!json.has("facturas"))return ht;
	    JSONArray facturasJson =json.getJSONArray("facturas");
	    
	    for(int i=0;i<facturasJson.length();i++) {//para cada factura del JSON
	    	JSONObject facturaJson = facturasJson.getJSONObject(i);//se saca la factura actual
	    	int idFactura = facturaJson.getInt("id");//se guarda el Id de la factura
	    	//double precioFactura = facturasJson.getJSONObject(i).getDouble("precio");//se guarda el precio
	    	Factura f = new Factura(idFactura);//se inicia la factura
	    	for(Producto p:jsonToHTProductos(facturaJson).values()) {//añade todos los productos encontrados a la factura
	    			 f.addProducto(p);
	    	}
	    	ht.put(idFactura, f);
	    }
	    System.out.println(ht);	    
	    return ht;
	}
	/**
	 * Metodo para pasar de Hashtable de facturas a JSON
	 * @param htfacturas
	 * @return
	 */
	public JSONObject htFacturasToJson(Hashtable<Integer,Factura> htfacturas) {
		JSONObject facturasJson = new JSONObject();
		for(Factura f: htfacturas.values()) {
			facturasJson.append("facturas", FacturaToJson(f));			
		}
		return facturasJson;
	}
	/**
	 * Metodo para convertir una unica factura a JSON
	 * @param factura factura a convertir a JSON
	 * @return JSON Generado
	 */
	public JSONObject FacturaToJson(Factura factura) {
		JSONObject facturaJson = new JSONObject();
		
		
		facturaJson.put("id", factura.getId());
		facturaJson.put("precio", factura.getPrecio());
		for(int i=0;i<factura.getProductos().size();i++) {
			JSONObject producto = new JSONObject();
			Producto p = factura.getProductos().get(i);
			producto.put("id", i);
			producto.put("nombre", p.getNombre());
			producto.put("precio", p.getPrecio());
			facturaJson.append("productos", producto);			
		}		
		System.out.println(facturaJson.toString());
		return facturaJson;
	}
	
	/*
	 * Para el manejo de la BBDD es más comodo el Hashtable pero puede hacer que nuestro codigo se vuelva más feo
	 * Con Json el manejo sería más complejo pero quizás el codigo sería más limpio hemos optado por usar ht para el manejo
	 * La razon es porque hay un metodo ya definido para añadir,borrar y modificar dentro de la ht que no hay con json
	 */
	
	
	/**
	 * Metodo para transformar de un Json de productos a un Hashtable de productos
	 * @param json objeto json a transformar
	 * @return devuelve la hashtable formada con los campos ID, producto
	 */
	public Hashtable<Integer, Producto> jsonToHTProductos(JSONObject json){
	    Hashtable<Integer, Producto> ht = new Hashtable<Integer, Producto>();
	    JSONArray productosJson =json.getJSONArray("productos");
	    if(productosJson == null)return ht;
	    for(int i=0;i<productosJson.length();i++) {
	    	JSONObject productoJson = productosJson.getJSONObject(i);
	    	int id = productoJson.getInt("id");
	    	String nombre = productoJson.getString("nombre");
	    	double precio = (productoJson.getDouble("precio"));
	    	Producto p = new Producto(nombre,precio,id);
	    	ht.put(id, p);
	    }
	    System.out.println(ht);	    
	    return ht;
	}
	/**
	 * Metodo para convertir una hashtable de productos a Json object 
	 * @param htProductos hashtable a convertir a JSON
	 * @return Json obtenido de la hashtable
	 */
	public JSONObject htProductosToJson(Hashtable<Integer,Producto> htProductos) {
		JSONObject productosJson = new JSONObject();
		for(Producto p: htProductos.values()) {
			JSONObject producto = new JSONObject();
			producto.put("id", p.getId());
			producto.put("nombre", p.getNombre());
			producto.put("precio", p.getPrecio());
			productosJson.append("productos", producto);			
		}
		return productosJson;
	}
	
	

	
	
}
