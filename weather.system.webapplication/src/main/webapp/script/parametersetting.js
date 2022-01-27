(function($) {		
    $(document).on("click", ".check-default", function (event) {
     	$(this).hasClass("checked") ? $(this).removeClass("checked") : $(this).addClass("checked"),
        $(this).find("input").prop("checked", $(this).hasClass("checked")),
        $(this).hasClass("checked") ? $(this).find(".fa-check").removeClass("hidden") : $(this).find(".fa-check").addClass("hidden");
    });
	$("#btnClose").click(function() {
		  event.stopPropagation();		
	      window.parent.JqueryDialog.Close();
		  return false;
	});
	window.onload=function()
	{
		 var index=parseInt($.trim($(".js-parameter").data("row"))),existsParam=null;
		 if(parent.parameter.length>0){
			 $(parent.parameter).each(function(i,item){
		   			if(index==item.rownumber){
		   				existsParam=item;
		   				return false;
		   			}
		      });	
		}
		 if(existsParam==null){
			 $("input[name='parametergroup']").each( function () {	  
				 $(this).parent("div").addClass("checked"), $(this).prop("checked",true);
			 });	
		}else{
			var values=existsParam.rowvalue.split(",");
			 $("input[name='parametergroup']").each( function () {	     
				 -1 != $.inArray($.trim($(this).val()), values)?($(this).parent("div").addClass("checked")):($(this).parent("div").find(".fa-check").addClass("hidden")),
				 $(this).prop("checked",-1 != $.inArray($.trim($(this).val()), values)?true:false);
			 });	
		}
		 $(".check-default").removeClass("hidden");
	};
	$("#btnSave").click(function() {
		var parameters=[];
		 $("input[name='parametergroup']:checkbox:checked").each( function () {	                      
	              parameters.push($.trim($(this).val()));
	      });		 
		 if(parameters.length>0)
		 {
			 var index=parseInt($.trim($(".js-parameter").data("row"))),isExists=false,
			 datarow={ rownumber:index, rowvalue:	parameters .join(",")  };
			$(parent.parameter).each(function(i,item){
					if(index==item.rownumber){
						isExists=true,parent.parameter.splice(index,1,datarow);
						return false;
					}
			});
			if(!isExists){
				 parent.parameter.push({
						rownumber:parseInt($.trim($(".js-parameter").data("row"))),
						rowvalue:	parameters .join(",")
				 });
			}
			$("#btnClose").trigger("click");
		}	
	});
})(jQuery);