/**
 * @author nlyk
 */
(function () {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('SettingsCtrl', ['$scope', '$http', '$window', '$location', '$mdDialog', 'processService', 'CONFIG', 'auth', '$filter',

            function ($scope, $http, $window, $location, $mdDialog, processService, config, auth, $filter) {
        	
        	 $scope.settings = null;
        	 $scope.facebookPage;
        	 
            processService.getSettings().then(
                    function (response) {
                        $scope.settings = response.data;  
                    },
                    //error callback
                    function (response){
                    	alertGenericResponseError(response);
                    }
            );
            
        	$scope.facebookLogin = function(){
        	    FB.init({         	    	
        	      appId: '973785599381892', 
        	      xfbml: true,
        	      version:'v2.5'        	       
        	    });
        	    
        	    
        		FB.getLoginStatus(function(response) {
        			  console.log(response.status);
        			  
        			  if($scope.facebookPage==null || $scope.facebookPage==""){
        				  alertGenericMessage($filter('translate')('undefinedFacebookPage'));
        				  return;
        			  }
        			  
        			  //if(response.status!='connected'){
        				  FB.login(function(response) {
        					   console.log(response.authResponse.accessToken);
        					   var fb = {userID:response.authResponse.userID, 
        							   accessToken:response.authResponse.accessToken,
        							   page:$scope.facebookPage};
        					   processService.postAccessToken(fb).then(
        			                    function (res) {
        			                    	if(res.data)	alertGenericMessage($filter('translate')('tokenClaimed'));
        			                    	else alertGenericMessage($filter('translate')('tokenNotClaimed'));
        			                    },
        			                    function (res){
        			                    	alertGenericResponseError(res);
        			                    }
        					   
        					   );
        					 }, {scope: 'publish_pages,manage_pages'});
        			  //}
        			  
        			  if(response.status=='connected'){
        				  console.log(response.authResponse.accessToken);
        			  }
        		});
        		

        		
        	  };
        	  
        	  // Load the SDK asynchronously
        	  (function(d, s, id) {
        	    var js, fjs = d.getElementsByTagName(s)[0];
        	    if (d.getElementById(id)) return;
        	    js = d.createElement(s); js.id = id;
        	    js.src = "http://connect.facebook.net/en_US/all.js";
        	    fjs.parentNode.insertBefore(js, fjs);
        	  }(document, 'script', 'facebook-jssdk'));
        	
        	  
                $scope.registries = null;               
                $scope.isPageNumbering = false;
                $scope.isNew = false;
                                
                processService.getRegistries().then(
                		function (response){
                			$scope.registries = response.data;
                		}
                );
                
                $scope.settingsChanged = function(){
                    processService.updateSettings($scope.settings).then(
                    		//update settings success callback
                            function (response) {
                            	$scope.settings = response.data;
                            	$scope.back();
                            },
                            //update settings error callback
                            function (response){
                				if(response.status == "403"){
                  					 $mdDialog.show($mdDialog.alert()
                  	                            .parent(document.body)
                  	                            .clickOutsideToClose(true)
                  	                            .title($filter('translate')('error'))
                  	                            .content("No authorized user")
                  	                            .ok($filter('translate')('confirm'))
                  	                    );
                      				}else{
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
                                              }})
                      				}
                            }
                    );
                };
                
                /**
                 * Display modal to edit registry
                 */
                $scope.editRegistry = function(registry,event){
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
                            			//delete registry success callback
                            			function (response){
                            		         processService.getRegistries().then(
                            	                		function (response){
                            	                			$scope.registries = response.data;
                            	                		});
                            			},
                            			//delete registry error callback
                            			function(response){
                            				if(response.status == "403"){
                           					 $mdDialog.show($mdDialog.alert()
                           	                            .parent(document.body)
                           	                            .clickOutsideToClose(true)
                           	                            .title($filter('translate')('error'))
                           	                            .content("No authorized user")
                           	                            .ok($filter('translate')('confirm'))
                           	                    );
	                           				}else{
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
	                                                   }})
	                           				}
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
                            				if(response.status == "403"){
                            					 $mdDialog.show($mdDialog.alert()
                            	                            .parent(document.body)
                            	                            .clickOutsideToClose(true)
                            	                            .title($filter('translate')('error'))
                            	                            .content("No authorized user")
                            	                            .ok($filter('translate')('confirm'))
                            	                    );
                            				}else{
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
                                                    }})
                            				}
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
                
                /**
                 * Helper function for showing generic error alert
                 * @param response
                 */
                function alertGenericResponseError(response) {
                    $mdDialog.show($mdDialog.alert()
                            .parent(document.body)
                            .clickOutsideToClose(true)
                            .title($filter('translate')('error'))
                            .content(response.data.message)
                            .ok($filter('translate')('confirm'))
                    );
                }
                
                
                /**
                 * Helper function for showing generic error alert
                 * @param response
                 */
                function alertGenericMessage(msg) {
                    $mdDialog.show($mdDialog.alert()
                            .parent(document.body)
                            .clickOutsideToClose(true)
                            .title($filter('translate')('error'))
                            .content(msg)
                            .ok($filter('translate')('confirm'))
                    );
                }
                
            }]
    );

})(angular);
