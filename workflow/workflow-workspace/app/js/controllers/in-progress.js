(function (angular) {

	'use strict';

	angular.module('wfworkspaceControllers').controller('InProgressCtrl', ['$scope', '$mdDialog', 'processService', 'CONFIG',
		/**
		 * @name InProgressCtrl
		 * @ngDoc controllers
		 * @memberof wfworkspaceControllers
		 * 
		 * @desc Controller used by In progress view
		 */
		function ($scope, $mdDialog, processService, config) {

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

				}

			).finally(function () {
				$scope.showProgress = false;
			});

			/**
			 * @memberof InProgressCtrl
			 * 
			 * @desc Shows instances by the definition name
			 * 
			 * @param {String} definitionName
			 */
			$scope.selectionChanged = function (definitionName) {
				$scope.instances = $scope.instancesMapByDefinitions[definitionName];
			};

			/**
			 * @memberof InProgressCtrl
			 * @desc Shows all instances
			 * 
			 */
			$scope.showAllInstances = function () {
				$scope.instances = $scope.allInstances;
			};

		}]);
})(angular);