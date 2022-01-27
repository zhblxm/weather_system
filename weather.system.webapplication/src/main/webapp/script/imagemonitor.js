(function($) { 
	window.onload = function() {
		FillImageInfo(StationCategoryId, '');
	}
	function removejscssfile(filename, filetype){
		var targetelement=(filetype=="js")? "script" : (filetype=="css")? "link" : "none"
		var targetattr=(filetype=="js")? "src" : (filetype=="css")? "href" : "none"
		var allsuspects=document.getElementsByTagName(targetelement)
		for (var i=allsuspects.length; i>=0; i--){
		if (allsuspects[i] && allsuspects[i].getAttribute(targetattr)!=null && allsuspects[i].getAttribute(targetattr).indexOf(filename)!=-1)
		   allsuspects[i].parentNode.removeChild(allsuspects[i])
		}
	}
	removejscssfile("bootstrap.min.css", "css");

	$('.cate1td4dva2a1b').click(function () {
		var wsNumber = '';
		var wsCategoryId = $(this).data("wscategoryid");
		FillImageInfo(wsCategoryId, wsNumber);
	});
	
	$('.js-station').click(function(){
		var wsNumber = $(this).data("wsnumber");
		var wsCategoryId = $(this).data("wscategoryid");
		FillImageInfo(wsCategoryId, wsNumber);
	});
	
	function FillImageInfo(wsCategoryId, wsNumber){
		$.post("/imagemonitor/getimage",{ wsCategoryId : wsCategoryId, wsNumber : wsNumber }, function(response,status){
			var ImageInfo = $("#divImageInfo");
			var ulImages = $("#ulImages");
			ImageInfo.empty();
			ulImages.empty();
			var strImage = "";
			$(response).each(function(i,item){
				strImage = "<div class='cate4td1d2'>";
				strImage += "<div class='cate4td1d2a'>";
				strImage += "<span class='cate4td1d2ap1'>" + item.weatherStationName + "</span>";
				strImage += "<span class='cate4td1d2ap3'>（" + item.images.length + "张图片）</span>";
				strImage += "</div>";
				strImage += "<div class='cate4td1d2bs'>";
				for(var i=0; i < item.images.length; i++){
					value = item.images[i];
					var index = (i+1).toString();
					strImage += "<div class='cate4td1d2b'>";
					strImage += "<div class='cate4td1d2b1' data-index='" + index +  "'>";
					strImage += "<div class='cate4td1d2b1hid' data-index='" + index +  "'>";
					strImage += "<div class='cate4td1d2b1hidm'>";
					strImage += "<img src='" + value.imageUrl + "' alt='' class='cate4td1d2b1g' />";
					strImage += "</div>";
					//strImage += "<div class='cate4td1d2b1p'>";     
					//strImage += "<span class='cate4td1d2b1p1'>" + value.imageTime + "</span>";  
					//strImage += "</div>";
					strImage += "</div>";
					strImage += "</div>";
					//strImage += "<div class='cate4td1d2b2'>" + value.imageDate + "</div>";
					strImage += "</div>";
					
					ulImages.append("<li><img src='" + value.imageUrl + "' alt='' /></li>");
				};
				strImage += "</div></div>";
			});
			
			ImageInfo.html(strImage);
			$('.cate4td1d2b1 .cate4td1d2b1hid').click(function () {
				var index = $(this).data("index");
				var clientHeight = document.documentElement.clientHeight;
				var clientWidth = document.documentElement.clientWidth;
				var height = document.documentElement.clientHeight - 10;
				var width = height * 1.5;
				var left = (clientWidth - width) / 2 + 500;
				$(".gundong").css("height",height);
				$(".gundong").css("width",width);
				$(".gundong").css("top","260px");
				$(".gundong").css("left",left);
				$("#hwslider").css("height",height);
				$("#hwslider").css("width",width);
				
				var hwSlider = $("#hwslider").hwSlider({
					start:index,
			    	width: width,
			    	height: height,
					autoPlay: false,
					arrShow: true,
					dotShow: true,
					touch: false
				});
				//hwSlider[0].init();
				$('.photoxs').show(); 
			});
		});
	}
	
})(jQuery);