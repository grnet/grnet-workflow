define(['angular', 'services/process-service'],

	function (angular) {

		'use strict';

		function taskDetailCtrl($scope, $filter, $routeParams, $location, $mdDialog, $window, processService, config) {

			// System constants
			$scope.imagePath = config.AVATARS_PATH;
			$scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

			// The task id
			var taskId = $routeParams['taskId'];

			// Sets the view during the render of the page on "completed tasks".
			// Used by completed tasks tab
			$scope.executionActiveView = "list";

			// Show the progress bar during rendering
			$scope.showProgress = true;

			/**
			 * Returns the difference between due date and current date
			 */
			$scope.taskDelay = function (task) {
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
			 * Get the task based by its id
			 * 
			 * @param taskId
			 *            The task's id
			 */
			function getTask(taskId) {
				$scope.showProgress = true;

				// get the selected task
				processService.getTask(taskId).then(
					// success callback
					function (response) {
						$scope.task = response.data;

						if ($scope.task.dueDate != null)
							$scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy H:mm");

						if ($scope.task.endDate != null)
							$scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");

						$scope.startDate = $filter('date')($scope.task.startDate, "d/M/yyyy");

						// check if task has approval property
						var formProperties = response.data.taskForm;
						for (var i = 0; i < formProperties.length; i++) {
							var property = formProperties[i];

							if (property.type === 'approve') {
								$scope.approveAction = property;
								break;
							}
						}
					},// error callback
					function (response) {
						exceptionModal(response);
					}

				).finally(function () {
					$scope.showProgress = false;
				});
			}

			getTask(taskId);

			/**
			 * Opens assignee modal
			 */
			$scope.selectAssigneeModal = function () {
				$mdDialog.show({
					controller: function ($mdDialog, candidates) {
						$scope.candidates = candidates;

						$scope.cancel = function () {
							$mdDialog.hide();
						};

						$scope.confirm = function () {
							$scope.task.assignee = $scope.assignee;
							$mdDialog.hide();
						};
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/assigneeSelection.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'candidates': $scope.task.candidates
					}
				})
			};

			/**
			 * Claim the task by the logged in user
			 */
			$scope.claimTask = function () {
				$scope.showProgress = true;

				processService.claimTask($scope.task.id).then(
					// success callback
					function (response) {
						$scope.task = response.data;
						getTask(taskId);
					},
					// error callback
					function (response) {
                        exceptionModal(response, $scope.task);
					}
				).finally(function () {
					$scope.showProgress = false;
				});
			};

			/**
			 * Completes task
			 */
			$scope.completeTask = function () {
				$scope.showProgress = true;

				processService.completeTask($scope.task).then(
					// success callback
					function (response) {
						$scope.task = response.data;
						$location.path('/task');
					},
					// error callback
					function (response) {
                        exceptionModal(response, $scope.task);
					}

				).finally(function () {
					$scope.showProgress = false;
				});
			};

			/**
			 * Handle the approve document task
			 * 
			 * @param outcome The result of the approve
			 * 
			 */
			$scope.acceptTask = function (outcome) {
				$scope.showProgress = true;
				$scope.approveAction.value = outcome;

				processService.completeTask($scope.task).then(
					// success callback
					function (response) {
						$scope.task = response.data;
						$location.path('/task');
					},
					// error callback
					function (response) {
                        exceptionModal(response, $scope.task);
					}

				).finally(function () {
					$scope.showProgress = false;
				});
			};

			/**
			 * Open a modal to display task details
			 */
			$scope.showTaskDetails = function (event) {
				$mdDialog.show({
					controller: function ($mdDialog) {

						$scope.cancel = function () {
							$mdDialog.hide();
						};
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/taskDetails.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'taskDetails': $scope.task.taskDetails
					}
				})
			};

			/**
			 * Tab change event
			 */
			$scope.onTabSelected = function (tab) {
				if (tab == "executionHistory") {
					// get tasks by task instance
					processService.getCompletedTasksByInstances($scope.task.processInstance.id).then(
						function (response) {
							$scope.historyTasks = response.data;
						});
				} else
					$scope.executionActiveView = "list";
			};

			/**
			 * Get the details of selected historic task
			 */
			$scope.goToCompletedTask = function (taskId) {
				processService.getTask(taskId).then(
					function (response) {
						$scope.historicTask = response.data;

						$scope.executionActiveView = "form";

						if ($scope.historicTask.dueDate != null)
							$scope.historicDueDate = $filter('date')($scope.historicTask.dueDate, "d/M/yyyy H:mm");

						if ($scope.historicTask.endDate != null)
							$scope.historicEndDate = $filter('date')($scope.historicTask.endDate, "d/M/yyyy");

						$scope.historicStartDate = $filter('date')($scope.historicTask.startDate, "d/M/yyyy");
					},
					function (response) {
                        exceptionModal(response, $scope.task);
					});
			};

			/**
			 * Temporary saves the task form data
			 */
			$scope.temporarySave = function () {
				$scope.showProgress = true;

				processService.temporarySave($scope.task).then(
					//success callback
					function (response) {
						getTask(taskId);
					},
					//error callback
					function (response) {
						exceptionModal(response);
					}
				).finally(function () {
					$scope.showProgress = false;
				});
			};

			/**
			 * Go back to historic tasks
			 */
			$scope.goBack = function () {
				$scope.executionActiveView = "list";
			};

			/**
			 * Redirects to print page
			 */
			$scope.goToPrintPage = function () {
				$location.path('/startform/print/' + $scope.task.processInstance.id, '_blank')
			};

			$scope.showProgressDiagram = function (event) {
				$mdDialog.show({
					controller: function ($mdDialog) {

						$scope.instance = $scope.task.processInstance;
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
						'instance': $scope.instance,
					}
				})
			};

			/**
			 * Navigates to previous page
			 */
			$scope.back = function () {
				$window.history.back();
			};

            function exceptionModal(response, task, event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {
                        $scope.error = response.data;

                        $scope.map = {};
                        $scope.map["supervisor"] = task.processInstance.supervisor;
                        $scope.map["taskName"] = task.name;
                        $scope.map["processInstanceName"] = task.processInstance.title;
                        $scope.map["process"] = task.definitionName;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                    },

                    templateUrl: 'templates/exception.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: event,
                    clickOutsideToClose: false
                })
            }

		}

		angular.module('wfWorkspaceControllers').controller('TaskDetailCtrl', ['$scope', '$filter', '$routeParams', '$location', '$mdDialog', '$window', 'processService', 'CONFIG', taskDetailCtrl]);
	}
);