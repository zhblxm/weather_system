var lastLength=0,navScroll;
(function ($) {
	 toastr.options={"closeButton":true,"debug":false,"positionClass":"toast-top-center","onclick":null,"showDuration":"300","hideDuration":"1000","timeOut":"5000","extendedTimeOut":"1000","showEasing":"swing","hideEasing":"linear","showMethod":"fadeIn","hideMethod":"fadeOut"};
    $(".logout").click(function(){
    	parent.document.location.href="/login/logout";
    });
    $(".category a").click(function(e){
		  e.stopPropagation();		
		  $(".category a").removeClass("on"),$(this).addClass("on"),
		    parent.document.getElementById("content").src=$(this).attr("href"); 
		  return false;
    });
    if($(".category").length>0){
    	navScroll= $(".category").niceScroll();
    }    
    if($(".message").length>0){
    	 $( ".message" ).tooltip({
    	      show: {
    	        effect: "slideDown",
    	        delay: 250
    	      },
    	      position: {
    	          my: "center bottom+20",
    	          at: "right top",
    	          using: function( position, feedback ) {
    	            $( this ).css( position );
    	            $( "<div>" )
    	              .addClass( "arrow" )
    	              .addClass( feedback.vertical )
    	              .addClass( feedback.horizontal )
    	              .appendTo( this );
    	          }
    	        },
    	      content: function() {    	          
    	            return "系统有"+($(".msg_number").text())+"未查看通知";
    	      }    	        
    	    });
    }        
    var fetchNotification=function(){
    	 $.get("/home/notifications", function(result){
       		 result.length==0?$(".msg_alert").addClass("none"):$(".msg_alert").removeClass("none"),$(".msg_alert").html(result.length);
       		 var content="";
       		 $(result).each(function(i,item){
       			 if(i>9) return false;
	       			content+=item.message;//+"&nbsp;&nbsp;。"
       		 });
       		 $(".marqu").html(content);      
       		 /*if(lastLength!= result.length){
       			lastLength=result.length;
       			toastr.info("系统监控到"+lastLength+"条的未查看通知！");
       		 }*/
       	 });
    }
    if(typeof(header) != "undefined" && header){
        fetchNotification();
        setInterval(function(){
        	fetchNotification();
        },1000 * 60 * 5);
    }
    $(".msg_alert").click(function(){
	    parent.document.getElementById("content").src="/notification"; 
    });
    //notification
})(jQuery);