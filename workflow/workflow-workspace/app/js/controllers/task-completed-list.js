define(['angular', 'services/process-service', 'util/core'],

	function (angular) {

		'use strict';

		function completedTasksCtrl($scope, $mdDialog, processService, cacheService, config) {

			$scope.imagePath = config.AVATARS_PATH;
			$scope.showProgress = true;

			// limit for date picker
			$scope.nextDay = new Date();
			$scope.nextDay.setDate($scope.nextDay.getDate() + 1);

			// holds the search parameters
			$scope.searchFilter = { dateAfter: null, dateBefore: null, instanceTitle: "", definitionId: null };

			// initialize variables
			$scope.instances = [];

			$scope.orderByOption = null;

			// order options
			$scope.options = [];
            $scope.sortOptions = { title: 'taskName', id: 'name' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'worker', id: 'assignee' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'processInstanceName', id: 'processInstance.title' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'process', id: 'definitionName' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'dueTo', id: 'dueDate' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'startDate', id: 'startDate' };
            $scope.options.push($scope.sortOptions);
            $scope.sortOptions = { title: 'complete', id: 'endDate' };
            $scope.options.push($scope.sortOptions);

			// get all active definitions in order to populate search filter
			processService.getActiveProcessDefinitions().then(
				// success callback
				function (response) {
					$scope.definitions = response.data;

					$scope.selectAllDefinitions = { name: "showAll", processDefinitionId: "all" };
					$scope.definitions.push($scope.selectAllDefinitions);

					$scope.initializeCriteria();
					$scope.showTasksByFilters();
				});

			/**
			 * Return processes definitions by selected definitions
			 */
			$scope.showTasksByFilters = function () {

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

				// the actual search for tasks
				processService.getSearchedUserTasks($scope.searchFilter.definitionId, $scope.searchFilter.instanceTitle, dateAfterTime, dateBeforeTime, "true").then(
					// success callback
					function (response) {
						$scope.tasks = response.data;

						tasksMapped = ArrayUtil.mapByProperty2innerProperty($scope.tasks, "processInstance", "id", "tasks");
						instanceIds = Object.keys(tasksMapped);

						instanceIds.forEach(function (item) {
							var task = tasksMapped[item]["tasks"][0];
							$scope.instances.push(task.processInstance);
						});


						// save criteria properties into the service
						cacheService.saveCriteria('completed', $scope.searchFilter);
					},
					//error callback
					function (response) {
						exceptionModal(response);
					}

				).finally(function () {
					$scope.showProgress = false;
				});
			};

			/**
			 * Initialize search criteria
			 */
			$scope.initializeCriteria = function () {

				var searchCriteria = cacheService.getCriteria("completed");

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

			$scope.clearAfterDate = function () {
				$scope.searchFilter.dateAfter = null;

				$scope.showTasksByFilters();
			};

			$scope.clearBeforeDate = function () {
				$scope.searchFilter.dateBefore = null;

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

		angular.module('wfWorkspaceControllers').controller('CompletedTasksCtrl', ['$scope', '$mdDialog', 'processService', 'cacheService', 'CONFIG', completedTasksCtrl]);
	}
);
