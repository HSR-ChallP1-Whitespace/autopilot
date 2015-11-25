var gyrzDataArray;
var roundTimeDataArray;
var velocityDataArray;
var currentUrl;

$(function() {
	$("#urlSelection").change(function () {
		currentUrl = this.value;
		drawCharts();
	});
	
	$( document ).ready(function() {
		initPage();
	});
});

function initPage() {
	loadUrls();
	drawCharts();
}

function drawCharts() {
	loadGYRZData();
	loadRoundTimeData();
	loadVelocityData();
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
	gyrzDataArray.push([null, 'GYR-Z', 'GYR-Z-Smoothed', 'GYR-Z-StdDev', 'GYR-Z-MeanDevFromZero']);
	var jsonData = $.ajax({
		url: '/data' + currentUrl + '/gyrz',
		dataType: 'json',
		async: false
	}).done(function(data) {
		$.each(data, function() {
			var item = [this.time, this.value, this.valueSmoothed, this.valueStdDev, this.meanDevFromZero];
			gyrzDataArray.push(item);
		});
		drawGYRZChart();
	});
}

function loadRoundTimeData() {
	roundTimeDataArray = [];
	roundTimeDataArray.push([null, 'Round time']);
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

function loadVelocityData() {
	velocityDataArray = [];
	velocityDataArray.push([null, 'Velocity']);
	var jsonData = $.ajax({
		url: '/data' + currentUrl + '/velocity',
		dataType: 'json',
		async: false
	}).done(function(data) {
		$.each(data, function() {
			var item = [this.timestamp, this.velocity];
			velocityDataArray.push(item);
		});
		drawVelocityChart();
	});
}

function drawGYRZChart() {
	$('#gyrz_chart_div').highcharts({
		data: {
            rows: gyrzDataArray
        },
        chart: {
        	type: 'line'
        },
        title: {
            text: 'GYR-Z'
        }
	});
}

function drawRoundTimeChart() {
	$('#roundtime_chart_div').highcharts({
		data: {
            rows: roundTimeDataArray
        },
        chart: {
        	type: 'line'
        },
        title: {
            text: 'Round Times'
        }
	});
}

function drawVelocityChart() {
	$('#velocity_chart_div').highcharts({
		data: {
            rows: velocityDataArray
        },
        chart: {
        	type: 'line'
        },
        title: {
            text: 'Velocity'
        }
	});
}
