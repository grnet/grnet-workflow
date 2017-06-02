define(['angular', 'services/process-service'],

	function (angular) {

		'use strict';

		function taskActivityListCtrl($scope, $mdDialog, processService, cacheService, config) {

			$scope.imagePath = config.AVATARS_PATH;
			$scope.showProgressBar = true;


			// limit for date picker
			$scope.nextDay = new Date();
			$scope.nextDay.setDate($scope.nextDay.getDate() + 1);

			$scope.instances = [];
			$scope.tasks = null;
			$scope.definitions = null;
			$scope.instanceItems = null;
			$scope.searchFilter = { dateAfter: null, dateBefore: null, instanceTitle: "", definitionId: null };

			$scope.orderByOption = null;

            $scope.options = [];
            $scope.sortOptions = { title: 'process', id: 'definitionName' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'taskName', id: 'name' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'processInstanceName', id: 'processInstance.title' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'dueTo', id: 'dueDate' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'startDate', id: 'startDate' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'complete', id: 'endDate' };
            $scope.options.push($scope.sortOptions);

			/**
			 * Get completed definitions for user
			 */
			processService.getActiveProcessDefinitions().then(
				// success callback
				function (response) {
					$scope.definitions = response.data;

					$scope.selectAllDefinitions = { name: "showAll", processDefinitionId: "all" };
					$scope.definitions.push($scope.selectAllDefinitions);

					$scope.initializeCriteria();
					$scope.showTasksByFilters();
				}).finally(function () {
					$scope.showProgressBar = false;
				});

			/**
			 * Returns the difference between due date and end date of task
			 */
			$scope.taskEndedDelay = function (endDate, dueDate) {
				if (endDate === null)
					return Infinity;

				var diff = endDate - dueDate.getTime();
				var diffInDays = diff / (1000 * 3600 * 24);

				return diffInDays;
			};


			/**
			 * Return processes definitions by selected definitions
			 */
			$scope.showTasksByFilters = function () {

				$scope.showProgressBar = true;

				var instanceIds = [];
				var historyTasks = [];
				var tasksMapped = {};

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

				processService.getSearchedUserTasks($scope.searchFilter.definitionId, $scope.searchFilter.instanceTitle, dateAfterTime, dateBeforeTime, "false").then(
					function (response) {
						$scope.tasks = response.data;

						tasksMapped = ArrayUtil.mapByProperty2innerProperty($scope.tasks, "processInstance", "id", "tasks");
						instanceIds = Object.keys(tasksMapped);

						instanceIds.forEach(function (item) {
							var task = tasksMapped[item]["tasks"][0];
							$scope.instances.push(task.processInstance);
						});

						// save criteria properties into the service
						cacheService.saveCriteria('activity', $scope.searchFilter);
					},

					function (response) {
						exceptionModal(response);

					}).finally(function () {
						$scope.showProgressBar = false;
					});
			};

			/**
			 * Initialize criteria
			 */
			$scope.initializeCriteria = function () {

				var searchCriteria = cacheService.getCriteria("activity");

				if (searchCriteria != null) {
					$scope.searchFilter = searchCriteria;

				} else {

					$scope.searchFilter.dateBefore = new Date();
					$scope.searchFilter.dateAfter = new Date();
					$scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth() - 3);
					$scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate() + 1);
					$scope.searchFilter.definitionId = "all";
					$scope.searchFilter.instanceTitle = "";
				}
			};

			/**
			 * Clear datepicker for date after
			 */
			$scope.clearAfterDate = function () {

				$scope.searchFilter.dateAfter = null;

				$scope.showTasksByFilters();
			};

			/**
			 * Clear datepicker for date before
			 */
			$scope.clearBeforeDate = function () {

				$scope.searchFilter.dateBefore = null;

				$scope.showTasksByFilters();
			};

            /**
             * Clear the instance title filter
             */
            $scope.clearInstanceTitle = function () {
                $scope.searchFilter.instanceTitle = "";
                $scope.showTasksByFilters();
            };

            /**
             * @memberof InProgressCtrl
             * @desc Clears any filter
             *
             */
            $scope.clearAllFilters = function () {
                $scope.searchFilter.dateAfter = 0;
                $scope.searchFilter.dateBefore = 0;
                $scope.searchFilter.instanceTitle = "";
                $scope.searchFilter.definitionId = "all";

                $scope.showTasksByFilters();
            };

            /**
			 * Sorting function
			 */
			$scope.sortBy = function (optionId) {
				$scope.orderByOption = optionId;
			};

			$scope.print = function () {
				window.print();
			};

			/**
			 * Exception modal
			 */
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
				});
			}

		}

		angular.module('wfWorkspaceControllers').controller('TaskActivityListCtrl', ['$scope', '$mdDialog', 'processService', 'cacheService', 'CONFIG', taskActivityListCtrl]);

	}
);
