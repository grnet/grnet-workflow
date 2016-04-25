/**
 * @author nlyk
 */
(function () {

    'use strict';
    
    angular.module('wfmanagerControllers')
    	.controller('InstanceDetailCtrl', ['$scope', '$http', '$location', '$filter', '$routeParams',
    	                                   '$mdDialog', 'processService', 'CONFIG', 'auth',

            function ($scope, $http, $location, $filter, $routeParams, $mdDialog, processService, config, authProvider) {
    			
    			$scope.instanceId = $routeParams.instanceId;
    			$scope.tasks = [];
    			$scope.imagePath = config.AVATARS_PATH;
    			
    			$scope.isAdmin = authProvider.getRoles().indexOf("ROLE_Admin") >= 0;
    			
    			$scope.instanceName = "";
    			
    			processService.getTasksByInstanceId($scope.instanceId).then(
    					
    					function(response){
    						$scope.tasks = response.data;
    						
    						$scope.instanceName = $scope.tasks[0].processInstance.title;
    					},
    					
    					function(response){
    						
    					});
    			
    			/**
    			 * Delete process instance
    			 */
    			$scope.deleteProcessInstance = function() {
    				
    				//create the dialog
    				var confirmDialog = $mdDialog.confirm()
    					.title($filter('translate')('delete'))
						.content($filter('translate')('deleteInstance'))
						.ariaLabel($filter('translate')('deleteInstance'))
    					.targetEvent(event)
    					.cancel($filter('translate')('cancel'))
    					.ok($filter('translate')('confirm'));
    				
    				//show the dialog
					$mdDialog.show(confirmDialog).then(
							// agree
							function () {
								processService.deleteProcessInstance($scope.instanceId).then(
			    						//success callback
										function(response) {
											$location.path("/history");
			    							
			    						//error callback	
			    						},function(response) {
			    							exceptionModal(response)	
			    						});
								
								$mdDialog.cancel();
							// canceled	
							},function() {
								$mdDialog.cancel();
							});
					};
					
	                function exceptionModal(response,event){
	                	$mdDialog.show({
	                		controller: function ($scope, $mdDialog) {
	                			$scope.error = response.data;
	                			
	                            $scope.cancel = function () {
	                            	 $mdDialog.hide();
	                            };
	                        },
	                        
	                        templateUrl: 'templates/exception.tmpl.html',
	                        parent: angular.element(document.body),
	                        targetEvent: event,
	                        clickOutsideToClose: false
	                	})
	                }
    			
            }]);
})(angular);
