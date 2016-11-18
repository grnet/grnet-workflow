(function (angular) {

	'use strict';

	angular.module('wfworkspaceControllers')
		.controller('InstanceDetailsCtrl', ['$scope', '$mdDialog', '$routeParams', '$filter', '$location', 'processService', 'CONFIG',
			/**
			 * @name InstanceDetailsCtrl
			 * @ngDoc controllers
			 * @memberof wfworkspaceControllers
			 * 
			 * @desc Controller used by Instance Details view
			 */
			function ($scope, $mdDialog, $routeParams, $filter, $location, processService, config) {

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

					}
				);

				/**
				 * @memberof InstanceDetailsCtrl
				 * @desc Returns the difference between due date and current date
				 * @param {Date} dueDate - Task's due date
				 * 
				 * @returns A number representing the diffrence between dates
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
				 * @memberof InstanceDetailsCtrl
				 * @desc Changes the view showing the given task
				 * 
				 * @param {Task}
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
				 * @memberof InstanceDetailsCtrl
				 * 
				 * @desc Change the view to task list
				 */
				$scope.goToTaskList = function () {
					$scope.backToTaskList = false;
					$scope.activeView = "taskList";
				};

				/**
				 * @memberof InstanceDetailsCtrl
				 * @desc Displays instance's progress
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
					});
				};

				/**
				 * @memberof InstanceDetailsCtrl
				 * @desc Removes assignee from a task
				 */
				$scope.removeAssignee = function () {
					processService.unclaimTask($scope.task.id).then(
						//success callback
						function (response) {
							$scope.task.assignee = null;

							//error callback	
						}, function (response) {
							$mdDialog.show($mdDialog.alert()
								.parent(document.body)
								.clickOutsideToClose(true)
								.title('Error')
								.content(response.data)
								.ok('Ok'))
						}
					);
				};

				/**
				 * @memberof InstanceDetailsCtrl
				 * @desc Opens a modal to select assignee for the task 
				 * 
				 * @param {event} event
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
										exceptionModal(response);
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
										exceptionModal(response);
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

				/**
				 * @memberof InstanceDetailsCtrl
				 * @desc Change the task's assignee to an already assigned task
				 */
				$scope.changeAssignee = function () {
					if ($scope.task.assignee != null) {
						processService.setAssigneeToTask($scope.task, $scope.assignee).then(
							function (response) {
								$scope.activeView = "taskList";
								$scope.showBack = true;
							}
							// error callback
							, function (response) {

							});
					} else {
						$scope.activeView = "taskList";
						$scope.showBack = true;
					}
				};

				/**
				 * @memberof InstanceDetailsCtrl
				 * @desc Redirects to previous page or change the view to task details
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