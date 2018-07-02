define(['angular', 'services/process-service'],

	function (angular) {

		'use strict';

		function printStartFormCtrl($scope, $routeParams, $mdDialog, processService, config) {

			var instanceId = $routeParams['instanceId'];
			var taskId = $routeParams['taskId'];
			$scope.taskForm = {};
			$scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

			/**
			 * Get the start from
			 */
			processService.getStartEventForm(instanceId).then(
				//success callback
				function (response) {
					$scope.taskForm = response.data;

					//error callback	
                },
                //error callback
                function (response) {
                    exceptionModal(response);
                }
			);

			processService.getInstanceById(instanceId).then(
				function (response) {
					$scope.instance = response.data;
                },
                //error callback
                function (response) {
                    exceptionModal(response);
                }
			);

			checkTask();

			function checkTask() {
				if (typeof taskId != 'undefined' && taskId != null && taskId != "") {

					processService.getTask(taskId).then(
						function (response) {
							$scope.task = response.data;
							var tempMap = {};
							$scope.uniqueTaskFormItems = { formItems: [] };

							// create a map contains the taskForm ids
							for (var i = 0; i < $scope.taskForm.length; i++) {
								tempMap[$scope.taskForm[i].id] = $scope.taskForm[i];
							}

							for (var j = 0; j < $scope.task.taskForm.length; j++) {
								if (typeof tempMap[$scope.task.taskForm[j].id] == 'undefined') {
									$scope.uniqueTaskFormItems.formItems.push($scope.task.taskForm[j]);
								}
							}
						}
					);
				}
			}

			$scope.print = function () {
				document.title = $scope.instance.title;
				window.print();
			};

            /**
             * @memberof TaskDetailCtrl
             * @desc Displays a modal panel, showing the exception message
             *
             * @param {any} response
             * @param {event} event
             */
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
                })
            }

		}

		angular.module('wfWorkspaceControllers').controller('PrintStartFormCtrl', ['$scope', '$routeParams', '$mdDialog', 'processService', 'CONFIG', printStartFormCtrl]);

	}
);