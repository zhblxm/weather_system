(function($) {
	  $('input, textarea').placeholder();
	    toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
		$("body").mLoading({  text:"加载中......"});	
	    window.onload=function(){
	        if($.trim($(".js-port").data("default"))!=""){
		    	$(".js-port").val($(".js-port").data("default"));
		    }
		    var baudrate="9600";
		    if($.trim($(".js-baudrate").data("default"))!=""){
		    	baudrate=$(".js-baudrate").data("default");
		    }
		    $(".js-baudrate").val(baudrate);
		    setTimeout(function(){
		    	$("body").mLoading("hide");	
		    },500);
	    }
		$("#btnSave").click(function() {		
			$.ajax({
				type : "POST",
				url : "/serial/asynSave",
				data : $("#js-serialform").serialize(),
				success : function(rsp) {
					if (rsp.statusCode == 0) {
						$("#UserId").val(rsp.messageObject);
						 toastr.success("串口信息保存成功！");
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