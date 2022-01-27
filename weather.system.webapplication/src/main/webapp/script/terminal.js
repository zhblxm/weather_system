(function($) {
	 $('#parameters').DataTable({
		 oLanguage: { 
			 sLengthMenu: "每页_MENU_记录",  sInfo: "显示第_START_到_END_条，共_TOTAL_条",  sSearch: "查询: ",  
			 oPaginate: {  
				 sFirst: "首页",  sPrevious: "上一页", sNext: "下一页",sLast: "尾页" 
					 }
		 }
	 }),$('#parameters').addClass("table table-striped table-bordered form-inline dataTable no-footer");
	 $('<label style="margin-top: 5px;padding-right: 10px;"><a class="btn btn-default btn-sm edit category-back" href="#" ><i class="fa fa-backward fa-lg"></i> 返回要素分类</a></label>').insertBefore($("#parameters_length label"));
	  $(document).on("click",".category-back",function(event)  {
	    	  event.stopPropagation();
	    	  window.history.back();
	    	  return false;
	});
})(jQuery);