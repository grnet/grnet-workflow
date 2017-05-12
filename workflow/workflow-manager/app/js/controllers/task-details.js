define(['angular', 'services/processservice'],

    function (angular) {

        'use strict';
        
        function taskDetailsCtrl($scope, $filter, $window, $routeParams, $mdDialog, processService, config) {

            var taskId = $routeParams['taskId'];
            $scope.task = null;

            $scope.startDate = null;
            $scope.dueDate = null;

            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

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

                    if ($scope.task.dueDate != null) {
                        $scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy H:mm");
                    }

                    if ($scope.task.endDate != null) {
                        $scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");
                    }

                    $scope.startDate = $filter('date')($scope.task.startDate, "d/M/yyyy");

                    getTaskDelay();

                    $scope.instanceId = $scope.task.processInstance.id;
                },
                // error callback
                function (response) {
                }
            );

            /**
             * Redirects to previous page
             */
            $scope.backTo = function () {
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

            $scope.sendNotificationEmail = function () {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog, assignee, taskId, processService) {

                        $scope.showProgressBar = false;
                        $scope.assignee = assignee;
                        $scope.success = false;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };

                        $scope.sendEmail = function () {
                            $scope.showProgressBar = true;

                            processService.sendTaskDueDateNotification(taskId, $scope.content).then(
                                // sucess callback
                                function () {
                                    $scope.content = null;
                                    $scope.success = true;
                                }
                            ).finally(function () {
                                $scope.showProgressBar = false;
                            });
                        };

                    },
                    templateUrl: 'templates/dueDateNotification.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: true,
                    locals: {
                        'assignee': $scope.task.assignee,
                        'taskId': $scope.task.id,
                        'processService': processService
                    }
                });
            };

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
                })
            };

        }

        angular.module('wfManagerControllers').controller('TaskDetailsCtrl', ['$scope', '$filter', '$window', '$routeParams', '$mdDialog', 'processService', 'CONFIG', taskDetailsCtrl]);
    }

);