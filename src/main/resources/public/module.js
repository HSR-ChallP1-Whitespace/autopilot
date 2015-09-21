'use strict';

var simulatorApp = angular.module('simulatorApp',
    [
	'ngRoute',
    'ngResource',
    'ui.bootstrap',

	'carrera.commons',	
    'simulator'
    ]);

simulatorApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/simulator', {
                templateUrl: 'simulator/simulator.html',
                controller: 'simulatorCtrl'
            }).
            otherwise({
                redirectTo: '/simulator'
            });
    }]);

