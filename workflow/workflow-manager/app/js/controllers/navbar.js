(function () {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('NavBarCtrl',
        ['$scope', '$mdSidenav', '$location', '$log', '$mdDialog',
            function ($scope, $mdSidenav, $location, $log, $mdDialog) {

                $scope.inputFile = null;

                $scope.toggle = function () {
                    $mdSidenav('navbar').toggle()
                        .then(function () {
                            $log.debug("toggle nav-bar is done");
                        });
                };

                $scope.onSwipeRight = function () {
                    $mdSidenav('navbar').open()
                        .then(function () {
                            $log.debug("open nav-bar is done");
                        });
                };

                $scope.onSwipeLeft = function () {
                    $mdSidenav('navbar').close()
                        .then(function () {
                            $log.debug("close nav-bar is done");
                        });
                };

                $scope.goTo = function (path) {
                    $mdSidenav('navbar').close();
                    $location.path(path);
                };

                $scope.showOwnerFilterDialog = function (ev) {
                    $mdDialog.show({
                        controller: DialogController,
                        templateUrl: 'ownerFilter.tmpl.html',
                        parent: angular.element(document.body),
                        targetEvent: ev,
                        clickOutsideToClose: true
                    })
                        .then(function (answer) {
                            $scope.status = 'You said the information was "' + answer + '".';
                        }, function () {
                            $scope.status = 'You cancelled the dialog.';
                        });
                };
            }]
    );

})();


function DialogController($scope, $mdDialog) {
    $scope.hide = function () {
        $mdDialog.hide();
    };
    $scope.cancel = function () {
        $mdDialog.cancel();
    };
    $scope.answer = function (answer) {
        $mdDialog.hide(answer);
    };
}