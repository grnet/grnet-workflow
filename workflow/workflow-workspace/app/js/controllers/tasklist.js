(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers').controller('TaskListCtrl', ['$scope', '$mdDialog', 'processService', 'CONFIG',
        /**
         * @name TaskListCtrl
         * @ngDoc controllers
         * @memberof wfworkspaceControllers
         * 
         * @desc Controller for the Task list view
         */
        function ($scope, $mdDialog, processService, config) {

            $scope.claimTasks = [];
            $scope.tasks = [];
            $scope.assignedTasks = [];
            $scope.unAssignedTasks = {};
            $scope.taskMapByProcess = [];

            $scope.imagePath = config.AVATARS_PATH;

            $scope.showProgress = true;

            /**
             * @memberOf TaskListCtrl
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

            processService.getTasksInProgress().then(
                // success callback
                function (response) {
                    $scope.tasks = response.data;
                    $scope.assignedTasks = response.data;

                    $scope.taskMapByProcess = ArrayUtil.mapByProperty2Property($scope.tasks, "definitionName", "assigned");

                    //get tasks to be claimed by user
                    processService.getClaimTasks().then(
                        // success callback
                        function (response) {
                            $scope.claimTasks = response.data;
                            $scope.unAssignedTasks = response.data;

                            $scope.taskMapByProcess = ArrayUtil.extendMapByProperty($scope.claimTasks, $scope.taskMapByProcess, "definitionName", "unassigned");
                        },
                        // error callback
                        function (response) {
                            exceptionModal(response);
                        });

                },
                // error callback
                function (response) {
                    exceptionModal(response);

                }).finally(function () {
                    $scope.showProgress = false;
                });

            /**
             * @memberof TaskListCtrl
             * @desc Handles the selected item from the item list
             * 
             * @param {String} definitionName
             */
            $scope.selectionChanged = function (definitionName) {
                $scope.assignedTasks = $scope.taskMapByProcess[definitionName]["assigned"];
                $scope.unAssignedTasks = $scope.taskMapByProcess[definitionName]["unassigned"];
            };

            /**
             * @memberof TaskListCtrl
             * @desc Shows all tasks
             */
            $scope.selectAllTasks = function () {
                $scope.assignedTasks = $scope.tasks;
                $scope.unAssignedTasks = $scope.claimTasks;
            };

            /**
             * @memberof TaskListCtrl
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
                });
            };

        }]);
})(angular);
