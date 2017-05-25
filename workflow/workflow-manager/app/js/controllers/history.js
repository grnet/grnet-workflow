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

            $scope.endedInstances = [];

            // search filter object
            $scope.searchFilter = { dateAfter: null, dateBefore: null, instanceTitle: "", definitionId: null };

            $scope.orderByOption = null;

            $scope.sortOptions = [];

            $scope.sortOption = { title: 'status', id: 'status' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'worker', id: 'supervisor' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'executionName', id: 'title' };
            $scope.sortOptions.push($scope.sortOption);

            // initialize search criteria
			initializeCriteria();

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
                    $location.search('definitionId', "");
                else
                    $scope.searchFilter.definitionId = $location.search().definitionId;
            };

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
                        exceptionModal(response);
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

            /**
             * @memberof HistoryCtrl
             * @desc Sorting tasks by given option
             *
             * @param {String} optionId
             */
            $scope.sortBy = function (optionId) {
                $scope.orderByOption = optionId;
            };

            $scope.clearAllFilters = function () {
				$scope.searchFilter.dateAfter = null;
				$scope.searchFilter.dateBefore = null;
				$scope.searchFilter.instanceTitle = "";
                $scope.searchFilter.definitionId = "all";

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
        }

		angular.module('wfManagerControllers').controller('HistoryCtrl', ['$scope', '$mdDialog', 'processService', 'cacheService', 'CONFIG', historyCtrl]);
	}
);