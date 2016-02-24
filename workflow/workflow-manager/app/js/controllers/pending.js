/**
 * @author nlyk
 */
(function () {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('PendingCtrl', ['$scope', '$http', '$location', '$mdDialog', 'processService', 'CONFIG', 'auth',

            function ($scope, $http, $location, $mdDialog, processService, config, auth) {
        	
        		$scope.imagePath = config.AVATARS_PATH;
                $scope.allTasks = null;
        		$scope.tasksMappedByDefinition = null;
                $scope.filteredTasks = null;
                $scope.workflowNames = [];
                
                var pairs = {};
                var names = [];
                
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
                 * Get all process definitions
                 */
                processService.getActiveTasks().then(
                    // success callback
                    function (response) {
                        // set default icon
                        $scope.allTasks = response.data;                          
                        
                        $scope.tasksMappedById = ArrayUtil.mapByProperty2Property($scope.allTasks, "processDefinitionId", "tasks");
                        $scope.taskIds = Object.keys($scope.tasksMappedById);
                        $scope.filteredTasks = response.data;
                        $scope.workflowNames = null;
                        
                        $scope.taskIds.forEach(function(item){
                        	var task = $scope.tasksMappedById[item]["tasks"][0];
                        	pairs[task.definitionName] = item;
                        	names.push(task.definitionName);
                        });
                       
                        $scope.workflowNames = names;
                    }
                );
                
                $scope.selectAll = function(){
                	$scope.filteredTasks = $scope.allTasks;
                };
                
                $scope.selectionChanged = function(name){
                	$scope.filteredTasks = null;
                	var id = pairs[name];
                	$scope.filteredTasks = $scope.tasksMappedById[id]["tasks"];
                };
                

            }]
    );

})(angular);
