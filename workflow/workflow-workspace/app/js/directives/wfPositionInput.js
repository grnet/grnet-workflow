define(['angular'],

	function (angular) {

		'use strict';

		function positionInput($mdDialog, $filter, $timeout, $http, config) {
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

					//center to municipality
					var center = { lat: config.MAP_CENTER_LAT, lng: config.MAP_CENTER_LNG };

					//holds the location
					//the first time the location will be at the center
					var currentLocation = { lat: config.MAP_CENTER_LAT, lng: config.MAP_CENTER_LNG };

					/**
					 * Initialize the map
					 */
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

						var input = document.getElementById('pac-input');
						var searchBox = new google.maps.places.SearchBox(input);

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

						scope.map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

						// Bias the SearchBox results towards current map's viewport.
						scope.map.addListener('bounds_changed', function () {
							searchBox.setBounds(scope.map.getBounds());
						});

						// Listen for the event fired when the user selects a prediction and retrieve
						// more details for that place.
						searchBox.addListener('places_changed', function () {
							var places = searchBox.getPlaces();

							if (places.length == 0) {
								return;
							}

							scope.placeMarker(places[0].geometry.location);

							var bounds = new google.maps.LatLngBounds();

							if (places[0].geometry.viewport) {
								// Only geocodes have viewport.
								bounds.union(places[0].geometry.viewport);
							} else {
								bounds.extend(places[0].geometry.location);
							}

							scope.map.fitBounds(bounds);

						});

						//a workaround for not displaying always the map
						google.maps.event.addListener(scope.map, "idle", function () {
							google.maps.event.trigger(scope.map, "resize");
							scope.map.setCenter(currentLocation);

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
					//initMap();

					/**
					 * Center to given position
					 * 
					 * @param event
					 * @param position
					 */
					scope.$on('centerToPosition', function (event, position) {
						scope.map.setZoom(16);
						scope.map.panTo({ lat: position.latitude, lng: position.longitude });
					});

					/**
					 * Places a new marker
					 * 
					 * @param location
					 */
					var addNewMarker = function (location) {
						//check the zoom level
						if (scope.map.zoom < 14) {
							//emit an exception to controller
							scope.$emit("zoomException", { errorCode: "ZoomUnderThreshold", message: $filter('translate')('zoomThresholdExc') });

							//increase the zoom level
							scope.map.setZoom(16);

							//set map center to previous location
							scope.map.panTo(center);
							return;
						} else {
							checkPostCode(location, true);
						}
					};

					/**
					 * Updates a new marker
					 * 
					 * @param location
					 */
					var updateMarker = function (location) {
						//check the zoom level
						if (scope.map.zoom < 14) {
							//emit an exception to controller
							scope.$emit("zoomException", { errorCode: "ZoomUnderThreshold", message: $filter('translate')('zoomThresholdExc') });

							//increase the zoom level
							scope.map.setZoom(16);

							//set map center to previous location
							scope.map.panTo(currentLocation);
							return;
						} else {
							checkPostCode(location, false);
						}
					};

					/**
					 * Checks if given post code equals with the municipality's one
					 * 
					 * @param location
					 * @param isNew
					 */
					var checkPostCode = function (location, isNew) {
						scope.inProgress = true;

						//get the address/postcode from the placed marker
						$http.get("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.lat() + "," + location.lng()).then(
							function (results) {
								scope.inProgress = false;

								if (results.data.status != "ZERO_RESULTS") {
									var addressName = results.data.results[0].address_components[1].long_name;
									var addressNum = results.data.results[0].address_components[0].long_name;
									var fullAddress = addressName + " " + addressNum;
									var postCode = null;

									for (var i = 0; i < results.data.results[0].address_components.length; i++) {
										if (results.data.results[0].address_components[i].types.indexOf("postal_code") > -1) {
											postCode = results.data.results[0].address_components[i].long_name.replace(" ", "");
										}
									}
								}

								//emit the address
								scope.$emit("setAddress", { address: fullAddress });

								if (isNew) {
									scope.marker = new google.maps.Marker({
										position: location,
										map: scope.map
									});

								} else {
									scope.marker.setPosition(location);
								}

								controller.$setViewValue({ latitude: location.lat(), longitude: location.lng() });
								currentLocation = location;
							});
					};

					/**
					 * Place marker
					 * 
					 * @param location
					 */
					scope.placeMarker = function (location) {
						if (scope.inProgress) {
							return;

						} else {
							if (scope.marker)
								updateMarker(location);
							else
								addNewMarker(location);
						}
					};

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

				}//end of link
			};
		}

		angular.module('wfWorkspaceDirectives').directive('wfPositionInput', ['$mdDialog', '$filter', '$timeout', '$http', 'CONFIG', positionInput]);
	}
);