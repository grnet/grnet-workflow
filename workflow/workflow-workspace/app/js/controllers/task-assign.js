/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('TaskAssignCtrl', ['$scope', '$http', '$routeParams', '$location', '$mdDialog', 'processService', 'CONFIG',

            /**
             * Controller for the tasks-to-assign view
             *
             * @param $scope
             * @param $http
             * @param $routeParams
             * @param $location
             * @param $mdDialog
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $http, $routeParams, $location, $mdDialog, processService, config) {

                var taskId = $routeParams['taskId'];
                $scope.task = null;

                // get the selected task
                processService.getTask(taskId)
                    .then(
                    // success callback
                    function (response) {
                        console.log(response.data);

                        $scope.task = response.data;
                    },
                    // error callback
                    function (response) {
                    }
                );

            }]
    );

})(angular);
