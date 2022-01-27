(function($) {
	  $('input, textarea').placeholder();
	  var userAgent = navigator.userAgent.toLowerCase();
	    $.browser = {
	        version: (userAgent.match(/.+(?:rv|it|ra|ie)[\/: ]([\d.]+)/) || [])[1],
	        safari: /webkit/.test(userAgent),
	        opera: /opera/.test(userAgent),
	        msie: /msie/.test(userAgent) && !/opera/.test(userAgent),
	        mozilla: /mozilla/.test(userAgent) && !/(compatible|webkit)/.test(userAgent)
	    };
	   toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	   var support=$.uploadPreview({
		    input_field: "#backgroundfile",
		    preview_box: "#bgpreview",
		    no_label: true
		  });
		   $.uploadPreview({
			    input_field: "#logofile",
			    preview_box: "#logopreview",
			    no_label: true
			  });
	   $("#backgroundfile").on("change",function(e){
		   var filevalue = this.value;  
           var index = filevalue.lastIndexOf('.');  
           if(index > -1){
        	   if(filevalue.substring(index - 1).toLocaleLowerCase() != "png"){
        		   toastr.error("图片格式错误，只能上传png格式图片！");
        		   this.outerHTML = this.outerHTML;
        	   } else {
        		   if( $.browser.msie &&  parseInt($.browser.version)<=9){
        			   $('#bgimg').attr('src', $(this).val()); 
        		   }else{
        			   $("#bgimg").remove();
        		   }
        	   } 
        	   
           } else {
        	   this.outerHTML = this.outerHTML;
           }
	   });
	   for(var i=1;i<=23;i++){
		   $("#executedate").append('<option value="'+i+'">'+(i<10?("0"+i):(""+i))+'</option>');
	   }
	   if($("#executedate").data("value")!=""){
		   $("#executedate").val($("#executedate").data("value"));
	   }
	   $("#logofile").on("change",function(e){		 
			 
			 var filevalue = this.value;  
	           var index = filevalue.lastIndexOf('.');  
	           if(index > -1){
	        	   if(filevalue.substring(index - 1).toLocaleLowerCase() != "png"){
	        		   toastr.error("图片格式错误，只能上传png格式图片！");
	        		   this.outerHTML = this.outerHTML;
	        	   } else {
	        		   if( $.browser.msie &&  parseInt($.browser.version)<=9){
	        			   $('#logoimg').attr('src', $(this).val()); 
	        		   }else{
	        			   $("#logoimg").remove();
	        		   }
	        	   } 
	        	   
	           } else {
	        	   this.outerHTML = this.outerHTML;
	           }
		});
		$(document).on("click",".switch a",function(event){  
			  event.stopPropagation();		
			  	$(".switch a").removeClass("on"),$(this).addClass("on");
			     $.scrollTo($(this).attr("href"),500); 
			  return false;
		});
		$(".switchdate").on("click",function(event){  
			  event.stopPropagation();	
			  $(".switchdate .active").removeClass("active"),$(this).find(".cate7b2back1").addClass("active"),
			  $("#autorefreshdatetype").val($(this).data("rtype"));
			  return false;
		});
	    $(document).on("click", ".autonumber,.autodate", function (event) {
	     	$(this).hasClass("checked") ? $(this).removeClass("checked") : $(this).addClass("checked"),
	        $(this).find("input").prop("checked", $(this).hasClass("checked")),
	        $(this).hasClass("checked") ? $(this).find(".fa-check").removeClass("hidden") : $(this).find(".fa-check").addClass("hidden");
	    });
	   $(".saveall").on("click",function(event){		 
		   	event.stopPropagation();		
			$("body").mLoading({  text:"正在保存......"});
			$("#js-systemform").ajaxSubmit({
			    type: "POST",
			    url:"/system/upload",
			    dataType: "json",
			    success: function(data){
			    	  if(data.statusCode==0) {
			    			toastr.success("系统设置保存成功");
			    	 }else{
			    		 toastr.error(data.message);
			    	}				
			    	 $("body").mLoading("hide");
			    }
			});
			return false;
	   });
	   $("#btnUploadBackground").on("click",function(event){		 
		   	event.stopPropagation();		
			$("body").mLoading({  text:"正在上传......"});
			if($.trim($("#backgroundfile").val())==""){
				  toastr.error("请选择背景图片！!");
				  $("body").mLoading("hide");
				  return;
			}
			singleUpload(0,"backgroundfile");
	   });
	   $("#btnUploadLogo").on("click",function(event){		 
		   	event.stopPropagation();		
			$("body").mLoading({  text:"正在上传......"});
			if($.trim($("#logofile").val())==""){
				  toastr.error("请选择Logo图片！!");
				  $("body").mLoading("hide");
				  return;
			}
			singleUpload(1,"logofile");
	   });
	   var singleUpload=function(type,id){
			$.ajaxFileUpload({
				type: "POST",
				url: "/system/singleupload/"+type,
			    fileElementId:id,
			    dataType: 'json',
			    async : true,
				success: function(data){
	     		   if(data.statusCode==0)
				   {
	     			  toastr.success("站点信息保存成功!");
				   }else  {
					   toastr.error(data.message);
				  }
	     		  $("body").mLoading("hide");
			    }
			});
	   };
	   $("#btnSyncImage").on("click",function(event){	
		    var imagepath = $("#imagepath").val();
		   	event.stopPropagation();		
			$("body").mLoading({  text:"正在同步......"});
			$.post("/system/syncimages",{ imagepath:imagepath }, function(data){
				if(data.statusCode==0)
				   {
	     			  toastr.success("图片同步成功!");
				   }else  {
					  toastr.error(data.message);
				  }
	     		  $("body").mLoading("hide");
			});
			return false;
	   });
})(jQuery);