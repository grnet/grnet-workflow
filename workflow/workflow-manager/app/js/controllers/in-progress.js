(function (angular) {

	'use strict';

	angular.module('wfmanagerControllers').controller('InProgressCtrl', ['$scope', '$mdDialog', 'processService', 'CONFIG',
		/**
		 * @name InProgressCtrl
		 * @ngDoc controllers
		 * @memberof wfmanagerControllers
		 * 
		 * @desc History controller for in progress view
		 * 
		 */
		function ($scope, $mdDialog, processService, config) {

			$scope.imagePath = config.AVATARS_PATH;

			processService.getInProgressInstances().then(
				//success callback
				function (response) {
					$scope.allInstances = response.data;

					$scope.instances = $scope.allInstances;
					$scope.instancesMapByDefinitions = ArrayUtil.mapByProperty($scope.allInstances, "definitionName");

					//error callback	
				}, function (response) {

				}
			);

			/**
			 * @memberof InProgressCtrl
			 * @desc Shows instances by the definition
			 * 
			 */
			$scope.selectionChanged = function (definitionName) {
				$scope.instances = $scope.instancesMapByDefinitions[definitionName];
			};

			/**
			 * @memberof InProgressCtrl
			 * @desc Shows all instances
			 */
			$scope.showAllInstances = function () {
				$scope.instances = $scope.allInstances;
			};

		}]);
})(angular);