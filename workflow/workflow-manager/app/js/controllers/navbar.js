define(
    ['angular', 'services/authprovider'],

    function (angular) {

        'use strict';

        function navBarCtrl($scope, $mdSidenav, $location, $mdDialog, authProvider) {

            $scope.inputFile = null;

            $scope.pages = [];
            $scope.page = { title: null, path: null, icon: null, color: null, disabled: null };

            $scope.toggle = function () {
                $mdSidenav('navbar').toggle()
                    .then(function () {
                    });
            };

            $scope.onSwipeRight = function () {
                $mdSidenav('navbar').open()
                    .then(function () {
                    });
            };

            $scope.onSwipeLeft = function () {
                $mdSidenav('navbar').close()
                    .then(function () {

                    });
            };

            $scope.goTo = function (path) {
                $mdSidenav('navbar').close();
                $location.path(path);
            };

            $scope.isSelected = function (path) {
                return path === $location.path();
            };

            $scope.logout = function () {
                authProvider.logout();
            };

            $scope.initializePages = function () {

                $scope.page = { title: 'processes', path: '/process', icon: 'processDefinitions.svg', color: null, disabled: false };
                $scope.pages.push($scope.page);

                $scope.page = { title: 'dashboard', path: '/dashboard', icon: 'chart_type.svg', color: "grey", disabled: false };
                $scope.pages.push($scope.page);

                if (authProvider.getRoles().indexOf("ROLE_Admin") >= 0) {
                    $scope.page = { title: 'externalForms', path: '/externalForms', icon: 'external.svg', color: "grey", disabled: false };
                    $scope.pages.push($scope.page);
                } else {
                    $scope.page = { title: 'externalForms', path: '/externalForms', icon: 'external.svg', color: "grey", disabled: true };
                    $scope.pages.push($scope.page);
                }

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
        }

        angular.module('wfManagerControllers').controller('NavBarCtrl', ['$scope', '$mdSidenav', '$location', '$mdDialog', 'auth', navBarCtrl]);

    });