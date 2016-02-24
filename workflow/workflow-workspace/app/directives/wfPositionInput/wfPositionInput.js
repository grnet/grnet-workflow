angular.module('wfDirectives')
	.directive('wfPositionInput', ['$mdDialog', function($mdDialog) {
		return {
			require: 'ngModel',
			restrict: 'E',
			scope: {
				wfPositionLabel: '@wfPositionLabel',
				wfPositionCenterLat: '@wfPositionCenterLat',
				wfPositionCenterLng: '@wfPositionCenterLng',
				ngRequired: '=',
				ngReadonly: '=',
				ngModel: '='
		    },
		    templateUrl: 'directives/wfPositionInput/wfPositionInput.tmpl.html',
			link: function (scope, element, attributes, controller) {
				
				scope.ngModel = scope.ngModel || {};
				
				if (typeof scope.ngModel !== 'object') {
					
					scope.ngModel = angular.fromJson(scope.ngModel);
				}

				var center = {lat: 37.980008, lng: 23.732331};
				
				if (scope.wfPositionCenterLat && scope.wfPositionCenterLng) {
					
					center = {
							lat: angular.fromJson(scope.wfPositionCenterLat),
							lng: angular.fromJson(scope.wfPositionCenterLng)
					};
				}
				
				scope.map = new google.maps.Map(element[0].querySelector('#map'), {
					center: center,
					zoom: 14
				});
				
				scope.marker;
				
				if (scope.ngModel.latitude && scope.ngModel.longitude) {
					
					var position = {lat: scope.ngModel.latitude, lng: scope.ngModel.longitude}
					scope.marker = new google.maps.Marker({
			            position: position,
			            map: scope.map
			        });
				}
				
				if (!scope.ngReadonly) {
					
					google.maps.event.addListener(scope.map, 'click', function(event) {
						scope.placeMarker(event.latLng);
					});
				}
				
				scope.placeMarker = function(location) {
					
				    if (scope.marker) {
				    	scope.marker.setPosition(location);
				        
				    } else {
				    	scope.marker = new google.maps.Marker({
				            position: location,
				            map: scope.map
				        });
				    }
				    
				    controller.$setViewValue({latitude: location.lat(), longitude: location.lng()});
				}
				
                controller.$validators.wfPositionInput = function(modelValue, viewValue) {
                	
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
	}]);