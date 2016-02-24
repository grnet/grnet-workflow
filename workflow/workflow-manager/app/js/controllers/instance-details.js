/**
 * @author nlyk
 */
(function () {

    'use strict';
    
    angular.module('wfmanagerControllers')
    	.controller('InstanceDetailCtrl', ['$scope', '$http', '$location','$routeParams',
    	                                   '$mdDialog', 'processService', 'CONFIG', 'auth',

            function ($scope, $http, $location, $routeParams, $mdDialog, processService, config, auth) {
    			
    			$scope.instanceId = $routeParams.instanceId;
    			$scope.tasks = [];
    			$scope.imagePath = config.AVATARS_PATH;
    			
    			processService.getTasksByInstanceId($scope.instanceId).then(
    					
    					function(response){
    						$scope.tasks = response.data;
    					},
    					
    					function(response){
    						
    					}
    			
    			); 
            }]
    );

})(angular);
