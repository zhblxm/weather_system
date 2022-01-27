(function($) {
	  $('input, textarea').placeholder(),toastr.options = { "closeButton": false, "debug": false, "positionClass": "toast-top-full-width","onclick": null,"top": "200","showDuration": "300","hideDuration": "1","timeOut": "2000","extendedTimeOut": "1000","showEasing": "swing", "hideEasing": "linear","showMethod": "fadeIn","hideMethod": "fadeOut"};
     $("#btnSave").click(function() {
    	 	var invalid=false;
			$("input[type='text']").each(function(i){
				if(1==$(this).attr("require") && ""==$.trim($(this).val())){
					invalid=true;toastr.error($(this).attr("placeholder") );
					return false;
				}
			});	
			if(invalid){
				return false;
			}
			if(""==$.trim($("#SMTPPwd").val())){
				toastr.error($("#SMTPPwd").attr("placeholder") );
				return false;
			}
			if(""==$.trim($("#SMSPwd").val())){
				toastr.error($("#SMSPwd").attr("placeholder") );
				return false;
			}
			var setting = {
					smtpserver : $.trim($("#SMTPServer").val()),
					smtpuserName : $.trim($("#SMTPUser").val()),
					smtpuserPwd:$("#SMTPPwd").val(),			
					receiveEmail:$("#SMTPReceive").val(),					
					smsurl:$.trim($("#SMSServer").val()),
					smsuserName:$.trim($("#SMSUser").val()),
					smsuserPwd:$.trim($("#SMSPwd").val()),
					smsphone:$.trim($("#SMSPhone").val()),
					uniqueId:$.trim($("#sid").val())
			};
			$.ajax({
				type : "POST",
				contentType : "application/json ; charset=utf-8",
				url : "/emailsetup/save",
				data : JSON.stringify(setting),
				dataType : "json",
				async : false,
				success : function(rsp) {
					if (rsp.statusCode == 0) {
						 toastr.success("邮件和短信服务设置成功！");
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