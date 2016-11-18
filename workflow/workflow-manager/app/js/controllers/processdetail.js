(function (angular) {

    'use strict';

    angular.module('wfmanagerControllers').controller('ProcessDetailCtrl', ['$scope', '$http', '$routeParams', '$location', '$mdDialog', '$filter', 'processService', 'auth', 'CONFIG',
        /**
         * @name ProcessDetailCtrl
         * @ngDoc controllers
         * @memberof wfmanagerControllers
         * 
         * @desc Controller used in Process details view
         */
        function ($scope, $http, $routeParams, $location, $mdDialog, $filter, processService, authProvider, config) {

            $scope.processId = $routeParams.processId;
            $scope.selectedTab = "process";
            $scope.taskMetadataActiveView = "taskMetadata";
            $scope.saveFirst = false;

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Get the workflow definition
             */
            function getProcess() {

                $scope.progressBar = true;

                // get the process data
                processService.getProcess($scope.processId).then(
                    // success callback
                    function (response) {
                        $scope.workflowDefinition = response.data;

                        // check if definition is new. Therefore needs to be saved first
                        if ($scope.workflowDefinition.owner == null)
                            $scope.saveFirst = true;

                        var endIndex = $scope.workflowDefinition.icon.indexOf(".");
                        $scope.iconName = $scope.workflowDefinition.icon.substring(0, endIndex);

                        if ($scope.workflowDefinition.activeDeploymentId == null) {
                            var version = response.data.processVersions[0];
                            $scope.workflowDefinition.activeDeploymentId = version.deploymentId;
                        }

                        $scope.workflowDefinition.icon = $scope.workflowDefinition.icon || config.DEFAULT_AVATAR;

                        // get groups
                        getGroups();
                    }

                ).finally(function () {
                    $scope.progressBar = false;
                });
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Get groups based on user's role
             */
            function getGroups() {
                //checking if user has role admin in order to show all groups/owners
                if (authProvider.getRoles().indexOf("ROLE_Admin") >= 0) {
                    processService.getGroups().then(
                        // success callback
                        function (response) {
                            $scope.groups = response.data;
                        }
                    );
                    // user is not admin and groups in which belongs will be returned	
                } else {
                    processService.getUserGroups().then(
                        function (response) {
                            $scope.groups = response.data;
                        }
                    );
                }
            };

            // get the definition
            getProcess();

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Saves the Process Definition form
             */
            $scope.save = function () {
                processService.updateProcess($scope.workflowDefinition).then(
                    // success callback
                    function (response) {
                        $scope.saveFirst = false;
                        $scope.processForm.$setPristine();
                    },
                    // error callback
                    function (response) {
                        exceptionModal(response);
                    });
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Deletes the process definition by using a modal panel in order to ask for confirmation from the user
             * 
             * @param {event} event
             */
            $scope.deleteProcess = function (event) {

                var confirm = $mdDialog.confirm()
                    .title($filter('translate')('deleteProcess'))
                    .content($filter('translate')('deleteProcessConf') + " " + $scope.workflowDefinition.name + " ?")
                    .ariaLabel($filter('translate')('deleteProcess'))
                    .targetEvent(event)
                    .ok($filter('translate')('confirm'))
                    .cancel($filter('translate')('cancel'));

                // show the confirm modal
                $mdDialog.show(confirm).then(
                    // confirmed
                    function () {
                        processService.deleteProcess($scope.processId).then(
                            // success delete callback
                            function () {
                                $location.path('/process');
                            }
                            // error delete callback
                            , function (response) {
                                exceptionModal(response);
                            }
                        );
                    }
                );
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Deletes the process definition by using a modal panel in order to ask for confirmation from the user
             * 
             * @param {event} event
             */
            $scope.askDeleteVersion = function (event) {

                // get the active version
                var version = $scope.findActiveVersion();

                // if the version is null, a modal shows up and informs user to delete the process definition instead
                if (version === null) {

                    var deleteProcessInsteadModal = $mdDialog.alert()
                        .parent(document.body)
                        .clickOutsideToClose(true)
                        .title($filter('translate')('deleteVersionError'))
                        .content($filter('translate')('versionNotFound'))
                        .ariaLabel($filter('translate')('deleteVersionError'))
                        .ok($filter('translate')('confirm'));

                    $mdDialog.show(deleteProcessInsteadModal);
                    return;
                }

                // show a confirmation modal for the version delete
                var confirm = $mdDialog.confirm()
                    .title($filter('translate')('deleteVersion'))
                    .content($filter('translate')('deleteVersionConf') + " " + version.version + " ?")
                    .ariaLabel($filter('translate')('deleteVersion'))
                    .targetEvent(event)
                    .ok($filter('translate')('confirm'))
                    .cancel($filter('translate')('cancel'));

                $mdDialog.show(confirm).then(
                    // agree callback
                    function () {
                        processService.deleteProcessVersion($scope.processId, $scope.workflowDefinition.activeDeploymentId).then(
                            // success delete callback
                            function (response) {
                                $scope.workflowDefinition = response.data;
                            }
                            // error delete callback
                            , function (response) {
                                exceptionModal(response);
                            }
                        );
                    }
                );
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Find the active version in the definition versions array
             * 
             * @returns {ProcessVersion}
             */
            $scope.findActiveVersion = function () {

                if ($scope.workflowDefinition == null)
                    return null;

                var versions = $scope.workflowDefinition.processVersions;

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
             * @memberOf ProcessDetailCtrl
             * @desc Displays a modal panel in order to add a new version by selecting a new BPM file
             * 
             * @param {event} event
             */
            $scope.addVersion = function (event) {
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
             * @memberOf ProcessDetailCtrl
             * @desc Shows a dialog with the process diagram
             * 
             * @param {event} event
             */
            $scope.showDiagram = function (event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog, process, service) {
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
                        'process': $scope.workflowDefinition
                    }
                })
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Displays all available icons
             * 
             * @param {event} event
             */
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
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Returns the label for the change status button
             * 
             * @returns {String}
             */
            $scope.newStatusButtonLabel = function () {
                var version = $scope.findActiveVersion() || {};

                switch (version.status) {
                    case 'new':
                    case 'inactive':
                        return 'activate';
                    case 'active':
                        return 'deActivate';
                }
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Changes the status of the selected version (new,inactive -> active  or active -> inactive)
             * 
             * @param {event} event
             */
            $scope.changeStatus = function (event) {

                //need save first in order to select active version
                if ($scope.saveFirst == true) {
                    var confirm = $mdDialog.alert()
                        .title($filter('translate')('saveFirst'))
                        .content($filter('translate')('saveFirst'))
                        .ariaLabel($filter('translate')('saveFirst'))
                        .targetEvent(event)
                        .ok($filter('translate')('close'));

                    $mdDialog.show(confirm).then(
                        // agree
                        function () {
                            $mdDialog.hide();
                        });

                } else {
                    var confirm = $mdDialog.confirm()
                        .title($filter('translate')('setActiveVersion'))
                        .content($filter('translate')('setActiveVersion'))
                        .ariaLabel($filter('translate')('setActiveVersion'))
                        .targetEvent(event)
                        .ok($filter('translate')('confirm'))
                        .cancel($filter('translate')('cancel'));

                    $mdDialog.show(confirm).then(
                        // agree
                        function () {
                            var version = $scope.findActiveVersion();
                            setVersionStatus(version, version.status === 'active' ? 'inactive' : 'active');
                        });
                }

            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Sets the status to the definition version
             * 
             * @param {ProcessVersion} version
             * @param {String} status
             */
            function setVersionStatus(version, status) {

                if (status === 'active') {
                    processService.setActiveVersion($scope.workflowDefinition.id, version.id).then(
                        // success callback
                        function (response) {
                            $scope.workflowDefinition = response.data;
                        }
                        // error callback
                        , function (response) {
                            exceptionModal(response);
                        }
                    );

                } else {
                    processService.deactivateVersion($scope.workflowDefinition.id, version.id).then(
                        // success callback
                        function (response) {
                            version.status = response.data.status;
                        }
                        // error callback
                        , function (response) {
                            exceptionModal(response);
                        }
                    );
                }
            };

            /************************************** Process definition tasks tab ***************************************/

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Event listener for definition's task tab. Prefetches all the required data while selecting the tab
             * 
             */
            $scope.clickedOnTaskDetailsTab = function () {
                var versions;
                var selectedVersionDeploymentId = $scope.workflowDefinition.activeDeploymentId;
                var selectedVersion;

                if (selectedVersionDeploymentId != null) {
                    versions = $scope.workflowDefinition.processVersions;
                    versions.forEach(function (entry) {

                        if (entry.deploymentId == selectedVersionDeploymentId)
                            selectedVersion = entry;
                    });

                    processService.getVersionTaskDetails(selectedVersion.id).then(
                        function (response) {
                            $scope.taskDetails = response.data;
                        }
                        // error callback
                        , function (response) {
                            exceptionModal(response);
                        }
                    );
                }
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Saves task's details. Such as a description and if the task is assigned by supervisor
             * 
             * @param {TaskDetails} taskDetails
             */
            $scope.saveTaskDetails = function (taskDetails) {
                processService.updateTaskDetails(taskDetails).then(
                    function () {
                        $mdDialog.hide();
                    }
                    // error callback
                    , function (response) {
                        $scope.exception = true;
                        $scope.exceptionMessage = response.data;
                    }
                );
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Returns to task list
             */
            $scope.goBackToTaks = function () {
                $scope.taskMetadataActiveView = "taskMetadata";
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Change task metadata tab to show task form items/details
             * 
             * @param {TaskDetails} taskDetails
             */
            $scope.goToTaskFormDetails = function (taskDetails) {
                $scope.editTaskDetails = taskDetails;
                $scope.taskMetadataActiveView = "taskFormItems";

                $scope.taskFormItems = [];

                processService.getTaskFormProperties(taskDetails.taskId, $scope.workflowDefinition.processDefinitionId).then(
                    //success callback
                    function (response) {
                        $scope.taskFormItems = response.data;
                    }
                    // error callback
                    , function (response) {
                        exceptionModal(response);
                    }
                );
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Edits a task's form item (task's form element/form property)
             * 
             * @param {FormProperty} formItem
             * @param {event} event
             */
            $scope.editFormItem = function (formItem, event) {

                $mdDialog.show({
                    controller: function ($scope, $mdDialog, formItem, editTaskDetails, workflowDefinition) {

                        $scope.formItem = formItem;
                        $scope.editTaskDetails = editTaskDetails;
                        $scope.workflowDefinition = workflowDefinition;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };

                        $scope.save = function () {
                            processService.saveTaskFormElement($scope.formItem, $scope.editTaskDetails.taskId, $scope.workflowDefinition.processDefinitionId).then(
                                //success save callback
                                function (response) {
                                    $mdDialog.hide();
                                }
                                //error save callback
                                , function (response) {
                                    exceptionModal(response);
                                }
                            );
                        };

                        $scope.clear = function () {
                            $scope.formItem.description = "";
                        };
                    },
                    templateUrl: 'templates/editFormItem.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: event,
                    locals: {
                        'formItem': formItem,
                        'editTaskDetails': $scope.editTaskDetails,
                        'workflowDefinition': $scope.workflowDefinition
                    }
                })
            };

            /************************************** Instances in progress ***************************************/

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Event listener for in progress instances tab. Prefetches all the required data while selecting the tab
             */
            $scope.clickedInProgressTab = function () {
                processService.getWorkflowInstances($scope.workflowDefinition.id).then(
                    // success callback
                    function (response) {
                        $scope.instances = response.data;
                    }
                    // error callback
                    , function (response) {
                        exceptionModal(response);
                    }
                );
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Change status of an instance to deleted and removes it from activiti
             * 
             * @param {ProcessInstance} instance
             * @param {event} event
             */
            $scope.cancelInstance = function (instance, event) {
                $mdDialog.show({
                    controller: cancelInstanceCtrl,
                    templateUrl: 'templates/cancelinstancemodal.tmpl.html',
                    parent: document.body,
                    scope: $scope,
                    targetEvent: event,
                    preserveScope: true,
                    clickOutsideToClose: true,
                    locals: {
                        'instance': instance
                    }
                });
            };

            function cancelInstanceCtrl($scope, $mdDialog, instance) {
                $scope.instance = instance;

                $scope.confirm = function () {
                    processService.cancelInstance($scope.instance.id).then(
                        // success callback
                        function () {
                            getInstances();
                            $mdDialog.cancel();
                        }
                        // error callback
                        , function (response) {
                            $mdDialog.cancel();
                            exceptionModal(response);
                        }
                    );
                };

                $scope.cancel = function () {
                    $mdDialog.cancel();
                };

                var getInstances = function () {
                    processService.getWorkflowInstances($scope.workflowDefinition.id).then(
                        // success callback
                        function (response) {
                            $scope.instances = response.data;
                        }
                    );
                }
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Deletes an instance
             * 
             * @param {ProcessInstance} instance
             * @param {event} event
             */
            $scope.deleteInstance = function (instance, event) {
                $mdDialog.show({
                    controller: deleteInstanceCtrl,
                    scope: $scope,
                    preserveScope: true,
                    templateUrl: 'templates/deleteInstance.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: event,
                    clickOutsideToClose: false,
                    locals: {
                        'instance': instance
                    }
                });
            };

            function deleteInstanceCtrl($scope, $mdDialog, instance) {

                $scope.instance = instance;

                $scope.cancel = function () {
                    $mdDialog.hide();
                };

                $scope.confirm = function () {
                    processService.deleteInstance($scope.instance.id).then(
                        // sucess callback
                        function (response) {
                            getInstances();
                            $mdDialog.cancel();
                        }
                        // error callback
                        , function (response) {
                            $mdDialog.cancel();
                            exceptionModal(response);
                        }
                    );
                };

                var getInstances = function () {
                    processService.getWorkflowInstances($scope.workflowDefinition.id).then(
                        // success callback
                        function (response) {
                            $scope.instances = response.data;
                        }
                    );
                }
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Either resume or suspend an instance
             * 
             * @param {ProcessInstance} instance
             * @param {String} action - resume/suspend
             * @param {event} event
             */
            $scope.actOnInstance = function (instance, action, event) {
                $mdDialog.show({
                    controller: ActOnInstanceConfirmController,
                    templateUrl: 'templates/actoninstancemodal.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: true,
                    locals: {
                        'instance': instance,
                        'action': action
                    }
                });
            };

            function ActOnInstanceConfirmController($scope, $mdDialog, instance, action) {

                $scope.instance = instance;
                $scope.action = action;

                $scope.confirm = function () {
                    processService.actOnInstance($scope.instance.id, action).then(
                        // success callback
                        function (response) {
                            $scope.instance.status = response.data.status;
                            $mdDialog.cancel();
                        }
                        // error callback
                        , function (response) {
                            $mdDialog.cancel();
                            exceptionModal(response);
                        }
                    );
                };

                $scope.cancel = function () {
                    $mdDialog.cancel();
                };
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Tab change listener
             * 
             * @param {String} tab
             */
            $scope.onTabSelected = function (tab) {
                $scope.taskMetadataActiveView = "taskMetadata";
                $scope.selectedTab = tab;
            };

            /**
             * @memberOf ProcessDetailCtrl
             * @desc Shows the progress of the instance as diagram
             * 
             * @param {ProcessInstance} instance
             * @param {event} event
             */
            $scope.showProgressDiagram = function (instance, event) {
                $mdDialog.show({
                    controller: function ($mdDialog) {

                        $scope.instance = instance;
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
             * @memberOf ProcessDetailCtrl
             * @desc Displays a modal panel showing the exception message
             * 
             * @param {any} response
             * @param {event} $event
             */
            function exceptionModal(response, $event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {

                        $scope.error = response.data;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                    },
                    templateUrl: 'templates/exception.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: $event,
                    clickOutsideToClose: false
                });
            };

        }]);
})(angular);