/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('TaskListCtrl', ['$scope', '$http', '$location', '$mdDialog', 'CONFIG',

            function ($scope, $http, $location, $mdDialog, config) {

                $scope.tasksToClaim = null;
                $scope.activeTasks = null;
                $scope.imagePath = config.AVATARS_PATH;

                $scope.subHidden = false;
            }]
    );

})(angular);
