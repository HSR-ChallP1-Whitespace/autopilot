'use strict';

angular.module('carrera.commons.rest')




.factory('Simulator', ['$resource',
    function($resource){
        return $resource('/api/simulator/:controller/:delta', {}, {
            getTrackInfo: {method:'GET', params:{controller:'track'}, isArray:false},
            start: {method:'POST', params:{controller:'start'}, isArray:false},
            stop: {method:'POST', params:{controller:'stop'}, isArray:false},
            startRace: {method:'POST', params:{controller:'startRace'}, isArray:false},
            stopRace: {method:'POST', params:{controller:'stopRace'}, isArray:false},
            reset: {method: 'POST', params: {controller:'reset'}, isArray: false},
            powerup: {method:'POST', params:{controller:'powerup', delta:10}, isArray:false},
            powerdown: {method:'POST', params:{controller:'powerdown', delta:10}, isArray:false},
            selectDesign: {method: 'POST', params: {controller: 'selectDesign'}, isArray: false}
        });
    }])
;

