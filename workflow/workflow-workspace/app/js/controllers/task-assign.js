/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('TaskAssignCtrl', ['$scope', '$filter', '$http', '$routeParams', '$location', '$mdDialog', 'processService', 'CONFIG',

            /**
             * Controller for the tasks-to-assign view
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
                $scope.task = null;
                
                $scope.startDate = null;
                $scope.dueDate = null;
                
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
                        
                        if($scope.task.assignee != null)
                        	
                        	$scope.isAssigned = true;
                        else
                        	$scope.isAssigned = false;
                        
                        
                        if($scope.task.dueDate != null){
                        	$scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy");
                        }
                        
                        $scope.startDate = $filter('date')($scope.task.startDate, "d/M/yyyy");
                        
                        $scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");
                        	
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
                
                /**
                 * Opens assignee modal
                 */
                $scope.selectAssigneeModal = function (event) {
                	$mdDialog.show({
                		controller: function ($mdDialog) {
                			$scope.candidates = null;
                			
                            $scope.cancel = function () {
                            	$mdDialog.hide();
                            };
                            
                            $scope.confirm = function(){
                            	$scope.task.assignee = $scope.assignee;
                            	$mdDialog.hide();
                            };
                            
                            $scope.getAllCandidates = function (){
                                processService.getAllCandidates()
                                    .then(
                                    // success callback
                                    function (response) {
                                    	$scope.candidates = response.data;
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
                            
                            processService.getCandidatesForTask(taskId).then(
                                // success callback
                                function (response) {
                                	$scope.candidates = response.data;
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
                	
                	if($scope.assignee != null)
                		$scope.isAssigned = true;
                	else
                		$scope.isAssigned = false;
                	
                };
               
                /**
                 * Confirm action
                 */
                
                $scope.confirmAction = function (){
                	if($scope.assignee != null){
                		processService.setAssigneeToTask($scope.task, $scope.assignee).then(
                				function (response) {
                					$location.path('/assign');
            					},
            					function (response) {});
                	}else{
                		$location.path('/assign');
                	}
                	
                };
                
                /**
                 * Unclaim a task
                 */
                
                $scope.unclaimTask = function(){
                	processService.unclaimTask(taskId).then(
                			function (response){
                				$scope.task.assignee = null;
                			},
                			
                			function(response){
                				$mdDialog.show($mdDialog.alert()
             		                   .parent(document.body)
             		                   .clickOutsideToClose(true)
             		                   .title('Error')
             		                   .content(response.data)
             		                   .ok('Ok'))
                			}
                	);
                	
                	$scope.isAssigned = false;
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
                
            }]
    );

})(angular);
