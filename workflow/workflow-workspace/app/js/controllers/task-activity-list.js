(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('TaskActivityListCtrl', ['$scope', '$location', '$mdDialog', 'processService', 'CONFIG',

                                             
                function ($scope, $location, $mdDialog, processService, config) {
        		
        		$scope.imagePath = config.AVATARS_PATH;
            	$scope.fetchingTasks = { state: false };
                $scope.instances = [];
                $scope.tasks = null;
                $scope.definitions = null;
                $scope.instanceItems = null;
                $scope.searchFilter = { dateAfter: null, dateBefore: null, instanceTitle: "", definitionId: null};
                
                $scope.orderByOption = null;
                
                $scope.options = [];
                $scope.sortOptions = {title: 'dueTo', id: 'dueDate'};
				$scope.options.push($scope.sortOptions);
				$scope.sortOptions = {title: 'taskName', id: 'name'};
				$scope.options.push($scope.sortOptions);
				$scope.sortOptions = {title: 'execution', id: 'instance.title'};
				$scope.options.push($scope.sortOptions);

				/**
                 * Get completed definitions for user
                 */
                processService.getProcesses().then(
                // success callback
                function (response) {
                	$scope.definitions = response.data;
                	
                	$scope.selectAllDefinitions = {name:"showAll", processDefinitionId: "null"};
                	$scope.definitions.push($scope.selectAllDefinitions);
                	
                	$scope.initializeCriteria();
                	$scope.showTasksByFilters();
                },
                // error callback
                function (response) {
                	
                });
                
                /**
                 * Returns the difference between due date and end date of task
                 */
                $scope.taskEndedDelay = function (endDate, dueDate) {
                	if (endDate === null)
                		return Infinity;
                	
                	var diff = endDate - dueDate.getTime();
        			var diffInDays = diff / (1000 * 3600 * 24);
        			
        			return diffInDays;
                };
                
                	
				/**
				 * Return processes definitions by selected definitions
				 */
				$scope.showTasksByFilters = function (){
					var instanceIds = [];
		            var historyTasks = [];
		            var tasksMapped = {};
					var dateAfterTime = new Date().getTime();
					var dateBeforeTime = new Date().getTime();
					
					if($scope.searchFilter.dateAfter != null)
						dateAfterTime = $scope.searchFilter.dateAfter.getTime();
					
					if($scope.searchFilter.dateBefore != null)
						dateBeforeTime = $scope.searchFilter.dateBefore.getTime();
						
					processService.getSearchedUserTasks($scope.searchFilter.definitionId, $scope.searchFilter.instanceTitle, dateAfterTime, dateBeforeTime, "false").then(
							function(response){
								$scope.tasks = response.data;
								
                            	tasksMapped = ArrayUtil.mapByProperty2innerProperty($scope.tasks, "processInstance", "id","tasks");
                            	instanceIds = Object.keys(tasksMapped);
                            	
                                instanceIds.forEach(function(item){
                                	var task = tasksMapped[item]["tasks"][0];
                                	$scope.instances.push(task.processInstance);
                                });
							},
							
							function(response){
								
							});
					};
	                
					/**
					 * Initialize criteria
					 */
	                $scope.initializeCriteria = function(){
	                	$scope.searchFilter.dateBefore = new Date();
	                	$scope.searchFilter.dateAfter = new Date();
	                	$scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth()-3);
	                	$scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate()+1);
	                	$scope.searchFilter.definitionId = "null";
	                	$scope.searchFilter.instanceTitle = "";
	                };
	                
	                /**
	                 * Clear datepicker for date after
	                 */
	                $scope.clearAfterDate = function(){
	                	$scope.searchFilter.dateAfter = new Date();
	                	$scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth()-3);
	                };
	                
	                /**
	                 * Clear datepicker for date before
	                 */
	                $scope.clearBeforeDate = function(){
	                	$scope.searchFilter.dateBefore = new Date();
	                	$scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate()+1);
	                };
	                
	        		/**
					 * Sorting function
					 */
					$scope.sortBy = function (optionId){
						$scope.orderByOption = optionId;
					};
	                
	            }]
    );

})(angular);
