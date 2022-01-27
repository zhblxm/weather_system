var table,count=1;
(function($) {
		$("body").mLoading({  text:"加载中......"});
	   $('input, textarea').placeholder(),toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	   var timeSetting={ timeText: '时间',hourText: '小时',minuteText: '分钟',secondText: '秒',   currentText: '现在', closeText: '完成',showSecond: true, dateFormat: "yy-mm-dd", timeFormat: 'HH:mm:ss',defaultValue: ""};
	   timeSetting.defaultValue=$('#startdate').val();
	   $('#startdate').prop("readonly", true).datetimepicker(timeSetting);
	   timeSetting.defaultValue=$('#enddate').val();	      
	    $('#enddate').prop("readonly", true).datetimepicker(timeSetting);
	    var stationDropdown=$('#station').select2({  
	          placeholder: "请选择站点，最多五个站点",  
	          tags:true,  
	          createTag:function (decorated, params) {  
	              return null;  
	          },  
	          maximumSelectionLength: 5,
	          width:'298px'  
	      });  
	   var defaultValue=[];
	  $("#station option").each(function (i,item){  
         if(i>0) return false;
         	defaultValue.push(item.value);
       }); 
	  stationDropdown.val(defaultValue).trigger("change")
	   table = $('#statistics').DataTable({
          oLanguage: {  sLengthMenu: "每页_MENU_记录",    sInfo: "显示第_START_到_END_条，共_TOTAL_条",   oPaginate: { sFirst: "首页", sPrevious: "上一页",  sNext: "下一页",  sLast: "尾页"   },sEmptyTable: "没有查询到符合当前条件的数据",  sInfoEmpty:     "点击上方查询进行检索",sZeroRecords:"没有查询到符合当前条件的数据"  },
          "processing": false, "serverSide": true, searching:false,
          columns:  [
                     {data: "weatherStationName", title: "站点名称", sortable: false},
                     {data: "weatherStationNumber", title: "站点编号", sortable: false },
                     {data: "weatherStationCategoryName",  title: "所属分类",sortable: false },                     
                     {data: "onTimeCount", title: "及时数",sortable: false },
                     {data: "onTimePercent", title: "及时率",sortable: false  },                     
                     {data: "delayedTimeCount", title: "逾时数",sortable: false },
                     {data: "delayedTimePercent", title: "逾时率",sortable: false  },                     
                     {data: "loseCount", title: "缺报数",sortable: false },
                     {data: "losePercent", title: "缺报率",sortable: false },                     
                     {data: "totalCount", title: "总数",sortable: false  }
            ],
            "fnRowCallback": function(oSettings, json) {  
           	   count++;
           } ,
	               "fnDrawCallback":function(aoData){
	            	   if(count==0) 	 
	            	   {
	            		   toastr.warning("没有查询到符合当前条件的数据!");
	            		   $("#toast-container").css({"top":"50%","marginTop":"-60px"});
	            	   }
	            	   count=0;  
	               },
            "ajax": "/statistics/stations?startdate=" + $("#startdate").val()+"&enddate="+ $("#enddate").val()+"&station="+$("#station").val().join()
	    }),$('#statistics').addClass("table table-striped table-bordered form-inline dataTable no-footer");
	      window.onload=function(){
	   	   		setTimeout(function()
	   			{
	   	   			$(".temptdrifrm").css("visibility","visible"),$("body").mLoading("hide");
	   			},500);
	      }
		$("#btnSearch").click(function() {
			if($.trim($("#station").val())=="")
			{
				 toastr.error("至少选择一个站点");
				 return;
			}
			table.ajax.url("/statistics/stations?startdate=" + $("#startdate").val()+"&enddate="+ $("#enddate").val()+"&station="+$("#station").val().join()).load();
		});
		$("#btnExport").click(function() {
			if($.trim($("#station").val())=="")
			{
				 toastr.error("至少选择一个站点");
				 return;
			}
			window.open("/statistics/download?startdate=" + $("#startdate").val()+"&enddate="+ $("#enddate").val()+"&station="+$("#station").val().join());
		});
})(jQuery);