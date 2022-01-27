(function($) {
	if (self.frameElement && self.frameElement.tagName == "FRAME") { 
		  parent.document.location.href="/login"
	 } 
	toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"2000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
	$('input, textarea').placeholder();		
	document.onkeydown = function(e){ 
	    var ev = document.all ? window.event : e;
	    if(ev.keyCode==13) {
	    	$(".js-login").trigger( "click" );
	     }
	}      
	$(".logct3j").click(function(){
		$(".logct3j").hasClass("on")?$(".logct3j").removeClass("on"):$(".logct3j").addClass("on");
	});
	$('.js-login').click(function(event){  
		  event.stopPropagation();		 
		  if($.trim($("#UserName").val())==''){
				toastr.warning("用户名必须填写", '');
		        return false;
		    }
		  if($.trim($("#UserPwd").val())==''){
				toastr.warning("密码必须填写", '');
		        return false;
		    }
		  $("#RemberMe").val($(".logct3j").hasClass("on")?"Y":"N");
		  $("body").mLoading({  text:"登录中......"});
		  $.ajax({
				type : "POST",
				contentType : "application/x-www-form-urlencoded; charset=utf-8",
				url : "/login/userLogin",
				data : $("#loginForm").serialize(),
				async : true,
				success : function(rsp) {
					 $("body").mLoading("hide");
					if (rsp.statusCode == 0) {
						 if(rsp.messageObject.isForceChangePwd==1)
						{
							 JqueryDialog.Open("修改密码",'/User/managepwd/'+rsp.messageObject.uniqueId,600,240);
						}else{
							document.location.href="/home";
						}						
					} else {
						toastr.error(rsp.message);
					}
				}
			});
		   return false;  
	 });  
	$('#btnClose').click(function(event){  
		  event.stopPropagation();		
	      window.parent.JqueryDialog.Close();
		  return false;
	});
	$('#btnSavePwd').click(function(event){  
		  event.stopPropagation();		 
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
		  $.ajax({
				type : "POST",
				contentType : "application/x-www-form-urlencoded; charset=utf-8",
				url : "/User/forceChangePwd/"+$("#UserId").val(),
				data : $("#js-userform").serialize(),
				async : true,
				success : function(rsp) {
					if (rsp.statusCode == 0) {
						parent.document.location.href="/home";									
					} else {
						toastr.error(rsp.message);
					}
				}
			});
		   return false;  
	 });  
})(jQuery);