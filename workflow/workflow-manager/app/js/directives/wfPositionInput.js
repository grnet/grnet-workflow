define(['angular'],

	function (angular) {

		'use strict';

		function positionInput($mdDialog, config) {
			return {
				require: 'ngModel',
				restrict: 'E',
				scope: {
					wfPositionLabel: '@wfPositionLabel',
					ngRequired: '=',
					ngReadonly: '=',
					wfIdPrefix: '@wfIdPrefix',
					ngModel: '='
				},
				templateUrl: 'templates/wfPositionInput.tmpl.html',
				link: function (scope, element, attributes, controller) {

					scope.ngModel = scope.ngModel || {};
					scope.showProgress = true;

					if (typeof scope.ngModel !== 'object') {
						scope.ngModel = angular.fromJson(scope.ngModel);
					}

					var center = { lat: config.MAP_CENTER_LAT, lng: config.MAP_CENTER_LNG };

					if (scope.wfPositionCenterLat && scope.wfPositionCenterLng) {

						center = {
							lat: angular.fromJson(scope.wfPositionCenterLat),
							lng: angular.fromJson(scope.wfPositionCenterLng)
						};
					}
					else if (scope.ngModel.latitude && scope.ngModel.longitude) {

						center = {
							lat: scope.ngModel.latitude,
							lng: scope.ngModel.longitude
						};
					}

					function initMap() {
						var wrapperElement;

						// check if an id prefix is present in order to create a wrapper for the map based on given id
						// else, the map will be wrapped in an element using id 'map'.
						// that change allows to have more than one instances of map by using a different wrapper id
						if (typeof scope.wfIdPrefix != 'undefined' && scope.wfIdPrefix != null && scope.wfIdPrefix != "") {

							//get the element which map will be attached to
							wrapperElement = document.getElementById(scope.wfIdPrefix);

						} else {
							wrapperElement = document.getElementById('map');
						}

						//create the map
						scope.map = new google.maps.Map(wrapperElement, {
							center: center,
							zoom: 14,
							mapTypeControl: false,
							mapTypeControlOptions: {
								style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
								position: google.maps.ControlPosition.TOP_CENTER
							},
							scaleControl: true,
							streetViewControl: false
						});

						//a workaround for not displaying always the map
						google.maps.event.addListener(scope.map, "idle", function () {
							google.maps.event.trigger(scope.map, "resize");

							google.maps.event.clearListeners(scope.map, "idle");
						});

						if (scope.ngModel.latitude && scope.ngModel.longitude) {
							var position = { lat: scope.ngModel.latitude, lng: scope.ngModel.longitude }
							scope.marker = new google.maps.Marker({
								position: position,
								map: scope.map
							});
						}

						//add click event listener and add the marker to location
						if (!scope.ngReadonly) {
							google.maps.event.addListener(scope.map, 'click', function (event) {
								scope.placeMarker(event.latLng);
							});
						}

						scope.showProgress = false;
					}

					//in order to show the map at all times
					//faced a problem while rendering the map
					window.setTimeout(initMap.bind(this), 550);

					if (!scope.ngReadonly) {

						google.maps.event.addListener(scope.map, 'click', function (event) {
							scope.placeMarker(event.latLng);
						});
					}

					scope.placeMarker = function (location) {

						if (scope.marker) {
							scope.marker.setPosition(location);

						} else {
							scope.marker = new google.maps.Marker({
								position: location,
								map: scope.map
							});
						}

						controller.$setViewValue({ latitude: location.lat(), longitude: location.lng() });
					}

					controller.$validators.wfPositionInput = function (modelValue, viewValue) {

						if (scope.ngRequired) {
							if (modelValue.latitude && modelValue.longitude) {
								return true;
							} else {
								return false;
							}
						} else {

							return true;
						}
					};
				}
			};
		}

		angular.module('wfManagerDirectives').directive('wfPositionInput', ['$mdDialog', 'CONFIG', positionInput]);
	}
);