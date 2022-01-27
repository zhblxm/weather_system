var table,count=1;
(function($) {
	   $("body").mLoading({  text:"加载中......"});
	   $('input, textarea').placeholder(),toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	   var timeSetting={ timeText: '时间',hourText: '小时',minuteText: '分钟',secondText: '秒',   currentText: '现在', closeText: '完成',showSecond: true, dateFormat: "yy-mm-dd", timeFormat: 'HH:mm:ss',defaultValue: ""};
	   timeSetting.defaultValue=$('#startdate').val();
	   $('#startdate').prop("readonly", true).datetimepicker(timeSetting);
	   timeSetting.defaultValue=$('#enddate').val();	      
	    $('#enddate').prop("readonly", true).datetimepicker(timeSetting);
	    var stationDropdown=$('#station').select2({ language: "zh-CN",  placeholder: "请选择站点，最多五个站点",  maximumSelectionLength: 5,width:'298px'   }),
	    terminalParameters=$('#parameters').select2({ language: "zh-CN",  placeholder: "请选择要素",  width:'298px'  }),
	   table = $('#aggstable').DataTable({
          oLanguage: {  sLengthMenu: "每页_MENU_记录",    sInfo: "显示第_START_到_END_条，共_TOTAL_条",   
        	   sEmptyTable:    "没有查询到符合当前条件的数据",  sInfoEmpty:     "点击上方查询进行检索",sZeroRecords:"没有查询到符合当前条件的数据",  
        	  oPaginate: { sFirst: "首页", sPrevious: "上一页",  sNext: "下一页",  sLast: "尾页"   }  },
        	   "processing": false,"searching":false, "serverSide": true,"searching":false,
               columns:  [
                          {data: "weatherStationName", title: "站点名称", sortable: false},
                          {data: "weatherStationNumber", title: "站点编号", sortable: false },
                          {data: "terminalDesc",  title: "要素名称",sortable: false },     
                          {data: "type", title: "统计类型",sortable: false },
                          {data: "value", title: "最高/低值",sortable: false },
                          {data: "lastDate", title: "日期",sortable: false }
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
                 "ajax": {
                     "url": "/agg/stations",
                     type: 'GET',
                     "dataType": 'json'
                 }
	    });		
	  $('#aggstable').addClass("table table-striped table-bordered form-inline dataTable no-footer");
	  var processParameterFun=function(){
		  var values=[],el=$('#parameters'),parametersValues=$('#parameters').val();
		  el.empty();
		  $.post("/agg/parameters",{station:$('#station').val().join(",")},function(response,status){
			  var pkeys = getObjectKeys(response);
			  var pvalues = getObjectValues(response, pkeys);
			  for(var i=0;i<pkeys.length;i++){
				  el.append('<option value="'+(pkeys[i])+'">'+(pvalues[i])+'</option>');
			  }
			el.val(parametersValues).trigger("change");
		 });
	  };
	  	$('#station').on("select2:select", function(e) { 
	  		processParameterFun();
		});
	 	$('#station').on("select2:unselect", function(e) { 
	 		processParameterFun();
		});	
	   window.onload=function(){
	   	   		setTimeout(function()
	   			{
	   	   			$(".temptdrifrm").css("visibility","visible"),$("body").mLoading("hide");
	   			},500);
	    }
		$("#btnSearch").click(function() {
			if($("#station").val()==null || $("#station").val().length==0)
			{
				 toastr.error("至少选择一个站点");
				 return;
			}
			if($("#parameters").val()==null || $("#parameters").val().length==0)
			{
				 toastr.error("至少选择一个要素");
				 return;
			}
			if( $("#parameters").val().length>10)
			{
				 toastr.error("最多选择十个要素");
				 return;
			}
			table.ajax.url("/agg/stations?startdate=" + $("#startdate").val()+"&enddate="+ $("#enddate").val()+"&station="+$("#station").val().join()+"&parameters="+$("#parameters").val().join()).load();
		});
		$("#btnExport").click(function() {
			if($("#station").val()==null || $("#station").val().length==0)
			{
				 toastr.error("至少选择一个站点");
				 return;
			}
			if($("#parameters").val()==null || $("#parameters").val().length==0)
			{
				 toastr.error("至少选择一个要素");
				 return;
			}
			if( $("#parameters").val().length>10)
			{
				 toastr.error("最多选择十个要素");
				 return;
			}
			 window.open("/agg/download?startdate=" + $("#startdate").val()+"&enddate="+ $("#enddate").val()+"&station="+$("#station").val().join()+"&parameters="+$("#parameters").val().join());
		});
})(jQuery);