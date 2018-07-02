define(['angular', 'services/authprovider'],

    function (angular) {

        'use strict';

        function pendingCtrl($scope, $http, $location, $mdDialog, processService, config, auth) {

            $scope.imagePath = config.AVATARS_PATH;
            $scope.allTasks = null;
            $scope.tasksMappedByDefinition = null;
            $scope.filteredTasks = null;
            $scope.definitionNames = [];

            $scope.maxDateBefore = new Date();
            $scope.maxDateBefore.setDate($scope.maxDateBefore.getDate() + 1);

            $scope.searchFilter = {dateAfter: null, dateBefore: null, taskName: "", definitionId: ""};

            $scope.orderByOption = null;

            $scope.options = [];

            $scope.sortOption = {title: 'taskName', id: 'name'};
            $scope.options.push($scope.sortOption);
            $scope.sortOption = {title: 'processDetail', id: 'definitionName'};
            $scope.options.push($scope.sortOption);
            $scope.sortOption = {title: 'processInstanceName', id: 'processInstance.title'};
            $scope.options.push($scope.sortOption);
            $scope.sortOption = {title: 'startDate', id: 'startDate'};
            $scope.options.push($scope.sortOption);
            $scope.sortOption = {title: 'dueTo', id: 'dueDate'};
            $scope.options.push($scope.sortOption);
            $scope.sortOption = {title: 'worker', id: 'assignee'};
            $scope.options.push($scope.sortOption);


            processService.getActiveProcesses().then(
                // success callback
                function (response) {
                    $scope.definitions = response.data;

                    $scope.selectAllDefinitions = {name: "showAll", processDefinitionId: "all"};
                    $scope.definitions.push($scope.selectAllDefinitions);

                    initializeCriteria();
                    $scope.searchTasks();
                },
                // error callback
                function (response) {
                    exceptionModal(response);
                });

            /**
             * @memberof PendingCtrl
             * @descr Initializes all search criteria
             *
             */
            function initializeCriteria() {
                $scope.searchFilter.dateBefore = new Date();
                $scope.searchFilter.dateAfter = new Date();
                $scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth() - 3);
                $scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate() + 1);
                $scope.searchFilter.taskName = "";
                $scope.searchFilter.definitionId = "all";
            }

            /**
             * @memberof PendingCtrl
             * @desc Searches for active tasks based on given criteria
             *
             */
            $scope.searchTasks = function () {
                var dateAfterTime;
                var dateBeforeTime;

                if ($scope.searchFilter.dateAfter)
                    dateAfterTime = $scope.searchFilter.dateAfter.getTime();
                else
                    dateAfterTime = 0;

                if ($scope.searchFilter.dateBefore)
                    dateBeforeTime = $scope.searchFilter.dateBefore.getTime();
                else
                    dateBeforeTime = 0;

                if (!$scope.searchFilter.definitionId)
                    $scope.searchFilter.definitionId = "all";

                if (!$scope.searchFilter.taskName)
                    $scope.searchFilter.taskName = "";

                processService.getActiveTasksByCriteria($scope.searchFilter.definitionId, $scope.searchFilter.taskName, dateAfterTime, dateBeforeTime).then(
                    // success callback
                    function (response) {
                        $scope.allTasks = response.data;

                        $scope.tasksMappedById = ArrayUtil.mapByProperty2Property($scope.allTasks, "definitionName", "tasks");
                        $scope.taskIds = Object.keys($scope.tasksMappedById);
                        $scope.filteredTasks = response.data;
                        $scope.definitionNames = null;

                        var pairs = {};
                        var names = [];

                        $scope.taskIds.forEach(function (item) {
                            var task = $scope.tasksMappedById[item]["tasks"][0];
                            pairs[task.definitionName] = item;
                            names.push(task.definitionName);
                        });

                        //make unique list from definition names
                        var u = {};
                        for (var i = 0, l = names.length; i < l; ++i) {
                            if (!u.hasOwnProperty(names[i])) {
                                $scope.definitionNames.push(names[i]);
                                u[names[i]] = 1;
                            }
                        }
                    }
                );

                /**
                 * @memberof PendingCtrl
                 * @desc Returns the difference between due date and current date
                 *
                 * @param {Task} task
                 * @returns {Number} - The difference between dates
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
                 * @memberof PendingCtrl
                 * @desc Selects all available workflow definitions from the checkbox
                 */
                $scope.selectAll = function () {
                    $scope.filteredTasks = $scope.allTasks;
                };

                /**
                 * @memberof PendingCtrl
                 * @desc Sorting tasks by given option
                 *
                 * @param {String} optionId
                 */
                $scope.sortBy = function (optionId) {
                    $scope.orderByOption = optionId;
                };

                /**
                 * @memberof PendingCtrl
                 * @desc Clears the date picker for the after date
                 *
                 */
                $scope.clearDateAfter = function () {
                    $scope.searchFilter.dateAfter = null;
                    $scope.searchTasks();
                };

                /**
                 * @memberof PendingCtrl
                 * @desc Clears the date picker for the before date
                 *
                 */
                $scope.clearDateBefore = function () {
                    $scope.searchFilter.dateBefore = null;
                    $scope.searchTasks();
                };

                /**
                 * @memberof PendingCtrl
                 * @desc Clears the task name filter
                 *
                 */
                $scope.clearTaskName = function () {
                    $scope.searchFilter.taskName = "";
                    $scope.searchTasks();
                };

                /**
                 * @memberof PendingCtrl
                 * @desc Clears any filter
                 *
                 */
                $scope.clearAllFilters = function () {
                    $scope.searchFilter.dateAfter = null;
                    $scope.searchFilter.dateBefore = null;
                    $scope.searchFilter.taskName = "";
                    $scope.searchFilter.definitionId = "all";

                    $scope.searchTasks();
                };

                $scope.print = function() {
                    window.print();
                };

                /**
                 * @memberof PendingCtrl
                 * @des Displays a modal panel, showing the exception message
                 *
                 * @param {any} response
                 * @param {event} $event
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

            }
        }
        angular.module('wfManagerControllers').controller('PendingCtrl', ['$scope', '$http', '$location', '$mdDialog', 'processService', 'CONFIG', 'auth', pendingCtrl]);
    }
);