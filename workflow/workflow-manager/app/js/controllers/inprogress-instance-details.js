define(['angular', 'services/processservice', 'services/authprovider'],

	function (angular) {

		'use strict';

		function inProgressInstanceDetailCtrl($scope, $mdDialog, $routeParams, $filter, $location, processService, config, authProvider) {

			$scope.imagePath = config.AVATARS_PATH;
			$scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

			var instanceId = $routeParams['instanceId'];
			$scope.instanceId = instanceId;

			$scope.activeView = "taskList";
			$scope.backToTaskList = false;

			$scope.isManager = false;

			if (authProvider.getRoles().length == 1 && authProvider.getRoles().indexOf("ROLE_Manager") > -1)
				$scope.isManager = true;

			processService.getTasksByInstanceId(instanceId).then(
				//success callback
				function (response) {
					$scope.tasks = response.data;

					//error callback	
				}, function (response) {

				});

			/**
			 * Returns the difference between due date and current date
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

			$scope.listTaskDelay = function (task) {
				var diff;

				if (task.dueDate === null)
					return Infinity;

				if (task.endDate) {
					diff = task.dueDate - task.endDate;

				} else {
					var currentDate = new Date();
					diff = task.dueDate - currentDate.getTime();
				}

				var diffInDays = diff / (1000 * 3600 * 24);
				return diffInDays;
			};

			/**
			 * Change the view to given task details
			 * 
			 * @param task
			 */
			$scope.goToTaskDetails = function (task) {
				$scope.activeView = "taskDetails";
				$scope.task = task;

				$scope.backToTaskList = true;

				if ($scope.task.dueDate != null) {
					$scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy H:mm");
				}

				if ($scope.task.endDate != null) {
					$scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");
				}

				$scope.startDate = $filter('date')($scope.task.startDate, "d/M/yyyy");
			};

			/**
			 * Change the view to task list
			 */
			$scope.goToTaskList = function () {
				$scope.backToTaskList = false;

				$scope.activeView = "taskList";
			};

			$scope.showProgressDiagram = function (evt) {
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
					targetEvent: evt,
					clickOutsideToClose: true,
					locals: {
						'service': $scope.service,
						'instance': $scope.instance,
					}
				})
			};

			$scope.selectSupervisor = function (event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog, processService, instanceId, instance) {

						var processService = processService;
						$scope.instanceSupervisor = { email: instance.supervisor };
						$scope.showProgressBar = true;

						processService.getSupervisors().then(
							function (response) {
								$scope.supervisors = response.data;
							}

						).finally(function () {
							$scope.showProgressBar = false;
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

								});
						}
					},
					templateUrl: 'templates/supervisorSelection.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'processService': processService,
						'instanceId': $scope.instanceId,
						'instance': $scope.tasks[0].processInstance
					}
				})
			};

			$scope.sendNotificationEmail = function () {
				$mdDialog.show({
					controller: function ($scope, $mdDialog, assignee, taskId, processService) {

						$scope.showProgressBar = false;
						$scope.assignee = assignee;
						$scope.success = false;

						$scope.cancel = function () {
							$mdDialog.hide();
						};

						$scope.sendEmail = function () {
							$scope.showProgressBar = true;

							processService.sendTaskDueDateNotification(taskId, $scope.content).then(
								// success callback
								function () {
									$scope.content = null;
									$scope.success = true;
								}
							).finally(function () {
								$scope.showProgressBar = false;
							});
						};

					},
					templateUrl: 'templates/dueDateNotification.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'assignee': $scope.task.assignee,
						'taskId': $scope.task.id,
						'processService': processService
					}
				});
			};

			/**
			 * Redirects to previous page or change the view to task details
			 */
			$scope.back = function () {

				if ($scope.backToTaskList) {
					$scope.backToTaskList = false;
					$scope.activeView = "taskList";
				}
				else
					$location.path("/inprogress");
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

		angular.module('wfManagerControllers').controller('InProgressInstanceDetailCtrl', ['$scope', '$mdDialog', '$routeParams', '$filter', '$location', 'processService', 'CONFIG', 'auth', inProgressInstanceDetailCtrl]);
	}
);