define(['angular', 'services/processservice'],

	function (angular) {

		'use strict';

		function inProgressCtrl($scope, $mdDialog, processService, config) {

			$scope.imagePath = config.AVATARS_PATH;

			processService.getInProgressInstances().then(
				//success callback
				function (response) {
					$scope.allInstances = response.data;

					$scope.instances = $scope.allInstances;
					$scope.instancesMapByDefinitions = ArrayUtil.mapByProperty($scope.allInstances, "definitionName");

					//error callback	
				}, function (response) {

				});

			/**
			 * Shows instances by the definition
			 */
			$scope.selectionChanged = function (definitionName) {
				$scope.instances = $scope.instancesMapByDefinitions[definitionName];
			};
			
			$scope.print = function() {
				window.print();
			};

			/**
			 * Shows all instances
			 */
			$scope.showAllInstances = function () {
				$scope.instances = $scope.allInstances;
			};

		}

		angular.module('wfManagerControllers').controller('InProgressCtrl', ['$scope', '$mdDialog', 'processService', 'CONFIG', inProgressCtrl]);
	}
);