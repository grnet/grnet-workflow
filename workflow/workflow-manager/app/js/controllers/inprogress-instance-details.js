(function (angular) {
	'use strict';

	angular.module('wfmanagerControllers').controller('InProgressInstanceDetailCtrl', ['$scope', '$mdDialog', '$routeParams', '$filter', '$location',
		'processService', 'CONFIG',
		/**
		 * @name InProgressInstanceDetailCtrl
		 * @ngDoc controllers
		 * @memberof wfmanagerControllers
		 * 
		 * @desc History controller for in progress instance details view
		 */
		function ($scope, $mdDialog, $routeParams, $filter, $location, processService, config) {

			var instanceId = $routeParams['instanceId'];
			$scope.imagePath = config.AVATARS_PATH;
			$scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

			$scope.instanceId = instanceId;
			$scope.activeView = "taskList";
			$scope.backToTaskList = false;

			processService.getTasksByInstanceId(instanceId).then(
				//success callback
				function (response) {
					$scope.tasks = response.data;
				}
				//error callback	
				, function (response) {

				}

			);

			/**
			 * @memberof InProgressInstanceDetailCtrl
			 * @desc Returns the difference between due date and current date
			 * 
			 * @param {Date} dueDate
			 * @returns {Number} - The difference between dates
			 */
			$scope.taskDelay = function (dueDate) {
				var diff;

				if (dueDate === null)
					return Infinity;

				if ($scope.task.endDate) {
					diff = dueDate - $scope.task.endDate;

				} else {
					var currentDate = new Date();
					diff = dueDate - currentDate.getTime();
				}

				var diffInDays = diff / (1000 * 3600 * 24);
				return diffInDays;
			};

			/**
			 * @memberof InProgressInstanceDetailCtrl
			 * @desc Change the view to given task details
			 * 
			 * @param {Task} task
			 */
			$scope.goToTaskDetails = function (task) {
				$scope.activeView = "taskDetails";
				$scope.task = task;

				$scope.backToTaskList = true;

				if ($scope.task.dueDate != null) {
					$scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy");
				}

				if ($scope.task.endDate != null) {
					$scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");
				}

				$scope.startDate = $filter('date')($scope.task.startDate, "d/M/yyyy");
			};

			/**
			 * @memberof InProgressInstanceDetailCtrl
			 * @desc Change the view to task list
			 */
			$scope.goToTaskList = function () {
				$scope.backToTaskList = false;

				$scope.activeView = "taskList";
			};

			/**
			 * @memberof InProgressInstanceDetailCtrl
			 * @desc Shows progress diagram
			 * 
			 * @param {event} event
			 */
			$scope.showProgressDiagram = function (event) {
				$mdDialog.show({
					controller: function ($mdDialog) {

						$scope.instance = $scope.tasks[0].processInstance;
						$scope.service = config.WORKFLOW_SERVICE_ENTRY;

						$scope.cancel = function () {
							$mdDialog.hide();
						};
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/progressDiagram.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'service': $scope.service,
						'instance': $scope.instance
					}
				})
			};

			/**
			 * @memberof InProgressInstanceDetailCtrl
			 * @desc Changes the supervisor of the instance by showing a modal panel with all available supervisors
			 * 
			 * @param {event} event
			 */
			$scope.selectSupervisor = function (event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog, processService, instanceId) {
						$scope.instanceSupervisor = { email: null };

						processService.getInstanceById(instanceId).then(
							// success callback
							function (response) {
								$scope.instanceSupervisor.email = response.data.supervisor;
							}
						);

						$scope.showProgressBar = true;

						processService.getSupervisors().then(
							function (response) {
								$scope.showProgressBar = false;
								$scope.supervisors = response.data;
							});

						$scope.cancel = function () {
							$mdDialog.hide();
						};

						$scope.confirm = function () {
							changeSupervisor();
							$mdDialog.hide();
						};

						function changeSupervisor() {
							processService.changeInstanceSupervisor(instanceId, $scope.instanceSupervisor.email).then(
								function (response) {

								},
								function (response) {

								}
							);
						}
					},
					templateUrl: 'templates/supervisorSelection.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'processService': processService,
						'instanceId': $scope.instanceId
					}
				})
			};

			/**
			 * @memberof InProgressInstanceDetailCtrl
			 * @desc Redirects to previous page or change the view to task details 
			 * 
			 */
			$scope.back = function () {

				if ($scope.backToTaskList) {
					$scope.backToTaskList = false;
					$scope.activeView = "taskList";
				}
				else
					$location.path("/inprogress");
			};

		}]);
})(angular);