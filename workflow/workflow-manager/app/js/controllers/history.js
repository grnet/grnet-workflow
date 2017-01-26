(function () {
	'use strict';

	angular.module('wfmanagerControllers').controller('HistoryCtrl', ['$scope', '$location', '$mdDialog', 'processService', 'CONFIG',
		/**
		 * @name HistoryCtrl
		 * @ngDoc controllers
		 * @memberof wfmanagerControllers
		 * 
		 * @desc History controller for history view
		 * 
		 */
		function ($scope, $location, $mdDialog, processService, config) {

			$scope.imagePath = config.AVATARS_PATH;

			$scope.maxDateBefore = new Date();
			$scope.maxDateBefore.setDate($scope.maxDateBefore.getDate() + 1);

            $scope.endedInstances = [];
			// search filter object
            $scope.searchFilter = { dateAfter: null, dateBefore: null, instanceTitle: "", definitionId: "" };

            $scope.orderByOption = null;

            $scope.sortOptions = [];

            $scope.sortOption = { title: 'status', id: 'status' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'supervisor', id: 'supervisor' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'executionName', id: 'title' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'processDetail', id: 'definitionName' };
            $scope.sortOptions.push($scope.sortOption);

            processService.getActiveProcesses().then(
                // success callback
                function (response) {
                    $scope.definitions = response.data;

                    $scope.selectAllDefinitions = { name: "showAll", processDefinitionId: "all" };
                    $scope.definitions.push($scope.selectAllDefinitions);

                    initializeCriteria();
                    $scope.searchInstances();
                },
                // error callback
                function (response) {
                    exceptionModal(response);
                });

            /**
             * @memberof HistoryCtrl
             * @descr Initializes all search criteria
             *
             */
            function initializeCriteria() {
                $location.url($location.path());
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

                if (!$location.search().instanceTitle)
                    $location.search('instanceTitle', "");
                else
                    $scope.searchFilter.instanceTitle = $location.search().instanceTitle;

                if (!$location.search().definitionId)
                    $location.search('definitionId', "all");
                else
                    $scope.searchFilter.definitionId = $location.search().definitionId;
            };

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

                if (!$scope.searchFilter.definitionId)
                    $scope.searchFilter.definitionId = "all";

                if (!$scope.searchFilter.instanceTitle)
                    $scope.searchFilter.instanceTitle = "";

                processService.getEndedInstances($scope.searchFilter.definitionId,$scope.searchFilter.instanceTitle, dateAfterTime, dateBeforeTime).then(
                    // success callback
                    function (response) {
                        $location.search('definitionId', $scope.searchFilter.definitionId);
                        $location.search('instanceTitle', $scope.searchFilter.instanceTitle);
                        $location.search('dateAfter', dateAfterTime);
                        $location.search('dateBefore', dateBeforeTime);

                        var instances = response.data;
                        var tasksMapped = ArrayUtil.mapByProperty2Property(instances, "id", "instances");
                        var instanceIds = Object.keys(tasksMapped);

                        $scope.endedInstances = [];

                        instanceIds.forEach(function (item) {
                            var instance = tasksMapped[item]["instances"][0];
                            $scope.endedInstances.push(instance);
                        });
                    },
                    // error callback
                    function (response) {
                        exceptionModal(response);
                    }
                );
            };

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
             * @desc Sorting tasks by given option
             *
             * @param {String} optionId
             */
            $scope.sortBy = function (optionId) {
                $scope.orderByOption = optionId;
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
                $scope.searchFilter.definitionId = "all";

				$scope.searchInstances();
			};

            /**
             * @memberof HistoryCtrl
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