define(['angular', 'services/process-service'],

    function (angular) {

        'use strict';

        function taskAssignCtrl($scope, $filter, $routeParams, $mdDialog, processService, config) {

            $scope.imagePath = config.AVATARS_PATH;
            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

            var taskId = $routeParams['taskId'];
            $scope.task = null;

            $scope.startDate = null;
            $scope.dueDate = null;

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

            // get the selected task
            processService.getTask(taskId).then(
                // success callback
                function (response) {
                    $scope.task = response.data;

                    if ($scope.task.dueDate != null)
                        $scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy");

                    $scope.startDate = $filter('date')($scope.task.startDate, "d/M/yyyy");

                    $scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");

                    getTaskDelay();

                },
                // error callback
                function (response) {
                    exceptionModal(response);
                });

            /**
             * Unclaim a task
             */
            $scope.removeAssignee = function () {
                processService.unclaimTask($scope.task.id).then(
                    //success callback
                    function (response) {
                        $scope.task.assignee = null;

                        //error callback	
                    }, function (response) {
                        $mdDialog.show($mdDialog.alert()
                            .parent(document.body)
                            .clickOutsideToClose(false)
                            .title('Error')
                            .content(response.data)
                            .ok('Ok'))
                    }
                );
            };

            /**
             * Opens a modal to select assignee for the task
             * 
             * @param event
             */
            $scope.selectAssignee = function (event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog, candidates, processService, task) {
                        $scope.candidates = candidates;
                        var processService = processService;
                        $scope.task = task;

                        $scope.showProgressBar = true;

                        $scope.cancel = function () {
                            $scope.task.assignee = null;
                            $mdDialog.hide();
                        };

                        $scope.confirm = function () {
                            setAssignee();
                            $mdDialog.hide();
                        };

                        function setAssignee() {
                            processService.setAssigneeToTask($scope.task, $scope.task.assignee).then(
                                function (response) {

                                },
                                function (response) {

                                }
                            ).finally(function () {
                                $scope.showProgressBar = false;
                            });
                        }

                        $scope.getAllCandidates = function () {
                            $scope.showProgressBar = true;

                            processService.getAllCandidates().then(
                                // success callback
                                function (response) {
                                    $scope.showProgressBar = false;
                                    $scope.candidates = response.data;
                                },
                                // error callback
                                function (response) {
                                    $scope.showProgressBar = false;
                                    exceptionModal(response);
                                });
                        };

                        $scope.getCandidatesForTask = function () {
                            $scope.showProgressBar = true;

                            processService.getCandidatesForTask($scope.task.id).then(
                                // success callback
                                function (response) {
                                    $scope.showProgressBar = false;
                                    $scope.candidates = response.data;

                                    // error callback
                                }, function (response) {
                                    $scope.showProgressBar = false;
                                    exceptionModal(response);
                                });
                        };

                        $scope.getCandidatesForTask();
                    },
                    templateUrl: 'templates/assigneeSelection.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: false,
                    locals: {
                        'candidates': $scope.task.candidates,
                        'processService': processService,
                        'task': $scope.task
                    }
                })
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

            $scope.showProgressDiagram = function (event) {
                $mdDialog.show({
                    controller: function ($mdDialog) {

                        $scope.instance = $scope.task.processInstance;
                        $scope.service = config.WORKFLOW_SERVICE_ENTRY;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                    },
                    scope: $scope,
                    preserveScope: true,
                    templateUrl: 'templates/progressDiagram.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: true,
                    locals: {
                        'service': $scope.service,
                        'instance': $scope.instance,
                    }
                })
            };

            function exceptionModal(response, $event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {
                        $scope.error = response.data;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                    },
                    templateUrl: 'templates/exception.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: $event,
                    clickOutsideToClose: false
                })
            };

        }

        angular.module('wfWorkspaceControllers').controller('TaskAssignCtrl', ['$scope', '$filter', '$routeParams', '$mdDialog', 'processService', 'CONFIG', taskAssignCtrl]);

    }
);