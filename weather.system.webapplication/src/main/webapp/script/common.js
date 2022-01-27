window.STATICTABLE = {}, window.GLOBALTABLE = {};
(function ($) {
//	if (!self.frameElement || self.frameElement.tagName != "FRAME") { 
//		  document.location.href="/home"
//	 } 
    $('.cate4td1d2b1 .cate4td1d2b1hid').click(function () { $('.photoxs').show(); });
    $('.guanbimg').click(function () { $('.photoxs').hide(); });
    $('.cate1td2dva2a1').click(function () {
        if ($(this).children('.cate1td2dva2a1a').hasClass('on')) {
            $(this).next('.cate1td2dva2a2').hide();
            $(this).children('.cate1td2dva2a1a').removeClass('on');
        } else {
            $(this).next('.cate1td2dva2a2').show();
            $(this).children('.cate1td2dva2a1a').addClass('on');
        }
    });
    $('.logct3j').click(function () { if ($(this).hasClass('on')) { $(this).removeClass('on'); } else { $(this).addClass('on'); } });
    $('.cate2td1dva2ack1').click(function () { if ($(this).hasClass('on')) { } else { $('.cate2td1dva2ack1').removeClass('on'); $(this).addClass('on'); } });
    $('.cate2td1dva2ack1b').click(function () { if ($(this).hasClass('on')) { } else { $('.cate2td1dva2ack1b').removeClass('on'); $(this).addClass('on'); } });
    $('.cate3td1dva2ack1').click(function () { if ($(this).hasClass('on')) { } else { $('.cate3td1dva2ack1').removeClass('on'); $(this).addClass('on'); } });
    $('.cate3td1dva2ack1a').click(function () { if ($(this).hasClass('on')) { } else { $('.cate3td1dva2ack1a').removeClass('on'); $(this).addClass('on'); } });
   /* $('.cate7b2back1').click(function () { if ($(this).hasClass('on')) { } else { $('.cate7b2back1').removeClass('on'); $(this).addClass('on'); } });
    $('.cate7b4back1').click(function () { if ($(this).hasClass('on')) { } else { $('.cate7b4back1').removeClass('on'); $(this).addClass('on'); } });
    $('.cate7a1 a').click(function () { $('.cate7a1 a').removeClass('on'); $(this).addClass('on'); });*/
    $('.temptdlsan').click(function(){
		if($(this).hasClass('on')){
			$(this).removeClass('on');
			parent.pageframe.cols="280,*";
		}else{
			$(this).addClass('on');
			parent.pageframe.cols="0,*";
		}
		
	});
    $('.temptdlsanright').click(function(){
		if($(this).hasClass('on')){
			$(this).removeClass('on');
			$('.cate2td2dv').css({"width":391+"px"});			
		}else{
			$(this).addClass('on');
			$('.cate2td2dv').css({"width":0+"px"});			
		}
	});
    $(".cate1td2dva2 ").niceScroll();
    window.GLOBALTABLE.createTable = function (settings) {
		$("body").mLoading({  text:"加载中......"});
    	$('#' + settings.id).removeClass('display').addClass('table table-striped table-bordered');
        $('#' + settings.id).removeClass('display').addClass('table table-striped table-bordered form-inline');
        window.STATICTABLE = $('#' + settings.id).DataTable({
            oLanguage: {
                "lengthMenu ": "每页_MENU_记录", sInfo: "显示第_START_到_END_条，共_TOTAL_条",sInfoEmpty:"共_TOTAL_条",sEmptyTable: "没有查询到符合当前条件的数据", sZeroRecords:"没有查询到符合当前条件的数据",
                sSearch: (settings.search==null?"查询: ":settings.search),"sSearchPlaceholder":(settings.placeholder==null?"":settings.placeholder),
                oPaginate: {
                    sFirst: "首页",
                    sPrevious: "上一页",
                    sNext: "下一页",
                    sLast: "尾页"
                }
            },
            "processing": false,
            "serverSide": true,
            dom: 'Bfrtip',
            "aaSorting": settings.sorting,
            columns: settings.columns,
            buttons: settings.buttons,
            "fnDrawCallback":function(aoData){
            	$("body").mLoading("hide");
            },
            "ajax": settings.url
        });
        if($('#' + settings.id).length==0){
        	$("body").mLoading("hide");
        }
    };
    window.GLOBALTABLE.Refresh = function () {
        if (window.STATICTABLE) window.STATICTABLE.ajax.reload();
    };
    $(".temptdrtbzuxo").click(function(){
    	document.location.href="/login/logout";
    });
})(jQuery);
function IsIntegerOnly(e)
{  
    var keycode = document.all ? event.keyCode :e.which;
   
    if(keycode==8 || keycode==0 || (keycode>47&&keycode<58)) 
    {
        return true;
    }   
    else
    {
        return false;
    }         
}


getObjectValues = function(source, keys){  
    var result=[],key,_length=0;  
    for(key in keys){  
         result[_length++] = source[keys[key]];  
    }  
    return result;  
}; 

getObjectKeys = function(source){  
    var result=[], key, _length=0; 
    for(key in source){  
       if(source.hasOwnProperty(key)){  
          result[_length++] = key;  
       }  
    }  
    return result.sort();;  
};