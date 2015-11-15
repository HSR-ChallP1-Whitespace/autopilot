var gyrzDataArray;
var roundTimeDataArray;
var currentUrl;

$(function() {
	$("#urlSelection").change(function () {
		currentUrl = this.value;
		drawCharts();
	});
});

function initPage() {
	loadUrls();
	drawCharts();
}

function drawCharts() {
	loadGYRZData();
	loadRoundTimeData();
}

function loadUrls() {
	var options = $("#urlSelection");
	var jsonData = $.ajax({
		url: '/data/source-urls',
		dataType: 'json',
		async: false
	}).done(function(data) {
		$.each(data, function() {
			options.append($("<option />").val(arguments[1]).text(arguments[0]));
		});
	});
	currentUrl = $("#urlSelection option:eq(0)").val();
}

function loadGYRZData() {
	gyrzDataArray = [];
	var jsonData = $.ajax({
		url: '/data' + currentUrl + '/gyrz',
		dataType: 'json',
		async: false
	}).done(function(data) {
		$.each(data, function() {
			var item = [this.time, this.value, this.valueSmoothed, this.valueStdDev];
			gyrzDataArray.push(item);
		});
		drawGYRZChart();
	});
}

function loadRoundTimeData() {
	roundTimeDataArray = [];
	var jsonData = $.ajax({
		url: '/data' + currentUrl + '/roundtimes',
		dataType: 'json',
		async: false
	}).done(function(data) {
		$.each(data, function() {
			var item = [this.round, this.roundTime];
			roundTimeDataArray.push(item);
		});
		drawRoundTimeChart();
	});
}

function drawGYRZChart() {
	var data = new google.visualization.DataTable();
	data.addColumn('number', 'X');
	data.addColumn('number', 'GYR-Z');
	data.addColumn('number', 'GYR-Z-Smoothed');
	data.addColumn('number', 'GYR-Z-StdDev');
	data.addRows(gyrzDataArray);
	
	var options = {
		hAxis: {
			title: 'Time'
		},
		vAxis: {
			title: 'GYR-Z'
		},
			backgroundColor: '#f1f8e9'
	};

	var chart = new google.visualization.LineChart(document.getElementById('gyrz_chart_div'));
	chart.draw(data, options);
}

function drawRoundTimeChart() {
	var data = new google.visualization.DataTable();
	data.addColumn('number', 'X');
	data.addColumn('number', 'Round Time');
	data.addRows(roundTimeDataArray);
	
	var options = {
		hAxis: {
			title: 'Time'
		},
		vAxis: {
			title: 'Round Time'
		},
			backgroundColor: '#f1f8e9'
	};

	var chart = new google.visualization.LineChart(document.getElementById('roundtime_chart_div'));
	chart.draw(data, options);
}