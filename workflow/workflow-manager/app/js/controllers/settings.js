/**
 * @author nlyk
 */
(function () {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('SettingsCtrl', ['$scope', '$http', '$window', '$location', '$mdDialog', 'processService', 'CONFIG', 'auth',

            function ($scope, $http, $window, $location, $mdDialog, processService, config, auth) {
                $scope.settings = null;
                $scope.registries = null;
                
                $scope.isPageNumbering = false;
                $scope.isNew = false;
                
                processService.getSettings().then(
                    function (response) {
                        $scope.settings = response.data;  
                    }
                );
                
                processService.getRegistries().then(
                		function (response){
                			$scope.registries = response.data;
                		}
                );
                
                $scope.settingsChanged = function(){
                    processService.updateSettings($scope.settings).then(
                            function (response) {
                            	$scope.settings = response.data;
                            	$scope.back();
                            }
                    );
                };
                
                /**
                 * Display modal to edit registry
                 */
                $scope.editRegistry = function(registry){
                	$scope.isNew = false;
                	$scope.registry = registry;
                	
                  	$mdDialog.show({
                		controller: function ($mdDialog) {
                			
                			$scope.saveRegistry = function(){
                				processService.updateRegistry($scope.registry).then(
                            			function (response){
                            				
                            				$mdDialog.hide();
                            			});
                			};
                			
                            $scope.cancel = function () {
                            	$mdDialog.hide();
                            };
                                                
                            $scope.deleteRegistry = function(){
                            	processService.deleteRegistry($scope.registry.id).then(
                            			function (response){
                            		         processService.getRegistries().then(
                            	                		function (response){
                            	                			$scope.registries = response.data;
                            	                		}
                            	                );
                            			},function(response){
                                        	$mdDialog.show({
                                        		controller: function ($scope, $mdDialog, error) {
                                        			$scope.error = error;
                                        			
                                                    $scope.cancel = function () {
                                                    	$mdDialog.hide();
                                                    };
                                                },
                                                scope: $scope,
                                                preserveScope: true,
                                                templateUrl: 'templates/exception.tmpl.html',
                                                parent: angular.element(document.body),
                                                targetEvent: event,
                                                locals: {
                                                	'error': response.data
                                                }
                                        	})
                            			});
                            	
                            	$mdDialog.hide();
                            }; 
                        },
                        scope: $scope,
                        preserveScope: true,
                        templateUrl: 'templates/updateRegistry.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                        	'registry': $scope.registry
                        }
                	})
                };
                
                $scope.back = function (){
                	$window.history.back();
                };
                
                /**
                 * Tab change listener
                 */
                $scope.onTabSelected = function (tab){
                	if(tab == 'generalSettings')
                		$scope.isPageNumbering = false;
                	else
                		$scope.isPageNumbering = true;
                };
                
                /**
                 * Display modal to add new registry
                 */
                $scope.addNewRegistry = function (){
                	$scope.registry = null;
                	
                	$scope.codePattern = "[0-9A-Za-z]+";
                	$scope.nextPattern = "[0-9]+";
                	
                	$scope.isNew = true;
                	
                  	$mdDialog.show({
                		controller: function ($mdDialog) {
                			
                            $scope.cancel = function () {
                            	$mdDialog.hide();
                            };
                            
                            $scope.saveRegistry = function(){
                            	processService.createRegistry($scope.registry).then(
                            			function (response){
                            				  processService.getRegistries().then(
                          	                		function (response){
                          	                			$scope.registries = response.data;
                          	                		}
                          	                );
                            				$mdDialog.hide();
                            			},
	                                // error callback
	                                function (response) {
                                        	$mdDialog.show({
                                        		controller: function ($scope, $mdDialog, error) {
                                        			$scope.error = error;
                                        			
                                                    $scope.cancel = function () {
                                                    	$mdDialog.hide();
                                                    };
                                                },
                                                scope: $scope,
                                                preserveScope: true,
                                                templateUrl: 'templates/exception.tmpl.html',
                                                parent: angular.element(document.body),
                                                targetEvent: event,
                                                locals: {
                                                	'error': response.data
                                                }
                                        	})
	                                });
                            };
                        },
                        scope: $scope,
                        preserveScope: true,
                        templateUrl: 'templates/updateRegistry.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                        	'registry': $scope.registry,
                        	'codePattern' : $scope.codePattern,
                        	'nextPattern' : $scope.nexyPattern,
                        	'isNew' : $scope.isNew
                        }
                	})
                };
                
            }]
    );

})(angular);
