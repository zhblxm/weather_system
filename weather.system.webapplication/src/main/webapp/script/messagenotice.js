(function($) {
	  $('input, textarea').placeholder();
	  var settings = {};
	    toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	    settings.id="messagenotices", settings.sorting = [[0, "desc"]],  settings.columns = [
	    {data: "messageNoticeDesc", title: "通知名称", sortable: false, name: "NAME" },
	    {data: "createDate", title: "创建日期", sortable: true },
	    {data: "createUser",  title: "创建人",sortable: false },
	    {data: "lastUpdateDate", title: "最近一次更新日期",sortable: false },
	    {data: "lastUpdateUser", title: "最近一次更新人",sortable: false  },
	    ], settings.buttons = [],settings.search="查询通知名称：",settings.placeholder="请输入通知名称",
	    settings.url = "/messagenotice/messagenotices";
	    var messagenotice=$("#"+settings.id);
	    if(messagenotice.data("update")==1 || messagenotice.data("delete")==1)
	    {
	    	 settings.columns.push({ 
	    		 data: "uniqueMessageNoticeId", title: "操作",sortable: false,sClass:"center", render: function(data, type, row) {
	    			var html="";	    				   
	    			if(messagenotice.data("update")==1 )
	   				{
	    				html+= '<a class="btn btn-default btn-sm edit" href="/messagenotice/update/'+data+'" ><i class="fa fa-eye fa-lg"></i> 编辑</a>';
	   				}
	    			if(messagenotice.data("delete")==1 )
	    			{
	    				html+= '&nbsp;&nbsp;<a class="btn btn-default btn-sm delete" href="#" data-id="'+data+'"><i class="fa fa-trash-o fa-lg"></i> 删除</a>';
	    			}
	    		    return html;
	    		 }
	    	});
	    	if(messagenotice.data("update")==1)
	    	{
	    		settings.buttons.push({
	    			text: '新增通知',action: function(e, dt, node, config) {
	    		    	document.location.href="/messagenotice/add";
	    			}
	    		});
	    	}
	    }
	    window.GLOBALTABLE.createTable(settings);
//	    if($("#MessagenoticeId").val()=="")
//	    {
//			$('input[name="permissions"]').each(function(){ 
//				$(this).attr("checked",true);
//			}); 
//	    }
	    $(document).on("click",".delete",function(event)
	    {
	    	  event.stopPropagation();
	    	  $.get("/messagenotice/delete/"+$(this).data("id"),function(data,status){
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
		$("#btnBack").click(function() {
			document.location.href="/messagenotice/manage";
		});
		$("#btnSave").click(function() {
			if($.trim($("#MessageNoticeDesc").val())=="")
			{
				 toastr.error("通知名称不能为空");
				 return;
			}
			if($.trim($("#Message").val())=="")
			{
				 toastr.error("通知内容不能为空");
				 return;
			}
			var messagenotice= {
					messageNoticeDesc : $("#MessageNoticeDesc").val(),
					message : $("#Message").val(),
					uniqueMessageNoticeId: $("#MessageNoticeId").val()
			};
			$.ajax({
				type : "POST",
				contentType : "application/json ; charset=utf-8",
				url : "/messagenotice/asynsave",
				data : JSON.stringify(messagenotice),
				dataType : "json",
				async : false,
				success : function(rsp) {
					if (rsp.statusCode == 0) {
						$("#MessageNoticeId").val(rsp.messageObject);
						 toastr.success("通知信息保存成功！");
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