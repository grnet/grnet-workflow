angular.module('wfDirectives')
	.directive('wfConversation', ['$mdDialog', function($mdDialog) {
		return {
			require: 'ngModel',
			restrict: 'E',
			scope: {
				wfConversationLabel: '@wfConversationLabel',
				ngRequired: '=',
				ngReadonly: '=',
				ngModel: '='
		    },
		    templateUrl: 'directives/wfConversation/wfConversation.tmpl.html',
			link: function (scope, element, attributes, controller) {
				
				scope.ngModel = scope.ngModel || {};
				
				if (typeof scope.ngModel !== 'object') {
					
					scope.ngModel = angular.fromJson(scope.ngModel);
				}
				
				scope.getDateString = function (time) {
                    
                	var date = new Date(time);
                	
                	return date.toLocaleString();
                };

                controller.$validators.wfPositionInput = function(modelValue, viewValue) {
                	
                	if (scope.ngRequired) {
                		if (modelValue) {
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