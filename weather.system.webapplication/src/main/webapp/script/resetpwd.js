(function($) {
	toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	$('input, textarea').placeholder();		
	document.onkeydown = function(e){ 
	    var ev = document.all ? window.event : e;
	    if(ev.keyCode==13) {
	    	$("#btnSavePwd").trigger( "click" );
	     }
	}      
	$('#btnBack').click(function(event){  
		  event.stopPropagation();		
	      document.location.href="/User/Manage"
		  return false;
	});
	$('#btnSavePwd').click(function(event){  
		  event.stopPropagation();		
		  if($.trim($("#OriginPassowrd").val())==''){
				toastr.warning("原始密码必须填写", '');
		        return false;
		    }
		  if($.trim($("#UserPassword").val())==''){
				toastr.warning("密码必须填写", '');
		        return false;
		    }
		  if($.trim($("#ConfirmPassword").val())==''){
				toastr.warning("确认密码必须填写", '');
		        return false;
		    }
		  if($.trim($("#ConfirmPassword").val())!=$.trim($("#UserPassword").val())){
				toastr.warning("两次密码输入必须一致！", '');
		        return false;
		    }
		  var passwordObj={
				  originPassowrd:$.trim($("#OriginPassowrd").val()),
				  newPassword:$.trim($("#UserPassword").val()),
				  isForcePwd:$('#IsForceChangePwd').is(':checked')?1:0
		  };
		  $.ajax({
				type : "POST",
				contentType : "application/json ; charset=utf-8",
				url : "/User/resetpwd/"+$("#UserId").val(),
				data : JSON.stringify(passwordObj),
				async : true,
				success : function(rsp) {
					if (rsp.statusCode == 0) {		
						toastr.success("密码已经成功修改！");
					} else {
						toastr.error(rsp.message);
					}
				}
			});
		   return false;  
	 });  
})(jQuery);