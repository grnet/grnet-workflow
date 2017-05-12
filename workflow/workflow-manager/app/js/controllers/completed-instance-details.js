define(['angular', 'services/processservice', 'services/authprovider'],

	function (angular) {

		'use strict';

		function completedInstanceDetailCtrl($scope, $location, $filter, $routeParams, $mdDialog, processService, config, authProvider) {

			$scope.instanceId = $routeParams.instanceId;
			$scope.tasks = [];
			$scope.imagePath = config.AVATARS_PATH;
			$scope.showProgressBar = true;

			$scope.isAdmin = authProvider.getRoles().indexOf('ROLE_Admin') >= 0;

			$scope.instanceName = '';

			processService.getTasksByInstanceId($scope.instanceId).then(
				// success callback
				function (response) {
					$scope.tasks = response.data;
					$scope.instanceName = $scope.tasks[0].processInstance.title;
				},
				// error callback
				function (response) {
					exceptionModal(response);

				}).finally(function () {
					$scope.showProgressBar = false;
				});

			/**
			 * Delete process instance
			 */
			$scope.deleteProcessInstance = function (event) {

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
					function () {
						processService.deleteProcessInstance($scope.instanceId).then(
							//success callback
							function () {
								$location.path('/history');
							},
							//error callback
							function (response) {
								exceptionModal(response);
							}
						);

						$mdDialog.cancel();
					},
					// canceled	
					function () {
						$mdDialog.cancel();
					}
				);
			};

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
			}

		}

		angular.module('wfManagerControllers').controller('CompletedInstanceDetailCtrl', ['$scope', '$location', '$filter', '$routeParams', '$mdDialog', 'processService', 'CONFIG', 'auth', completedInstanceDetailCtrl]);

	}
);