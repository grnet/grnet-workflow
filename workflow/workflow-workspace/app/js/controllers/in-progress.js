define(['angular', 'services/process-service'],

	function (angular) {

		'use strict';

		function inProgressCtrl($scope, $mdDialog, processService, config) {

			$scope.imagePath = config.AVATARS_PATH;
			$scope.showProgress = true;

            $scope.activeMaxDateBefore = new Date();
            $scope.activeMaxDateBefore.setDate($scope.activeMaxDateBefore.getDate() + 1);

            $scope.inProgressInstances = [];
            // search filter object
            $scope.searchFilter = { dateAfter: null, dateBefore: null, instanceTitle: "", definitionId: "" };

            $scope.activeOrderByOption = null;

            $scope.activeSortOptions = [];

            var sortOption = { title: 'process', id: 'definitionName' };
            $scope.activeSortOptions.push(sortOption);
            sortOption = { title: 'processInstanceName', id: 'title' };
            $scope.activeSortOptions.push(sortOption);
            sortOption = { title: 'supervisor', id: 'supervisor' };
            $scope.activeSortOptions.push(sortOption);
            sortOption = { title: 'startDate', id: 'startDate' };
            $scope.activeSortOptions.push(sortOption);

            processService.getActiveProcessDefinitions().then(
				//success callback
				function (response) {
                    $scope.activeDefinitions = response.data;

                    $scope.selectAllActiveDefinitions = { name: "showAll", processDefinitionId: "all" };
                    $scope.activeDefinitions.push($scope.selectAllActiveDefinitions);

                    initializeCriteria();
                    $scope.searchInProgressInstances();
				}, function (response) {
                    exceptionModal(response);
				}).finally(function () {
					$scope.showProgress = false;
				});

            function initializeCriteria() {
                $scope.searchFilter.dateBefore = new Date();
                $scope.searchFilter.dateAfter = new Date();
                $scope.searchFilter.dateAfter.setMonth($scope.searchFilter.dateAfter.getMonth() - 3);
                $scope.searchFilter.dateBefore.setDate($scope.searchFilter.dateBefore.getDate() + 1);
                $scope.searchFilter.instanceTitle = "";
                $scope.searchFilter.definitionId = "all";
            }

            $scope.searchInProgressInstances = function () {
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

                processService.getInProgressInstances($scope.searchFilter.definitionId, $scope.searchFilter.instanceTitle, dateAfterTime, dateBeforeTime).then(
                    // success callback
                    function (response) {
                        var instances = response.data;
                        var tasksMapped = ArrayUtil.mapByProperty2Property(instances, "id", "instances");
                        var instanceIds = Object.keys(tasksMapped);

                        $scope.inProgressInstances = [];

                        instanceIds.forEach(function (item) {
                            var instance = tasksMapped[item]["instances"][0];
                            if($scope.activeDefinitions.map(function(a) {return a.name;}).indexOf(instance.definitionName) >= 0)
                                $scope.inProgressInstances.push(instance);
                        });
                    },
                    // error callback
                    function (response) {
                        exceptionModal(response);
                    }
                );
            };

			$scope.print = function() {
				window.print();
			};

            /**
             * @memberof InProgressCtrl
             * @desc Clears the date picker for the after date
             *
             */
            $scope.activeClearDateAfter = function () {
                $scope.searchFilter.dateAfter = 0;
                $scope.searchInProgressInstances();
            };

            /**
             * @memberof InProgressCtrl
             * @desc Clears the date picker for the before date
             *
             */
            $scope.activeClearDateBefore = function () {
                $scope.searchFilter.dateBefore = 0;
                $scope.searchInProgressInstances();
            };

            /**
             * @memberof InProgressCtrl
             * @desc Clears the instance title filter
             *
             */
            $scope.activeClearInstanceTitle = function () {
                $scope.searchFilter.instanceTitle = "";
                $scope.searchInProgressInstances();
            };

            /**
             * @memberof InProgressCtrl
             * @desc Sorting tasks by given option
             *
             * @param {String} optionId
             */
            $scope.activeSortBy = function (optionId) {
                $scope.activeOrderByOption = optionId;
            };

            /**
             * @memberof InProgressCtrl
             * @desc Clears any filter
             *
             */
            $scope.activeClearAllFilters = function () {
                $scope.searchFilter.dateAfter = 0;
                $scope.searchFilter.dateBefore = 0;
                $scope.searchFilter.instanceTitle = "";
                $scope.searchFilter.definitionId = "all";

                $scope.searchInProgressInstances();
            };

            /**
             * @memberof InProgressCtrl
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

		angular.module('wfWorkspaceControllers').controller('InProgressCtrl', ['$scope', '$mdDialog', 'processService', 'CONFIG', inProgressCtrl]);
	}
);