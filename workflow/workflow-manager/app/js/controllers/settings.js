(function () {

	'use strict';

	angular.module('wfmanagerControllers').controller('SettingsCtrl', ['$scope', '$mdDialog', 'processService', 'CONFIG',
		/**
		 * @name SettingsCtrl
		 * @ngDoc controllers
		 * @memberof wfmanagerControllers
		 * 
		 * @desc Controller used in Settings view
		 */
		function ($scope, $mdDialog, processService, config) {

			$scope.showProgressBar = true;

			processService.getSettings().then(
				function (response) {
					$scope.settings = response.data;
				},
				//error callback
				function (response) {
					exceptionModal(response);
				}

			).finally(function () {
				$scope.showProgressBar = false;
			});

			/**
			 * @memberof SettingsCtrl
			 * @desc Updates settings
			 */
			$scope.updateSettings = function () {

				processService.updateSettings($scope.settings).then(
					//success callback
					function (response) {
						$scope.settings = response.data;
						$scope.settingsForm.$setPristine();
					}
					// error callback
					, function (response) {
						exceptionModal(response);
					}
				);
			};

			/**
			 * @memberof SettingsCtrl
			 * @desc Displays a modal panel showing the exception message
			 * 
			 * @param {any} response
			 * @param {event} $event
			 */
			function exceptionModal(response, $event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog, error) {

						$scope.error = error;

						$scope.cancel = function () {
							$mdDialog.cancel();
						};
					},
					templateUrl: 'templates/exception.tmpl.html',
					parent: angular.element(document.body),
					targetEvent: $event,
					clickOutsideToClose: false,
					locals: {
						'error': response.data
					}
				});
			};

		}]);
})(angular);