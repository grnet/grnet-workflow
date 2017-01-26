(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers').controller('TaskActivityListCtrl', ['$scope', '$location', '$mdDialog', 'processService', 'CONFIG',
		/**
		 * @name TaskActivityListCtrl
		 * @ngDoc controllers
		 * @memberof wfworkspaceControllers
		 * 
		 * @desc Controller used by Task activity view
		 */
		function ($scope, $location, $mdDialog, processService, config) {

			$scope.currentDate = new Date();
			$scope.currentDate.setDate($scope.currentDate.getDate() + 1);

			$scope.imagePath = config.AVATARS_PATH;
			$scope.showProgressBar = true;

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
			 * @memberof TaskActivityListCtrl
			 * @desc Returns the difference between due date and end date of task
			 * 
			 * @param {Date} endDate
			 * @param {Date} dueDate
			 * @returns
			 */
			$scope.taskEndedDelay = function (endDate, dueDate) {

				if (endDate === null)
					return Infinity;

				var diff = endDate - dueDate.getTime();
				var diffInDays = diff / (1000 * 3600 * 24);

				return diffInDays;
			};

			/**
			 * @memberof TaskActivityListCtrl
			 * @desc Shows tasks according to given filters
			 */
			$scope.showTasksByFilters = function () {
				$scope.showProgressBar = true;

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

				processService.getSearchedUserTasks($scope.searchFilter.definitionId, $scope.searchFilter.instanceTitle, dateAfterTime, dateBeforeTime, "false").then(
					function (response) {
						$scope.tasks = response.data;

						$location.search('definitionId', $scope.searchFilter.definitionId);
						$location.search('instanceTitle', $scope.searchFilter.instanceTitle);
						$location.search('dateAfter', dateAfterTime);
						$location.search('dateBefore', dateBeforeTime);

						tasksMapped = ArrayUtil.mapByProperty2innerProperty($scope.tasks, "processInstance", "id", "tasks");
						instanceIds = Object.keys(tasksMapped);

						instanceIds.forEach(function (item) {
							var task = tasksMapped[item]["tasks"][0];
							$scope.instances.push(task.processInstance);
						});
					},
					function (response) {
						exceptionModal(response);
					}
				).finally(function () {
					$scope.showProgressBar = false;
				});
			};

			/**
			 * @memberof TaskActivityListCtrl
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
			 * @memberof TaskActivityListCtrl
			 * @desc Clears the "After date" filter
			 */
			$scope.clearAfterDate = function () {
				$scope.searchFilter.dateAfter = null;

				$scope.showTasksByFilters();
			};

			/**
			 * @memberof TaskActivityListCtrl
			 * @desc Clears the "Before date" filter
			 */
			$scope.clearBeforeDate = function () {
				$scope.searchFilter.dateBefore = null;

				$scope.showTasksByFilters();
			};

			/**
			 * @memberof TaskActivityListCtrl
			 * @desc Clears the "Instance title" filter
			 */
			$scope.clearInstanceTitle = function () {
				$scope.searchFilter.instanceTitle = "";

				$scope.showTasksByFilters();
			};

			/**
			 * @memberof TaskActivityListCtrl
			 * @desc Sorts the tasks by given option
			 */
			$scope.sortBy = function (optionId) {
				$scope.orderByOption = optionId;
			};

			/**
			 * @memberof TaskActivityListCtrl
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
			 * @memberof TaskActivityListCtrl
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
