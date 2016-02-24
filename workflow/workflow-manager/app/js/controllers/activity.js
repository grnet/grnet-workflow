/**
 * @author nlyk
 */
(function () {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('ActivityCtrl', ['$scope', '$http', '$location', '$mdDialog', '$filter',
                                    'processService', 'CONFIG', 'auth',

            function ($scope, $http, $location, $mdDialog, $filter, processService, config, auth) {
        		
        		$scope.imagePath = config.AVATARS_PATH;
        		
                $scope.users = [];
                $scope.filteredUsers = [];
                $scope.userTasks = [];
                $scope.text = "";
                $scope.selectedUser;
                $scope.executionActiveView="list";
                
                $scope.after = new Date();
                $scope.after.setMonth($scope.after.getMonth()-3);               
                $scope.before = new Date();
                
                $scope.imagePath = config.AVATARS_PATH;
                $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;
                
                $scope.startDate = null;
                $scope.dueDate = null;
                $scope.endDate = null;
                
                $scope.task;
                
                var retrieveTasks;
                var user;
                
                processService.getUsers().then(
                    function (response) {
                        $scope.users = response.data;    
                    }
                );
                
                
                $scope.getMatches = function(searchText){    
                	$scope.text = searchText;
                	$scope.filteredUsers = $scope.users.filter($scope.applyFiltering)
                };
                

                $scope.applyFiltering = function(o){
                	return ( o.email.toLowerCase().search($scope.text) > -1 ||
                			o.lastName.toLowerCase().search($scope.text) > -1 ||
                			o.username.toLowerCase().search($scope.text) > -1);
                };
                
                
                $scope.userSelected = function(){
                	if($scope.selectedUser == null)		return;
                	user = $scope.selectedUser;
                	retrieveTasks();                	
                };
                
                
                retrieveTasks = function(){                	
                	processService.getUserActivity(Date.parse($scope.after), Date.parse($scope.before), $scope.selectedUser.id).then(                			
                            function (response) {
                            	$scope.userTasks = response.data;
                       
                            	$scope.userTasks.forEach(function(element){
                                    if(element.dueDate != null){
                              		  $scope.dueDate = $filter('date')(element.dueDate, "d/M/yyyy");
                                    }
                              	  
                                    if(element.endDate != null){
                              		  $scope.endDate = $filter('date')(element.endDate, "d/M/yyyy");
                              	  	}
                                    
                                    $scope.startDate = $filter('date')(element.startDate, "d/M/yyyy"); 
                                    console.log($scope.startDate);
                            	});
                            	
                            }                                                             
                    );                	
                };
                
                
               $scope.datesChanged = function(){
            	   $scope.userTasks = null;
            	   $scope.selectedUser = user;
            	   retrieveTasks();
               };
               
               
               $scope.goToDetails = function(task){
            	   $scope.executionActiveView = "form";
            	   $scope.task = task;
            	   console.log($scope.executionActiveView);
               };
               
               
               $scope.taskDelay = function (dueDate) {
               	if (dueDate === null)
               		return Infinity;
               	
               	var currentDate = new Date();
               	var diff = dueDate - currentDate.getTime();
       			var diffInDays = diff / (1000 * 3600 * 24);
       			
       			return diffInDays;
               };
               
               
               $scope.goBack = function (){
           		$scope.executionActiveView = "list";
               };
                
            }]
    );

})(angular);
