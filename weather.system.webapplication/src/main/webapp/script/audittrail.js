(function($) {	
    $('#logs').DataTable({
        oLanguage: { 
        	sLengthMenu: "每页_MENU_记录", sInfo: "显示第_START_到_END_条，共_TOTAL_条",  sSearch: "查询: ",sEmptyTable: "没有查询到符合当前条件的数据",  sInfoEmpty:     "共_TOTAL_条",sZeroRecords:"没有查询到符合当前条件的数据",
        	oPaginate: { sFirst: "首页", sPrevious: "上一页",sNext: "下一页", sLast: "尾页" },sSearch:"查询操作者: ","sSearchPlaceholder":"请输入操作者名称"
    },
        "serverSide": true, "aaSorting": [[0, "desc"]], 
        columns: [
              	{data: "userName", title: "操作者名称", sortable: true,name: "NAME" },
            	{data: "requestDate", title: "访问日期", sortable: true,name: "DATE" },
            	{data: "description",  title: "执行动作",sortable: false },
            	{data: "clientIP", title: "客户端IP地址",sortable: false },
            	{ data: "userAuditTrailLogId", title: "",sortable: false,sClass:"center", render: function(data, type, row) {
            	         return  '<a class="btn btn-default btn-sm js-view" href="#" data-id="'+data+'"><i class="fa fa-book"></i>详情</a>';
            	     }
            	 }
                ],
        "ajax": "/audittrail/logs"
    }),$('#logs').addClass("table table-striped table-bordered form-inline dataTable no-footer");
    $(document).on("click",".js-view",function(event) {
		  event.stopPropagation();
		 JqueryDialog.Open("日志详情","/audittrail/detail/"+$(this).data("id"),600,400);
		 return false;
    });
})(jQuery);