/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('NavBarCtrl',
        ['$scope', '$mdSidenav', '$location','$log', 'auth',

            function ($scope, $mdSidenav, $location, $log, authProvider) {
        		
        		$scope.pages = [];
        		$scope.page = {title: null, path: null, icon: null, color: null, disabled: null};
        		
                $scope.inputFile = null;
                
                $scope.toggle = function () {
                    $mdSidenav('navbar').toggle();
                };

                $scope.onSwipeRight = function () {
                    $mdSidenav('navbar').open();
                };

                $scope.onSwipeLeft = function () {
                    $mdSidenav('navbar').close();
                };

                $scope.goTo = function (path) {
                    $mdSidenav('navbar').close().then(function () {
                        $location.path(path);
                    });
                };
                
                $scope.logout = function () {
                    authProvider.logout();
                };
                
                $scope.initializePages = function (){
                	
                	$scope.page = {title: 'myTasks', path: '/task', icon: 'myTasks.svg', color: 'red', disabled: false};
            		$scope.pages.push($scope.page);
            		
                	if(authProvider.getRoles().indexOf("ROLE_Supervisor") >= 1 ){
                		$scope.page = {title:  'assignTasks', path: '/assign', icon: 'assignTasks.svg', color: 'red', disabled: false };
                		$scope.pages.push($scope.page);
                		
                		$scope.page = {title: 'completedTasks', path: '/completed', icon: 'completedTasks.svg', color: 'green', disabled: false};
                		$scope.pages.push($scope.page);
                	}else{
                		$scope.page = {title: 'assignTasks', path: '/assign', icon: 'assignTasks.svg', color: 'red', disabled: true };
                		$scope.pages.push($scope.page);
                		
                		$scope.page = {title: 'completedTasks', path: '/completed', icon: 'completedTasks.svg', color: 'green', disabled: true};
                		$scope.pages.push($scope.page);
                	}
                		
            		$scope.page = {title: 'myActivity', path: '/activity', icon: 'myActivity.svg', color: 'green', disabled: false};
            		$scope.pages.push($scope.page);
            		
            		$scope.page = {title: 'startNewProcess', path: '/process', icon: 'startProccess.svg', color: 'blue', disabled: false};
            		$scope.pages.push($scope.page);
            		
                };

            }]
    );
    


})(angular);
