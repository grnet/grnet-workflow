(function (angular) {
	'use strict';

    angular.module('wfworkspaceControllers').controller('CompletedTasksCtrl', ['$scope', '$location', '$mdDialog', 'processService', 'CONFIG',
        /**
         * @name CompletedTasksCtrl
         * @ngDoc controllers
         * @memberof wfworkspaceControllers
         * 
         * @desc Controller for the Completed tasks view
         */
		function ($scope, $location, $mdDialog, processService, config) {

			$scope.imagePath = config.AVATARS_PATH;
			$scope.showProgress = true;

			$scope.currentDate = new Date();
			$scope.currentDate.setDate($scope.currentDate.getDate() + 1);

			$scope.instances = [];
			$scope.tasks = null;
			$scope.definitions = null;
			$scope.searchFilter = { dateAfter: null, dateBefore: null, instanceTitle: "", definitionId: null };
			$scope.orderByOption = null;

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

			processService.getActiveProcessDefinitions().then(
				// success callback
				function (response) {
					$scope.definitions = response.data;

					$scope.selectAllDefinitions = { name: "showAll", processDefinitionId: "all" };
					$scope.definitions.push($scope.selectAllDefinitions);

					$scope.initializeCriteria();
					$scope.showTasksByFilters();
				}
			);

			/**
			 * @memberof CompletedTasksCtrl
			 * @desc Shows tasks by given criteria
			 */
			$scope.showTasksByFilters = function () {
				$scope.showProgress = true;

				var dateAfterTime;
				var dateBeforeTime;
				var instanceIds = [];
				var historyTasks = [];
				var tasksMapped = {};

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

				if (!$scope.searchFilter.instanceTitle)
					$scope.searchFilter.instanceTitle = "";

				processService.getSearchedUserTasks($scope.searchFilter.definitionId, $scope.searchFilter.instanceTitle, dateAfterTime, dateBeforeTime, "true").then(
					function (response) {
						$location.search('definitionId', $scope.searchFilter.definitionId);
						$location.search('instanceTitle', $scope.searchFilter.instanceTitle);
						$location.search('dateAfter', dateAfterTime);
						$location.search('dateBefore', dateBeforeTime);

						$scope.tasks = response.data;

						tasksMapped = ArrayUtil.mapByProperty2innerProperty($scope.tasks, "processInstance", "id", "tasks");
						instanceIds = Object.keys(tasksMapped);

						instanceIds.forEach(function (item) {
							var task = tasksMapped[item]["tasks"][0];
							$scope.instances.push(task.processInstance);
						});
					},
					//error callback
					function (response) {
						exceptionModal(response);
					}).finally(function () {
						$scope.showProgress = false;
					});
			};

			/**
			 * @memberof CompletedTasksCtrl
			 * @desc Initialize search criteria
			 */
			$scope.initializeCriteria = function () {
				$scope.searchFilter.definitionId = $location.search().definitionId;
				$scope.searchFilter.instanceTitle = $location.search().instanceTitle;

				if (!$location.search().dateAfter || $location.search().dateAfter == 0) {
					$scope.searchFilter.dateAfter = new Date();
					$scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth() - 3);
					$location.search('dateAfter', $scope.searchFilter.dateAfter.getTime());

				} else
					$scope.searchFilter.dateAfter = new Date(parseFloat($location.search().dateAfter));

				if (!$location.search().dateBefore || $location.search().dateBefore == 0) {
					$scope.searchFilter.dateBefore = new Date();
					$scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate() + 1);
					$location.search('dateBefore', $scope.searchFilter.dateBefore.getTime());

				} else
					$scope.searchFilter.dateBefore = new Date(parseFloat($location.search().dateBefore));
			};

			/**
			 * @memberof CompletedTasksCtrl
			 * @desc Clears the "After date" filter
			 */
			$scope.clearAfterDate = function () {
				$scope.searchFilter.dateAfter = null;

				$scope.showTasksByFilters();
			};

			/**
			 * @memberof CompletedTasksCtrl
			 * @desc Clears the "Before date" filter
			 */
			$scope.clearBeforeDate = function () {
				$scope.searchFilter.dateBefore = null;

				$scope.showTasksByFilters();
			};

			/**
			 * @memberof CompletedTasksCtrl
			 * @desc Clears the "Instance title" filter
			 */
			$scope.clearInstanceTitle = function () {
				$scope.searchFilter.instanceTitle = "";

				$scope.showTasksByFilters();
			};

			/**
			 * @memberof CompletedTasksCtrl
			 * @desc Sorting tasks by given option
			 * 
			 * @param {String} optionId
			 */
			$scope.sortBy = function (optionId) {
				$scope.orderByOption = optionId;
			};

			/**
			 * @memberof CompletedTasksCtrl
			 * @desc Clears all filters
			 */
			$scope.clearAllFilters = function () {
				$scope.searchFilter.definitionId = "all";
				$scope.searchFilter.instanceTitle = "";
				$scope.searchFilter.dateBefore = null;
				$scope.searchFilter.dateAfter = null;

				$scope.showTasksByFilters();
			};

            /**
             * @memberof CompletedTasksCtrl
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

		}]);
})(angular);
