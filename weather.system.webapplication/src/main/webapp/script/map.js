(function($) {
	var map = L.map('map').setView([39.991754014757, 116.487585177952], 10),marker;
	L.tileLayer(host+'/{z}/{x}/{y}.png', {
	    minZoom: 10,
	    maxZoom: 11,
	    attribution: '&copy; 气象中心平台管理系统'
	}).addTo(map);
	L.control.search({
    url: '/weatherStationcategory/mapsearch?word={s}',
    textPlaceholder: '输入城市名称',
    hideMarkerOnCollapse: true,
    moveToLocation: function(latlng, title, map) {
        if (marker != null) {
            marker.remove();
        }
        marker = L.marker(latlng, L.icon({
            iconUrl: '/leaflet/images/marker1.png',
            iconSize: [20, 34],
            iconAnchor: [10, 10],
            popupAnchor: [0, -10],
            shadowUrl: 'resources/images/shadow50.png',
            shadowSize: [37, 34],
            shadowAnchor: [10, 10]
        }));
        marker.addTo(map).bindPopup("当前"+title+"附近").openPopup();
        map.setView(latlng, 10);
    }
	}).addTo(map);	
	function setmapcenter(e) {
	    //console.log("[" + e.latlng.lat + "," + e.latlng.lng + "],");
		 var center=map.getCenter();
	    if($(parent.document).length>0)
	    {
	    	$(parent.document).find("#Longitude").val(center.lng),	$(parent.document).find("#Latitude").val(center.lat);
	    }
	}
	map.on('mousemove ', setmapcenter);
})(jQuery);