define(['angular', 'services/process-service'],

    function (angular) {

        'use strict';

        function processDetailCtrl($scope, $location, $mdDialog, $filter, processService, config) {

            /** @type {WorkflowDefinition[]} */
            $scope.workflowDefinitions = null;
            $scope.groupProcesses = null;
            $scope.process = null;
            $scope.selectedIndex = null;
            $scope.activeVersion = null;
            $scope.showProgress = true;

            // get the processes
            processService.getProcesses().then(
                // success callback
                function (response) {

                    $scope.processes = response.data;

                    if (response.data.length > 0)
                        $scope.selectedProcess = response.data[0].id;

                    //$scope.groupProcesses = ArrayUtil.mapByProperty(response.data, "owner");

                    // since there are no available proccess will redirect to another page
                    if (response.data.length == 0) {
                        $mdDialog.show($mdDialog.alert()
                            .parent(document.body)
                            .title($filter('translate')('noAvailableProcess'))
                            .content($filter('translate')('noAvailableProcess'))
                            .ok($filter('translate')('confirm')))

                        $location.path('/task');
                    }

                    if (response.data.length > 0)
                        $scope.processSelectionChanged(response.data[0].id);
                }
            ).finally(function() {
                $scope.showProgress = false;
            });

            $scope.processSelectionChanged = function (processId) {
                $scope.selectedIndex = processId;

                processService.getProcess(processId).then(
                    // success callback
                    function (response) {
                        $scope.process = response.data;

                        for (var processVersion in $scope.process.processVersions) {

                            // look for active version
                            if ($scope.process.processVersions.hasOwnProperty(processVersion)) {
                                if ($scope.process.processVersions[processVersion].status == "active") {

                                    $scope.activeVersion = $scope.process.processVersions[processVersion].version;
                                }
                            }
                        }

                    }
                );
            };

            /**
             * Shows a dialog with the process diagram
             * @param event
             */
            $scope.showDiagram = function (event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog, service, process) {
                        $scope.process = process.id;
                        $scope.service = service;
                        $scope.definitionName = process.name;

                        $scope.cancel = function () {
                            $mdDialog.cancel();
                        };

                    },
                    templateUrl: 'templates/diagram.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: true,
                    locals: {
                        'service': config.WORKFLOW_SERVICE_ENTRY,
                        'process': $scope.process
                    }
                })
            };

            /**
             * Returns true if the selected version is active
             * @param {WorkflowDefinition} process
             */
            $scope.isActive = function (process) {
                return processService.isProcessActive(process);
            };

            /**
             * Shows the UI to start a new instance of the selected process
             */
            $scope.startProcess = function () {
                $location.path('/process/start/' + $scope.process.id);
            };

        }

        angular.module('wfWorkspaceControllers').controller('ProcessDetailCtrl', ['$scope', '$location', '$mdDialog', '$filter', 'processService', 'CONFIG', processDetailCtrl]);
    }

);
