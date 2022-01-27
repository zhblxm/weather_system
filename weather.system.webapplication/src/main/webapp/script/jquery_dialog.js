var _frameWidthTemp = 0;
var _frameHeithTemp = 0;
var ENABLE = true;
var JqueryDialog = {

    "cBackgroundColor": "#ffffff",

    "cBorderSize": 3,

    "cBorderColor": "#999999",

    "cHeaderBackgroundColor": "#ffffff",

    "cCloseText": "X Close",

    "cCloseTitle": "Close",

    "cBottomBackgroundColor": "#f0f0f0",

    "cDragTime": "100",

    Open: function (dialogTitle, iframeSrc, iframeWidth, iframeHeight) {
        JqueryDialog.init(dialogTitle, iframeSrc, iframeWidth, iframeHeight, true, true, true);
    },

    Open1: function (dialogTitle, iframeSrc, iframeWidth, iframeHeight, isSubmitButton, isCancelButton, isDrag) {
        JqueryDialog.init(dialogTitle, iframeSrc, iframeWidth, iframeHeight, isSubmitButton, isCancelButton, isDrag);
    },

    Open2: function (dialogTitle, iframeSrc, iframeWidth, iframeHeight) {
        JqueryDialog.init(dialogTitle, iframeSrc, iframeWidth, iframeHeight, true, true, false);
    },

    init: function (dialogTitle, iframeSrc, iframeWidth, iframeHeight, isSubmitButton, isCancelButton, isDrag) {
        var _client_width = document.body.clientWidth;
        var _client_height = document.documentElement.scrollHeight;
        _frameWidthTemp = iframeWidth;
        _frameHeithTemp = iframeHeight;
        //create shadow
        if (typeof ($("#jd_shadow")[0]) == "undefined") {
            $("body").prepend("<div id='jd_shadow'>&nbsp;</div>");
            var _jd_shadow = $("#jd_shadow");
            _jd_shadow.css("width", _client_width + "px");
            _jd_shadow.css("height", _client_height + "px");
            _jd_shadow.css("top", 0 + "px");
        }

        //create dialog
        if (typeof ($("#jd_dialog")[0]) != "undefined") {
            $("#jd_dialog").remove();
        }
        $("body").prepend("<div id='jd_dialog'></div>");

        //dialog location
        var _jd_dialog = $("#jd_dialog");
        var _left = (_client_width - (iframeWidth + JqueryDialog.cBorderSize * 2 + 5)) / 2;
        _left = _left + (document.documentElement.scrollLeft <= 0 ? document.body.scrollLeft : document.documentElement.scrollLeft);
        //_jd_dialog.css("left", (_left < 0 ? 0 : _left) + document.documentElement.scrollLeft + "px");
        _jd_dialog.css("left", (_left < 0 ? 0 : _left) + "px");
        var _top = (document.documentElement.clientHeight - (iframeHeight + JqueryDialog.cBorderSize * 2 + 30)) / 2;
        _top = _top + (document.documentElement.scrollTop <= 0 ? document.body.scrollTop : document.documentElement.scrollTop);
        //_jd_dialog.css("top", (_top < 0 ? 0 : _top) + document.documentElement.scrollTop + "px");
        _jd_dialog.css("top", (_top < 0 ? 0 : _top) + "px");
        //create dialog shadow
        _jd_dialog.append("<div id='jd_dialog_s'>&nbsp;</div>");
        var _jd_dialog_s = $("#jd_dialog_s");
        //iframeWidth + double border
        _jd_dialog_s.css("width", iframeWidth + JqueryDialog.cBorderSize * 2 + "px");
        //iframeWidth + double border + header + bottom
        _jd_dialog_s.css("height", iframeHeight + JqueryDialog.cBorderSize * 2 + 30 + "px");

        //create dialog main
        _jd_dialog.append("<div id='jd_dialog_m'></div>");
        var _jd_dialog_m = $("#jd_dialog_m");
        _jd_dialog_m.css("border", JqueryDialog.cBorderColor + " " + JqueryDialog.cBorderSize + "px solid");
        _jd_dialog_m.css("width", iframeWidth + "px");
        _jd_dialog_m.css("background-color", JqueryDialog.cBackgroundColor);

        //header
        _jd_dialog_m.append("<div id='jd_dialog_m_h'></div>");
        var _jd_dialog_m_h = $("#jd_dialog_m_h");
        _jd_dialog_m_h.css("background-color", JqueryDialog.cHeaderBackgroundColor);
        if (!ENABLE) {
            _jd_dialog_m_h.css("cursor", "text");
        }
        //header left
        _jd_dialog_m_h.append("<span id='jd_dialog_m_h_l'>" + dialogTitle + "</span>");
        //_jd_dialog_m_h.append("<span id='jd_dialog_m_h_r' title='" + JqueryDialog.cCloseTitle + "' onclick='JqueryDialog.Close();'>" + JqueryDialog.cCloseText + "</span>");
        var closeButton = '<img src=\'/resources/images/JAlert/btn-close.png\'/>';
        _jd_dialog_m_h.append("<span id='jd_dialog_m_h_r' title='" + JqueryDialog.cCloseTitle + "' onclick='JqueryDialog.Close();'>" + closeButton + "</span>");

        //body
        _jd_dialog_m.append("<div id='jd_dialog_m_b'></div>");
        var _jd_dialog_m_b = $("#jd_dialog_m_b");
        _jd_dialog_m_b.css("width", iframeWidth + "px");
        _jd_dialog_m_b.css("height", iframeHeight + "px");

        _jd_dialog_m_b.append("<div id='jd_dialog_m_b_1'>&nbsp;</div>");
        var _jd_dialog_m_b_1 = $("#jd_dialog_m_b_1");
        _jd_dialog_m_b_1.css("top", "30px");
        _jd_dialog_m_b_1.css("width", iframeWidth + "px");
        _jd_dialog_m_b_1.css("height", iframeHeight + "px");
        _jd_dialog_m_b_1.css("display", "none");

        _jd_dialog_m_b.append("<div id='jd_dialog_m_b_2'></div>");
        //iframe
        $("#jd_dialog_m_b_2").append("<iframe id='jd_iframe' src='" + iframeSrc + "' scrolling='auto' frameborder='0' width='" + iframeWidth + "' height='" + iframeHeight + "' />");

        //bottom
        //_jd_dialog_m.append("<div id='jd_dialog_m_t' style='background-color:" + JqueryDialog.cBottomBackgroundColor + ";'></div>");
        // var _jd_dialog_m_t = $("#jd_dialog_m_t");

        //register drag
        if (isDrag) {
            DragAndDrop.Register(_jd_dialog[0], _jd_dialog_m_h[0]);
        }
    },


    Close: function () {
        $("#jd_shadow").remove();
        $("#jd_dialog").remove();
    },

    Ok: function () {
        var frm = $("#jd_iframe");
        if (frm[0].contentWindow.Ok()) {
            JqueryDialog.Close();
        }
        else {
            frm[0].focus();
        }
    },

    SubmitCompleted: function (alertMsg, isCloseDialog, isRefreshPage) {
        if ($.trim(alertMsg).length > 0) {
            alert(alertMsg);
        }
        if (isCloseDialog) {
            JqueryDialog.Close();
            if (isRefreshPage) {
                window.location.href = window.location.href;
            }
        }
    }
};

