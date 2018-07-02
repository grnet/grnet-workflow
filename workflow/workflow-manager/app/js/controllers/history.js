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
            $scope.sortOption = { title: 'supervisor', id: 'supervisor' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'executionName', id: 'title' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'process', id: 'definitionName' };
            $scope.sortOptions.push($scope.sortOption);
            $scope.sortOption = { title: 'endDate', id: 'endDate' };
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
                $scope.searchFilter.dateBefore = new Date();
                $scope.searchFilter.dateAfter = new Date();
                $scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth() - 3);
                $scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate() + 1);
                $scope.searchFilter.instanceTitle = "";
                $scope.searchFilter.definitionId = "all";
            }

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