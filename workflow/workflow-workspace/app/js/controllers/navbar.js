/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('NavBarCtrl',
        ['$scope', '$mdSidenav', '$location', '$log',

            function ($scope, $mdSidenav, $location, $log) {

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

            }]
    );

})(angular);
