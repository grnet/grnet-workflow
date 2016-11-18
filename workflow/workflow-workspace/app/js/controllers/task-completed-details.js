(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers').controller('TaskCompletedDetailsCtrl', ['$scope', '$filter', '$window', '$routeParams', '$mdDialog', 'processService', 'CONFIG',
        /**
         * @name TaskCompletedDetailsCtrl
         * @ngDoc controllers
         * @memberof wfworkspaceControllers
         * 
         * @desc Controller for the task-completed-details view
         */
        function ($scope, $filter, $window, $routeParams, $mdDialog, processService, config) {

            $scope.imagePath = config.AVATARS_PATH;
            var taskId = $routeParams['taskId'];
            $scope.task;

            $scope.startDate = null;
            $scope.dueDate = null;
            $scope.endDate = null;

            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

            // get the selected task
            processService.getTask(taskId).then(
                // success callback
                function (response) {
                    $scope.task = response.data;

                    if ($scope.task.dueDate != null)
                        $scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy");

                    if ($scope.task.endDate != null)
                        $scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");

                    $scope.startDate = $filter('date')($scope.task.startDate, "d/M/yyyy");

                    getTaskDelay();
                },
                // error callback
                function (response) {
                }
            );

            /**
             * @memberOf TaskCompletedDetailsCtrl
             * @desc Returns the difference between due date and current date
             * 
             * @returns {Number} - Difference in days
             */
            var getTaskDelay = function () {
                var diff;

                if ($scope.task.dueDate === null)
                    return Infinity;

                if ($scope.task.endDate) {
                    diff = $scope.task.dueDate - $scope.task.endDate;

                } else {
                    var currentDate = new Date();
                    diff = $scope.task.dueDate - currentDate.getTime();
                }

                $scope.diffInDays = diff / (1000 * 3600 * 24);
            };

            /**
             * @memberOf TaskCompletedDetailsCtrl
             * @desc Redirects to previous page 
             */
            $scope.goBack = function () {
                $window.history.back();
            };

            /**
             * @memberOf TaskCompletedDetailsCtrl
             * @desc Shows a process diagram marking task's position
             * 
             * @param {event} event
             */
            $scope.showTaskProcessDiagram = function (event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog, process, service, task) {
                        $scope.process = process;
                        $scope.service = service;
                        $scope.task = task;
                        $scope.definitionName = task.definitionName;

                        $scope.cancel = function () {
                            $mdDialog.cancel();
                        };

                    },
                    templateUrl: 'templates/diagram.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: true,
                    locals: {
                        'service': config.WORKFLOW_SERVICE_ENTRY,
                        'process': $scope.task.processId,
                        'task': $scope.task
                    }
                })
            };

            /**
             * @memberof TaskCompletedDetailsCtrl
             * @desc Displays a modal panel showing task's details
             * 
             * @param {event} event
             */
            $scope.showTaskDetails = function (event) {
                $mdDialog.show({
                    controller: function ($mdDialog) {

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                    },
                    scope: $scope,
                    preserveScope: true,
                    templateUrl: 'templates/taskDetails.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: true,
                    locals: {
                        'taskDetails': $scope.task.taskDetails
                    }
                });
            };

        }]);
})(angular);
