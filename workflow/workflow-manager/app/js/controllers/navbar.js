(function () {

    'use strict';

    angular.module('wfmanagerControllers').controller('NavBarCtrl', ['$scope', '$mdSidenav', '$location', 'auth',
        /**
         * @name NavBarCtrl
         * @ngDoc controllers
         * @memberof wfmanagerControllers
         */
        function ($scope, $mdSidenav, $location, authProvider) {

            $scope.inputFile = null;

            $scope.pages = [];
            $scope.page = { title: null, path: null, icon: null, color: null, disabled: null };

            /**
             * @memberof NavBarCtrl
             * @desc Toggle the navigation side bar
             */
            $scope.toggle = function () {
                $mdSidenav('navbar').toggle();
            };

            /**
             * @memberof NavBarCtrl
             * @desc Swipe right listener in order to open the navigation side bar
             */
            $scope.onSwipeRight = function () {
                $mdSidenav('navbar').open();
            };

            /**
             * @memberof NavBarCtrl
             * @desc Swipe left listener in order to close the navigation side bar
             */
            $scope.onSwipeLeft = function () {
                $mdSidenav('navbar').close();
            };

            /**
             * @memberof NavBarCtrl
             * @desc Redirects to given path
             * 
             * @param {String} path
             */
            $scope.goTo = function (path) {
                $mdSidenav('navbar').close();
                $location.path(path);
            };

            /**
             * @memberof NavBarCtrl
             * @desc Checks if the given path is selected
             * 
             * @param {String} path
             * @returns {Boolean} - Whether the given path is selected or not
             */
            $scope.isSelected = function (path) {
                return path === $location.path();
            };

            /**
             * @memberof NavBarCtrl
             * @desc Logouts the user
             */
            $scope.logout = function () {
                authProvider.logout();
            };

            /**
             * @memberof NavBarCtrl
             * @desc Initializes the available pages. Also check the logged in order to disable/enable some of the pages
             */
            $scope.initializePages = function () {

                $scope.page = { title: 'processes', path: '/process', icon: 'processDefinitions.svg', color: null, disabled: false };
                $scope.pages.push($scope.page);

                $scope.page = { title: 'processesHistory', path: '/history', icon: 'history.svg', color: null, disabled: false };
                $scope.pages.push($scope.page);

                $scope.page = { title: 'pendingTasks', path: '/pending', icon: 'inbox.svg', color: 'purple', disabled: false };
                $scope.pages.push($scope.page);

                $scope.page = { title: 'executionsInProgress', path: '/inprogress', icon: 'inProgressInstances.svg', color: 'purple', disabled: false };
                $scope.pages.push($scope.page);

                $scope.page = { title: 'userActivity', path: '/activity', icon: 'activity.svg', color: 'purple', disabled: false };
                $scope.pages.push($scope.page);

                if (authProvider.getRoles().indexOf("ROLE_Admin") >= 0) {
                    $scope.page = { title: 'settings', path: '/settings', icon: 'settings.svg', color: 'grey', disabled: false };
                    $scope.pages.push($scope.page);
                } else {
                    $scope.page = { title: 'settings', path: '/settings', icon: 'settings.svg', color: 'grey', disabled: true };
                    $scope.pages.push($scope.page);
                }
            };
        }]);
})();