(function (angular) {

    'use strict';

    angular.module('wfmanagerControllers').controller('TaskDetailsCtrl', ['$scope', '$filter', '$window', '$routeParams', '$mdDialog', 'processService', 'CONFIG',
		/**
		 * @name TaskDetailsCtrl
		 * @ngDoc controllers
		 * @memberof wfmanagerControllers
		 * 
		 * @desc Controller used in Task details view
		 */
        function ($scope, $filter, $window, $routeParams, $mdDialog, processService, config) {

            var taskId = $routeParams['taskId'];
            $scope.task = null;

            $scope.startDate = null;
            $scope.dueDate = null;

            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

            /**
             * @memberOf TaskDetailsCtrl
             * @desc Returns A difference in days between the due date(if present) and the complete task
             * 
             * @returns {Number} - The difference between dates
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
             * @memberOf TaskDetailsCtrl
             * @desc Redirects to previous page
             */
            $scope.backTo = function () {
                $window.history.back();
            };

            /**
             * @memberOf TaskDetailsCtrl
             * @desc Open a modal to display task details
             */
            $scope.showTaskDetails = function () {
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
                })
            };

            /**
             * @memberOf TaskDetailsCtrl
             * @desc Opens a modal panel in order to display the process diagram
             * 
             * @param {event} event
             */
            $scope.showProgressDiagram = function (event) {
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
                });
            };

        }]);
})(angular);
