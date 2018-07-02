define(['angular', 'services/processservice'],

	function (angular) {

		'use strict';

		function activityCtrl($scope, $filter, processService, cacheService, config) {

			// constance variables in order to get images and documents url
			$scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;
			$scope.imagePath = config.AVATARS_PATH;

			// initialize view to task list
			$scope.activeView = 'taskList';

			// limit for date picker
			$scope.nextDay = new Date();
			$scope.nextDay.setDate($scope.nextDay.getDate() + 1);

			// search filter object
            $scope.searchFilter = { dateAfter: null, dateBefore: null, user: null };

			// initialize search criteria
			initializeCriteria();

			$scope.options = [];
			$scope.orderByOption = null;

            $scope.sortOption = { title: 'taskName', id: 'name' };
            $scope.options.push($scope.sortOption);
            $scope.sortOption = { title: 'processDetail', id: 'definitionName' };
            $scope.options.push($scope.sortOption);
            $scope.sortOption = { title: 'processInstanceName', id: 'processInstance.title' };
            $scope.options.push($scope.sortOption);
            $scope.sortOption = { title: 'startDate', id: 'startDate' };
            $scope.options.push($scope.sortOption);
            $scope.sortOption = { title: 'endDate', id: 'endDate' };
            $scope.options.push($scope.sortOption);

            // get users in order to fill autocomplete
			processService.getUsers().then(
				// success callback
				function (response) {
					$scope.users = response.data;
                    $scope.usersLoaded = true;
                }
			);

			/**
			 * Used by autocomplete in order to search for users
			 * 
			 * @param searchText {String}
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
				$scope.groupFilter.definitionId = 'showAll';

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

			$scope.print = function () {
				window.print();
			};

			$scope.filterTasksByDefinition = function () {
				var selectedDefinition = '' + $scope.groupFilter.definitionId;

				if (selectedDefinition.indexOf('showAll') >= 0) {
					$scope.filteredTasks = $scope.userTasks;

				} else {
					$scope.filteredTasks = $scope.userTasks.filter(function (e) {
						return selectedDefinition.indexOf(e.processId + '') >= 0;
					});
				}
			};

			$scope.clearDateAfter = function () {
				$scope.searchFilter.dateAfter = null;

				$scope.searchTasks();
			};

			$scope.clearDateBefore = function () {
				$scope.searchFilter.dateBefore = null;

				$scope.searchTasks();
			};

			$scope.clearAllFilters = function () {
				$scope.searchFilter.dateAfter = null;
				$scope.searchFilter.dateBefore = null;

                $scope.selectedUser = null;
				$scope.searchText = null;

				$scope.searchTasks();

				$scope.enableProcessFilter = false;
				$scope.definitions = {};
			};

			/**
			 * Changes view to task's details
			 * 
			 * @param task {Task}
			 */
			$scope.goToDetails = function (taskId) {
				$scope.activeView = 'taskDetails';

				processService.getTask(taskId).then(
					function (response) {
						$scope.task = response.data;

						// check if task completed after due date
						var diff;

						if ($scope.task.dueDate === null)
							return Infinity;

						if ($scope.task.endDate) {
							diff = $scope.task.dueDate - $scope.task.endDate;

						} else {
							var completeDate = new Date();
							diff = $scope.task.dueDate - completeDate.getDate();
						}

						$scope.diffInDays = diff / (1000 * 3600 * 24);
					}
				);
			};

			/**
			 * Changes view to task list
			 */
			$scope.goBack = function () {
				$scope.activeView = 'taskList';
			};

			/**
			  * Sorting function
			  */
			$scope.sortBy = function (optionId) {
                $scope.orderByOption = "-" + optionId;
			};

			function initializeCriteria() {

				var searchCriteria = cacheService.getCriteria("activity");

				if (searchCriteria != null) {
					$scope.searchFilter = searchCriteria;
				} else {

					$scope.searchFilter.dateAfter = new Date();
					$scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth() - 3);

					$scope.searchFilter.dateBefore = new Date();
					$scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate() + 1);

					$scope.maxDateBefore = new Date();
					$scope.maxDateBefore.setDate($scope.maxDateBefore.getDate() + 1);

					$scope.groupFilter = { definitionId: null };
					$scope.enableProcessFilter = false;
				}
			}

		}
		angular.module('wfManagerControllers').controller('ActivityCtrl', ['$scope', '$filter', 'processService', 'cacheService', 'CONFIG', activityCtrl]);
	}
);