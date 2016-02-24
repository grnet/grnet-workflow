/**
 * @author nlyk
 */
(function () {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('HistoryCtrl', ['$scope', '$http', '$location', '$mdDialog', 
                                    'processService', 'CONFIG', 'auth',

            function ($scope, $http, $location, $mdDialog, processService, config, auth) {
                //$scope.historyTasks = null;
                $scope.endedInstances = [];
                $scope.instance;                                
                $scope.noCache = true;
                $scope.tasks = [];
                
                $scope.after = new Date();
                $scope.after.setMonth($scope.after.getMonth()-3);               
                $scope.before = new Date();
                
                $scope.imagePath = config.AVATARS_PATH;

                var instanceIds = [];
                var historyTasks = [];
                var tasksMapped = {};
                
                $scope.getMatches = function(searchText){                	

                	var title;                	
                	$scope.tasks = [];
                	
                	title = (searchText==null || searchText.length==0) ? ' ' : searchText;
                	
                	processService.getEndedInstancesTasks(title, Date.parse($scope.after), Date.parse($scope.before), true).then(                			
                            function (response) {
                            	$scope.endedInstances = [];
                                historyTasks = response.data;                                  
                                
                            	tasksMapped = 
                            		ArrayUtil.mapByProperty2innerProperty(historyTasks, "processInstance", "id","tasks");
                            	instanceIds = Object.keys(tasksMapped);
                            	
                                instanceIds.forEach(function(item){
                                	var task = tasksMapped[item]["tasks"][0];
                                	$scope.endedInstances.push(task.processInstance);
                                });
                                
                               
                            }                                                       
                    );
                	
                	return $scope.endedInstances;
        		};
        		
                
        		$scope.itemSelected = function(){
        			if($scope.instance==null)	return;
                	console.log($scope.instance.title);
                };
                
                
                $scope.clearCriteria = function(){
                	$scope.before = new Date();
                    $scope.after = new Date();
                    $scope.after.setMonth($scope.after.getMonth()-3);           
                	$scope.getMatches("");
                }
                
                
                $scope.selectInstance = function(selectedInstance){
                	$scope.tasks = tasksMapped[selectedInstance.id]["tasks"];
                }
                
                // to initialize results
                // displays all instances
                $scope.getMatches("");
                
            }]
    );

})(angular);
