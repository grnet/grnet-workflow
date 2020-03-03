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

                $scope.page = { title: 'processes', path: '/process', icon: 'processDefinitions.svg', color: 'blue', disabled: false };
                $scope.pages.push($scope.page);

                //User should only see the processes and exit menu items
                if(!(authProvider.getRoles().indexOf("ROLE_Admin") < 0 &&
                    authProvider.getRoles().indexOf("ROLE_Supervisor") < 0 &&
                    authProvider.getRoles().indexOf("ROLE_ProcessAdmin") < 0 &&
                    authProvider.getRoles().indexOf("ROLE_User") > -1))
                {
                    $scope.page = {
                        title: 'dashboard',
                        path: '/dashboard',
                        icon: 'chart_type.svg',
                        color: "blue",
                        disabled: false
                    };
                    $scope.pages.push($scope.page);

                    if (authProvider.getRoles().indexOf("ROLE_Admin") >= 0) {
                        $scope.page = {
                            title: 'externalForms',
                            path: '/externalForms',
                            icon: 'external.svg',
                            color: "blue",
                            disabled: false
                        };
                        $scope.pages.push($scope.page);
                    } else {
                        $scope.page = {
                            title: 'externalForms',
                            path: '/externalForms',
                            icon: 'external.svg',
                            color: "blue",
                            disabled: true
                        };
                        $scope.pages.push($scope.page);
                    }

                    $scope.page = {
                        title: 'processesHistory',
                        path: '/history',
                        icon: 'history.svg',
                        color: 'blue',
                        disabled: false
                    };
                    $scope.pages.push($scope.page);

                    $scope.page = {
                        title: 'pendingTasks',
                        path: '/pending',
                        icon: 'inbox.svg',
                        color: 'blue',
                        disabled: false
                    };
                    $scope.pages.push($scope.page);

                    $scope.page = {
                        title: 'executionsInProgress',
                        path: '/inprogress',
                        icon: 'inProgressInstances.svg',
                        color: 'blue',
                        disabled: false
                    };
                    $scope.pages.push($scope.page);

                    $scope.page = {
                        title: 'userActivity',
                        path: '/activity',
                        icon: 'activity.svg',
                        color: 'blue',
                        disabled: false
                    };
                    $scope.pages.push($scope.page);

                    if (authProvider.getRoles().indexOf("ROLE_Admin") >= 0) {
                        $scope.page = {
                            title: 'settings',
                            path: '/settings',
                            icon: 'settings.svg',
                            color: 'blue',
                            disabled: false
                        };
                        $scope.pages.push($scope.page);
                    } else {
                        $scope.page = {
                            title: 'settings',
                            path: '/settings',
                            icon: 'settings.svg',
                            color: 'blue',
                            disabled: true
                        };
                        $scope.pages.push($scope.page);
                    }
                }

            };
        }

        angular.module('wfManagerControllers').controller('NavBarCtrl', ['$scope', '$mdSidenav', '$location', '$mdDialog', 'auth', navBarCtrl]);

    });