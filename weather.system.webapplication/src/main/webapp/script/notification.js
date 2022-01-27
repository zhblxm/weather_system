(function($) {	
    $('#notifications').DataTable({
        oLanguage: { sLengthMenu: "每页_MENU_记录", sInfo: "显示第_START_到_END_条，共_TOTAL_条",  sSearch: "查询: ", oPaginate: { sFirst: "首页", sPrevious: "上一页",sNext: "下一页", sLast: "尾页" }},
        "serverSide": true,"searching":false, "aaSorting": [[0, "desc"]],
        columns: [
              	{data: "notificationDesc", title: "类型", sortable: false },
            	{data: "message",  title: "消息",sortable: false },
            	{data: "createDate", title: "日期",sortable: false }
                ],
          "fnRowCallback": function(oSettings, json) {  
        	  if(json.isChecked==1){
        		  $(oSettings).css("fontWeight","bold");
        	  }
         } ,
        "ajax": "/notification/logs"
    }),$('#notifications').addClass("table table-striped table-bordered form-inline dataTable no-footer");
    var categories=$(parent.window["Navigation"].document).find(".category a");
    $(parent.window["Navigation"].document).find(".category a").removeClass("on");
    $(categories).each(function(i,item){
    	if($(item).find(".catedvb").text()=="系统通知"){
    		$(this).addClass("on");
    		parent.window["Navigation"].navScroll.doScrollTop(700,3000);
    		return false;
    	}
    });
})(jQuery);