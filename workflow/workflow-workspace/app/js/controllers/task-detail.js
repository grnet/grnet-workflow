(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('TaskDetailCtrl', ['$scope', '$filter', '$http', '$routeParams', '$location', '$mdDialog', 'processService', 'CONFIG',

            /**
             * Controller for the tasks-details view
             *
             * @param $scope
             * @param $http
             * @param $routeParams
             * @param $location
             * @param $mdDialog
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $filter, $http, $routeParams, $location, $mdDialog, processService, config) {
        		
        		$scope.imagePath = config.AVATARS_PATH;
        		var taskId = $routeParams['taskId'];
        		$scope.executionActiveView = "list";
        		
        		$scope.showProgress = false;
        		
        		$scope.historyTasks = [];
        		
        		 $scope.task = null;
                 $scope.historicTask = null;
                
                $scope.assignee = null;
                $scope.instance = {
                		title: "",
                		supervisor: null,
                		variableValues: null
                };
                
                $scope.startDate = null;
                $scope.dueDate = null;
                $scope.endDate = null;
                
                $scope.historicStartDate = null;
                $scope.historicDueDate = null;
                $scope.historicEndDate = null;
                
                
                $scope.approveAction = null;
                $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;
                
                /**
                 * Returns the difference between due date and current date
                 */
                $scope.taskDelay = function (dueDate) {
                	if (dueDate === null)
                		return Infinity;
                	
                	var currentDate = new Date();
                	var diff = dueDate - currentDate.getTime();
        			var diffInDays = diff / (1000 * 3600 * 24);
        			
        			return diffInDays;
                };
                
                // get the selected task
                processService.getTask(taskId).then(
                    // success callback
                    function (response) {
                        
                    	$scope.task = response.data;
                    	
                	  if($scope.task.dueDate != null){
                		  $scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy");
                      }
                	  
                	  if($scope.task.endDate != null){
                		  $scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");
                	  }
                      
                      $scope.startDate = $filter('date')($scope.task.startDate, "d/M/yyyy");
                        
                        var formProperties = response.data.taskForm;
                        
                        for (var i = 0; i < formProperties.length; i++) {
                        	
                        	var property = formProperties[i];
                        	
                        	if (property.type === 'approve') {
                        		
                        		$scope.approveAction = property;
                        		break;
                        	}
                        }
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
                    }
                );
                
                /**
                 * Opens assignee modal
                 */
                $scope.selectAssigneeModal = function (){
                	$mdDialog.show({
                		controller: function ($mdDialog, candidates ) {
                			$scope.candidates = candidates;
                			
                            $scope.cancel = function () {
                            	$mdDialog.hide();
                            };
                            
                            $scope.confirm = function(){
                            	$scope.task.assignee = $scope.assignee; 
                            	$mdDialog.hide();
                            };
                        },
                        scope: $scope,
                        preserveScope: true,
                        templateUrl: 'templates/assigneeSelection.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                        	'candidates': $scope.task.candidates
                        }
                	})
                };
                
                $scope.claimTask = function(){
                	
                    processService.claimTask($scope.task.id).then(
                        // success callback
                        function (response) {
                            $scope.task = response.data;
                            $location.path('/task');
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
                        }
                    );
                };
                
                /**
                 * Completes the task
                 */
                $scope.completeTask = function (){
                	
                	$scope.showProgress = true;
                	
                    processService.completeTask($scope.task).then(
                            // success callback
                            function (response) {
                                $scope.task = response.data;
                                $scope.showProgress = false;
                                $location.path('/task');
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
                            }
                        );
                };
                
                $scope.acceptTask = function(outcome) {
                	
                	$scope.approveAction.value = outcome;
                	
                    processService.completeTask($scope.task).then(
                        // success callback
                        function (response) {
                            $scope.task = response.data;
                            $location.path('/task');
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
                        }
                    );
                	
                };
                
                
                /**
                 * Open a modal to display task details
                 */
                $scope.showTaskDetails = function (){
                  	$mdDialog.show({
                		controller: function ($mdDialog) {
                			
                            $scope.cancel = function () {
                            	$mdDialog.hide();
                            };
                        },
                        scope: $scope,
                        preserveScope: true,
                        templateUrl: 'templates/taskDetails.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                        	'taskDetails': $scope.task.taskDetails
                        }
                	})
                };
                
                /**
                 * Tab change event
                 */
                $scope.onTabSelected = function (tab){
                	if(tab == "executionHistory"){
                		// get tasks by task instance
                		processService.getCompletedTasksByInstances($scope.task.processInstance.id).then(
                				function (response){
                					$scope.historyTasks = response.data;
                				},
                				//error callback
                				function (response){});
                	}else
                		$scope.executionActiveView = "list";
                };
                
                /**
                 * Get the details of selected historic task
                 */
                $scope.goToCompletedTask = function (taskId){
                	processService.getTask(taskId).then(
                			function (response){
                				$scope.historicTask = response.data;
                        		
                        		$scope.executionActiveView = "form";
                        		
                        		if($scope.historicTask.dueDate != null){
                        			$scope.historicDueDate = $filter('date')($scope.historicTask.dueDate, "d/M/yyyy");
                        		}
                	  
                        		if($scope.historicTask.endDate != null){
                        			$scope.historicEndDate = $filter('date')($scope.historicTask.endDate, "d/M/yyyy");
                        		}
                      
                        		$scope.historicStartDate = $filter('date')($scope.historicTask.startDate, "d/M/yyyy");
                			},
                			function (response){
                				
                			});
                };
                
                /**
                 * Temporary saves the task form data
                 */
                $scope.temporarySave = function (){
                	
                	processService.temporarySave($scope.task).then(
                			function (response){
                				
                			},
                			function (response){
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
                }
                
                /**
                 * Go back to historic tasks
                 */
                $scope.goBack = function (){
            		$scope.executionActiveView = "list";
                };
                
            }]
    );

})(angular);
