window.category={};
(function($) {
		$('input, textarea').placeholder();		
		$( "[title]" ).tooltip();
	   var settings = {};
	    toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	    settings.id="categories", settings.sorting = [[0, "desc"]],  settings.columns = [
	    { data: "weatherStationCategoryName", title: "分类名称", sortable: true,name: "NAME" },
	    {data: "parentCategoryName", title: "所属分类",sortable: false },
	    {data: "longitude", title: "经度",sortable: false  },
	    {data: "latitude", title: "纬度",sortable: false  },
	    {data: "createUser",  title: "创建人",sortable: false },
	    {  data: "createDate", title: "创建日期", sortable: false },
	    ], settings.buttons = [],settings.search="分类名查询：",settings.placeholder="请输入站点分类名称",
	    settings.url = "/weatherStationcategory/categories";
	    if($("#"+settings.id).data("update")==1 || $("#"+settings.id).data("delete")==1)
	    {
	    	 settings.columns.push(
	    			  { data: "uniqueCategoryId", title: "操作",sortable: false,sClass:"center", render: function(data, type, row) {
	    				   var html="";
	    				   if($("#"+settings.id).data("update")==1 )
	   				        {
	    					   html+= '<a class="btn btn-default btn-sm edit" href="/weatherStationcategory/update/'+data+'" ><i class="fa fa-eye fa-lg"></i> 编辑</a>';
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
	    		        text: '新增站点分类',action: function(e, dt, node, config) {
	    		            document.location.href="/weatherStationcategory/add";
	    		        }
	    		    });
	    	    }
	    }
	    window.GLOBALTABLE.createTable(settings);
	    $(document).on("click",".delete",function(event)
	    {
	    	  event.stopPropagation();
	    	  window.category=$(this);
	    	  jConfirm("确认要删除站点分类吗？", '删除站点分类', function (result) {
	              if (result) {
	            	  $.get("/weatherStationcategory/delete/"+window.category.data("id"),function(data,status){
		    	    		  window.GLOBALTABLE.Refresh();
		    	    		   if(data.statusCode==0)
		    				   {
		    	    			   toastr.success("站点分类已经成功删除！");
		    				   }else  {
		    					   toastr.error(data.message);
		    				  }
	    	    		  });
	              }
	          });
	    	  return false;
	    });
	    $(".station_panel .content img").click(function() {
			 JqueryDialog.Open("选择经纬度","/weatherStationcategory/map",800,600);
	    });
	$('.js-category').select2({
		placeholder : "请选择分类",
		language : "zh-CN",
		width : "400px",
		minimumResultsForSearch: Infinity,
		theme : "classic"
	});
	$("#btnBack").click(function() {
		document.location.href="/weatherStationcategory";
	});
	$("#btnSave").click(function() {
		if($.trim($("#CategoryName").val())=="")
		{
			 toastr.error("分类名称不能为空");
			 return;
		}
		if($.trim($("#Longitude").val())=="")
		{
			 toastr.error("经度不能为空");
			 return;
		}
		if($.trim($("#Latitude").val())=="")
		{
			 toastr.error("纬度不能为空");
			 return;
		}
		var category = {
				uniqueCategoryId : $("#CategoryId").val(),
				weatherStationCategoryName : $("#CategoryName").val(),
				parentCategoryName:$(".js-category").find("option:selected").text(),
				uniqueParentId:$(".js-category").val(),
				longitude:$.trim($("#Longitude").val()),
				latitude:$.trim($("#Latitude").val())
		};
		$.ajax({
			type : "POST",
			contentType : "application/json ; charset=utf-8",
			url : "/weatherStationcategory/asynsavecategory",
			data : JSON.stringify(category),
			dataType : "json",
			async : false,
			success : function(rsp) {
				if (rsp.statusCode == 0) {
					 toastr.success("分类信息保存成功!"),
					$("#CategoryId").val(rsp.messageObject);
					document.location.href="/weatherStationcategory";
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