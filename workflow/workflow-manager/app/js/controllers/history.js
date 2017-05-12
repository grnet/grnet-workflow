define(['angular', 'services/processservice', 'util/core'],

	function (angular) {

		'use strict';

		function historyCtrl($scope, $mdDialog, processService, cacheService, config) {

			// Constance variable in order to get images
			$scope.imagePath = config.AVATARS_PATH;

			$scope.maxDateBefore = new Date();
			$scope.maxDateBefore.setDate($scope.maxDateBefore.getDate() + 1);

			// limit for date picker
			$scope.nextDay = new Date();
			$scope.nextDay.setDate($scope.nextDay.getDate() + 1);

			// search filter object
			$scope.searchFilter = { 'dateAfter': null, dateBefore: null, instanceTitle: "" };

			// initialize search criteria
			initializeCriteria();

			$scope.searchInstances = function () {
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

				processService.getEndedInstancesTasks($scope.searchFilter.instanceTitle, dateAfterTime, dateBeforeTime, true).then(
					// success callback
					function (response) {

						var tasks = response.data;
						var tasksMapped = ArrayUtil.mapByProperty2innerProperty(tasks, "processInstance", "id", "tasks");
						var instanceIds = Object.keys(tasksMapped);
						$scope.endedInstances = [];

						instanceIds.forEach(function (item) {
							var task = tasksMapped[item]["tasks"][0];
							$scope.endedInstances.push(task.processInstance);
						});

						// save criteria properties into the service
						cacheService.saveCriteria('history', $scope.searchFilter);
					},
					// error callback
					function (response) {

					}
				);
			};

			// search for instances
			$scope.searchInstances();

			$scope.clearDateAfter = function () {
				$scope.searchFilter.dateAfter = null;
				console.log("date-after:", $scope.searchFilter.dateAfter);
						
				$scope.searchInstances();
			};

			$scope.clearDateBefore = function () {
				$scope.searchFilter.dateBefore = null;
				
				$scope.searchInstances();
			};

			$scope.clearInstanceTitle = function () {
				$scope.searchFilter.instanceTitle = "";

				$scope.searchInstances();
			};

			$scope.clearAllFilters = function () {
				$scope.searchFilter.dateAfter = null;
				$scope.searchFilter.dateBefore = null;
				$scope.searchFilter.instanceTitle = "";

				$scope.searchInstances();
			};

			$scope.filteringOptions = function (event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog) {

						$scope.cancel = function () {
							$scope.searchInstances();
							$mdDialog.hide();
						};
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/filterHistory.tmpl.html',
					parent: angular.element(document.body),
					targetEvent: event
				});
			};

			$scope.print = function () {
				window.print();
			};

			function initializeCriteria() {

				var searchCriteria = cacheService.getCriteria("history");

				if (searchCriteria != null) {
					$scope.searchFilter = searchCriteria;

				} else {
					$scope.searchFilter.dateAfter = new Date();
					$scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth() - 3);
					$scope.searchFilter.dateBefore = new Date();
				}
			};
		}

		angular.module('wfManagerControllers').controller('HistoryCtrl', ['$scope', '$mdDialog', 'processService', 'cacheService', 'CONFIG', historyCtrl]);
	}
);