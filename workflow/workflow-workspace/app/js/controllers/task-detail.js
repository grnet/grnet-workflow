(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers').controller('TaskDetailCtrl', ['$scope', '$filter', '$routeParams', '$location', '$mdDialog', '$window', 'processService', 'CONFIG',
        /**
         * @name TaskDetailCtrl
         * @ngDoc controllers
         * @memberof wfworkspaceControllers
         * 
         * @desc Controller for the Task detail view
         */
        function ($scope, $filter, $routeParams, $location, $mdDialog, $window, processService, config) {

            $scope.imagePath = config.AVATARS_PATH;
            var taskId = $routeParams['taskId'];
            $scope.executionActiveView = "list";

            $scope.showProgress = true;

            $scope.historyTasks = [];

            $scope.task = null;
            $scope.historicTask = null;

            $scope.assignee = null;
            $scope.instance = {
                title: "",
                supervisor: null,
                variableValues: null
            };

            $scope.startDate = null;
            $scope.dueDate = null;
            $scope.endDate = null;

            $scope.historicStartDate = null;
            $scope.historicDueDate = null;
            $scope.historicEndDate = null;


            $scope.approveAction = null;
            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

            /**
             * @memberOf TaskDetailCtrl
             * @desc Returns the difference between due date(if present) and current date 
             * 
             * @param {Task} task
             * @returns {Number} - Difference between dates
             */
            $scope.taskDelay = function (task) {
                var diff;

                if (task.dueDate === null)
                    return Infinity;

                if (task.endDate) {
                    diff = task.dueDate - task.endDate;

                } else {
                    var currentDate = new Date();
                    diff = task.dueDate - currentDate.getTime();
                }

                var diffInDays = diff / (1000 * 3600 * 24);
                return diffInDays;
            };

            /**
             * @memberOf TaskDetailCtrl
             * @desc Returns task by given id
             * 
             * @param {String} taskId
             */
            function getTask(taskId) {
                $scope.showProgress = true;

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

                        var formProperties = response.data.taskForm;

                        for (var i = 0; i < formProperties.length; i++) {

                            var property = formProperties[i];

                            if (property.type === 'approve') {

                                $scope.approveAction = property;
                                break;
                            }
                        }
                    },
                    // error callback
                    function (response) {
                        exceptionModal(response);

                    }).finally(function () {
                        $scope.showProgress = false;
                    });
            }

            getTask(taskId);

            /**
             * @memberof TaskDetailCtrl
             * @desc Displays a modal panel showing available candidates for the task.
             * If no candidates found, then all users will be available as candidates
             * 
             */
            $scope.selectAssigneeModal = function () {
                $mdDialog.show({
                    controller: function ($mdDialog, candidates) {
                        $scope.candidates = candidates;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };

                        $scope.confirm = function () {
                            $scope.task.assignee = $scope.assignee;
                            $mdDialog.hide();
                        };
                    },
                    scope: $scope,
                    preserveScope: true,
                    templateUrl: 'templates/assigneeSelection.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: true,
                    locals: {
                        'candidates': $scope.task.candidates
                    }
                })
            };

            /**
             * @memberOf TaskDetailCtrl
             * @desc Claims the task (assigns the task to user)
             */
            $scope.claimTask = function () {
                $scope.showProgress = true;

                processService.claimTask($scope.task.id).then(
                    // success callback
                    function (response) {
                        $scope.task = response.data;
                        getTask(taskId);
                    },
                    // error callback
                    function (response) {
                        exceptionModal(response, $scope.task);

                    }).finally(function () {
                        $scope.showProgress = false;
                    });
            };

            /**
             * @memberOf TaskDetailCtrl
             * @desc Completes a task
             */
            $scope.completeTask = function () {
                $scope.showProgress = true;

                processService.completeTask($scope.task).then(
                    // success callback
                    function (response) {
                        $scope.task = response.data;
                        $location.path('/task');
                    },
                    // error callback
                    function (response) {
                        exceptionModal(response, $scope.task);
                    }

                ).finally(function () {
                    $scope.showProgress = false;
                });
            };

            /**
             * @memberOf TaskDetailCtrl
             * @desc Completes a task (if the task is an approve document)
             * 
             * @param {String} outcome
             */
            $scope.acceptTask = function (outcome) {
                $scope.showProgress = true;
                $scope.approveAction.value = outcome;

                processService.completeTask($scope.task).then(
                    // success callback
                    function (response) {
                        $scope.task = response.data;
                        $location.path('/task');
                    },
                    // error callback
                    function (response) {
                        exceptionModal(response, $scope.task);
                    }

                ).finally(function () {
                    $scope.showProgress = false;
                });
            };

            /**
             * @memberof TaskDetailCtrl
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
                })
            };

            /**
             * @memberof TaskDetailCtrl
             * @desc Tab change event listener. Gets completed tasks when user selects the particular tab
             * 
             * @param {String} tab
             */
            $scope.onTabSelected = function (tab) {
                if (tab == "executionHistory") {
                    // get tasks by task instance
                    processService.getCompletedTasksByInstances($scope.task.processInstance.id).then(
                        function (response) {
                            $scope.historyTasks = response.data;
                        });
                } else
                    $scope.executionActiveView = "list";
            };

            /**
             * @memberof TaskDetailCtrl
             * @desc Returns task details for a completed task
             * 
             * @param {String} taskId
             */
            $scope.goToCompletedTask = function (taskId) {
                processService.getTask(taskId).then(
                    function (response) {
                        $scope.historicTask = response.data;

                        $scope.executionActiveView = "form";

                        if ($scope.historicTask.dueDate != null) {
                            $scope.historicDueDate = $filter('date')($scope.historicTask.dueDate, "d/M/yyyy");
                        }

                        if ($scope.historicTask.endDate != null) {
                            $scope.historicEndDate = $filter('date')($scope.historicTask.endDate, "d/M/yyyy");
                        }

                        $scope.historicStartDate = $filter('date')($scope.historicTask.startDate, "d/M/yyyy");
                    },
                    function (response) {
                        exceptionModal(response);
                    });
            };

            /**
             * @memberof TaskDetailCtrl
             * @desc Temporary saves the task form data
             */
            $scope.temporarySave = function () {
                $scope.showProgress = true;

                processService.temporarySave($scope.task).then(
                    //success callback
                    function (response) {
                        // get the task
                        getTask(taskId);
                    },
                    //error callback
                    function (response) {
                        exceptionModal(response, $scope.task);
                    }

                ).finally(function () {
                    $scope.showProgress = false;
                });
            };

            /**
             * @memberof TaskDetailCtrl
             * @desc Go back to historic tasks
             */
            $scope.goBack = function () {
                $scope.executionActiveView = "list";
            };

            /**
             * @memberof TaskDetailCtrl
             * @desc Redirects to print page
             */
            $scope.goToPrintPage = function () {
                $location.path('/startform/print/' + $scope.task.processInstance.id, '_blank')
            };

            /**
             * @memberof TaskDetailCtrl
             * @desc Displays a modal panel showing the process diagram
             */
            $scope.showProgressDiagram = function () {
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

            /**
             * @memberof TaskDetailCtrl
             * @desc Displays a modal panel, showing the exception message 
             * 
             * @param {any} response
             * @param {event} event
             */
            function exceptionModal(response, task, event) {
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
                    targetEvent: event,
                    clickOutsideToClose: false
                })
            };

            /**
             * @memberof TaskDetailCtrl
             * @desc Navigates to previous page
             */
            $scope.back = function () {
                $window.history.back();
            };

        }]);
})(angular);
