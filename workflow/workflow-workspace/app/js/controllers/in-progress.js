define(['angular', 'services/process-service'],

	function (angular) {

		'use strict';

		function inProgressCtrl($scope, $mdDialog, processService, config) {

			$scope.imagePath = config.AVATARS_PATH;
			$scope.showProgress = true;

			processService.getInProgressInstances().then(
				//success callback
				function (response) {
					$scope.allInstances = response.data;

					$scope.instances = $scope.allInstances;
					$scope.instancesMapByDefinitions = ArrayUtil.mapByProperty($scope.allInstances, "definitionName");

					//error callback	
				}, function (response) {

				}).finally(function () {
					$scope.showProgress = false;
				});

			/**
			 * Shows instances by the definition
			 */
			$scope.selectionChanged = function (definitionName) {
				$scope.instances = $scope.instancesMapByDefinitions[definitionName];
			};

			/**
			 * Shows all instances
			 */
			$scope.showAllInstances = function () {
				$scope.instances = $scope.allInstances;
			};

			$scope.print = function() {
				window.print();
			};

		}

		angular.module('wfWorkspaceControllers').controller('InProgressCtrl', ['$scope', '$mdDialog', 'processService', 'CONFIG', inProgressCtrl]);
	}
);