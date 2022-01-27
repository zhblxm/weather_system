var terminalTable,reg = /^\+?[1-9][0-9]*$/,uploaderrormessage="";
(function($) {
	 $( "[title]" ).tooltip();
	 	var userAgent = navigator.userAgent.toLowerCase();
    	$.browser = {
	        version: (userAgent.match(/.+(?:rv|it|ra|ie)[\/: ]([\d.]+)/) || [])[1],
	        safari: /webkit/.test(userAgent),
	        opera: /opera/.test(userAgent),
	        msie: /msie/.test(userAgent) && !/opera/.test(userAgent),
	        mozilla: /mozilla/.test(userAgent) && !/(compatible|webkit)/.test(userAgent)
	    };
		$('input, textarea').placeholder(),toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
		$.fn.extFunction = function($this) {
			var index=$this.parent().siblings("td:eq(0)").parent().index(),pindex=-1;			
			$(parameter).each(function(i,item){
				if(index==item.rownumber){
					pindex=i;return false;
				}
			});
			if(pindex>-1){
				parameter.splice(pindex,1);
				$(parameter).each(function(i,item){
					if(item.rownumber>pindex){
						--item.rownumber;
					}
				});
			}
		};
		 $.uploadPreview({
			    input_field: "#fileCover",
			    preview_box: "#stationcover",
			    no_label: true
		});
		  
		 $(document).on("change","#fileCover",function(e){		 
			 //e.stopPropagation();
			  $(".path").val($(this).val());
			 if( $.browser.msie &&  parseInt($.browser.version)<=9){
				 $('#js-cover').attr('src', $(this).val()); 
			  }else{
				    $("#js-cover").remove();
			 }
			$(".imgpopup").attr("data-cover",1);
			 if($.trim($(this).val())==""){
					$(".imgpopup").attr("data-cover",0);
			}
				$("body").mLoading({  text:"加载中......"});
				$.ajaxFileUpload({
					type: "POST",
					url: "/weatherstation/validimage",
				    fileElementId:'fileCover',
				    dataType: 'json',
				    async : false,
					success: function(data){
						 if(data.statusCode!=0)
						   {
							 uploaderrormessage=data.message;
							 toastr.error(data.message);
						   }else{
							   uploaderrormessage="";
						   }
							setTimeout(function(){
								$("body").mLoading("hide");
							},1000);
				    }
				});
		});		 
		$(".js_close_view").click(function() {
	    		$(".imgpopup").hasClass("popup_show")?($(".imgpopup").removeClass("popup_show"),$(".modal-content").addClass("hidden")):($(".imgpopup").addClass("popup_show"),$(".modal-content").removeClass("hidden"));
	    });
	    $("#preview").click(function() {	    	
	    	if(0== $(".imgpopup").attr("data-cover")){
	    		toastr.warning("未发现站点封面，无法预览!");
	    		return;
	    	}
	    	$(".imgpopup").addClass("popup_show"),$(".modal-content").removeClass("hidden");
	    });		 
		 
		window.onload=function()
		{
			$("input[type='text']").each(function(i){
				if(1==$(this).attr("IsIntegerOnly")){
					$(this).keypress(function(e){
						return IsIntegerOnly(e);
					});
				}
			});	
			if(jsondata.length==0) jsondata.push(['','','','','',""]);
			$.get("/weatherstation/getTerminalCategories",function(data){
			   var	terminalCategory="<select class='js-parameter'>";
			   $(data).each(function(index,element){		
				   terminalCategory+=' <option value="'+element.categoryUniqueId+'">'+element.terminalParamCategoryName+'</option>';
			   });
			   terminalCategory+='</select>';
				terminalTable = $('#weatherstationtable').editTable({
				    data: jsondata, 
				    tableClass: 'inputtable',  
				    jsonData: false, 
				    field_templates: {
				        'select' : {
				        	 html:terminalCategory,
					            getValue: function (input) {
					                return $(input).val();
					            },
					            setValue: function (input, value) {
					                var select = $(input);
					                select.find('option').filter(function() {
					                    return $(this).val() == value; 
					                }).attr('selected', true);
					                return select;
					            }
				        },
				        'select_Frequency' : {
				            html: '<select><option value="m">分钟</option><option value="h">小时</option></select>',
				            getValue: function (input) {
				                return $(input).val();
				            },
				            setValue: function (input, value) {
				                var select = $(input);
				                select.find('option').filter(function() {
				                    return $(this).val() == value; 
				                }).attr('selected', true);
				                return select;
				            }
				        },
				        'select_Battery' : {
				            html: '<select><option value="directcurrent">城市-直流电</option><option value="leadcell">自动站-铅酸电池</option><option value="lithium">便携站-锂电池</option></select>',
				            getValue: function (input) {
				                return $(input).val();
				            },
				            setValue: function (input, value) {
				                var select = $(input);
				                select.find('option').filter(function() {
				                    return $(this).val() == value; 
				                }).attr('selected', true);
				                return select;
				            }
				        }
				        , 'view' : {
				            html: '<a class="btn btn-default btn-sm terminal-view" href="#"><i class="fa fa-cog"></i>设置</a>',
				            getValue: function (input) {
				                return  $(input).attr('data-id');
				            },
				            setValue: function (input, value) {		
				                    return $(input).attr('data-id', value);
				            }
				        }
				    },
				    row_template: ['text', 'select','select_Battery', 'text',  'select_Frequency','view'],
					headerCols: ['设备型号','配置要素','电池类型','采集频率','采集单位','要素显示设置'],
				    maxRows: 1, 
				    first_row: false, 
				    validate_field: function (col_id, value, col_type, $element) {
				        return true;
				    }
				});
			});
		};		
		$(document).on("click",".terminal-view",function(event){
	    	 event.stopPropagation();
	    	if( ""==$.trim($(this).parent().siblings("td:eq(0)").find("input").val())){
					  jAlert('error',"请在表格内输入设备编号！", '必填提示');
			 }else {
				 JqueryDialog.Open("设置可见要素","/weatherstation/detail/"+$(this).parent().siblings().find(".js-parameter").val()+"?row="+$(this).parent().siblings("td:eq(0)").parent().index(),800,600);
			}
			return false;
		});
	    $(".station_panel .content img").click(function() {
			 JqueryDialog.Open("选择经纬度","/weatherStationcategory/map",800,600);
	    });
		$('.js-category').select2({
			placeholder : "请选择分类",
			language : "zh-CN",
			width : "240px",
			theme : "classic"
		});
		$("#btnBack").click(function() {
			document.location.href="/weatherstation";
		});
		$(document).on("keydown","input[type='text']",function(){
			$(this).css("border","1px solid #a7a7a7");
		});
		var validationForm=function()
		{
			var invalid=false,terminalnumber=[];
			if( ""==$(".js-category").val()){
				toastr.error("请选择站点分类!");
				return true;
			}
			$("input[type='text']").each(function(i){
				if(1==$(this).data("require") && ""==$.trim($(this).val())){
					$(this).css("border","1px solid red");
					invalid=true;toastr.error($(this).attr("placeholder") );
					return false;
				}
			});	
			if(!invalid){
				 $(terminalTable.getData()).each(function(i,terminal){			
						if( "" == $.trim(terminal[0]) && "" == $.trim(terminal[2]) ){ return true; }
						if( ""==$.trim(terminal[0])){ 	invalid=true,toastr.error("请在表格内输入设备编号！"); return false; }
						if( !reg.test(terminal[3]) || parseInt(terminal[3])>99){ invalid=true,toastr.error("请在表格内输入采集频率并且只能是100以内数字！"); return false; 	}
						if(-1 == $.inArray($.trim(terminal[0]), terminalnumber)){
							terminalnumber.push($.trim(terminal[0]));
						}else{
							invalid=true,toastr.error("表格内输入设备编号必须唯一！"); return false; 
						}						
						return true;
				 });
			}
			if(!invalid && uploaderrormessage!=""){
				invalid=true,toastr.error(uploaderrormessage); 
			}			
			return invalid;
		};
		$("#btnSave").click(function() {
			var invalid=validationForm();	
			 if(invalid) {
				 return false;
			}			
			$("body").mLoading({  text:"加载中......"});
			var station = {
					uniqueWeatherStationId : $.trim($("#weatherStationId").val()),
					weatherStationName : $.trim($("#weatherStationName").val()),
					uniqueCategoryId:$(".js-category").val(),			
					weatherStationCategoryName:$(".js-category").find("option:selected").text(),
					longitude:$.trim($("#Longitude").val()),
					latitude:$.trim($("#Latitude").val()),
					weatherStationNumber:$.trim($("#weatherStationNumber").val()),
					altitude:$.trim($("#altitude").val()),				
					contactUserName:$.trim($("#contactUserName").val()),
					contactPhone:$.trim($("#contactPhone").val()),
					emergencyPhone:$.trim($("#emergencyPhone").val()),
					gRPSPort:$.trim($("#gRPSPort").val()),				
					isAutoSynTerminal:0,
					autoSynFrequency:$.trim($("#autoSynFrequency").val()),
					autoSynFrequencyUnit:$.trim($(".js-AutoSynFrequencyUnit").val()),
					weatherStationTerminals:[]
			};
			if($('#isAutoSynTerminal').is(':checked')) {
				station.isAutoSynTerminal=1;
			};
		   $(terminalTable.getData()).each(function(i,terminal){		
			   if( ""!=$.trim(terminal[0]) && ""!=$.trim(terminal[3])){
						var paramedetail={
									terminalModel:$.trim(terminal[0]),				
									uniqueTPCId : terminal[1],
									uniqueStationId : $.trim($("#weatherStationId").val()),
									batteryType:$.trim(terminal[2]),
									acquisitionFrequency:$.trim(terminal[3]),
									acquisitionFrequencyUnit:terminal[4],
									uniqueWSTId:$.trim(terminal[5]),
									acquisitionFrequencyDesc:'',
									terminalParameters:""
						 };
			
						$(parameter).each(function(j,item){
							if(i==item.rownumber){
								paramedetail.terminalParameters=item.rowvalue;
								return false;
							}
						});
						station.weatherStationTerminals.push(paramedetail);
				}
		  });
			$.ajax({
				type : "POST",
				contentType : "application/json ; charset=utf-8",
				url : "/weatherstation/asynsavestation",
				data : JSON.stringify(station),
				dataType : "json",
				async : true,
				success : function(rsp) {
					if (rsp.statusCode == 0) {
						$("#weatherStationId").val(rsp.messageObject.uniqueWeatherStationId),
						$("#weatherstationtable table").find("tr").each(function(i,row){
						    $(rsp.messageObject.weatherStationTerminals).each(function(j,item){
							     if(item.terminalModel==$.trim($(row).children().eq(0).find("input").val())){
							    	 $(row).children().eq(5).find(".terminal-view").attr('data-id', $.trim(item.uniqueWSTId));
							     }
							 });
						 });
						if($.trim($("#fileCover").val())!=""){
							$.ajaxFileUpload({
								type: "POST",
								url: "/weatherstation/upload/"+$("#weatherStationId").val(),
							    fileElementId:'fileCover',
								data : station,
							    dataType: 'json',
							    async : false,
								success: function(data){
					     		   if(data.statusCode==0)
								   {
					     			  toastr.success("站点信息保存成功!");
					     			  document.location.href="/weatherstation";
								   }else  {
									   toastr.error(data.message);
								  }
							    }
							});
						}else{
							  toastr.success("站点信息保存成功!");
							  document.location.href="/weatherstation";
						}
						
					} else {
						 toastr.error(rsp.message);
					}
					setTimeout(function(){
						$("body").mLoading("hide");
					},1000);
				},
				error : function(msg) {
					 toastr.error(msg);
				}
			});
		});

})(jQuery);