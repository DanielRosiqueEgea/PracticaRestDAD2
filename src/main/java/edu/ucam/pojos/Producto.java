package edu.ucam.pojos;

public class Producto {

	private double precio;
	private String nombre;
	private int id;
	
	public Producto(String nombre,double precio, int id) {
		this.precio= precio;
		this.nombre=nombre;
		this.id= id;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		this.precio = precio;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String toString() {
		String datos = "";
		datos = "\nID PRODUCTO: "+this.id; 
		datos += "\nNOMBRE PRODUCTO: " +this.nombre;
		datos += "\nPRECIO PRODUCTO: "+this.precio;
		return datos;
	}
	
}
