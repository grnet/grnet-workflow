(function (angular) {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('TaskDetailsCtrl', ['$scope', '$filter', '$window', '$http', '$routeParams', '$location', '$mdDialog', 'processService', 'CONFIG',
                                        
                function ($scope, $filter, $window, $http, $routeParams, $location, $mdDialog, processService, config) {

                var taskId = $routeParams['taskId'];
                $scope.task = null;
                
                $scope.startDate = null;
                $scope.dueDate = null;

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
                
             }]
    );

})(angular);
