window.terminal={};
(function($) {
	 var settings = {};
	    toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	    settings.id="categories", settings.sorting = [[0, "desc"]],  settings.columns = [
	    { data: "terminalParamCategoryName", title: "要素分类名称", sortable: true,name: "NAME" },
	    {  data: "createDate", title: "创建日期", sortable: false },
	    {data: "createUser",  title: "创建人",sortable: false },
	    {data: "lastUpdateDate", title: "最近一次更新日期",sortable: false },
	    {data: "lastUpdateUser", title: "最近一次更新人",sortable: false  },
	    ], settings.buttons = [],settings.search="查询要素分类：",settings.placeholder="请输入要素分类名称",
	    settings.url = "/terminal/categories";
	    if($("#"+settings.id).data("update")==1 || $("#"+settings.id).data("delete")==1)
	    {
	    	 settings.columns.push(
	    			  { data: "categoryUniqueId", title: "操作",sortable: false,sClass:"center", render: function(data, type, row) {
	    				   var html="";
	    				   if($("#"+settings.id).data("view")==1 )
	   				       {
	   					   html+= '<a class="btn btn-default btn-sm" href="/terminal/detail/'+data+'"><i class="fa fa-eye fa-lg"></i> 查看</a>';
	   				        }
	    				   if($("#"+settings.id).data("delete")==1 )
	   				       {
	   					   html+= '&nbsp;&nbsp;<a class="btn btn-default btn-sm delete" href="#" data-id="'+data+'"><i class="fa fa-trash-o fa-lg"></i> 删除</a>';
	   				        }
	    				   html+= '&nbsp;&nbsp;<a class="btn btn-default btn-sm" href="/terminal/download/'+data+'"><i class="fa fa-cloud-download fa-lg"></i> 下载</a>';
	    		            return html;
	    		        }
	    		    }
	    	);
	    	  if($("#"+settings.id).data("update")==1)
	    	    {
	    		  settings.buttons.push({
	    		        text: '新增要素分类',action: function(e, dt, node, config) {
	    		            document.location.href="/terminal/addcategory";
	    		        }
	    		    });
	    	    }
	    }
	    window.GLOBALTABLE.createTable(settings);
	    $(document).on("click",".delete",function(event){
	    	  event.stopPropagation();
	    	  window.terminal=$(this);
	          jConfirm("确认要删除要素分类吗？", '删除要素分类', function (result) {
	              if (result) {
	            	  $.get("/terminal/delete/"+$(window.terminal).data("id"),function(data,status){
	            		  window.GLOBALTABLE.Refresh();
	            		   if(data.statusCode==0)
	        			   {
	            			   toastr.success(data.message);
	        			   }else  {
	        				   toastr.error(data.message);
	        			  }
	            	});
	              }
	          });
	    	  return false;
	 });
	$('#fileTerminalCategor').change(function(event){
		  event.stopPropagation();
		  $(".path").val($(this).val());
	});
	$('#btnUpload').click(function(event){
		$.ajaxFileUpload({
			type: "POST",
			url: "/terminal/uploadterminalcategory",
		    fileElementId:'fileTerminalCategor',
		    dataType: 'json',
		    async : false,
			success: function(data){
     		   if(data.statusCode==0)
			   {
    			   toastr.success("文件上传成功，2秒后跳到要素分类列表页面。")
    			   setTimeout(function()
    				{
    				   document.location.href="/terminal/manage"
    				},2000);
			   }else  {
				   toastr.error(data.message);
			  }
		    }
		});
	});
})(jQuery);