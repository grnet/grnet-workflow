/**
 * @memberOf workflow-manager
 */
(function () {

    'use strict';

    angular.module('wfmanagerControllers').controller('ActivityCtrl', ['$scope', '$filter', '$location', '$mdDialog', 'processService', 'CONFIG',

        /**
         * @name ActivityCtrl
         * @ngDoc controllers
         * @memberof wfmanagerControllers
         * @desc Controller used by Activity view
         * @author nlyk
         */
            function ($scope, $filter, $location, $mdDialog, processService, config) {

            // constance variables in order to get images and documents url
            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;
            $scope.imagePath = config.AVATARS_PATH;

            // initialize view to task list
            $scope.activeView = "taskList";

            // search filter object
            $scope.searchFilter = { dateAfter: null, dateBefore: null };

            // initialize search criteria
            initializeCriteria();

            $scope.options = [];
            $scope.orderByOption = null;

            $scope.sortOptions = { title: 'taskName', id: 'name' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'endDate', id: 'endDate' };
            $scope.options.push($scope.sortOptions);

            // get users in order to fill autocomplete
            processService.getUsers().then(
                // success callback
                function (response) {
                    $scope.users = response.data;
                }
            );

            /**
             * @memberof ActivityCtrl
             *
             * @desc Used by autocomplete in order to return users that matches user's input
             *
             * @param {String} searchText
             */
            $scope.getMatches = function (searchText) {
                $scope.text = searchText;
                $scope.filteredUsers = $scope.users.filter(userFiltering);

                $scope.enableProcessFilter = false;
                $scope.definitions = {};
            };

            /**
             * @memberof ActivityCtrl
             *
             * @desc Returns any users that matched the user's input
             *
             * @param {User} user
             * @returns Any matched users on given criteria
             */
            var userFiltering = function (user) {
            	var curEmail = "";
            	var curLastName = "";
            	var curUsername = "";

            	if(user.email)
            		curEmail = user.email.toLowerCase();
            	if(user.lastName)
            		curLastName = user.lastName.toLowerCase();
            	if(user.username)
            		curUsername = user.username.toLowerCase();

                return (
                    curEmail.toLowerCase().search($scope.text) > -1 ||
                    curLastName.toLowerCase().search($scope.text) > -1 ||
                    curUsername.toLowerCase().search($scope.text) > -1
                );
            };

            /**
             * @memberof ActivityCtrl
             *
             * @returns {Task[]} [tasks]
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

                if (!$scope.selectedUser)
                    return;

                // enable process filter since we got tasks therefore process definitions
                // also select the "showAll" option
                $scope.enableProcessFilter = true;
                $scope.groupFilter.definitionId = "showAll";

                processService.getUserActivity(dateAfterTime, dateBeforeTime, $scope.selectedUser.id).then(
                    // success callback
                    function (response) {
                        $scope.userTasks = response.data;
                        $scope.definitions = {};

                        $scope.definitions["showAll"] = $scope.definitions["showAll"] || {
                                id: "showAll",
                                title: "showAll"
                            };

                        $scope.userTasks.forEach(function (element) {

                            if (element.dueDate != null)
                                $scope.dueDate = $filter('date')(element.dueDate, "d/M/yyyy");

                            if (element.endDate != null)
                                $scope.endDate = $filter('date')(element.endDate, "d/M/yyyy");

                            $scope.startDate = $filter('date')(element.startDate, "d/M/yyyy");

                            // get the definitions from tasks
                            var group = (element.processId || "_empty_").toString();

                            if (!$scope.definitions.hasOwnProperty(group)) {
                                $scope.definitions[group] = $scope.definitions[group] || {
                                        id: element.processId,
                                        title: element.definitionName
                                    }
                            }
                        });

                        $scope.filteredTasks = $scope.userTasks;
                    },
                    // error callback
                    function (response) {

                    }
                );
            };

            /**
             * @memberof ActivityCtrl
             *
             * @desc Applies definition filter once tasks sent from the api
             *
             */
            $scope.filterTasksByDefinition = function () {
                var selectedDefinition = "" + $scope.groupFilter.definitionId;

                if (selectedDefinition.indexOf("showAll") >= 0) {
                    $scope.filteredTasks = $scope.userTasks;

                } else {
                    $scope.filteredTasks = $scope.userTasks.filter(function (e) {
                        return selectedDefinition.indexOf(e.processId + "") >= 0;
                    });
                }
            };

            /**
             * @memberof ActivityCtrl
             *
             * @desc Clears the date picker for the after date
             */
            $scope.clearDateAfter = function () {
                $scope.searchFilter.dateAfter = null;

                $scope.searchTasks();
            };

            /**
             * @memberof ActivityCtrl
             *
             * @desc Clears the date picker for the before date
             */
            $scope.clearDateBefore = function () {
                $scope.searchFilter.dateBefore = null;

                $scope.searchTasks();
            };

            /**
             * @memberof ActivityCtrl
             *
             * @desc Clears any filter
             */
            $scope.clearAllFilters = function () {
                $scope.searchFilter.dateAfter = null;
                $scope.searchFilter.dateBefore = null;

                $scope.searchText = null;

                if ($scope.selectedUser)
                    $scope.selectedUser.id = "";

                $scope.filteredTasks = [];

                $scope.enableProcessFilter = false;
                $scope.definitions = {};
            };

            /**
             * @memberof ActivityCtrl
             *
             * @desc Changes view to task's details
             *
             * @param {Task} task
             */
            $scope.goToDetails = function (task) {
                $scope.activeView = "taskDetails";
                $scope.task = task;
            };

            /**
             * @memberof ActivityCtrl
             *
             * @desc Go back to previous page
             */
            $scope.goBack = function () {
                $scope.activeView = "taskList";
            };

            /**
             * @memberof ActivityCtrl
             *
             * @desc Calculates difference in days between completed task date and due date
             *
             * @param task {Task}
             *
             * @returns {Number} The difference between the dates
             */
            $scope.taskDelay = function (task) {
                var diff;

                if (task.dueDate === null)
                    return Infinity;

                if (task.endDate) {
                    diff = task.dueDate - task.endDate;

                } else {
                    var completeDate = new Date();
                    diff = task.dueDate - completeDate.getDate();
                }

                var diffInDays = diff / (1000 * 3600 * 24);
                return diffInDays;
            };

            /**
             * @memberof ActivityCtrl
             * @desc Sorts the user tasks's based on the selection
             *
             * @param {String} optionId - Sorted by that option
             */
            $scope.sortBy = function (optionId) {
                $scope.orderByOption = "-" + optionId;
            };

            /**
             * @memberof ActivityCtrl
             * @desc Initializes all search criteria
             *
             */
            function initializeCriteria() {
                $scope.searchFilter.dateAfter = new Date();
                $scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth() - 3);

                $scope.searchFilter.dateBefore = new Date();
                $scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate() + 1);

                $scope.maxDateBefore = new Date();
                $scope.maxDateBefore.setDate($scope.maxDateBefore.getDate() + 1);

                $scope.groupFilter = { definitionId: null };
                $scope.enableProcessFilter = false;
            };
        }]);
})(angular);