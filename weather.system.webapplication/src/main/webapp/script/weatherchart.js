var table, count = 0, toastrCount = 0;
(function($) {
	$("body").mLoading({
		text : "加载中......"
	});
	$('input, textarea').placeholder(), toastr.options = {
		"closeButton" : true,
		"debug" : false,
		"positionClass" : "toast-top-center",
		"onclick" : null,
		"showDuration" : "300",
		"hideDuration" : "1000",
		"timeOut" : "2000",
		"extendedTimeOut" : "1000",
		"showEasing" : "swing",
		"hideEasing" : "linear",
		"showMethod" : "fadeIn",
		"hideMethod" : "fadeOut"
	};
	var timeSetting = {
		timeText : '时间',
		hourText : '小时',
		minuteText : '分钟',
		secondText : '秒',
		currentText : '现在',
		closeText : '完成',
		showSecond : true,
		dateFormat : "yy-mm-dd",
		timeFormat : 'HH:mm:ss',
		defaultValue : ""
	};
	timeSetting.defaultValue = $('#startdate').val();
	$('#startdate').prop("readonly", true).datetimepicker(timeSetting);
	timeSetting.defaultValue = $('#enddate').val();
	$('#enddate').prop("readonly", true).datetimepicker(timeSetting);
	var stationDropdown = $('#station').select2({
		language : "zh-CN",
		placeholder : "请选择站点，最多五个站点",
		maximumSelectionLength : 5,
		width : '298px'
	}), table = $('#searchTable').DataTable({
		bAutoWidth : false
	});

	window.onload = function() {
		$("#tablecontainer").css("width", $(window).width() - 30);
		setTimeout(function() {
			$(".temptdrifrm").css("visibility", "visible"), $("body").mLoading(
					"hide");
		}, 500);
	}
	
	$("#station").change(function(){
		var stations = $("#station").val();
		if(stations == null){
			stations = "";
		} else {
			stations = stations.join();
		}
		$.post("/weatherchart/parameters",{ station:stations }, function(response,status){
			var parameters = $("#parameters");
			parameters.empty();
			response.sort(function (p1, p2) {
				var pv1 = p1.order,pv2 = p2.order;  
				return pv1 < pv2?-1:(pv1 == pv2?0:1);
			});
			$(response).each(function(i,item){
				parameters.append("<option value='" + item.id + "'>" + item.description + "</option>");
			});
		});
	});
	
	$("#btnSearch").click(function() {
		toastrCount = 0;
		if ($("#station").val() == null || $("#station").val().length == 0) {
			toastr.error("至少选择一个站点");
			return;
		}
		if ($("#parameters").val() == null || $("#parameters").val().length == 0) {
			toastr.error("至少选择一个要素");
			return;
		}
		$("body").mLoading({ text : "加载中......" });
		
		var param = { 
						startdate:$("#startdate").val(), 
						parameters:$("#parameters").val(), 
						station:$("#station").val().join(),
						linecharttype:$("#linecharttype").val()
					};
		
		$.post("/weatherchart/stations", param, function(response){
			if(response == "" ||response == undefined){
				$('#container').empty();
				return;
			}
			
			var obj = JSON.parse(response);
			
			$('#container').highcharts({
		        chart: {
		            type: 'line'
		        },
		        title: {
		            text: $("#parameters").find("option:selected").text()
		        },
		        subtitle: {
		            text: obj.rainfalltext
		        },
		        xAxis: {
		            categories: obj.categories
		        },
		        yAxis: {
		            title: {
		                text: ''
		            }
		        },
		        plotOptions: {
		            line: {
		                dataLabels: {
		                    enabled: true          // 开启数据标签
		                },
		                enableMouseTracking: false // 关闭鼠标跟踪，对应的提示框、点击事件会失效
		            }
		        },
		        series: obj.series
		    });
		});
		
		
		$("body").mLoading("hide");
	});
	
	$("#btnExport").click(function() {
				
	});
})(jQuery);