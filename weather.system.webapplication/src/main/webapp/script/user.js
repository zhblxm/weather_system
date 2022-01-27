(function($) {
	  $('input, textarea').placeholder();
	  var settings = {};
	    toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	    settings.id="users", settings.sorting = [[0, "desc"]],  settings.columns = [
	    { data: "userName", title: "管理员名称", sortable: true,name: "NAME" },
	    { data: "createDate", title: "创建日期", sortable: false },
	    {data: "createUser",  title: "创建人",sortable: false },
	    {data: "lastUpdateDate", title: "最近一次更新日期",sortable: false },
	    {data: "lastUpdateUser", title: "最近一次更新人",sortable: false  },
	    ], settings.buttons = [],settings.search="查询管理员：",settings.placeholder="请输入管理员名称",
	    settings.url = "/User/users";
	    var user=$("#"+settings.id);
	    if(user.data("update")==1 || user.data("delete")==1 || user.data("reset")==1)
	    {
	    	 settings.columns.push(
	    			  { data: "uniqueUserId", title: "操作",sortable: false,sClass:"center", render: function(data, type, row) {
	    				   var html="";	    				   
	    				   if(user.data("update")==1 )
	   				        {
	    					   html+= '<a class="btn btn-default btn-sm edit" href="/User/Update/'+data+'" ><i class="fa fa-eye fa-lg"></i> 编辑</a>';
	   				        }
	    				   if(user.data("reset")==1 )
	   				        {
	    					   html+= '&nbsp;&nbsp;<a class="btn btn-default btn-sm edit" href="/User/manageresetpwd/'+data+'" ><i class="fa fa-key fa-lg"></i>修改密码</a>';
	   				        }
	    				   if(user.data("delete")==1 )
	    				    {
	    					   html+= '&nbsp;&nbsp;<a class="btn btn-default btn-sm delete" href="#" data-id="'+data+'"><i class="fa fa-trash-o fa-lg"></i> 删除</a>';
	    				    }
	    		            return html;
	    		        }
	    		    }
	    	);
	    	  if(user.data("update")==1)
	    	    {
	    		  settings.buttons.push({
	    		        text: '新增管理员',action: function(e, dt, node, config) {
	    		            document.location.href="/User/Add";
	    		        }
	    		    });
	    	    }
	    }
	    window.GLOBALTABLE.createTable(settings);
	    if($("#UserId").val()=="")
	    {
			$('input[name="permissions"]').each(function(){ 
				$(this).attr("checked",true);
			}); 
	    }
	    $(document).on("click",".delete",function(event)
	    {
	    	  event.stopPropagation();
	    	  $.get("/User/Delete/"+$(this).data("id"),function(data,status){
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
			document.location.href="/User/Manage";
		});
		$("#btnSave").click(function() {
			if($.trim($("#UserName").val())=="")
			{
				 toastr.error("用户名不能为空");
				 return;
			}
			if($("#UserId").val()=="" && $.trim($("#UserPassword").val())=="")
			{
				 toastr.error("密码不能为空");
				 return;
			}
			var user= {
					userName : $("#UserName").val(),
					userPassword : $("#UserPassword").val(),
					userEmail : $("#UserEmail").val(),
					isForceChangePwd : $('#IsForceChangePwd').is(':checked')?1:0,
					uniqueUserId: $("#UserId").val(),
					uniqueGroupId : $(".js-category").val(),
					weatherStationId:""
			};
			$.ajax({
				type : "POST",
				contentType : "application/json ; charset=utf-8",
				url : "/User/AsynSave",
				data : JSON.stringify(user),
				dataType : "json",
				async : false,
				success : function(rsp) {
					if (rsp.statusCode == 0) {
						$("#UserId").val(rsp.messageObject);
						 toastr.success("管理员信息保存成功！");
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