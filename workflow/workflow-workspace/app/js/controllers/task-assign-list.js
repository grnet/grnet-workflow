/**
 * @author nlyk
 */
(function (angular) {
	
	'use strict';

    angular.module('wfworkspaceControllers')
        .controller('TaskAssignListCtrl', ['$scope', '$http', '$location', '$mdDialog', '$filter', 'processService', 'CONFIG',

            /**
             * Controller for the tasks-to-assign view
             *
             * @param $scope
             * @param $http
             * @param $location
             * @param $mdDialog
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $http, $location, $mdDialog, $filter, processService, config) {

                $scope.imagePath = config.AVATARS_PATH;
                $scope.status = { selectAll: true };
                
                $scope.tasks = null;
                $scope.assignedTasks = null;
                $scope.unAssignedTasks = null;
                $scope.assignedFiltered = null;
                $scope.unAssignedFiltered = null;
                $scope.processes = null;
                
                $scope.sortOptions = { title: null, id: null };
                
                $scope.orderByOption = null;
                
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
                 * Get all supervised tasks
                 */
                processService.getSupervisedTasks().then(
                		//success
                		function (response){
                			$scope.tasks = response.data;
                			
                			if(response.data.length == 0){
                            	$mdDialog.show($mdDialog.alert()
              		                   .parent(document.body)
              		                   .title($filter('translate')('noAvailableTasks'))
              		                   .content($filter('translate')('noAvailableTasks'))
              		                   .ok($filter('translate')('confirm')))
                             	$location.path('/task');
                			}
                			
                			//separate assigned tasks from list
                			$scope.assignedTasks = $scope.tasks.filter(function(element){return element.assignee});
                			
                			//separate unassigned tasks from list
                			$scope.unAssignedTasks = $scope.tasks.filter(function(element){return !element.assignee});
                			
                			$scope.processes = {};
                			$scope.tasks.forEach( function( o ) {
                				var group = (o.processId || "_empty_").toString();
                				if ( !$scope.processes.hasOwnProperty(group) ) {
                					$scope.processes[group] = $scope.processes[group] || {
                							id: o.processId,
                							title: o.definitionName,
                							selected: true
                					}
                				}
                			});
                			
                			//filter list with selected definitions
                			//in this case all available definitions are selected
                			$scope.updateFilteredTasks();
                		},
                		//fail
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
                		}
                );
                
                /**
                 * Select/De-select all
                 */
                $scope.selectAll = function () {
                	
                	if ($scope.status.selectAll === true) {
                		$scope.assignedFiltered = $scope.assignedTasks;
                		$scope.unAssignedFiltered = $scope.unAssignedTasks;
                		
                		//Select all
                		for(var key in $scope.processes) {
                			if ($scope.processes.hasOwnProperty(key)){
                				$scope.processes[key].selected = true;
                			}
                		}
                	}
                	else {
                		$scope.assignedFiltered = [];
                		$scope.unAssignedFiltered = [];
                		
                		//De-select all
                		for(var key in $scope.processes) {
                			if ($scope.processes.hasOwnProperty(key)){
                				$scope.processes[key].selected = false;
                			}
                		}
                	}
                };
                
                /**
                 * Filter assigned/unassigned lists by selected definitions
                 */
				$scope.updateFilteredTasks = function () {
					
					var selectedDefinitions = Object.keys($scope.processes)
												.filter(function (e) {return $scope.processes[e].selected === true;});
					
					$scope.assignedFiltered = $scope.assignedTasks.filter(function (e) {
						return selectedDefinitions.indexOf(e.processId + "") >= 0 ;});
					
					$scope.unAssignedFiltered = $scope.unAssignedTasks.filter(function (e) {
						return selectedDefinitions.indexOf(e.processId + "") >= 0 ; });
				};
				
				/**
				 * Tab change event
				 */
				$scope.onTabSelected = function (tab){
					$scope.options = [];
					
					if(tab == 'new'){
						$scope.sortOptions = {title: 'dueTo', id: 'dueDate'};
						$scope.options.push($scope.sortOptions);
						
						$scope.sortOptions = {title: 'taskName', id: 'name'};
						$scope.options.push($scope.sortOptions);
						
					}else if(tab == 'assigned'){
						$scope.sortOptions = {title: 'dueTo', id: 'dueDate'};
						$scope.options.push($scope.sortOptions);
						
						$scope.sortOptions = {title: 'taskName', id: 'name'};
						$scope.options.push($scope.sortOptions);
						
						$scope.sortOptions = {title: 'worker', id: 'assignee'};
						$scope.options.push($scope.sortOptions);
					}
					
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
