define(['angular', 'services/process-service'],

    function (angular) {

        'use strict';

        function taskCompletedDetailsCtrl($scope, $filter, $window, $routeParams, $location, $mdDialog, processService, config) {

            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;
            $scope.imagePath = config.AVATARS_PATH;

            $scope.startDate = null;
            $scope.dueDate = null;
            $scope.endDate = null;


            $scope.showProgress = true;

            // get the selected task
            processService.getTask($routeParams['taskId']).then(
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
                    exceptionModal(response);
                }

            ).finally(function () {
                $scope.showProgress = false;
            });


            /**
             * Returns the difference between due date and current date
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
             * Redirects to previous page
             */
            $scope.goBack = function () {
                $window.history.back();
            };

            /**
             * Open a modal to display task details
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
             * @desc Displays a modal panel, showing the exception message
             *
             * @param {any} response
             * @param {event} event
             */
            function exceptionModal(response, event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {
                        $scope.error = response.data;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                    },

                    templateUrl: 'templates/exception.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: event,
                    clickOutsideToClose: false
                })
            }

        }

        angular.module('wfWorkspaceControllers').controller('TaskCompletedDetailsCtrl', ['$scope', '$filter', '$window', '$routeParams', '$location', '$mdDialog', 'processService', 'CONFIG', taskCompletedDetailsCtrl]);
    }
);