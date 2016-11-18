(function (angular) {

	'use strict';

    angular.module('wfworkspaceControllers').controller('TaskAssignListCtrl', ['$scope', '$location', '$mdDialog', '$filter', 'processService', 'CONFIG',
		/**
		 * @name TaskAssignListCtrl
		 * @ngDoc controllers
		 * @memberof wfworkspaceControllers
		 * 
		 * @desc Controller used by Task assign list view
		 */
		function ($scope, $location, $mdDialog, $filter, processService, config) {

			$scope.imagePath = config.AVATARS_PATH;
			$scope.status = { selectAll: true };

			$scope.tasks = null;
			$scope.assignedTasks = null;
			$scope.unAssignedTasks = null;
			$scope.assignedFiltered = null;
			$scope.unAssignedFiltered = null;
			$scope.processes = null;

			$scope.sortOptions = { title: null, id: null };
			$scope.orderByOption = null;
			$scope.showProgressBar = true;

			/**
			 * @memberOf TaskAssignListCtrl
			 * @desc Returns the difference between due date(if present) and current date
			 * 
			 * @param {Task} task
			 * @returns {Number} - Difference in days
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
			 * Get all supervised tasks
			 */
			processService.getSupervisedTasks().then(
				//success
				function (response) {
					$scope.tasks = response.data;

					if (response.data.length == 0) {
						$mdDialog.show($mdDialog.alert()
							.parent(document.body)
							.title($filter('translate')('noAvailableTasks'))
							.content($filter('translate')('noAvailableTasks'))
							.ok($filter('translate')('confirm')))
						$location.path('/task');
					}

					//separate assigned tasks from list
					$scope.assignedTasks = $scope.tasks.filter(function (element) { return element.assignee });

					//separate unassigned tasks from list
					$scope.unAssignedTasks = $scope.tasks.filter(function (element) { return !element.assignee });

					$scope.processes = {};
					$scope.tasks.forEach(function (o) {
						var group = (o.processId || "_empty_").toString();
						if (!$scope.processes.hasOwnProperty(group)) {
							$scope.processes[group] = $scope.processes[group] || {
								id: o.processId,
								title: o.definitionName,
								selected: true
							}
						}
					});

					//filter list with selected definitions
					//in this case all available definitions are selected
					$scope.updateFilteredTasks();
				},
				//fail
				function (response) {
					$mdDialog.show({
						controller: function ($scope, $mdDialog, error) {
							$scope.error = error;

							$scope.cancel = function () {
								$mdDialog.hide();
							};
						},
						scope: $scope,
						preserveScope: true,
						templateUrl: 'templates/exception.tmpl.html',
						parent: angular.element(document.body),
						targetEvent: event,
						locals: {
							'error': response.data
						}
					})

				}).finally(function () {
					$scope.showProgressBar = false;
				});

			/**
			 * @memberOf TaskAssignListCtrl
			 * @desc Selects/De-selects all process definitions 
			 */
			$scope.selectAll = function () {

				if ($scope.status.selectAll === true) {
					$scope.assignedFiltered = $scope.assignedTasks;
					$scope.unAssignedFiltered = $scope.unAssignedTasks;

					//Select all
					for (var key in $scope.processes) {
						if ($scope.processes.hasOwnProperty(key)) {
							$scope.processes[key].selected = true;
						}
					}
				}
				else {
					$scope.assignedFiltered = [];
					$scope.unAssignedFiltered = [];

					//De-select all
					for (var key in $scope.processes) {
						if ($scope.processes.hasOwnProperty(key)) {
							$scope.processes[key].selected = false;
						}
					}
				}
			};

			/**
			 * @memberOf TaskAssignListCtrl
			 * @desc Filter assigned/unassigned lists by selected definitions 
			 */
			$scope.updateFilteredTasks = function () {

				var selectedDefinitions = Object.keys($scope.processes)
					.filter(function (e) { return $scope.processes[e].selected === true; });

				$scope.assignedFiltered = $scope.assignedTasks.filter(function (e) {
					return selectedDefinitions.indexOf(e.processId + "") >= 0;
				});

				$scope.unAssignedFiltered = $scope.unAssignedTasks.filter(function (e) {
					return selectedDefinitions.indexOf(e.processId + "") >= 0;
				});
			};

			/**
			 * @memberOf TaskAssignListCtrl
			 * @desc Tab change event (each tab has its own sorting options)
			 * 
			 * @param {String} tab
			 */
			$scope.onTabSelected = function (tab) {
				$scope.options = [];

				if (tab == 'new') {
					$scope.sortOptions = { title: 'dueTo', id: 'dueDate' };
					$scope.options.push($scope.sortOptions);

					$scope.sortOptions = { title: 'taskName', id: 'name' };
					$scope.options.push($scope.sortOptions);

				} else if (tab == 'assigned') {
					$scope.sortOptions = { title: 'dueTo', id: 'dueDate' };
					$scope.options.push($scope.sortOptions);

					$scope.sortOptions = { title: 'taskName', id: 'name' };
					$scope.options.push($scope.sortOptions);

					$scope.sortOptions = { title: 'worker', id: 'assignee' };
					$scope.options.push($scope.sortOptions);
				}
			};

			/**
			 * @memberOf TaskAssignListCtrl
			 * @desc Sorts tasks by given option
			 * 
			 * @param {String} optionId
			 */
			$scope.sortBy = function (optionId) {
				$scope.orderByOption = optionId;
			};

		}]);
})(angular);
