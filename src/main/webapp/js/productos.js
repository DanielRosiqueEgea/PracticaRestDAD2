$(document).ready(function(){
	        $.get("rest/getProductos", //cada vez que se carga el documento muestra todos los productos de la hashtable
			      {},
			        function(data,status){	
			    	  console.log("V9 añadir a factura productos.js");
			    	  if(data.productos == null){ //si no hay productos en la respuesta
			    		  $('#productos').append("<br><strong> NO HAY PRODUCTOS O LA PETICION NO ES CORRECTA</strong>");
			    	  }else{
			    	  
			    	  jQuery.each(data.productos, function(i, val) {
			    		  $('#productos').append("<strong>ID: " + val.id + 
			    				  "</strong><br>Name: <input type=\"text\" id=\"nameMod"+val.id+"\" value=\""+ val.nombre+"\" >" +   
			    				  "<br>Precio: <input type=\"number\" id=\"priceMod"+val.id+"\" value=\""+val.precio+ "\">");
			    			$('#productos').append("<br><button class=\"deleteProducto\" value=\""+val.id+"\">Borrar Producto</button>");
			    			//a cada producto se le añade un boton de eliminar y uno de modificar
			    			$('#productos').append("<button class=\"modProducto\" value=\""+val.id+"\">Modificar Producto</button>");
			    	  		$('#productos').append("<button class=\"addProductoToFactura\" value=\""+val.id+"\">Add a factura</button><br><br>");
			    	  		
			    	  });
			    	  
			    	  
			    	  //a toda la clase de producto se le asigna la funcion de enviar la peticion de delete
			    	  //No se hace con document ready por que se ejecutaría antes de que existiesen los botones.
			    	  $(".deleteProducto").click(function(event){	

			  			enviarDelete(event.target.value); 
			  			/*
			  			event target value funciona de la siguiente forma:
			  			event taget es el boton específico sobre el que se ejecuta el metodo, 
			  			de esta forma diferenciamos los botones a pesar de tener la misma clase
			  			
			  			Value es el valor del boton, que hemos añadido anteriormente de forma dinamica.
			  			
			  			*/
			    	  });
			    	  $(".modProducto").click(function(event){
				  			enviarMod(event.target.value); 
				    	  });
			    	    $(".addProductoToFactura").click(function(event){
				  			addProductoToFactura(event.target.value); 
				    	  });
			    	  }
			        });
	       
	});
	
	function enviarMod(value){ // la funcion enviarMod envía un id, un nombre y un precio al servicio JSON para que lo modifique

		
			
			let id = value;
  			let nombre = $('#nameMod'+value).val();
 			let precio = $('#priceMod'+value).val();
  			console.log("id="+id +"precio= "+precio +"nombre="+nombre);
  			if (isNaN(id)|| nombre=="" || isNaN(precio) || precio=="") {
				
   				alert("Hay campos vacios");
   				return;
 			}
		var sendInfo = {producto:{
			id:value, //usamos jquery para enviar los valores de los inputs
			precio: precio,
			nombre: nombre}};
	
	
	
	    $.ajax({
	           type: "POST",
	           headers: { 
	               'Accept': 'application/json',
	               'Content-Type': 'application/json' 
	           },
	           url: "rest/modProducto",
	           dataType: "json",
	           success: function (msg) {
	               if (msg) {
	                   alert(msg.resultado); //el mensaje hay que cambiarlo para mostrar el resultado de la operacion
	                   location.reload();//recargamos la pagina para que no se muestre el producto eliminado
	               } else {
	                   alert("Error!");
	               }
	           },
	           
	           data:  JSON.stringify(sendInfo)
	    });
		
	};
	
	function addProductoToFactura(value){ 
		// la funcion envía los datos de un producto al servicio JSON para que lo añada a la factura
		//esto puede causar un problema si has modificado los datos del producto y no has pulsado Modificar Producto Anteriormente		
			let id = value;
  			let nombre = $('#nameMod'+value).val();
 			let precio = $('#priceMod'+value).val();
  			console.log("id="+id +"precio= "+precio +"nombre="+nombre);
  			if (isNaN(id)|| nombre=="" || isNaN(precio) || precio=="") {
				
   				alert("Hay campos vacios");
   				return;
 			}
		var sendInfo = {producto:{
			id:value, //usamos jquery para enviar los valores de los inputs
			precio: precio,
			nombre: nombre}};
	
	
	
	    $.ajax({
	           type: "POST",
	           headers: { 
	               'Accept': 'application/json',
	               'Content-Type': 'application/json' 
	           },
	           url: "rest/addProductoToFactura",
	           dataType: "json",
	           success: function (msg) {
	               if (msg) {
	                   alert(msg.resultado); //el mensaje hay que cambiarlo para mostrar el resultado de la operacion
	                   location.reload();//recargamos la pagina para que no se muestre el producto eliminado
	               } else {
	                   alert("Error!");
	               }
	           },
	           
	           data:  JSON.stringify(sendInfo)
	    });
		
	};
	
		
	function enviarDelete(value){ // la funcion enviar Delete envía un id al servicio JSON para que lo elimine

		var sendInfo = {id:value};
	
	    $.ajax({
	           type: "POST",
	           headers: { 
	               'Accept': 'application/json',
	               'Content-Type': 'application/json' 
	           },
	           url: "rest/deleteProducto",
	           dataType: "json",
	           success: function (msg) {
	               if (msg) {
	                   alert(msg.resultado); //el mensaje hay que cambiarlo para mostrar el resultado de la operacion
	                   location.reload();//recargamos la pagina para que no se muestre el producto eliminado
	               } else {
	                   alert("Error!");
	               }
	           },
	           
	           data:  JSON.stringify(sendInfo)
	    });
		
	};
	
	//funcion para enviar los datos de un producto a insertar
	$(document).ready(function(){
		$("#addProducto").click(function(){	
			console.log("SE va a añadir un producto");
				
			let id = $('#idNewProd').val();
  			let nombre = $('#nombreNewProd').val();
 			let precio = $('#precioNewProd').val();
  			console.log("id="+id +"precio= "+precio +"nombre="+nombre);
  			if (isNaN(id)||id==""|| nombre=="" || isNaN(precio) || precio=="") {
				
   				alert("Hay campos vacios");
   				return;
 			}
			
			var sendInfo = {producto:{
				id: id, //usamos jquery para enviar los valores de los inputs
				precio: precio,
				nombre: nombre}};
		
		    $.ajax({
		           type: "POST",
		           headers: { 
		               'Accept': 'application/json',
		               'Content-Type': 'application/json' 
		           },
		           url: "rest/addProducto",
		           dataType: "json",
		           success: function (msg) {
		               if (msg) {
		                   alert(msg.resultado); //el mensaje hay que cambiarlo para mostrar el resultado de la operacion
		                   location.reload();//recargamos la pagina para que se muestre el producto introducido
		               } else {
		                   alert("Error!");
		               }
		           },
		           
		           data:  JSON.stringify(sendInfo)
		    });
	 });
});
		