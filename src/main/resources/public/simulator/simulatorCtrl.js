'use strict';


angular.module('simulator')

    .controller('simulatorCtrl', function ($scope, $routeParams, Simulator, ngstomp) {

        $scope.isSimulatorRunning = false;
        $scope.doesExist = false;
        $scope.raceTrackId= "<raceTrackID>";

        $scope.selectedDesign = "Budapest";
        $scope.availableDesigns = [ $scope.selectedDesign, "Berlin", "Oerlikon", "Hollywood"];

        $scope.connected = false;

        $scope.roundNumber = 1;
        $scope.scaleFactor = 4;
        $scope.gaugeParams = {};
        $scope.gaugeParams.accerlationScale = 1000;
        $scope.MAX_SPEED = 600;

        $scope.baseAnchor = {};
        $scope.canvasWidth = 1200;
        $scope.canvasHeight = 800;
        $scope.sizeOption = {
            description: "1200x800",
            width: 1200,
            height: 800
        };

        $scope.selectedSpec = {description: 'Gyro Z', coord: { vector: 'g', axis: 2 },
            range: { lower: -8000, upper: 8000}};

        $scope.displaySpecifications = [
            {description: 'Acceleration X', coord: { vector: 'a', axis: 0 }},
            {description: 'Acceleration Y', coord: { vector: 'a', axis: 1 }},
            {description: 'Acceleration Z', coord: { vector: 'a', axis: 2 }},
            {description: 'Gyro X', coord: { vector: 'g', axis: 0 }},
            {description: 'Gyro Y', coord: { vector: 'g', axis: 1 }},
            $scope.selectedSpec,
            {description: 'Magnetic X', coord:  { vector: 'm', axis: 0 }},
            {description: 'Magnetic Y', coord:  { vector: 'm', axis: 1 }},
            {description: 'Magnetic Z', coord:  { vector: 'm', axis: 2 }}
        ];



        $scope.sizeOptions = [
        $scope.sizeOption,
        {
            description: "800x600",
            width: 800,
            height: 600
        }, {
            description: "600x400",
            width: 600,
            height: 400
        }];

        $scope.padding = 40.0;

        $scope.ngStompClient = {};
        $scope.recentTime = null;
        $scope.recentNews = { event : { a: [0,1,2], g: [3,4,5], m: [6,7,8]},
            position: { poxX: 0, posY: 0 }};

        $scope.newsWorking = false;
        $scope.currentTeam = "no Team";

        $scope.recentSensorValue = function () {
            var vector = $scope.selectedSpec.coord.vector;
            var axis = $scope.selectedSpec.coord.axis;
            return $scope.recentNews.event[vector][axis];
        };

        $scope.simulatorOn = function () {
            if ( $scope.timerOn === undefined ) {
                var diff = new Date().getTime() - $scope.recentTime;
                return diff < 1000;
            } else {
                return $scope.timerOn;
            }
        };

        $scope.selectSpecification = function () {
        };

        $scope.selectCanvas = function () {
            console.log("choosing canvas " + $scope.sizeOption.description);
            $scope.canvasWidth = $scope.sizeOption.width;
            $scope.canvasHeight = $scope.sizeOption.height;
            var canvas = document.getElementById("racetrack");
            canvas.style.width = $scope.sizeOption.width;
            canvas.style.height = $scope.sizeOption.height;
            adjustScale();
        };

        $scope.selectDesign = function () {
            console.log("Selecting Design: " + $scope.selectedDesign);
            Simulator.selectDesign ($scope.selectedDesign, function (trackInfo) {
                $scope.isSimulatorRunning = true;
                $scope.tracks = trackInfo.sections;
                $scope.raceTrackId = trackInfo.trackId;

                $scope.boundaryWidth = trackInfo.width;
                $scope.boundaryHeight = trackInfo.height;

                $scope.originalAnchorX = trackInfo.initialAnchor.posX;
                $scope.originalAnchorY = trackInfo.initialAnchor.posY;
                $scope.baseAnchor.angle = trackInfo.initialAnchor.angle360;

                adjustScale();
            });
        };

        $scope.connect = function () {

            var REST_API_URL = '';
            $scope.ngStompClient = ngstomp(REST_API_URL+'/messages');
            $scope.ngStompClient.connect({}, function (client, frame) {
                $scope.connected = true;

                /*
                $scope.ngStompClient.subscribe('/topic/echo', function (message) {
                    var msg = JSON.parse(message.body).message;
                    console.log(msg);
                });
                */

                $scope.ngStompClient.subscribe('/topic/simulator/clock', function (message) {
                    var msg = JSON.parse(message.body).timestamp;
                    $scope.recentTime = msg;
                });

                $scope.ngStompClient.subscribe('/topic/simulator/news', function (message) {

                    $scope.newsWorking = true;

                    var msg = JSON.parse(message.body);
                    if ( msg.event.type === 'ROUND_PASSED') {
                        $scope.roundNumber = msg.roundNumber;
                    } else {
                        $scope.roundNumber = msg.roundNumber;
                        $scope.currentTeam = msg.teamId;
                        $scope.recentNews = msg;
                        drawOnCanvas();
                    }
                });

            }, function (client, frame) {
                console.log('sth went wrong: ' + client + '  - ' + frame);
            });
        };

        $scope.powerup = function() {
            console.log("Power up.");
            Simulator.powerup({}, function (response) {
            });
        };

        $scope.powerdown = function() {
            console.log("Power down.");
            Simulator.powerdown({}, function (response) {
            });
        };

        $scope.reset = function() {
            console.log("Resetting track.");
            Simulator.reset({}, function (response) {
            });
        };

        $scope.startClock = function() {
            Simulator.start({}, function( response) {
                console.log("simulator activated.");
                $scope.timerOn = true;
            });
        };

        $scope.stopClock = function() {
            Simulator.stop({}, function ( response) {
                console.log("simulator deactivated.");
                $scope.timerOn = false;
            });
        };

        $scope.startRace = function() {
            Simulator.startRace({}, function( response) {
                console.log("simulator activated.");
                $scope.timerOn = true;
            });
        };

        $scope.stopRace = function() {
            Simulator.stopRace({}, function ( response) {
                console.log("simulator deactivated.");
                $scope.timerOn = false;
            });
        };

        $scope.echo = function () {
            $scope.ngStompClient.send('/stomp/messages', {}, JSON.stringify({
                'message': 'echo: ' + new Date().getMilliseconds(),
                'destination': '/topic/echo'
            }) )
        };

        $scope.disconnect = function () {
            if($scope.connected){
                $scope.ngStompClient.disconnect(function(){
                    console.log("disconnected.");
                });
                $scope.connected = false;
            }
        };

        $scope.$on('$destroy', function () {
            // Cleanup
            $scope.disconnect();
        });

        $scope.resetCar = function () {
            Simulator.resetCar({}, function ( response ) {
                console.log ( response );
                $scope.recentNews = { position: { poxX: 0, posY: 0 }};
                drawOnCanvas();
            })
        };

        var adjustScale = function () {

            var px_cm_x = ( $scope.canvasWidth - 2 * $scope.padding ) / $scope.boundaryWidth;
            var px_cm_y = ( $scope.canvasHeight - 2 * $scope.padding ) / $scope.boundaryHeight;
            $scope.scaleFactor = Math.min( px_cm_x, px_cm_y);

            $scope.baseAnchor = {
                angle: $scope.baseAnchor.angle,
                x: $scope.padding + $scope.originalAnchorX * $scope.scaleFactor,
                y: $scope.padding + $scope.originalAnchorY * $scope.scaleFactor
            };
            drawOnCanvas ();
        };

        $scope.init = function () {

            $scope.doesExist = true;
            $scope.isSimulatorRunning = true;

            Simulator.getTrackInfo({}, function (trackInfo) {
                $scope.isSimulatorRunning = true;
                $scope.tracks = trackInfo.sections;
                $scope.raceTrackId = trackInfo.trackId;

                $scope.boundaryWidth = trackInfo.width;
                $scope.boundaryHeight = trackInfo.height;

                $scope.originalAnchorX = trackInfo.initialAnchor.posX;
                $scope.originalAnchorY = trackInfo.initialAnchor.posY;
                $scope.baseAnchor.angle = trackInfo.initialAnchor.angle360;

                adjustScale();
                $scope.connect();
            }, function(err) {
                // ON ERROR
                $scope.isSimulatorRunning = false;
            });
        };

        var drawOnCanvas = function () {

            if(!$scope.isSimulatorRunning){
                return;
            }

            var tracks = $scope.tracks;
            var canvas = document.getElementById("racetrack");
            if ( !angular.isDefined ( canvas ) || canvas === null ) {
                return;
            }
            var ctx = canvas.getContext("2d");
            clear(canvas);
            ctx.fillStyle = "#A0A0A0";
            ctx.strokeStyle = "#F00000";
            ctx.lineWidth=3;
            $scope.width = 20;
            var scale = $scope.scaleFactor;

            var anchor = $scope.baseAnchor;

            // Draw the racetrack
            tracks.forEach(function (sector) {
                ctx.fillStyle = "#A0A0A0";
                if (angular.isUndefined(sector.radius)) {
                    if ( angular.isDefined(sector.id) ) {
                        ctx.fillStyle = "#FF0000";
                    }
                    anchor = straight(ctx, anchor, scale * sector.length);
                } else {
                    anchor = curve ( ctx, anchor, scale * sector.radius, Math.abs(sector.angle), sector.orientation === "LEFT");
                }
            });

            // Draw the final / start
            drawFinal(ctx, $scope.baseAnchor);

            drawPosition (ctx );
            drawSensorValue ();
            drawVelocity ();
            drawPower ();
        };

        var drawFinal = function(ctx, anchor){
            var ofillStyle = ctx.fillStyle;
            var oStrokeStyle = ctx.strokeStyle;
            var width = 15;
            var height = 38;

            // Black underground for final
            ctx.fillStyle = "#000000";
            ctx.beginPath();
            ctx.rect(anchor.x - (width/2), anchor.y - (height/2), width, height);
            ctx.fill();
            ctx.closePath();

            // White final marks
            var markSize = width / 2;
            ctx.fillStyle = "#FFFFFF";
            ctx.lineWidth="1";

            ctx.beginPath();
            for (var i = 0; i < height/markSize; i++) {
                var leftSide = anchor.x - (width/2);
                leftSide = (i % 2) ? leftSide + markSize : leftSide;

                var topPos = anchor.y - (height/2);
                topPos += markSize * i;
                ctx.rect(leftSide, topPos, markSize, markSize);
                ctx.fill();
            }
            ctx.closePath();


            // Black border for final
            ctx.strokeStyle = "#000000";
            ctx.beginPath();
            ctx.rect(anchor.x - (width/2), anchor.y - (height/2), width, height);
            ctx.stroke();
            ctx.closePath();

            // Restore styles
            ctx.fillStyle = ofillStyle;
            ctx.strokeStyle = oStrokeStyle;
        };


        var drawSensorValue = function () {
            if (angular.isDefined($scope.recentNews.event)) {
                var lower = $scope.selectedSpec.range.lower;
                var upper = $scope.selectedSpec.range.upper;
                var value = $scope.recentSensorValue();

                var canvas = document.getElementById("sensorValue");
                if (!angular.isDefined(canvas) || canvas === null) {
                    return;
                }
                var range = upper - lower;
                var posx = ( value - lower ) / range * canvas.width;
                var posy = 10;

                var ctx = canvas.getContext("2d");
                clear(canvas);
                ctx.beginPath();
                ctx.fillStyle = "#A02000";
                ctx.arc(posx, posy, 10, 2 * Math.PI, false);
                ctx.fill();
                ctx.closePath();
            }
        };

        /**
         * Intended for value range [0.1 - 10000] to
         * logarithmically map it down to [0-500] by preserving accuracy on small values
         * @param x
         * @returns {number}
         */
        var logScale = function(x){
            return (Math.log(x)/Math.log(1.09)) + 1.0;
        };

        var clear = function  ( canvas ) {
            var ctx = canvas.getContext("2d");
            ctx.beginPath();
            ctx.rect(0, 0, canvas.width, canvas.height);
            ctx.fillStyle = "#FFFFFF";
            ctx.fill();
            ctx.closePath();
        };

        var drawVelocity = function () {
            if ( angular.isDefined ($scope.recentNews.position)) {
                var canvas = document.getElementById("velocity");
                if ( !angular.isDefined ( canvas ) || canvas === null ) {
                    return;
                }
                var ctx = canvas.getContext("2d");
                clear(canvas);
                ctx.beginPath();
                ctx.fillStyle = "#A02000";
                var width = canvas.width * ($scope.recentNews.velocity / $scope.MAX_SPEED);
                ctx.rect(0, 0, width, canvas.height);
                ctx.fill();
                ctx.closePath();
            }
        };

        var drawPower = function () {
            if ( angular.isDefined ($scope.recentNews.position)) {
                var canvas = document.getElementById("power");
                if ( !angular.isDefined ( canvas ) || canvas === null ) {
                    return;
                }
                var ctx = canvas.getContext("2d");
                clear(canvas);
                ctx.beginPath();
                ctx.fillStyle = "#A02000";
                var width = canvas.width * ($scope.recentNews.currentPower / 260);
                ctx.rect(0, 0, width, canvas.height);
                ctx.fill();
                ctx.closePath();
            }
        };

        var drawPosition = function ( ctx ) {

            if ( angular.isDefined ($scope.recentNews.position)) {
                ctx.fillStyle = "#A04000";
                var r = $scope.width / 2;
                ctx.beginPath();
                var posx = $scope.baseAnchor.x + $scope.recentNews.position.posX * $scope.scaleFactor;
                var posy = $scope.baseAnchor.y + $scope.recentNews.position.posY * $scope.scaleFactor;
                ctx.arc(posx, posy, r, 2 * Math.PI, false);
                ctx.fill();
            }
        };


        var markAnchor = function ( ctx, anchor ) {
            var ofillStyle = ctx.fillStyle;
            var oStrokeStyle = ctx.strokeStyle;

            ctx.fillStyle = "#FF0000";


            ctx.beginPath();
            ctx.arc(anchor.x, anchor.y, 10, 0, Math.PI * 2, false );
            ctx.fill();
            ctx.closePath();

            ctx.fillStyle = ofillStyle;
            ctx.strokeStyle = oStrokeStyle;
        };

        /** *************************************************************************************************
         *
         * Draw a curve
         * @param ctx the canvas context
         * @param anchor the anchor = position and direction of the section to be created
         * @param radius the curvature radius of the curved section
         * @param angle the curvature angle of the curved section
         * @param ccw indicator for counter-clock-wise curvature.
         * @returns the anchor for the next section;
         */
        var curve = function ( ctx, anchor, radius, angle, ccw) {

            //markAnchor(ctx, anchor);

            var sgn = ccw? 1: -1;

            // True driving angle
            var angleR0 = anchor.angle * Math.PI/180.0;
            var center = { x: anchor.x - sgn * radius * Math.sin(angleR0), y: anchor.y - sgn * radius * Math.cos(angleR0) };

            // arc angels
            var ang0 = sgn * Math.PI/2 - angleR0;
            var ang1 = ang0 - sgn * angle * Math.PI / 180.0;

            var width = $scope.width;
            var outer = radius + width / 2;
            var inner = radius - width / 2;
            ctx.beginPath();
            ctx.arc(center.x, center.y, outer, ang0, ang1, ccw );
            ctx.lineTo(center.x + inner * Math.cos ( ang1 ), center.y + inner * Math.sin ( ang1 ));
            ctx.arc(center.x, center.y, inner, ang1, ang0, !ccw);
            ctx.closePath();
            ctx.fill();
            //ctx.stroke();


            var newAnchor = {
                x: center.x + radius * Math.cos ( ang1 ),
                y: center.y + radius * Math.sin ( ang1 ),
                angle: anchor.angle + sgn * angle };

            return newAnchor;
        };

        /** *************************************************************************************************
         *
         * @param length the length of the straight section
         * @param ctx the canvas context
         * @param anchor the anchor = position and direction of the section to be created
         *
         * @returns the anchor for the next section
         */
        var straight = function ( ctx, anchor, length) {

            var angleR = Math.PI * anchor.angle / 180;
            var sin = Math.sin(angleR);
            var cos = Math.cos(angleR);
            var width = $scope.width;

            var p1 = {x : anchor.x - sin * width / 2.0, y: anchor.y - cos * width / 2.0 } ;
            var p2 = {x : p1.x + cos * length, y: p1.y - sin * length };
            var p3 = {x : p2.x + sin * width, y: p2.y + cos * width };
            var p4 = {x : p3.x - cos * length, y: p3.y + sin * length };

            ctx.beginPath();
            ctx.moveTo(p1.x, p1.y);
            ctx.lineTo(p2.x, p2.y);
            ctx.lineTo(p3.x, p3.y);
            ctx.lineTo(p4.x, p4.y);
            ctx.fill();
            ctx.closePath();

            var newAnchor = {
                x: anchor.x + length * Math.cos(angleR),
                y: anchor.y - length * Math.sin(angleR),
                angle: anchor.angle};

            return newAnchor;
        };

        $scope.showParams = function () {
            $scope.paramsClosed = false;
        };

        $scope.hideParams = function () {
            $scope.paramsClosed = true;
        };

        $scope.saveParams = function () {
            Simulator.saveParams ( $scope.gaugeParams, {controller: "params"}, function () {
                console.log("Saved gauge params");
            });
        };



        $scope.init();
    });
