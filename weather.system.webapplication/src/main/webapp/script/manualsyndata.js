(function($) {
	$("body").mLoading({  text:"加载中......"});
	$('input, textarea').placeholder(),toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	var timeSetting={ timeText: '时间',hourText: '小时',minuteText: '分钟',secondText: '秒',   currentText: '现在', closeText: '完成',showSecond: true, dateFormat: "yy-mm-dd", timeFormat: 'HH:mm:ss',defaultValue: ""};
	timeSetting.defaultValue=$('#startdate').val();
	$('#startdate').prop("readonly", true).datetimepicker(timeSetting);
	timeSetting.defaultValue=$('#enddate').val();	      
	$('#enddate').prop("readonly", true).datetimepicker(timeSetting);
	var stationDropdown=$('#station').select2({ language: "zh-CN",  placeholder: "请选择站点，最多五个站点",  maximumSelectionLength: 5,width:'298px'   });
	
	window.onload=function(){
		$("#tablecontainer").css("width",$(window).width()-30);
		setTimeout(function()
		{
			$(".temptdrifrm").css("visibility","visible"),$("body").mLoading("hide");
		},500);
	}
	$("#btnSynData").click(function() {
		if($("#station").val()==null || $("#station").val().length==0)
		{
			 toastr.error("至少选择一个站点");
			 return;
		}
		if($("#startdate").val()==null || $("#startdate").val().length==0)
		{
			 toastr.error("请选择开始时间");
			 return;
		}
		if($("#enddate").val()==null || $("#enddate").val().length==0)
		{
			 toastr.error("请选择结束时间");
			 return;
		}
		$.post("/manualsyndata/syndata",{station:$('#station').val().join(","), startdate:$("#startdate").val(), enddate:$("#enddate").val()},function(response,status){
			if(response == "0"){		//补数错误
				
			} else if(response == "1"){	//补数开始
				
			}
		 });
	});
})(jQuery);