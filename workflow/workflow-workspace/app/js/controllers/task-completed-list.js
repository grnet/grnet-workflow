/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('CompletedTasksCtrl', ['$scope', '$http', '$location', '$mdDialog', 'processService', 'CONFIG',

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
                function ($scope, $http, $location, $mdDialog, processService, config) {

	        	$scope.imagePath = config.AVATARS_PATH;
	        	$scope.fetchingTasks = { state: false };
	        	
	            $scope.instances = [];
	            $scope.tasks = null;
	            $scope.definitions = null;
	            $scope.searchFilter = { dateAfter: null, dateBefore: null, instanceTitle: "", definitionId: null};
	            $scope.orderByOption = null;
                
                $scope.options = [];
                
                $scope.sortOptions = {title: 'dueTo', id: 'dueDate'};
				$scope.options.push($scope.sortOptions);
				$scope.sortOptions = {title: 'taskName', id: 'name'};
				$scope.options.push($scope.sortOptions);
				$scope.sortOptions = {title: 'worker', id: 'assignee'};
				$scope.options.push($scope.sortOptions);
				$scope.sortOptions = {title: 'execution', id: 'processInstance.title'};
				$scope.options.push($scope.sortOptions);
				
                processService.getActiveProcessDefinitions().then(
                        // success callback
                        function (response) {
                        	$scope.definitions = response.data;
                        	
                        	$scope.selectAllDefinitions = {name:"showAll", processDefinitionId: "all"};
                        	$scope.definitions.push($scope.selectAllDefinitions);
                        	
                        	$scope.initializeCriteria();
                        	$scope.showTasksByFilters();
                        },
                        // error callback
                        function (response) {
                        	exceptionModal(response);
                        });
     
            	/**
				 * Return processes definitions by selected definitions
				 */
				$scope.showTasksByFilters = function (){
					var dateAfterTime = new Date().getTime();
					var dateBeforeTime = new Date().getTime();
					var instanceIds = [];
			        var historyTasks = [];
			        var tasksMapped = {};
					
					if($scope.searchFilter.dateAfter != null)
						dateAfterTime = $scope.searchFilter.dateAfter.getTime();
					
					if($scope.searchFilter.dateBefore != null)
						dateBeforeTime = $scope.searchFilter.dateBefore.getTime();
						
					processService.getSearchedUserTasks($scope.searchFilter.definitionId, $scope.searchFilter.instanceTitle, dateAfterTime, dateBeforeTime, "true").then(
							function(response){
								$scope.tasks = response.data;
								
                            	tasksMapped = ArrayUtil.mapByProperty2innerProperty($scope.tasks, "processInstance", "id","tasks");
                            	instanceIds = Object.keys(tasksMapped);
                            	
                                instanceIds.forEach(function(item){
                                	var task = tasksMapped[item]["tasks"][0];
                                	$scope.instances.push(task.processInstance);
                                });
							},
							//error callback
							function(response){
								exceptionModal(response);
							});
					};
				
		        $scope.initializeCriteria = function(){
                	$scope.searchFilter.dateBefore = new Date();
                	$scope.searchFilter.dateAfter = new Date();
                	$scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth()-3);
                	$scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate()+1);
                	$scope.searchFilter.definitionId = "all";
                	$scope.searchFilter.instanceTitle = "";
                };
                
                $scope.clearAfterDate = function(){
                	$scope.searchFilter.dateAfter = new Date();
                	$scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth()-3);
                };
                
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
				
				/**
				 * Exception modal
				 */
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
