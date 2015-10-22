/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfmanagerControllers')

        .controller('ProcessDetailCtrl',
        ['$scope', '$http', '$routeParams', '$location', '$mdDialog', '$timeout', 'processService', 'CONFIG',

            /**
             * Controller for the Process Details view
             * @param {object} $scope
             * @param {$http} $http
             * @param {$routeParams} $routeParams
             * @param {$location} $location
             * @param {$mdDialog} $mdDialog
             * @param $timeout
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $http, $routeParams, $location, $mdDialog, $timeout, processService, config) {

                /** @type {WorkflowDefinition} */
                $scope.workflowDefinition = null;
                $scope.processId = $routeParams.processId;
                $scope.iconName = null;

                // get the process data
                processService.getProcess($scope.processId)
                    .then(
                    // success callback
                    function (response) {
                        $scope.workflowDefinition = response.data;
                        $scope.workflowDefinition.icon = $scope.workflowDefinition.icon || config.DEFAULT_AVATAR;
                    }
                );

                /**
                 * Saves the Process Definition form
                 */
                $scope.save = function () {
                    processService.updateProcess($scope.workflowDefinition)
                        .then(
                        // success callback
                        function (response) {
                            $scope.workflowDefinition = response.data;
                        },
                        // error callback
                        function (response) {
                            $mdDialog.show($mdDialog.alert()
                                    .parent(document.body)
                                    .clickOutsideToClose(true)
                                    .title('Save failed')
                                    .content('Failed to save process definition<br><pre>'
                                    + response.data.message + '</pre>')
                                    .ok('Ok!')
                            );
                        }
                    );
                };

                /**
                 * Click event Handler for the Delete Process button
                 * @param {event} event
                 */
                $scope.askDeleteProcess = function (event) {

                    var confirm = $mdDialog.confirm()
                        .title('Delete process')
                        .content('Would you like to delete process ' + $scope.workflowDefinition.name + " ?")
                        .ariaLabel('Delete process')
                        .targetEvent(event)
                        .ok('Yes')
                        .cancel('No');

                    $mdDialog.show(confirm).then(function () {
                        processService.deleteProcess($scope.processId)
                            .then(
                            // success callback
                            function () {
                                $location.path('/process');
                            });
                    });
                };

                /**
                 * Find the active version in the definition versions array
                 * @return DefinitionVersion
                 */
                $scope.findActiveVersion = function () {
                    if ($scope.workflowDefinition == null) {
                        return null;
                    }

                    var versions = $scope.workflowDefinition.definitionVersions;

                    /** @type {DefinitionVersion} */
                    var version = null;
                    for (var index = 0; index < versions.length; index++) {
                        if (versions[index].deploymentId === $scope.workflowDefinition.activeDeploymentId) {
                            version = versions[index];
                            break;
                        }
                    }

                    return version;
                };

                /**
                 * Click event handler for the delete version button
                 * @param event
                 */
                $scope.askDeleteVersion = function (event) {

                    /** @type {DefinitionVersion} */
                    var version = $scope.findActiveVersion();

                    if (version === null) {
                        $mdDialog.show(
                            $mdDialog.alert()
                                .parent(document.body)
                                .clickOutsideToClose(true)
                                .title('Delete Version error')
                                .content('The active version not found in version list')
                                .ariaLabel('Failed to delete version')
                                .ok('Ok!')
                        );
                        return;
                    }

                    var confirm = $mdDialog.confirm()
                        .title('Delete process version')
                        .content('Would you like to delete version ' + version.version + " ?")
                        .ariaLabel('Delete process version')
                        .targetEvent(event)
                        .ok('Yes')
                        .cancel('No');

                    $mdDialog.show(confirm).then(function () {

                        processService.deleteProcessVersion(
                            $scope.processId,
                            $scope.workflowDefinition.activeDeploymentId)
                            .then(
                            // success callback
                            function (response) {
                                $scope.workflowDefinition = response.data;
                            },

                            // error callback
                            function (response) {
                                $mdDialog.show(
                                    $mdDialog.alert()
                                        .parent(document.body)
                                        .clickOutsideToClose(true)
                                        .title('Delete Version error')
                                        .content('Failed to delete version.<br>' + response.data.message)
                                        .ariaLabel('Failed to delete version')
                                        .ok('Ok!')
                                );
                            }
                        );
                    });
                };

                /**
                 * Click event handler fot the Add Version button
                 * @param event
                 */
                $scope.showAddVersion = function (event) {
                    $mdDialog.show({
                        controller: 'addDefinitionController',
                        templateUrl: 'templates/adddefinition.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                            'process': $scope.workflowDefinition
                        }
                    });
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
                            'process': $scope.workflowDefinition
                        }
                    })
                };

                $scope.showIconSelect = function (event) {
                    $mdDialog.show({
                        controller: DialogController,
                        templateUrl: 'templates/iconselect.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true
                    })
                        .then(function (answer) {
                            $scope.workflowDefinition.icon = answer.filename;
                            $scope.iconName = answer.name;
                            $scope.processForm.icon.$setDirty();
                        });
                };

                function DialogController($scope, $mdDialog, $http) {

                    $http.get('img/avatars/avatars.json').then(function (response) {
                        $scope.avatars = response.data;
                    });

                    $scope.hide = function () {
                        $mdDialog.hide();
                    };
                    $scope.cancel = function () {
                        $mdDialog.cancel();
                    };
                    $scope.answer = function (answer) {
                        $mdDialog.hide(answer);
                    };
                }

                /**
                 * Returns the label for the change status button
                 * @return {string}
                 */
                $scope.newStatusButtonLabel = function () {
                    var version = $scope.findActiveVersion() || {};

                    switch (version.status) {
                        case 'new':
                        case 'inactive':
                            return 'Activate';
                        case 'active':
                            return 'Deactivate';
                    }
                };

                /**
                 * Changes the status of the selected version
                 * (new,inactive -> active  or active -> inactive)
                 */
                $scope.changeStatus = function () {

                    var confirm = $mdDialog.confirm()
                        .title('Set active version')
                        .content('All other changes will be lost. Do you want to continue?')
                        .ariaLabel('Set active version')
                        .targetEvent(event)
                        .ok('Yes')
                        .cancel('No');

                    $mdDialog.show(confirm)
                        .then(
                        // agree
                        function () {
                            var version = $scope.findActiveVersion();
                            $scope.setVersionStatus(version, version.status === 'active' ? 'inactive' : 'active');
                        });
                };

                /**
                 * Sets the status to the definition version
                 * @param {DefinitionVersion} version
                 * @param {string} status
                 */
                $scope.setVersionStatus = function (version, status) {

                    if (status === 'active') {
                        processService.setActiveVersion($scope.workflowDefinition.id, version.id)
                            .then(
                            // success callback
                            function (response) {
                                $scope.workflowDefinition = response.data;
                                $scope.resetVersionSelect()
                            },
                            // fail callback
                            function (response) {
                                alertStatusChangeFailed(response);
                            }
                        );
                    }
                    else {
                        processService.deactivateVersion($scope.workflowDefinition.id, version.id)
                            .then(
                            // success callback
                            function (response) {
                                version.status = response.data.status;
                                $scope.resetVersionSelect()
                            },
                            // fail callback
                            function (response) {
                                alertStatusChangeFailed(response);
                            }
                        );
                    }
                };

                /**
                 * Helper function for showing error alert
                 * @param response
                 */
                function alertStatusChangeFailed(response) {
                    $mdDialog.show($mdDialog.alert()
                            .parent(document.body)
                            .clickOutsideToClose(true)
                            .title('Update status failed')
                            .content('Failed to update status of the version<br>' + response.data.message)
                            .ok('Ok!')
                    );
                }

                // workaround to update the md-select after status change
                $scope.recreateSelect = true;
                $scope.resetVersionSelect = function () {
                    $scope.recreateSelect = false;
                    $timeout(function () {
                        $scope.recreateSelect = true;
                    }, 0);
                }

            }]
    );

})(angular);
