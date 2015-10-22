/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')

        .controller('ProcessDetailCtrl',
        ['$scope', '$location', '$mdDialog', 'processService', 'CONFIG',

            /**
             * Controller for the Process Details view
             * @param {object} $scope
             * @param {$location} $location
             * @param {$mdDialog} $mdDialog
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $location, $mdDialog, processService, config) {

                /** @type {WorkflowDefinition[]} */
                $scope.processes = null;
                $scope.selectedProcessIndex = null;
                $scope.process = null;

                // get the processes
                processService.getProcesses()
                    .then(
                    // success callback
                    function (response) {
                        $scope.processes = response.data;
                        if ($scope.processes.length > 0) {
                            $scope.selectedProcessIndex = 0;
                            $scope.process = $scope.processes[0];
                        }
                    },
                    // error callback
                    function (response) {
                    }
                );

                $scope.processSelectionChanged = function () {
                    $scope.process = $scope.processes[$scope.selectedProcessIndex];
                };

                /**
                 * Shows a dialog with the process diagram
                 * @param event
                 */
                $scope.showDiagram = function (event) {
                    $mdDialog.show({
                        controller: function ($scope, $mdDialog, process, service) {
                            $scope.process = process;
                            $scope.service = service;
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
                 * Shows the UI to start a new instance of the selected process
                 */
                $scope.startProcess = function () {
                    $location.path('/process/start/' + $scope.process.id);
                };

            }]
    );

})(angular);
