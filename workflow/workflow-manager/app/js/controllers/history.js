(function () {

	'use strict';

	angular.module('wfmanagerControllers').controller('HistoryCtrl', ['$scope', '$location', 'processService', 'CONFIG',
		/**
		 * @name HistoryCtrl
		 * @ngDoc controllers
		 * @memberof wfmanagerControllers
		 * 
		 * @desc History controller for history view
		 * 
		 */
		function ($scope, $location, processService, config) {

			// Constance variable in order to get images
			$scope.imagePath = config.AVATARS_PATH;

			$scope.maxDateBefore = new Date();
			$scope.maxDateBefore.setDate($scope.maxDateBefore.getDate() + 1);

			// search filter object
			$scope.searchFilter = { dateAfter: null, dateBefore: null, instanceTitle: "" };

			// initialize search criteria
			initializeCriteria();

			/**
			 * @memberof HistoryCtrl
			 * @desc Searches for instances based on given criteria
			 * 
			 */
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
						$location.search('instanceTitle', $scope.searchFilter.instanceTitle);
						$location.search('dateAfter', dateAfterTime);
						$location.search('dateBefore', dateBeforeTime);

						var tasks = response.data;
						var tasksMapped = ArrayUtil.mapByProperty2innerProperty(tasks, "processInstance", "id", "tasks");
						var instanceIds = Object.keys(tasksMapped);
						$scope.endedInstances = [];

						instanceIds.forEach(function (item) {
							var task = tasksMapped[item]["tasks"][0];
							$scope.endedInstances.push(task.processInstance);
						});
					},
					// error callback
					function (response) {

					}
				);
			};

			// search for instances
			$scope.searchInstances();

			/**
			 * @memberof HistoryCtrl
			 * @desc Clears the date picker for the after date
			 * 
			 */
			$scope.clearDateAfter = function () {
				$scope.searchFilter.dateAfter = 0;
				$scope.searchInstances();
			};

			/**
			 * @memberof HistoryCtrl
			 * @desc Clears the date picker for the before date
			 * 
			 */
			$scope.clearDateBefore = function () {
				$scope.searchFilter.dateBefore = 0;
				$scope.searchInstances();
			};

			/**
			 * @memberof HistoryCtrl
			 * @desc Clears the instance title filter
			 * 
			 */
			$scope.clearInstanceTitle = function () {
				$scope.searchFilter.instanceTitle = "";
				$scope.searchInstances();
			};

			/**
			 * @memberof HistoryCtrl
			 * @desc Clears any filter
			 * 
			 */
			$scope.clearAllFilters = function () {
				$scope.searchFilter.dateAfter = 0;
				$scope.searchFilter.dateBefore = 0;
				$scope.searchFilter.instanceTitle = "";

				$scope.searchInstances();
			};

			/**
			 * @memberof HistoryCtrl
			 * @descr Initializes all search criteria
			 * 
			 */
			function initializeCriteria() {
				if (!$location.search().dateAfter) {
					$scope.searchFilter.dateAfter = new Date();
					$scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth() - 3);

					$location.search('dateAfter', $scope.searchFilter.dateAfter.getTime());

				} else
					$scope.searchFilter.dateAfter = new Date(parseFloat($location.search().dateAfter));

				if (!$location.search().dateBefore) {
					$scope.searchFilter.dateBefore = new Date();

					// plus one day
					$scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate() + 1);

					// set to url as parameter
					$location.search('dateBefore', $scope.searchFilter.dateBefore.getTime());

				} else
					$scope.searchFilter.dateBefore = new Date(parseFloat($location.search().dateBefore));

				if (!$location.search().instanceTitle)
					$location.search('instanceTitle', "");
				else
					$scope.searchFilter.instanceTitle = $location.search().instanceTitle;
			};

		}]);
})(angular);