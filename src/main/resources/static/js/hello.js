$(document).ready(function() {
    $.ajax({
        url: "http://localhost:8080/greeting?name=Ben"
    }).then(function(data) {
       $('.greeting-id').append(data.id);
       $('.greeting-content').append(data.content);
    });
});

$("#tabs-3").load(function() {
	console.log('Tab - focused');
	$.ajax({
		url: "http://localhost:8080/monitor?server=capture-dev1-wrangler1.altidev.net&service=Bootstrap"
	}).then(function(data) {
		$("#tabs-3").children("p").append(data);
	});
});

$( "#tabs" ).tabs({
	activate: function( event, ui ) {
		console.log('tab activated');
		if(ui.newTab.index() == $("#tabs-3")) {
			$.ajax({
				url: "http://localhost:8080/monitor?server=capture-dev1-wrangler1.altidev.net&service=Bootstrap"
			}).then(function(data) {
				$("#tabs-3").children("p").append(data);
			});
        }
	}
	});