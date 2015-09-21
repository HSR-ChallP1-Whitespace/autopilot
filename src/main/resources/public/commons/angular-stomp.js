/*
 * Copyright: 2012, V. Glenn Tarcea
 * MIT License Applies
 */

angular.module('carrera.commons.stomp', []).
    factory('ngstomp', function($rootScope) {
        var stompClient = {};
        var that = this;

        function NGStomp(url) {
            var socket = new SockJS(url);
            that.stompClient = Stomp.over(socket);
        }

        NGStomp.prototype.subscribe = function(queue, callback) {
            that.stompClient.subscribe(queue, function() {
                var args = arguments;
                $rootScope.$apply(function() {
                    callback(args[0]);
                })
            })
        };

        NGStomp.prototype.send = function(queue, headers, data) {
            that.stompClient.send(queue, headers, data);
        };

        NGStomp.prototype.connect = function(headers, on_connect, on_error) {
            that.stompClient.connect( headers,
                function(frame) {
                    $rootScope.$apply(function() {
                        on_connect.call(stompClient, frame);
                    })
                },
                function (frame) {
                    $rootScope.$apply(function() {
                        on_error.call(stompClient, frame);
                    })
                }
            );
        };

        NGStomp.prototype.disconnect = function(callback) {
            that.stompClient.disconnect(function() {
                callback();
            })
        };


        return function(url) {
            return new NGStomp(url);
        }
    });