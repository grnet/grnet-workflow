(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('TaskCompletedDetailsCtrl', ['$scope', '$filter', '$window', '$http', '$routeParams', '$location', '$mdDialog', 'processService', 'CONFIG',

            /**
             * Controller for the task-completed-details view
             *
             * @param $scope
             * @param $http
             * @param $routeParams
             * @param $location
             * @param $mdDialog
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $filter, $window, $http, $routeParams, $location, $mdDialog, processService, config) {
        		
        		$scope.imagePath = config.AVATARS_PATH;
                var taskId = $routeParams['taskId'];
                $scope.task = null;
                
                $scope.startDate = null;
                $scope.dueDate = null;
                $scope.endDate = null;
                
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
                
                /**
                 * Redirects to previous page
                 */
                $scope.goBack = function (){
                	$window.history.back();
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
                    },
                    // error callback
                    function (response) {
                    }
                );
                
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
