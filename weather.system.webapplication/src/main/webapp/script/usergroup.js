(function($) {		
    var settings = {};
    toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
    settings.id="groups", settings.sorting = [[0, "desc"]],  settings.columns = [
    { data: "groupName", title: "用户组名称", sortable: true,name: "NAME" },
    {  data: "createDate", title: "创建日期", sortable: false },
    {data: "createUser",  title: "创建人",sortable: false },
    {data: "lastUpdateDate", title: "最近一次更新日期",sortable: false },
    {data: "lastUpdateUser", title: "最近一次更新人",sortable: false  },
    ], settings.buttons = [],settings.search="查询用户组：",settings.placeholder="请输入用户组名称",
    settings.url = "/UserGroup/groups";
    if($("#"+settings.id).data("update")==1 || $("#"+settings.id).data("delete")==1)
    {
    	 settings.columns.push(
    			  { data: "groupUniqueId", title: "操作",sortable: false,sClass:"center", render: function(data, type, row) {
    				   var html="";
    				   if($("#"+settings.id).data("update")==1 )
   				        {
    					   html+= '<a class="btn btn-default btn-sm edit" href="/UserGroup/Update/'+data+'" ><i class="fa fa-eye fa-lg"></i> 编辑</a>';
   				        }
    				   if($("#"+settings.id).data("delete")==1 )
   				    {
   					   html+= '&nbsp;&nbsp;<a class="btn btn-default btn-sm delete" href="#" data-id="'+data+'"><i class="fa fa-trash-o fa-lg"></i> 删除</a>';
   				    }
    		            return html;
    		        }
    		    }
    	);
    	  if($("#"+settings.id).data("update")==1)
    	    {
    		  settings.buttons.push({
    		        text: '新增用户组',action: function(e, dt, node, config) {
    		            document.location.href="/UserGroup/Add";
    		        }
    		    });
    	    }
    }
    window.GLOBALTABLE.createTable(settings);
    if($("#GroupId").val()=="")
    {
		$('input[name="permissions"]').each(function(){ 
			$(this).attr("checked",true);
		}); 
    }
	$('#stationgroup').select2({
		language : "zh-CN",
		width : "400px",
		theme : "classic"
	});	
	$('#stationgroup').on("select2:unselect", function(e) { 
		var values=[],el=$('#station'),selectValues=$('#station').val();
		$($('#station').find("option")).each(function(i,item){ 
			if($(this).data("category")!=e.params.data.id){
				values.push(this);
			}
		}); 
		el.empty(),
		$(values).each(function(i,item){ 
			el.append('<option value="'+(item.value)+'" data-category="'+($(item).data("category"))+'">'+(item.text)+'</option>');
		}),
		el.val(selectValues).trigger("change");
	});	
	$('#stationgroup').on("select2:select", function(e) { 
		var values=[],el=$('#station'),selectValues=el.val(),stationSelValues=$('#station').val();
		if("all"==e.params.data.id){
			$('#stationgroup').val(["all"]).trigger("change");
		}else{	
			$($('#stationgroup').val()).each(function(i,item){ 
				if(item!="all") {
					values.push(item);
				}
			}); 
			$('#stationgroup').val(values).trigger("change");
		}
		el.empty();
	  	  $.post("/UserGroup/stations",{categories:$('#stationgroup').val().join(",")},function(response,status){
			    if(el.find("option").length==0)	el.append('<option value="all" data-category="">全部</option>');
				$(response.data).each(function(i,item){ 
					el.append('<option value="'+(item.uniqueWeatherStationId)+'" data-category="'+(item.uniqueCategoryId)+'">'+(item.weatherStationName)+'</option>');
				}); 
				el.val(selectValues).trigger("change");
		 });
	});
	$('#station').select2({
		language : "zh-CN",
		width : "400px",
		theme : "classic"
	});
	$('#station').on("select2:select", function(e) { 
		var values=[],el=$('#station'),selectValues=el.val();
		if("all"==e.params.data.id){
			el.val(["all"]).trigger("change");
		}else{	
			$(selectValues).each(function(i,item){ 
				if(item!="all") {
					values.push(item);
				}
			}); 
			el.val(values).trigger("change");
		}
	});

    if(usercategories.length==0){
    	usercategories.push("all")
    }
    $('#stationgroup').val(usercategories).trigger("change");
    $.post("/UserGroup/stations",{categories:usercategories.join(",")},function(response,status){
		 	var el=$('#station');
		    if(el.find("option").length==0)	el.append('<option value="all" data-category="">全部</option>');
			$(response.data).each(function(i,item){ 
				el.append('<option value="'+(item.uniqueWeatherStationId)+'" data-category="'+(item.uniqueCategoryId)+'">'+(item.weatherStationName)+'</option>');
			}); 
		    if(userstations.length==0){
		    	userstations.push("all")
		    }
			el.val(userstations).trigger("change");
	 });
    $(document).on("click",".delete",function(event)
    {
    	  event.stopPropagation();
    	  $.get("/UserGroup/Delete/"+$(this).data("id"),function(data,status){
    		  window.GLOBALTABLE.Refresh();
    		   if(data.statusCode==0)
			   {
    			   toastr.success(data.message);
			   }else  {
				   toastr.error(data.message);
			  }
    		  });
    	  return false;
    });
    $(document).on("click",".permission li input",function(event) {
	    	  event.stopPropagation();
	    	  if(event.target.type=="checkbox"){
	    		  var input=$(this).parents("ul").find(".title input");
	    		  if(input.length>0){
	    			  if($(this).is(":checked")){
	    				  $(input).attr("checked",true);;
	    			  }else{
	    				  var checklist=$(this).parents("ul").find("input"),hasChecked=false;
	    					$(checklist).each(function(i,item){ 
	    						  if(!$(item).hasClass("none") && $(item).is(":checked")){
	    							  hasChecked=true;
	    							  return false;
	    						  }
	    					}); 
	    					if(!hasChecked){
	    						$(input).attr("checked",false);
	    					}
	    			  }
	    			}
	    	  }
    });
	$("#btnBack").click(function() {
		document.location.href="/UserGroup";
	});
	$("#btnCheckAll").click(function() {
		$('input[name="permissions"]').each(function(){ 
			  this.checked=true;
		}); 
	});
	$("#btnUnCheckAll").click(function() {
		$('input[name="permissions"]').each(function(){ 
			 this.checked=false;
		}); 
	});
	$("#btnSave").click(function() {
		if($.trim($("#GroupName").val())=="")
		{
			 toastr.error("用户组名称不能为空");
			 return;
		}
		var userGroup = {groupName : $("#GroupName").val(),groupUniqueId : $("#GroupId").val(),permissions:[],weatherCategory:$('#stationgroup').val().join(","),weatherStation:$('#station').val().join(",")};
		$('input[name="permissions"]:checked').each(function(){ 
			userGroup.permissions.push($(this).val()); 
		}); 
		$.ajax({
			type : "POST",
			contentType : "application/json ; charset=utf-8",
			url : "/UserGroup/AsynSave",
			data : JSON.stringify(userGroup),
			dataType : "json",
			async : false,
			success : function(rsp) {
				if (rsp.statusCode == 0) {
					 toastr.success("用户组保存成功！");
					$("#GroupId").val(rsp.messageObject);
				} else {
					 toastr.error(rsp.message);
				}
			},
			error : function(msg) {
				 toastr.error(msg);
			}
		});
	});

})(jQuery);