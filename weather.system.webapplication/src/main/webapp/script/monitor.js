var latlng=[39.911, 116.422],map;
if(stations.length>0){
	latlng[0]=stations[0].latlng[0],latlng[1]=stations[0].latlng[1];
}
var map = L.map('map').setView([latlng[0],latlng[1]], 10),control,curreMarker;
L.tileLayer($("#map").data("host")+"/{z}/{x}/{y}.png", {
    minZoom:10,maxZoom: 11,
    attribution: '&copy; 气象中心平台管理系统'
}).addTo(map); 
if(stations.length>0){
	$(stations).each(function(i,item){
	    var extIcon = L.icon({
	        iconUrl: '/leaflet/images/marker-icon-2x.png',
	        iconSize: [20, 34],
	        iconAnchor: [10, 10],
	        popupAnchor: [0,-10],
	        shadowUrl: '/leaflet/images/marker-shadow.png',
	        shadowSize: [37, 34],
	        shadowAnchor: [10, 10]
	    });
	    var extMarker=L.marker(item.latlng, { icon: extIcon });
	    extMarker.id=item.id;
	    extMarker.name=item.name;
	    extMarker.cover=item.cover;
	    extMarker.on('click', function(event){
	    		if(control!=null && curreMarker!=null && curreMarker.id==this.id){
	    			return;
	    		}
	    		curreMarker=this;
	    	  	$.get("/monitor/station/"+(this.id),function(data,status){
	    	  		var keys=getKeys(data.messageObject);
	    	  		var values=getValues(data.messageObject, keys);
	    	  		var rows=keys.length;
	    	  		var extControl= L.Control.extend({
	    			 	options: { position: 'topleft' },
	    			    onAdd: function (map) {
	    			           var container = L.DomUtil.create('div', 'markerpopup'), html=' <div class="cate1td1dvbu"><div class="cate1td1dvb1"><p class="cate1td1dvb1p">'+curreMarker.name+', 采集日期('+data.message+')</p><div class="cate1td1dvb1gb"><img src="/resources/images/category1tb1.png" alt="" class="cate1td1dvb1ig  btnclose"></div></div><div class="cate1td1dvb2">';
	    			           var trClass = '';
	    			            if($.trim(extMarker.cover)!=""){
		    			            html+='<div class="cate1td1dvb2a"><div class="cate1td1dvb2ad"><img src="/resources/cover/'+extMarker.cover+'" alt="" class="cate1td1dvb2ai"></div></div>';
	    			            }
	    			        	html+='<div class="cate1td1dvb2b"><table class="cate1td1dvb2btab"><tbody>';
	    			           for(var i=0;i<rows;i){
	    			        	   if(parseInt(i % 4) == 0){
	    			        		   trClass = 'cate1td1dvb2btr1';
	    			        	   } else {
	    			        		   trClass = 'cate1td1dvb2btr2';
	    			        	   }
	    			        	   html+='<tr class="' + trClass + '"><td class="cate1td1dvb2btd1"><div class="cate1td1dvb2btd1d"><div class="cate1td1dvb2btd1da">'+keys[i].substr(6)+'：</div><div class="cate1td1dvb2btd1db">'+values[i]+'</div></div></td>';
			                       if(i+1<rows){
			                    	   html+='<td class="cate1td1dvb2btd2"><div class="cate1td1dvb2btd2d"><div class="cate1td1dvb2btd2da">'+keys[i+1].substr(6)+'：</div><div class="cate1td1dvb2btd2db">'+values[i+1]+'</div> </div></td>';
			                       } else {
			                    	   html+='<td class="cate1td1dvb2btd2"><div class="cate1td1dvb2btd2d"><div class="cate1td1dvb2btd2da"></div><div class="cate1td1dvb2btd2db"></div> </div></td>';
			                       }
			                            
			                          i+=2;
			                          html+='</tr>';
			                          if(i>15) break;
	    			           }	    	
	    			        html+='</tbody></table></div></div><div class="cate1td1dvb3"><a href="/weathersearch" class="cate1td1dvb3btna" style="color:#fff;" >历史数据查询</a><a href="/weatherchart" class="cate1td1dvb3btnb" style="color:#fff;">曲线分析</a></div></div><div class="cate1td1dvbsan"><div class="cate1td1dvbbs"></div></div> ';
	    			        $(container).html(html);
	    			       $(document).on("click",".btnclose",function(event){
	    	                	  map.removeControl(control),curreMarker=null;
	    			       });
	    			        return container;
	    			    }
	    		});
	    		control=new extControl();
	    		control.addTo(map);
			});
	    }).addTo(map).bindTooltip(item.name).openTooltip();;
	});
}


getValues = function(source, keys){  
    var result=[],key,_length=0;  
    for(key in keys){  
         result[_length++] = source[keys[key]];  
    }  
    return result;  
}; 

getKeys = function(source){  
    var result=[], key, _length=0; 
    for(key in source){  
       if(source.hasOwnProperty(key)){  
          result[_length++] = key;  
       }  
    }  
    return result.sort();;  
}; 

(function($) { 
	$(document).on("click",".js-station",function(){
		if(control!=null){
			 map.removeControl(control),curreMarker=null;
		}
		var valueArray=$(this).data("latlng").split(",");
		map.setView([parseFloat($.trim(valueArray[0])),parseFloat($.trim(valueArray[1]))], 10);
	});
})(jQuery);