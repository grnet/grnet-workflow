(function () {

    'use strict';

    angular.module('wfmanagerControllers').controller('CompletedInstanceDetailCtrl', ['$scope', '$location', '$filter', '$routeParams', '$window',
		'$mdDialog', 'processService', 'CONFIG', 'auth',
		/**
		 * @name CompletedInstanceDetailCtrl
		 * @ngDoc controllers
		 * @memberof wfmanagerControllers
		 * @desc Controller used by Completed instances view
		 * 
		 */
		function ($scope, $location, $filter, $routeParams, $window, $mdDialog, processService, config, authProvider) {

			$scope.instanceId = $routeParams.instanceId;
			$scope.tasks = [];
			$scope.imagePath = config.AVATARS_PATH;
			$scope.isAdmin = authProvider.getRoles().indexOf("ROLE_Admin") >= 0;
			$scope.instanceName = "";

			processService.getTasksByInstanceId($scope.instanceId).then(
				// success response
				function (response) {
					$scope.tasks = response.data;
					$scope.instanceName = $scope.tasks[0].processInstance.title;
				},
				// error response
				function (response) {
					exceptionModal(response);
				}
			);

			/**
			 * @memberof CompletedInstanceDetailCtrl
			 * @desc Delete process instance
			 */
			$scope.deleteProcessInstance = function () {

				//create the dialog
				var confirmDialog = $mdDialog.confirm()
					.title($filter('translate')('delete'))
					.content($filter('translate')('deleteInstance'))
					.ariaLabel($filter('translate')('deleteInstance'))
					.targetEvent(event)
					.cancel($filter('translate')('cancel'))
					.ok($filter('translate')('confirm'));

				//show the dialog
				$mdDialog.show(confirmDialog).then(
					// agree
					function () {
						processService.deleteProcessInstance($scope.instanceId).then(
							//success callback
							function (response) {
								$location.path("/history");

								//error callback	
							}, function (response) {
								exceptionModal(response)
							});

						$mdDialog.cancel();
						// canceled	
					}, function () {
						$mdDialog.cancel();
					});
			};

			/**
			 * @memberof CompletedInstanceDetailCtrl
			 * @desc A helper function that displays modal panel showing the error that occured
			 * 
			 * @param {any} response
			 * @param {event} $event
			 */
			function exceptionModal(response, $event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog) {

						$scope.error = response.data;

						$scope.cancel = function () {
							$mdDialog.hide();
						};
					},

					templateUrl: 'templates/exception.tmpl.html',
					parent: angular.element(document.body),
					targetEvent: $event,
					clickOutsideToClose: false
				});
			};

			/**
			 * @memberof CompletedInstanceDetailCtrl
			 * @desc Returns to previous page
			 */
			$scope.goBack = function () {
				$window.history.back()
			};

		}]);
})(angular);
