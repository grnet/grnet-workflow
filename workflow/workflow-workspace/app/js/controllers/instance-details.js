define(['angular', 'services/process-service'],

	function (angular) {

		'use strict';

		function instanceDetailsCtrl($scope, $mdDialog, $routeParams, $filter, $location, processService, config) {

			$scope.imagePath = config.AVATARS_PATH;
			$scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

			var instanceId = $routeParams['instanceId'];
			$scope.instanceId = instanceId;

			$scope.activeView = "taskList";
			$scope.backToTaskList = false;

			processService.getTasksByInstanceId(instanceId).then(
				//success callback
				function (response) {
					$scope.tasks = response.data;
					//error callback	
				}, function (response) {
                    exceptionModal(response, $scope.task);
				});

			/**
			 * Returns the difference between due date and current date
			 */
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

			/**
			 * Change the view to given task details
			 * 
			 * @param task
			 */
			$scope.goToTaskDetails = function (task) {
				$scope.activeView = "taskDetails";
				$scope.task = task;

				$scope.backToTaskList = true;

				if ($scope.task.dueDate != null)
					$scope.dueDate = $filter('date')($scope.task.dueDate, "d/M/yyyy");

				if ($scope.task.endDate != null)
					$scope.endDate = $filter('date')($scope.task.endDate, "d/M/yyyy");

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

			/**
			 * Unclaim a task
			 */
			$scope.removeAssignee = function () {
				processService.unclaimTask($scope.task.id).then(
					//success callback
					function (response) {
						$scope.task.assignee = null;

						//error callback	
					}, function (response) {
                        exceptionModal(response, $scope.task);
					}
				);
			};

			/**
			 * Opens a modal to select assignee for the task
			 * 
			 * @param event
			 */
			$scope.selectAssignee = function (event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog, candidates, processService, task) {
						$scope.candidates = candidates;
						var processService = processService;
						$scope.task = task;
						$scope.prevAssignee = $scope.task.assignee;

						$scope.showProgressBar = true;

						$scope.cancel = function () {
							$scope.task.assignee = $scope.prevAssignee;
							$mdDialog.hide();
						};

						$scope.confirm = function () {
							setAssignee();
							$mdDialog.hide();
						};

						function setAssignee() {
							processService.setAssigneeToTask($scope.task, $scope.task.assignee).then(
								function (response) {
									$scope.showProgressBar = false;
								},
								function (response) {
									$scope.showProgressBar = false;
								});
						}

						$scope.getAllCandidates = function () {
							$scope.showProgressBar = true;

							processService.getAllCandidates().then(
								// success callback
								function (response) {
									$scope.showProgressBar = false;
									$scope.candidates = response.data;
								},
								// error callback
								function (response) {
									$scope.showProgressBar = false;
                                    exceptionModal(response, $scope.task);
								});
						};

						$scope.getCandidatesForTask = function () {
							$scope.showProgressBar = true;

							processService.getCandidatesForTask($scope.task.id).then(
								// success callback
								function (response) {
									$scope.showProgressBar = false;
									$scope.candidates = response.data;

									// error callback
								}, function (response) {
									$scope.showProgressBar = false;
                                    exceptionModal(response, $scope.task);
								});
						};

						$scope.getCandidatesForTask();
					},
					templateUrl: 'templates/assigneeSelection.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'candidates': $scope.task.candidates,
						'processService': processService,
						'task': $scope.task
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
								// sucess callback
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
			 * Change assignee to the task
			 */
			$scope.changeAssignee = function () {
				if ($scope.task.assignee != null) {
					processService.setAssigneeToTask($scope.task, $scope.assignee).then(
						function (response) {
							$scope.activeView = "taskList";
							$scope.showBack = true;
						},
						function (response) {
                            exceptionModal(response, $scope.task);
						});
				} else {
					$scope.activeView = "taskList";
					$scope.showBack = true;
				}
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

            /**
             * @memberof InstanceDetailsCtrl
             * @desc Displays a modal panel, showing the exception message
             *
             * @param {any} response
             * @param {event} event
             */
            function exceptionModal(response, task, event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {
                        $scope.error = response.data;

                        $scope.map = {};
                        $scope.map["supervisor"] = task.processInstance.supervisor;
                        $scope.map["taskName"] = task.name;
                        $scope.map["processInstanceName"] = task.processInstance.title;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                    },

                    templateUrl: 'templates/exception.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: event,
                    clickOutsideToClose: false
                })
            };
		}

		angular.module('wfWorkspaceControllers').controller('InstanceDetailsCtrl', ['$scope', '$mdDialog', '$routeParams', '$filter', '$location', 'processService', 'CONFIG', instanceDetailsCtrl]);
	}
);