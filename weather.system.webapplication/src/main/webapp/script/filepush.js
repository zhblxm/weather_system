(function($) {
    $('input, textarea').placeholder(),
    toastr.options = {
        "closeButton": false,
        "debug": false,
        "positionClass": "toast-top-full-width",
        "onclick": null,
        "top": "200",
        "showDuration": "300",
        "hideDuration": "1",
        "timeOut": "2000",
        "extendedTimeOut": "1000",
        "showEasing": "swing",
        "hideEasing": "linear",
        "showMethod": "fadeIn",
        "hideMethod": "fadeOut"
    };
    $(".switchdate").on("click",
    function(event) {
        event.stopPropagation();
        $(".switchdate .active").removeClass("active"),
        $(this).find(".cate7b2back1").addClass("active"),
        $("#ftptype").val($(this).data("ftptype"));
        return false;
    });
    $(".switchonoff").on("click", function(event) {
        event.stopPropagation();
        $(".switchonoff .active").removeClass("active"),
        $(this).find(".cate7b2back1").addClass("active"),
        $("#ftponoff").val($(this).data("ftponoff"));
        return false;
    });
    $('#fileTemplet').change(function(event){
		event.stopPropagation();
		$(".path").val($(this).val());
	});
    $("#btnSave").click(function() {
        var invalid = false;
        $("input[type='text']").each(function(i) {
            if (1 == $(this).attr("require") && "" == $.trim($(this).val())) {
                invalid = true;
                toastr.error($(this).attr("placeholder"));
                return false;
            }
        });
        if (invalid) {
            return false;
        }
        if ("" == $.trim($("#FtpPwd").val())) {
            toastr.error($("#FtpPwd").attr("placeholder"));
            return false;
        }
        var setting = {
            ftpurl: $.trim($("#FtpUrl").val()),
            ftpport: $.trim($("#FtpPort").val()),
            ftpuser: $("#FtpUser").val(),
            ftppwd: $("#FtpPwd").val(),
            ftptype: $.trim($("#ftptype").val()),
            ftponoff: $.trim($("#ftponoff").val())
        };
        $.post("/filepush/save", setting,
        function(response) {
            if (response.statusCode == 0) {
                toastr.success("文件推送设置成功！");
            } else {
                toastr.error(rsp.message);
            }
        });

    });
    $("#btnUpload").on("click", function(event) {
        event.stopPropagation();
        $("body").mLoading({
            text: "正在上传......"
        });
        if ($.trim($("#fileTemplet").val()) == "") {
            toastr.error("请选择模板文件！!");
            $("body").mLoading("hide");
            return;
        }
        $.ajaxFileUpload({
            type: "POST",
            url: "/filepush/upload",
            fileElementId: "fileTemplet",
            dataType: 'json',
            async: true,
            success: function(data) {
                if (data.statusCode == 0) {
                    toastr.success("模板文件保存成功!");
                } else {
                    toastr.error(data.message);
                }
                $("body").mLoading("hide");
            }
        });
    });
})(jQuery);