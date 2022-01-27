var table, count = 0,
toastrCount = 0; (function($) {
    $("body").mLoading({
        text: "加载中......"
    });
    $('input, textarea').placeholder(),
    toastr.options = {
        "closeButton": true,
        "debug": false,
        "positionClass": "toast-top-center",
        "onclick": null,
        "showDuration": "300",
        "hideDuration": "1000",
        "timeOut": "2000",
        "extendedTimeOut": "1000",
        "showEasing": "swing",
        "hideEasing": "linear",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut"
    };
    
    var timeSetting = {
        timeText: '时间',
        hourText: '小时',
        minuteText: '分钟',
        secondText: '秒',
        currentText: '现在',
        closeText: '完成',
        showSecond: true,
        dateFormat: "yy-mm-dd",
        timeFormat: 'HH:mm:ss',
        defaultValue: ""
    };
    timeSetting.defaultValue = $('#startdate').val();
    $('#startdate').prop("readonly", true).datetimepicker(timeSetting);
    timeSetting.defaultValue = $('#enddate').val();
    $('#enddate').prop("readonly", true).datetimepicker(timeSetting);
    timeSetting.defaultValue = $('#startdateReport').val();
    $('#startdateReport').prop("readonly", true).datetimepicker(timeSetting);
    timeSetting.defaultValue = $('#enddateReport').val();
    $('#enddateReport').prop("readonly", true).datetimepicker(timeSetting);
    
    var stationDropdown = $('#station').select2({
        language: "zh-CN",
        placeholder: "请选择站点，最多五个站点",
        maximumSelectionLength: 5,
        width: '298px'
    }),
    table = $('#searchTable').DataTable({
        bAutoWidth: false
    });
    $('#searchTable').addClass("table table-striped table-bordered form-inline dataTable no-footer");
    window.onload = function() {
        $("#tablecontainer").css("width", $(window).width() - 30);
        setTimeout(function() {
            $(".temptdrifrm").css("visibility", "visible"),
            $("body").mLoading("hide");
        },
        500);
    }
    $("#btnSearch").click(function() {
        toastrCount = 0;
        if ($("#station").val() == null || $("#station").val().length == 0) {
            toastr.error("至少选择一个站点");
            return;
        }
        $("body").mLoading({
            text: "加载中......"
        });
        table.destroy(),
        $("#searchTable tbody tr").remove();
        $.post("/weathersearch/cols", {
            station: $("#station").val().join()
        },
        function(response, status) {
            $("#searchTable thead tr").remove();
            var tableHeader = "<tr>";
            //keys = Object.keys(response),
            //values = Object.values(response),
            var keys = getObjectKeys(response);
			var values = getObjectValues(response, keys);
            cols = [];
            cols.push({
                data: "weatherstationname",
                title: "<nobr>站点名称</nobr>",
                sortable: false
            });
            cols.push({
                data: "weatherstationnumber",
                title: "<nobr>站点编号</nobr>",
                sortable: false
            });
            cols.push({
                data: "datetime",
                title: "<nobr>日期时间</nobr>",
                sortable: false
            });
            response.sort(function(p1, p2) {
                var pv1 = p1.order,
                pv2 = p2.order;
                return pv1 < pv2 ? -1 : (pv1 == pv2 ? 0 : 1);
            });
            tableHeader += "<th></th><th></th><th></th>";
            $(response).each(function(i, item) {
                tableHeader += "<th></th>";
                cols.push({
                    data: item.name + "value",
                    title: "<nobr>" + item.description + "</nobr>",
                    sortable: false
                });
            });
            tableHeader += "</tr>";
            $(tableHeader).appendTo($("#searchTable thead")),
            $("#tablecontainer").removeClass("none");
            var option = {
                oLanguage: {
                    sLengthMenu: "每页_MENU_记录",
                    sInfo: "显示第_START_到_END_条，共_TOTAL_条",
                    sEmptyTable: "没有查询到符合当前条件的数据",
                    sInfoEmpty: "点击上方查询进行检索",
                    sZeroRecords: "没有查询到符合当前条件的数据",
                    oPaginate: {
                        sFirst: "首页",
                        sPrevious: "上一页",
                        sNext: "下一页",
                        sLast: "尾页"
                    }
                },
                "processing": false,
                "searching": false,
                "serverSide": true,
                "searching": false,
                "fnRowCallback": function(nRow, aData, iDataIndex) {
                    $(nRow.cells).each(function(i, item) {
                        if (i == 2) $(this).html("<nobr>" + $(this).text() + "</nobr>");
                        if ($.trim($(this).text()) == "N/A") {
                            $(this).addClass("disabled");
                        }
                    });
                    count++
                    return nRow;
                },
                "fnDrawCallback": function(aoData) {
                    if (count == 0) {
                        if (toastrCount == 0) {
                            toastrCount++;
                            toastr.warning("没有查询到符合当前条件的数据!");
                            $("#toast-container").css({
                                "top": "50%",
                                "marginTop": "-60px"
                            });
                        }
                    }
                    count = 0;
                },
                columns: cols,
                "ajax": {
                    "url": "/weathersearch/stations?startdate=" + $("#startdate").val() + "&enddate=" + $("#enddate").val() + "&station=" + $("#station").val().join(),
                    type: 'GET',
                    "dataType": 'json'
                }
            };
            if (cols.length > 12) {
                $("#tablecontainer").css("width", $(window).width() - 30);
                option.sScrollX = "100%",
                option.bScrollCollapse = true,
                option.bAutoWidth = false;
            }
            table = $('#searchTable').DataTable(option);
            new $.fn.dataTable.FixedColumns(table, {
                "leftColumns": 3
            });
            $("body").mLoading("hide");
        });
        
        var selectdate = new Date($("#startdate").val().replace(/-/g,"/")); 
        var startdate = selectdate.getFullYear()+"-"+(selectdate.getMonth()+1)+"-"+selectdate.getDate();
        var enddate = startdate + " 23:59:59";  
        var startdate = startdate + " 0:0:0"
        
        $.post("/agg/searchReport", 
        	{
	            station: $('#station').val()[0],
	            parameters: "pressure,temperature,windspeed,humidity",
	            startdate: startdate,
	            enddate: enddate
	        },
	        function(response, status) {
	        	$("body").mLoading("hide");
	            var data = response.data;
	            if(data.length == 0){
	            	$("#txtDayRainfallAll").val('');
	            	$("#txtDayMaxWindspeed").val('');
	            	$("#txtDayMaxWindspeedTime").val('');
	            	$("#txtDayMinWindspeed").val('');
	            	$("#txtDayMinWindspeedTime").val('');
	            	$("#txtDayMaxTemperature").val('');
	            	$("#txtDayMaxTemperatureTime").val('');
	            	$("#txtDayMinTemperature").val('');
	            	$("#txtDayMinTemperatureTime").val('');
	            	$("#txtDayMaxHumidity").val('');
	            	$("#txtDayMaxHumidityTime").val('');
	            	$("#txtDayMinHumidity").val('');
	            	$("#txtDayMinHumidityTime").val('');
	            	$("#txtDayMaxPressure").val('');
	            	$("#txtDayMaxPressureTime").val('');
	            	$("#txtDayMinPressure").val('');
	            	$("#txtDayMinPressureTime").val('');
	            } else {
	            	$.each(data, function( i, item ) {
						if(item.terminalName == 'rainfall'){
							if(item.type == '累积雨量'){
								$("#txtDayRainfallAll").val(item.value);
							}
						} else if(item.terminalName == 'windspeed') {
							if(item.type == '最高值'){
								$("#txtDayMaxWindspeed").val(item.value);
								$("#txtDayMaxWindspeedTime").val(item.lastDate);
							} else if(item.type == '最低值'){
								$("#txtDayMinWindspeed").val(item.value);
								$("#txtDayMinWindspeedTime").val(item.lastDate);
							} 
						} else if(item.terminalName == 'temperature') {
							if(item.type == '最高值'){
								$("#txtDayMaxTemperature").val(item.value);
								$("#txtDayMaxTemperatureTime").val(item.lastDate);
							} else if(item.type == '最低值'){
								$("#txtDayMinTemperature").val(item.value);
								$("#txtDayMinTemperatureTime").val(item.lastDate);
							} 
						} else if(item.terminalName == 'humidity') {
							if(item.type == '最高值'){
								$("#txtDayMaxHumidity").val(item.value);
								$("#txtDayMaxHumidityTime").val(item.lastDate);
							} else if(item.type == '最低值'){
								$("#txtDayMinHumidity").val(item.value);
								$("#txtDayMinHumidityTime").val(item.lastDate);
							} 
						} else if(item.terminalName == 'pressure') {
							if(item.type == '最高值'){
								$("#txtDayMaxPressure").val(item.value);
								$("#txtDayMaxPressureTime").val(item.lastDate);
							} else if(item.type == '最低值'){
								$("#txtDayMinPressure").val(item.value);
								$("#txtDayMinPressureTime").val(item.lastDate);
							} 
						}
					});
	            	
	            }
	        }
        );
    });
    
    $("#btnExport").click(function() {
        if ($("#station").val() == null || $("#station").val().length == 0) {
            toastr.error("至少选择一个站点");
            return;
        }
        window.open("/weathersearch/download?startdate=" + $("#startdate").val() + "&enddate=" + $("#enddate").val() + "&station=" + $("#station").val().join());
    });
    $(".cate2td2dvadsq1").click(function() {
		$(".cate2td2dvadsq1").removeClass("on");
		$(this).addClass("on");
		$(".cate2td2dvadsha").hide();
		$("#" + $(this).attr('data-reportId')).show();
	});
	$("#btnSearchReport").click(function() {
        if ($("#station").val() == null || $("#station").val().length == 0) {
            toastr.error("至少选择一个站点");
            return;
        }
        $("body").mLoading({
            text: "加载中......"
        });

        $.post("/agg/searchReport", 
        	{
	            station: $('#station').val()[0],
	            parameters: "pressure,rainfall,temperature,windspeed,humidity",
	            startdate: $("#startdateReport").val(),
	            enddate: $("#enddateReport").val()
	        },
	        function(response, status) {
	        	$("body").mLoading("hide");
	            var data = response.data;
	            if(data.length == 0){
	            	$("#txtRainfallAll").val('');
	            	$("#txtMaxWindspeed").val('');
	            	$("#txtMinWindspeed").val('');
	            	$("#txtMaxTemperature").val('');
	            	$("#txtMinTemperature").val('');
	            	$("#txtMaxHumidity").val('');
	            	$("#txtMinHumidity").val('');
	            	$("#txtMaxPressure").val('');
	            	$("#txtMinPressure").val('');
	            } else {
	            	$.each(data, function( i, item ) {
						if(item.terminalName == 'rainfall'){
							if(item.type == '累积雨量'){
								$("#txtRainfallAll").val(item.value);
							}
						} else if(item.terminalName == 'windspeed') {
							if(item.type == '最高值'){
								$("#txtMaxWindspeed").val(item.value);
							} else if(item.type == '最低值'){
								$("#txtMinWindspeed").val(item.value);
							} 
						} else if(item.terminalName == 'temperature') {
							if(item.type == '最高值'){
								$("#txtMaxTemperature").val(item.value);
							} else if(item.type == '最低值'){
								$("#txtMinTemperature").val(item.value);
							} 
						} else if(item.terminalName == 'humidity') {
							if(item.type == '最高值'){
								$("#txtMaxHumidity").val(item.value);
							} else if(item.type == '最低值'){
								$("#txtMinHumidity").val(item.value);
							} 
						} else if(item.terminalName == 'pressure') {
							if(item.type == '最高值'){
								$("#txtMaxPressure").val(item.value);
							} else if(item.type == '最低值'){
								$("#txtMinPressure").val(item.value);
							} 
						}
					});
	            }
	        }
        );
    });
})(jQuery);