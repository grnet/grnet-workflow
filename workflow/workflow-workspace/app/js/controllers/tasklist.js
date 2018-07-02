define(['angular', 'services/process-service', 'util/core'],

    function (angular) {

        'use strict';

        function taskListCtrl($scope, $mdDialog, processService, config) {

            $scope.claimTasks = [];
            $scope.tasks = [];
            $scope.assignedTasks = [];
            $scope.unAssignedTasks = {};
            $scope.taskMapByProcess = [];

            $scope.sortOptions = [];
            $scope.orderByOption = null;

            $scope.sortOption = { title: 'dueTo', id: 'dueDate' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'taskName', id: 'name' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'process', id: 'definitionName' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'processInstanceName', id: 'processInstance.title' };
            $scope.sortOptions.push($scope.sortOption);

            $scope.imagePath = config.AVATARS_PATH;

            $scope.showProgress = true;

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
                        });

                }).finally(function () {
                    $scope.showProgress = false;
                });


            /**
             * Handles the selected item from the item list
             */
            $scope.selectionChanged = function (definitionName) {
                $scope.assignedTasks = $scope.taskMapByProcess[definitionName]["assigned"];
                $scope.unAssignedTasks = $scope.taskMapByProcess[definitionName]["unassigned"];
            };

            /**
             * Shows all the tasks
             */
            $scope.selectAllTasks = function () {
                $scope.assignedTasks = $scope.tasks;
                $scope.unAssignedTasks = $scope.claimTasks;
            };

            /**
             * @memberOf TaskListCtrl
             * @desc Sorts tasks by given option
             *
             * @param {String} optionId
             */
            $scope.sortBy = function (optionId) {
                $scope.orderByOption = optionId;
            };
        }

        angular.module('wfWorkspaceControllers').controller('TaskListCtrl', ['$scope', '$mdDialog', 'processService', 'CONFIG', taskListCtrl]);
    }
);
