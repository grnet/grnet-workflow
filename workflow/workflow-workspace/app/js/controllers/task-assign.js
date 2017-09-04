define(['angular', 'services/process-service', 'services/authprovider'],

    function (angular) {

        'use strict';

        function taskAssignCtrl($scope, $filter, $routeParams, $mdDialog, processService, config, authProvider) {

            $scope.imagePath = config.AVATARS_PATH;
            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;
            $scope.selectedUser = null;

            var taskId = $routeParams['taskId'];
            $scope.task = null;

            $scope.startDate = null;
            $scope.dueDate = null;


            $scope.canAssign = authProvider.getRoles().indexOf("ROLE_Admin") > -1 || authProvider.getRoles().indexOf("ROLE_Supervisor") > -1;


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
                        exceptionModal(response, $scope.task);
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

                        $scope.hideDialog = function () {
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
                                });
                        };

                        $scope.notifyAdmin = function () {
                            $scope.selectedUser = null;
                            $mdDialog.show({
                                controller: function ($scope, $mdDialog, processService, task) {
                                    $scope.inputCancel = function () {
                                        $scope.selectedUser = null;
                                        $mdDialog.hide();
                                    };
                                    $scope.inputConfirm = function () {
                                        $scope.showProgressBar = false;

                                        processService.notifyNoCandidates(task.id, $scope.selectedUser).then(
                                            //success callback
                                            function () {
                                                $scope.showProgressBar = false;
                                            },
                                            // error callback
                                            function (response) {
                                                $scope.showProgressBar = false;
                                                exceptionModal(response);
                                            }
                                        );
                                        $mdDialog.hide();
                                    };
                                },
                                templateUrl: 'templates/inputCandidate.tmpl.html',
                                parent: document.body,
                                clickOutsideToClose: false,
                                locals: {
                                    'processService': processService,
                                    'task': $scope.task
                                }
                            });

                        };

                        $scope.getCandidatesForTask = function () {
                            $scope.showProgressBar = true;

                            processService.getCandidatesForTask($scope.task.id).then(
                                // success callback
                                function (response) {
                                    $scope.showProgressBar = false;
                                    $scope.candidates = response.data;
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

            function exceptionModal(response, task, $event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {
                        $scope.error = response.data;

                        $scope.map = {};
                        $scope.map["supervisor"] = task.processInstance.supervisor;
                        $scope.map["taskName"] = task.name;
                        $scope.map["processInstanceName"] = task.processInstance.title;

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

        angular.module('wfWorkspaceControllers').controller('TaskAssignCtrl', ['$scope', '$filter', '$routeParams', '$mdDialog', 'processService', 'CONFIG', 'auth', taskAssignCtrl]);

    }
);