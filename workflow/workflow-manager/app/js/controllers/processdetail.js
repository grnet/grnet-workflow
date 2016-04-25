/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfmanagerControllers')

        .controller('ProcessDetailCtrl',
        ['$scope', '$http', '$routeParams', '$location', '$mdDialog', '$timeout', '$filter', 'processService', 'auth', 'CONFIG',

            /**
             * Controller for the Process Details view
             * @param {object} $scope
             * @param {$http} $http
             * @param {$routeParams} $routeParams
             * @param {$location} $location
             * @param {$mdDialog} $mdDialog
             * @param $timeout
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $http, $routeParams, $location, $mdDialog, $timeout, $filter, processService, authProvider, config) {

                /** @type {WorkflowDefinition} */
                $scope.workflowDefinition = null;
                $scope.processId = $routeParams.processId;
                $scope.iconName = null;
                $scope.groups = null;
                $scope.taskDetails = null;
                $scope.editTaskDetails = null;
                $scope.instances = null;
                $scope.selectedTask = null; 
                $scope.selectedTab = "process";
                $scope.userTaskFormElements = [];
                
                $scope.taskMetadataActiveView = "taskMetadata";
                
                $scope.saveFirst = false; 
                

                function getProcess() {
                    // get the process data
                    processService.getProcess($scope.processId)
                        .then(
                        // success callback
                        function (response) {
                            $scope.workflowDefinition = response.data;
                            
                            if($scope.workflowDefinition.owner == null){
                            	$scope.saveFirst = true;
                            }
                            
                            var endIndex = $scope.workflowDefinition.icon.indexOf(".");
                            $scope.iconName = $scope.workflowDefinition.icon.substring(0, endIndex);
                            
                            if($scope.workflowDefinition.activeDeploymentId == null){
                            	var version = response.data.processVersions[0];
                            	$scope.workflowDefinition.activeDeploymentId = version.deploymentId;
                            }
                            
                            $scope.workflowDefinition.icon = $scope.workflowDefinition.icon || config.DEFAULT_AVATAR;
                        }
                    );
                }
                
                getProcess();

                
                //checking if user has role admin in order to show all groups/owners
                if(authProvider.getRoles().indexOf("ROLE_Admin") >= 0){
                    processService.getGroups().then(
        	                // success callback
        	                function (response) {
        	                	$scope.groups = response.data;
        	                }
                        );
                }else{
                	processService.getUserGroups().then(
                			 function (response) {
         	                	$scope.groups = response.data;
         	                }
                	);
                }
                
                
                processService.getRegistries()
                .then(
                // success callback
                function (response) {
                	$scope.registries = response.data;
                	}
                );
                
                
                /**
                 * Saves the Process Definition form
                 */
                $scope.save = function () {
                    processService.updateProcess($scope.workflowDefinition)
                        .then(
                        // success callback
                        function (response) {
                            $scope.workflowDefinition = response.data;
                            //$scope.redirectTo('/process');
                            getProcess();
                            $scope.saveFirst = false; 
                        },
                        // error callback
                        function (response) {
                        	exceptionModal(response);
                        });
                };

                /**
                 * Click event Handler for the Delete Process button
                 * @param {event} event
                 */
                $scope.askDeleteProcess = function (event) {

                    var confirm = $mdDialog.confirm()
                        .title($filter('translate')('deleteProcess'))
                        .content($filter('translate')('deleteProcessConf') + " " + $scope.workflowDefinition.name + " ?")
                        .ariaLabel($filter('translate')('deleteProcess'))
                        .targetEvent(event)
                        .ok($filter('translate')('confirm'))
                        .cancel($filter('translate')('cancel'));

                    $mdDialog.show(confirm).then(function () {
                        processService.deleteProcess($scope.processId)
                            .then(
                            // success callback
                            function () {
                                $location.path('/process');
                            },
                            // error callback
                            function (response) {
                            	exceptionModal(response);
                            });
                    });
                };

                /**
                 * Find the active version in the definition versions array
                 * @return DefinitionVersion
                 */
                $scope.findActiveVersion = function () {
                    if ($scope.workflowDefinition == null) {
                        return null;
                    }

                    var versions = $scope.workflowDefinition.processVersions;

                    /** @type {DefinitionVersion} */
                    var version = null;
                    for (var index = 0; index < versions.length; index++) {
                        if (versions[index].deploymentId === $scope.workflowDefinition.activeDeploymentId) {
                            version = versions[index];
                            break;
                        }
                    }

                    return version;
                };

                /**
                 * Click event handler for the delete version button
                 * @param event
                 */
                $scope.askDeleteVersion = function (event) {

                    /** @type {DefinitionVersion} */
                    var version = $scope.findActiveVersion();

                    if (version === null) {
                        $mdDialog.show(
                            $mdDialog.alert()
                                .parent(document.body)
                                .clickOutsideToClose(true)
                                .title($filter('translate')('deleteVersionError'))
                                .content($filter('translate')('versionNotFound'))
                                .ariaLabel($filter('translate')('deleteVersionError'))
                                .ok($filter('translate')('confirm'))
                        );
                        return;
                    }

                    var confirm = $mdDialog.confirm()
                        .title($filter('translate')('deleteVersion'))
                        .content($filter('translate')('deleteVersionConf') + " " + version.version + " ?")
                        .ariaLabel($filter('translate')('deleteVersion'))
                        .targetEvent(event)
                        .ok($filter('translate')('confirm'))
                        .cancel($filter('translate')('cancel'));

                    $mdDialog.show(confirm).then(function () {

                        processService.deleteProcessVersion(
                            $scope.processId,
                            $scope.workflowDefinition.activeDeploymentId)
                            .then(
                            // success callback
                            function (response) {
                                $scope.workflowDefinition = response.data;
                            },

                            // error callback
                            function (response) {
                            	exceptionModal(response);
                            });
                    });
                };
                
                /**
                 * Redirects to given path and re-render the view of the given path
                 * @param redirectPath
                 */
                
                $scope.redirectTo = function (redirectPath){
                	$location.path(redirectPath);
                };

                /**
                 * Click event handler fot the Add Version button
                 * @param event
                 */
                $scope.showAddVersion = function (event) {
                    $mdDialog.show({
                        controller: 'addDefinitionController',
                        templateUrl: 'templates/adddefinition.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                            'process': $scope.workflowDefinition
                        }
                    });
                };

                /**
                 * Shows a dialog with the process diagram
                 * @param event
                 */
                $scope.showDiagram = function (event) {
                    $mdDialog.show({
                        controller: function ($scope, $mdDialog, process, service) {
                            $scope.process = process;
                            $scope.service = service;
                            $scope.cancel = function () {
                                $mdDialog.cancel();
                            };
                        },
                        templateUrl: 'templates/diagram.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                            'service': config.WORKFLOW_SERVICE_ENTRY,
                            'process': $scope.workflowDefinition
                        }
                    })
                };

                $scope.showIconSelect = function (event) {
                    $mdDialog.show({
                        controller: DialogController,
                        templateUrl: 'templates/iconselect.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true
                    })
                        .then(function (answer) {
                            $scope.workflowDefinition.icon = answer.filename;
                            $scope.iconName = answer.name;
                            $scope.processForm.icon.$setDirty();
                        });
                };

                function DialogController($scope, $mdDialog, $http) {

                    $http.get('img/avatars/avatars.json').then(function (response) {
                        $scope.avatars = response.data;
                    });

                    $scope.hide = function () {
                        $mdDialog.hide();
                    };
                    $scope.cancel = function () {
                        $mdDialog.cancel();
                    };
                    $scope.answer = function (answer) {
                        $mdDialog.hide(answer);
                    };
                }

                /**
                 * Returns the label for the change status button
                 * @return {string}
                 */
                $scope.newStatusButtonLabel = function () {
                    var version = $scope.findActiveVersion() || {};

                    switch (version.status) {
                        case 'new':
                        case 'inactive':
                            return 'activate';
                        case 'active':
                            return 'deActivate';
                    }
                };

                /**
                 * Changes the status of the selected version
                 * (new,inactive -> active  or active -> inactive)
                 */
                $scope.changeStatus = function (event) {
                	
                	//need save first in order to select active version
                	if($scope.saveFirst == true){
                		var confirm = $mdDialog.alert()
	                        .title($filter('translate')('saveFirst'))
	                        .content($filter('translate')('saveFirst'))
	                        .ariaLabel($filter('translate')('saveFirst'))
	                        .targetEvent(event)
	                        .ok($filter('translate')('close'));
                		
                		 $mdDialog.show(confirm).then(
                         // agree
                         function () {
                        	 $mdDialog.hide();
                         });
                		 
                	}else {
                		var confirm = $mdDialog.confirm()
                			.title($filter('translate')('setActiveVersion'))
	                        .content($filter('translate')('setActiveVersion'))
	                        .ariaLabel($filter('translate')('setActiveVersion'))
	                        .targetEvent(event)
	                        .ok($filter('translate')('confirm'))
	                        .cancel($filter('translate')('cancel'));

                    $mdDialog.show(confirm).then(
                        // agree
                        function () {
                            var version = $scope.findActiveVersion();
                            $scope.setVersionStatus(version, version.status === 'active' ? 'inactive' : 'active');
                        });
                	}

                };
                
                
                
                /**
                 * Put here function that returns a workflow version's tasks
                 */
                $scope.clickedOnTaskDetailsTab = function(){
                	 var versions;
                	 var selectedVersionDeploymentId = $scope.workflowDefinition.activeDeploymentId;
                	 var selectedVersion;
                	 
                	 if(selectedVersionDeploymentId != null){
                		 versions = $scope.workflowDefinition.processVersions;
                		 versions.forEach(function(entry){
                			 if(entry.deploymentId == selectedVersionDeploymentId)	selectedVersion = entry;
                		 });
                		 
                         processService.getVersionTaskDetails(selectedVersion.id)
                         .then(
	                         function (response) {
	                             $scope.taskDetails = response.data;
	                         },
	                 		function (response) {
	                 			alertGenericResponseError(response);
	                 		}
                         );
                	 }
                 }
                
            
                /**
                 * Show modal window with the task description
                 */
                $scope.editTaskDescription = function (id, event) {
                    $scope.taskDetails.forEach(function(entry){
                    	if(entry.id == id){
                    		$scope.selectedTask = entry;
                    	}
                    });
                    $mdDialog.show({
                        controller: function ($scope, $mdDialog, $http, task){
                        	$scope.editValue = task;
                        	
                        	
                            $scope.save = function () {
                                processService.updateTaskDetails($scope.editValue).then(
                                function () {
                                	$mdDialog.hide();
                                	
                                },function(response){
                                	$scope.exception = true;
                                	$scope.exceptionMessage = response.data;
                                	
                                });
                            };
                            
                            $scope.cancel = function () {
                                $mdDialog.cancel();
                            };
                            
                        },
                        templateUrl: 'templates/updatetaskdetails.tmpl.html',
                        parent: document.body,
                        locals:{
                        	'task': $scope.selectedTask,
                        	'exception': $scope.exception,
                        	'exceptionMessage': $scope.exceptionMessage,
                        },
                        clickOutsideToClose: true
                    })

                };
                
                
                function UpdateTaskController($scope, $mdDialog, $http, task) {
                	$scope.editValue = task;
                	
               
                }
                

                /**
                 * Cancel an instance
                 */ 
                $scope.cancelInstance = function(instance, event){
                    $mdDialog.show({
                        controller: CancelInstanceConfirmController,
                        templateUrl: 'templates/cancelinstancemodal.tmpl.html',
                        parent: document.body,
                        locals:{
                        	'instance': instance,
                        	'instances': $scope.instances
                        },
                        clickOutsideToClose: true
                    })
                }

                function CancelInstanceConfirmController($scope, $mdDialog, $http, instance, instances) {
                	$scope.instance = instance;
                	$scope.instances = instances;
                	
                    $scope.confirm= function () {
                        processService.cancelInstance($scope.instance.id)
                        .then(
	                        function () {
	                        	$mdDialog.hide();
	                        	var i = $scope.instances.indexOf($scope.instance);
	                        	if(i!=-1)	$scope.instances.splice(i,1);
	                        },
	                		function (response) {
	                			alertGenericResponseError(response);
	                		}                        
                        );
                    };
                    $scope.cancel = function () {
                        $mdDialog.cancel();
                   };    
                }
 
                
                /**
                 * Act on instance (suspend/resume)
                 */ 
                $scope.actOnInstance = function(instance, action, event){
                    $mdDialog.show({
                        controller: ActOnInstanceConfirmController,
                        templateUrl: 'templates/actoninstancemodal.tmpl.html',
                        parent: document.body,
                        locals:{
                        	'instance': instance,
                        	'action' : action
                        },
                        clickOutsideToClose: true
                    })
                }

                function ActOnInstanceConfirmController($scope, $mdDialog, $http, instance, action) {
                	$scope.instance = instance;
                	$scope.action = action;
                	
                    $scope.confirm= function () {
                        processService.actOnInstance($scope.instance.id, action)
                        .then(
	                        function (response) {
	                        	$scope.instance.status = response.data.status;               
	                        	$mdDialog.hide();
	                        },
	                		function (response) {
	                			alertGenericResponseError(response);
	                		}
                        );
                    };
                    $scope.cancel = function () {
                        $mdDialog.cancel();
                   };    
                }
                       
                
                
                /**
                 * Put here function that returns workflow instances
                 */
                $scope.clickedInProgressTab = function(){
                	
                   processService.getWorkflowInstances($scope.workflowDefinition.id)
                     .then(
                      function (response) {
                         $scope.instances = response.data;
                         if($scope.instances == null || $scope.instances.length==0)	return;
//                  		 $scope.instances.forEach(function(entry){
//                			 entry.startDate = new Date(entry.startDate).toString("dd MM YY");
//                		 });
                      }
                    );
                 }
                

                /**
                 * Sets the status to the definition version
                 * @param {DefinitionVersion} version
                 * @param {string} status
                 */
                $scope.setVersionStatus = function (version, status) {

                    if (status === 'active') {
                        processService.setActiveVersion($scope.workflowDefinition.id, version.id)
                            .then(
                            // success callback
                            function (response) {
                                $scope.workflowDefinition = response.data;
                                $scope.resetVersionSelect()
                            },
                            // fail callback
                            function (response) {
                                alertStatusChangeFailed(response);
                            }
                        );
                    }
                    else {
                        processService.deactivateVersion($scope.workflowDefinition.id, version.id)
                            .then(
                            // success callback
                            function (response) {
                                version.status = response.data.status;
                                $scope.resetVersionSelect()
                            },
                            // fail callback
                            function (response) {
                                alertStatusChangeFailed(response);
                            }
                        );
                    }
                };
                
                
                
                /**
                 * External Forms Tab
                 */
                $scope.clickedExtFormsTab = function(){
                	
                	$scope.supervisors;
                	$scope.xforms = null;
                    $scope.registries;
                    
                    $scope.xform;
                	
                	processService.getExternalForms($scope.workflowDefinition.id)
                    .then(
                    		// success callback
                    		function (response) {
                    			$scope.xforms = response.data;
                    		},
                    		// fail callback
                    		function (response) {
                    			alertGenericResponseError(response);
                    		}
                    );       
                	               	
                    processService.getUsers()
                    .then(
                    		// success callback
                    		function (response) {
                    			$scope.supervisors = response.data;
                    		},
                    		// fail callback
                    		function (response) {
                    			alertGenericResponseError(response);
                    		}
                    );
                    
                    // delete external form confirmation
                    $scope.askDeleteXForm = function(xform){
                    	
                        var confirm = $mdDialog.confirm()
                        .title($filter('translate')('deleteExternalForm'))
                        .content($filter('translate')('deleteExternalFormConf') + " " + xform.formId + " ?")
                        .ariaLabel($filter('translate')('deleteExternalForm'))
                        .ok($filter('translate')('confirm'))
                        .cancel($filter('translate')('cancel'));
                        
                    
                        $mdDialog.show(confirm).then(function () {
                            processService.deleteExternalForm(xform.formId)
                                .then(
                                // success callback
                                		function (response) {
                                        	var x = $scope.xforms.indexOf(xform);
                                        	if(x!=-1)	$scope.xforms.splice(x,1);
                                		}                                );
                        });
                    
                    };                    
 
                    
                    // suspend external form confirmation
                    $scope.askSuspendXForm = function(xform){
                    	
                        var confirm = $mdDialog.confirm()
                        .title($filter('translate')('suspendExternalForm'))
                        .content($filter('translate')('suspendExternalFormConf') + " " + xform.formId + " ?")
                        .ariaLabel($filter('translate')('suspendExternalForm'))
                        .ok($filter('translate')('confirm'))
                        .cancel($filter('translate')('cancel'));
                        
                    
                        $mdDialog.show(confirm).then(function () {
                        	var action = 'suspend';
                        	processService.actOnExternalForm(xform.formId,action)
                        	.then(
                            		function (response) {
                                    	xform.enabled = response.data.enabled;
                            		},
                            		function (response) {
                            			alertGenericResponseError(response);
                            		}
                        	);
                        });
                    
                    };   
                    
                    
                    // resume external form confirmation
                    $scope.askResumeXForm = function(xform){
                    	
                        var confirm = $mdDialog.confirm()
                        .title($filter('translate')('resumeExternalForm'))
                        .content($filter('translate')('resumeExternalFormConf') + " " + xform.formId + " ?")
                        .ariaLabel($filter('translate')('resumeExternalForm'))
                        .ok($filter('translate')('confirm'))
                        .cancel($filter('translate')('cancel'));
                        
                    
                        $mdDialog.show(confirm).then(function () {
                        	var action = 'resume';
                        	processService.actOnExternalForm(xform.formId,action)
                        	.then(
                            		function (response) {
                                    	xform.enabled = response.data.enabled;
                            		},          
                            		function (response) {
                            			alertGenericResponseError(response);
                            		}
                            );
                        });
                    
                    };   
                
                };
                
                
                
                /**
                 * Add External form dialog
                 */
                $scope.addXForm = function(){                	
                	
                    $mdDialog.show({
                        controller: AddXFormController,
                        templateUrl: 'templates/addxform.tmpl.html',
                        parent: document.body,
                        locals:{
                        	'workflow': $scope.workflowDefinition,
                        	'supervisors': $scope.supervisors,
                        	'xforms':$scope.xforms
                        },
                        clickOutsideToClose: true
                    })
                };
                
                /**
                 * Edit External form dialog
                 */
                $scope.editXForm = function(xform, event){ 
                	$scope.xform = xform;
                	
                  	$mdDialog.show({
                		controller: function ($scope, $mdDialog) {
                			
                            $scope.cancel = function () {
                            	$mdDialog.hide();
                            };
                            
                            $scope.deleteXForm = function (){
                            	processService.deleteExternalForm($scope.xform.formId).then(
                                        // success callback
                                        function () {
                                           	processService.getExternalForms($scope.workflowDefinition.id).then(
                                            		// success callback
                                            		function (response) {
                                            			$scope.xforms = response.data;
                                            			$mdDialog.hide();
                                            		},
                                            		// fail callback
                                            		function (response) {
                                            			alertGenericResponseError(response);
                                            		}
                                            ); 
                                        },
                                        // error callback
                                        function (response) {
                                        	alertGenericResponseError(response);
                                        });
                            }
                            
                            $scope.update = function (){
                                processService.updateExternalForm($scope.xform).then(
                                		function () {
                                			$mdDialog.hide();
                                			},                        
                        		function (response) {
                        			alertGenericResponseError(response);
                        		});
                            };
                            
                        },
                        
                        scope: $scope,
                        preserveScope: true,
                        templateUrl: 'templates/editxform.tmpl.html',
                        parent: angular.element(document.body),
                        targetEvent: event,
                        locals: {
                        	'xform': $scope.xform,
                        	'supervisors': $scope.supervisors,
                        }
                	})
                };
                
                
                
                function AddXFormController($scope, $mdDialog, $http, workflow, supervisors, xforms) {
                	
                	$scope.xforms=xforms;
                	
                	$scope.xform={};
                	$scope.xform.workflowDefinitionId = workflow.id;
                	$scope.xform.enabled=true;
                	
                	$scope.supervisors = supervisors;                	
                	
                    $scope.save = function () {
                        processService.saveExternalForm($scope.xform)
                        .then(
                        function () {
                        	$scope.xforms.push($scope.xform);
                        	$mdDialog.hide();
                        },                        
                		function (response) {
                			alertGenericResponseError(response);
                		}
                		
                        );
                    };
                    $scope.cancel = function () {
                        $mdDialog.cancel();
                    };
                }
                
                
                
                /**
                 * Helper function for showing error alert
                 * @param response
                 */
                function alertStatusChangeFailed(response) {
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
                }

                
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
                

                // workaround to update the md-select after status change
                $scope.recreateSelect = true;
                $scope.resetVersionSelect = function () {
                    $scope.recreateSelect = false;
                    $timeout(function () {
                        $scope.recreateSelect = true;
                    }, 0);
                }
                
                
                /**
                 * Tab change listener
                 */
                $scope.onTabSelected = function (tab){
                	$scope.taskMetadataActiveView = "taskMetadata";
                	$scope.selectedTab = tab;
                };
                
                /**
                 * Change task metadata tab to show task form items/details
                 */
                $scope.goToTaskFormDetails = function (taskDetails){
                	
                	$scope.editTaskDetails = taskDetails;
                	$scope.taskMetadataActiveView = "taskFormItems";
                	
                	$scope.taskFormItems = [];
                	
                	processService.getTaskFormProperties(taskDetails.taskId, $scope.workflowDefinition.processDefinitionId).then(
                			//success callback
                			function (response){
                				$scope.taskFormItems = response.data;
                			}
                	);
                	
                	
                };
                
                $scope.saveTaskDetails = function (taskDetails){
                    processService.updateTaskDetails(taskDetails).then(
                            function () {
                            	$mdDialog.hide();
                            	
                            },function(response){
                            	$scope.exception = true;
                            	$scope.exceptionMessage = response.data;
                            	
                            });
                	
                };
                
                /**
                 * Edit External form dialog
                 */
                $scope.editFormItem = function(formItem,event){ 
                	
                	$scope.formItem = formItem;

                	$mdDialog.show({
                		controller: function ($scope, $mdDialog) {
                			
                            $scope.cancel = function () {
                            	$mdDialog.hide();
                            };
                            
                            $scope.save = function (){
                            	
                            	processService.saveTaskFormElement($scope.formItem, $scope.editTaskDetails.taskId, $scope.workflowDefinition.processDefinitionId).then(
                            			
                            			//success callback
                            			function (response){
                            				$mdDialog.hide();
                            				
                            			//error callback	
                            			}, function (response){}
                            	);
                            };
                            
                            $scope.clear = function () {
                            	$scope.formItem.description = "";
                            };
                            
                        },
                        
                        scope: $scope,
                        preserveScope: true,
                        templateUrl: 'templates/editFormItem.tmpl.html',
                        parent: angular.element(document.body),
                        targetEvent: event,
                        locals: {
                        	'formItem': $scope.formItem,
                        }
                	})
                };

                
                /**
                 * Return to task list
                 */
                $scope.goBackToTaks = function (){
                	
                	 $scope.taskMetadataActiveView = "taskMetadata";
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

            }]
    );

})(angular);