var DragAndDrop = function () {

    var _clientWidth;
    var _clientHeight;
    var _controlObj;
    var _dragObj;
    var _flag = false;

    var _dragObjCurrentLocation;

    var _mouseLastLocation;

    //var _timer;

    //var intervalMove = function(){
    //	$(_dragObj).css("left", _dragObjCurrentLocation.x + "px");
    //	$(_dragObj).css("top", _dragObjCurrentLocation.y + "px");
    //};

    var getElementDocument = function (element) {
        return element.ownerDocument || element.document;
    };

    var dragMouseDownHandler = function (evt) {

        if (_dragObj) {

            evt = evt || window.event;

            _clientWidth = document.body.clientWidth;
            _clientHeight = document.documentElement.scrollHeight;

            $("#jd_dialog_m_b_1").css("display", "");

            _flag = true;

            _dragObjCurrentLocation = {
                x: $(_dragObj).offset().left,
                y: $(_dragObj).offset().top
            };
            _mouseLastLocation = {
                x: evt.screenX,
                y: evt.screenY
            };

            $(document).bind("mousemove", dragMouseMoveHandler);
            $(document).bind("mouseup", dragMouseUpHandler);


            if (evt.preventDefault)
                evt.preventDefault();
            else
                evt.returnValue = false;


            //_timer = setInterval(intervalMove, 10);
        }
    };


    var dragMouseMoveHandler = function (evt) {
        if (_flag) {

            evt = evt || window.event;

            if (ENABLE) {
                var _mouseCurrentLocation = {
                    x: evt.screenX,
                    y: evt.screenY
                };


                _dragObjCurrentLocation.x = _dragObjCurrentLocation.x + (_mouseCurrentLocation.x - _mouseLastLocation.x);
                _dragObjCurrentLocation.y = _dragObjCurrentLocation.y + (_mouseCurrentLocation.y - _mouseLastLocation.y);


                _mouseLastLocation = _mouseCurrentLocation;

                var _left = _dragObjCurrentLocation.x;
                var _top = _dragObjCurrentLocation.y;
                var _client_width = document.documentElement.clientWidth < document.documentElement.scrollWidth ? document.documentElement.scrollWidth : document.documentElement.clientWidth;
                if (_left < 0) {
                    _left = 0;
                }
                else {

                    if (_frameWidthTemp + _left > _client_width) {

                        _left = _client_width - _frameWidthTemp - 10;
                    }
                }
                if (_top < 0) {
                    _top = 0;
                }
//                else {
//        
//                    var _client_height = document.documentElement.clientHeight < document.documentElement.scrollHeight ? document.documentElement.scrollHeight : document.documentElement.clientHeight;
//                    if (_frameHeithTemp + _top > (_client_height)) {
//                        _top = _client_height - _frameHeithTemp;
//                    }
//                }

                $(_dragObj).css("left", _left + "px");
                $(_dragObj).css("top", _top + "px");
            }
            if (evt.preventDefault)
                evt.preventDefault();
            else
                evt.returnValue = false;
        }
    };


    var dragMouseUpHandler = function (evt) {
        if (_flag) {
            evt = evt || window.event;


            $("#jd_dialog_m_b_1").css("display", "none");


            cleanMouseHandlers();


            _flag = false;


            //if(_timer){
            //	clearInterval(_timer);
            //	_timer = null;
            //}
        }
    };


    var cleanMouseHandlers = function () {
        if (_controlObj) {
            $(_controlObj.document).unbind("mousemove");
            $(_controlObj.document).unbind("mouseup");
        }
    };

    return {

        Register: function (dragObj, controlObj) {

            _dragObj = dragObj;
            _controlObj = controlObj;

            $(_controlObj).bind("mousedown", dragMouseDownHandler);
        }
    }

} ();

//-->