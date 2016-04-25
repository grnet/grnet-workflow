(function (angular) {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('TaskDetailsCtrl', ['$scope', '$filter', '$window', '$http', '$routeParams', '$location', '$mdDialog', 'processService', 'CONFIG',
                                        
                function ($scope, $filter, $window, $http, $routeParams, $location, $mdDialog, processService, config) {

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
                        
                        if($scope.task.dueDate != null){
                          	$scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy");
                          }
                        
                        if($scope.task.endDate != null) {
                        	$scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");
                        }
                          
                          $scope.startDate = $filter('date')($scope.task.startDate, "d/M/yyyy");
                    },
                    // error callback
                    function (response) {
                    }
                );
                
                /**
                 * Redirects to previous page
                 */
                $scope.backTo = function() {
                	$window.history.back();
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
                
                $scope.showProgressDiagram = function (){
                  	$mdDialog.show({
                		controller: function ($mdDialog) {
                			
                			$scope.instance = $scope.task.processInstance;
                			$scope.service = config.WORKFLOW_SERVICE_ENTRY;
                			
                            $scope.cancel = function () {
                            	$mdDialog.hide();
                            };
                        },
                        scope: $scope,
                        preserveScope: true,
                        templateUrl: 'templates/progressDiagram.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                        	'service': $scope.service,
                        	'instance': $scope.instance,
                        }
                	})
                };
                
             }]
    );

})(angular);
