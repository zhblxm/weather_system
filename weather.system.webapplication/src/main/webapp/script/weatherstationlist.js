(function($) {
		$('input, textarea').placeholder();		
		   var settings = {};
		    toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
		    settings.id="categories", settings.sorting = [[0, "desc"]],  settings.columns = [
		    {data: "weatherStationNumber", title: "站点编号",sortable: false },
		    { data: "weatherStationName", title: "站点名称", sortable: true,name: "NAME" },
		    {data: "longitude", title: "经度",sortable: false },
		    {data: "latitude", title: "纬度",sortable: false  },
		    {data: "altitude", title: "海拔",sortable: false  },
		    {data: "gRPSPort",  title: "端口号",sortable: false },
		    {data: "autoSynFrequency", title: "同步频率", sortable: false },
		    {data: "isAutoSynTerminal", title: "自动进步", sortable: false },
		    {data: "contactUserName",  title: "联系人",sortable: false },
		    {data: "contactPhone", title: "联系电话", sortable: false },
		    ], settings.buttons = [],settings.search="站点名称查询：",settings.placeholder="请输入站点名称",
		    settings.url = "/weatherstation/stations";
		    if($("#"+settings.id).data("update")==1 || $("#"+settings.id).data("delete")==1)
		    {
		    	 settings.columns.push(
		    			  { data: "uniqueWeatherStationId", title: "操作",sortable: false,sClass:"center", render: function(data, type, row) {
		    				   var html="";
		    				   if($("#"+settings.id).data("update")==1 )
		   				        {
		    					   html+= '<a class="btn btn-default btn-sm edit" href="/weatherstation/update/'+data+'" ><i class="fa fa-eye fa-lg"></i> 编辑</a>';
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
		    		        text: '新增站点',action: function(e, dt, node, config) {
		    		            document.location.href="/weatherstation/add";
		    		        }
		    		    });
		    	    }
		    }
		    window.GLOBALTABLE.createTable(settings);
		    $(document).on("click",".delete",function(event)
		    {
		    	  event.stopPropagation();
		    	  window.station=$(this);
		    	  jConfirm("确认要删除站点吗？", '删除站点', function (result) {
		              if (result) {
				    	  $.get("/weatherstation/delete/"+window.station.data("id"),function(data,status){
				    		  window.GLOBALTABLE.Refresh();
				    		   if(data.statusCode==0)
							   {
				    			   toastr.success("站点已经成功删除！");
							   }else  {
								   toastr.error(data.message);
							  }
				    		  });
		              }
		          });
		    	  return false;
		    });

})(jQuery);