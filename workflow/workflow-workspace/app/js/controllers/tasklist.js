/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')

        .controller('TaskListCtrl', ['$scope', '$location', '$mdDialog', 'processService', 'CONFIG',

            /**
             * Controller for the Process Details view
             * @param {object} $scope
             * @param {$location} $location
             * @param {$mdDialog} $mdDialog
             * @param {ProcessService} processService
             * @param config
             */
            function ($scope, $location, $mdDialog, processService, config) {

    		$scope.claimTasks = [];
            $scope.tasks = [];
            $scope.assignedTasks = [];
            $scope.unAssignedTasks = {};
            $scope.taskMapByProcess = [];
            
            $scope.imagePath = config.AVATARS_PATH;

            $scope.taskDelay = function (dueDate) {
            	if (dueDate === null)
            		return Infinity;
            	
            	var currentDate = new Date();
            	var diff = dueDate - currentDate.getTime();
    			var diffInDays = diff / (1000 * 3600 * 24);
    			
    			return diffInDays;
            }
           
            processService.getTasksInProgress().then(
                // success callback
                function (response) {
                	$scope.tasks = response.data;
                	$scope.assignedTasks = response.data;
                	
                	$scope.taskMapByProcess = ArrayUtil.mapByProperty2Property($scope.tasks, "definitionName", "assigned");
                },
                // error callback
                function (response) {
                	exceptionModal(response);
                });
            
            //get tasks to be claimed by user
            
            processService.getClaimTasks().then(
                    // success callback
                    function (response) {
                    	$scope.claimTasks = response.data;
                    	$scope.unAssignedTasks = response.data;
                    	
                    	$scope.taskMapByProcess = ArrayUtil.extendMapByProperty($scope.claimTasks, $scope.taskMapByProcess, "definitionName", "unassigned"); 
                    	},
                        // error callback
                        function (response) {
                    		exceptionModal(response);
                        });
            
            /**
             * Handles the selected item from the item list
             */
            $scope.selectionChanged = function (definitionName) {
            	$scope.assignedTasks = $scope.taskMapByProcess[definitionName]["assigned"];
            	$scope.unAssignedTasks = $scope.taskMapByProcess[definitionName]["unassigned"];
            };
            
            /**
             * Shows all the tasks
             */
            $scope.selectAllTasks = function (){
            	$scope.assignedTasks = $scope.tasks; 
            	$scope.unAssignedTasks = $scope.claimTasks;                	
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
