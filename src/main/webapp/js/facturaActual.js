 $(document).ready(function(){
    $.get("rest/getFacturaActual", //cada vez que se carga el documento muestra la factura actual
	      {},
	        function(data,status){	
	    	  console.log("V7 facturas.js");
	    	  console.log(data.id);
	    	  var id = data.id;
	    	  console.log(id==null);
	    	  if(id != null){ 
		
	    		  $('#headerActual').append(id+" Precio ->"+data.precio.toFixed(2)+"Euros");
	    		  
	    		  jQuery.each(data.productos, function(i, val) {
		    		  $('#facturaActual').append("<br><strong>ID: " + val.id + 
		    				  "</strong><br>Name:"+ val.nombre +   
		    				  "<br>Precio: " + val.precio );
		    			$('#facturaActual').append("<br><button class=\"delProdFactura\" value=\""+val.id+"\">Borrar Producto</button>");
		    			
		    	  		
		    	  });
				 $(".delProdFactura").click(function(event){	

			  			enviarDelProdFactura(event.target.value); 
			  	
			    	  });
				
	    	  }
	    	 
	    	 });
	    	    	 
});
function enviarDelProdFactura(value){ // la funcion enviar Delete envía un id al servicio JSON para que lo elimine
		console.log("Vamos a borrar el producto: "+value);
		var sendInfo = {id:value};
	
	    $.ajax({
	           type: "POST",
	           headers: { 
	               'Accept': 'application/json',
	               'Content-Type': 'application/json' 
	           },
	           url: "rest/delProdFactura",
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
	
	
	$(document).ready(function(){
		$("#generarFactura").click(function(){	
			console.log("Se va a guardar la factura");
				
			
			var sendInfo = {bool:1};
		
		    $.ajax({
		           type: "POST",
		           headers: { 
		               'Accept': 'application/json',
		               'Content-Type': 'application/json' 
		           },
		           url: "rest/guardarFactura",
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

	
 
 