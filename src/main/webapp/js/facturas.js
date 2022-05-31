 $(document).ready(function(){
    $.get("rest/getFacturas", //cada vez que se carga el documento muestra la factura actual
	      {},
	        function(data,status){	
	    	  console.log("V1 facturas.js");

	    		  jQuery.each(data.facturas, function(i, val) {
		    		  $('#tablaFacturas').append("<tr><td id=\"col"+val.id+"\"><h3>ID Factura: " + val.id + 
		    				  " Precio Factura: "+ val.precio+" Euros<h3></td></tr>");
		    			  
	    		  jQuery.each(val.productos, function(i, prod) {
		    		  $('#col'+val.id).append("<br><strong>ID Prod: " + prod.id + 
		    				  "</strong><br>Name:"+ prod.nombre +   
		    				  "<br>Precio: " + prod.precio );		
		    	  });	  
		    				  
		    		$('#col'+val.id).append("<br><button class=\"delFactura\" value=\""+val.id+"\">Borrar Factura</button>");
		    			
		    	  		
		    	  });
				 $(".delFactura").click(function(event){	

			  			enviarDelFactura(event.target.value); 
			  	
			    	  });
				
	    	  });
	    	  
	    	  
	    	  });
	    	  function enviarDelFactura(value){ // la funcion enviar Delete envía un id al servicio JSON para que lo elimine
		var sendInfo = {id:value};
	
	    $.ajax({
	           type: "POST",
	           headers: { 
	               'Accept': 'application/json',
	               'Content-Type': 'application/json' 
	           },
	           url: "rest/deleteFactura",
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
	