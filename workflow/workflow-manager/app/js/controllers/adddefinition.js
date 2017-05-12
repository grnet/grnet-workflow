define(['angular', 'services/processservice'],

	function (angular) {

		'use strict';

		function addDefinitionController($scope, $mdDialog, $location, processService, process) {

			/** @type {WorkflowDefinition} */
			$scope.process = process;
			$scope.bpmnFile = null;
			$scope.exception = false;
			$scope.exceptionMessage = null;

			$scope.inProgress = false;

			$scope.hide = function () {
				$mdDialog.hide();
			};

			$scope.cancel = function () {
				$mdDialog.cancel();
			};

			$scope.answer = function () {
				$scope.inProgress = true;

				// Create new Process Definition
				if (process === null) {
					processService.createProcess($scope.bpmnFile).then(
						// success callback
						function (response) {
							$mdDialog.hide(response);
							$location.path('/process/' + response.data.id);
						},
						// error callback
						function (response) {
							//$mdDialog.hide(response);
							$scope.exception = true;
							$scope.exceptionMessage = response.data;
						}

					).finally(function () {
						$scope.inProgress = false;
					});
				}
				// Add Version to a Process Definition
				else {
					processService.createProcessVersion(process.id, $scope.bpmnFile).then(
						// success callback
						function (response) {
							$mdDialog.hide(response);
							var newVersion = response.data;
							// add the new version to the open process definition
							$scope.process.processVersions.unshift(newVersion);
							$scope.process.activeDeploymentId = newVersion.deploymentId;
						},
						// error callback
						function (response) {
							//$mdDialog.hide(response);
							$scope.exception = true;
							$scope.exceptionMessage = response.data;

						}

					).finally(function () {
						$scope.inProgress = false;
					});
				}
			};
		}

		angular.module('wfManagerControllers').controller('AddDefinitionController', ['$scope', '$mdDialog', '$location', 'processService', 'process', addDefinitionController]);
	}
);