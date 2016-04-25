/**
 * Created by nlyk on 3/10/2015.
 */
(function (angular) {

    angular.module('wfmanagerControllers')
        .controller('addDefinitionController', ['$scope', '$mdDialog', '$location', 'processService', 'process',

            /**
             * Submit a new BPMN file. If a process is passed as argument, then we'll attempt
             * to create a new version. Otherwise a new process definition will be created.
             * @param $scope
             * @param $mdDialog
             * @param $location
             * @param {ProcessService} processService   - Service for accessing the workflow-definition API
             * @param {WorkflowDefinition} process      - the current workflow definition object
             */
                function ($scope, $mdDialog, $location, processService, process) {

                /** @type {WorkflowDefinition} */
                $scope.process = process;
                $scope.bpmnFile = null;
                $scope.inProgress = false;
                $scope.exception = false;
                $scope.exceptionMessage = null;

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
                        processService.createProcess($scope.bpmnFile)
                            .then(
                            // success callback
                            function (response) {
                                $mdDialog.hide(response);
                                $location.path('/process/' + response.data.id);
                            },
                            // error callback
                            function (response) {
                                //$mdDialog.hide(response);
                                //errorAlert(response);
                            	$scope.exception = true;
                            	$scope.exceptionMessage = response.data;
                            }
                        );
                    }
                    // Add Version to a Process Definition
                    else {
                        processService.createProcessVersion(process.id, $scope.bpmnFile)
                            .then(
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
                                //errorAlert(response);
                            	$scope.exception = true;
                            	$scope.exceptionMessage = response.data;
                            	
                            }
                        );
                    }
                };

                // helper function to show an error alert
                function errorAlert(response) {
                	$mdDialog.show({
                		controller: function ($scope, $mdDialog, error) {
                			$scope.error = error;
                			
                            $scope.cancel = function () {
                            	$mdDialog.hide();
                            };
                        },
                        scope: $scope,
                        preserveScope: true,
                        templateUrl: 'templates/exception.tmpl.html',
                        parent: angular.element(document.body),
                        targetEvent: event,
                        locals: {
                        	'error': response.data
                        }
                	})
                }
            }
        ]);
})(angular);