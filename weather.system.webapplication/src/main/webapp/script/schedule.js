(function($) {	
    $('#schedules').DataTable({
        oLanguage: { sLengthMenu: "每页_MENU_记录", sInfo: "显示第_START_到_END_条，共_TOTAL_条",  sSearch: "查询: ", oPaginate: { sFirst: "首页", sPrevious: "上一页",sNext: "下一页", sLast: "尾页" }},
        "serverSide": true,"searching":false, "aaSorting": [[0, "desc"]],
        columns: [
              	{data: "taskDesc", title: "任务类型", sortable: false },
            	{data: "taskStartDate",  title: "开始日期",sortable: false },
            	{data: "taskEndDate",  title: "完成日期",sortable: false },
            	{data: "taskMessage", title: "执行结果",sortable: false }
                ],
        "ajax": "/schedule/logs"
    }),$('#schedules').addClass("table table-striped table-bordered form-inline dataTable no-footer");
})(jQuery);