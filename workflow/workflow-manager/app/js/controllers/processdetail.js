define(['angular', 'services/processservice', 'services/authprovider'],

    function (angular) {

        'use strict';

        function processDetailCtrl($scope, $http, $routeParams, $location, $mdDialog, $filter, processService, authProvider, config) {

            document.addEventListener("trix-focus", function(event) {
                event.target.toolbarElement.style.display = "block";
            });

            document.addEventListener("trix-blur", function(event) {
                event.target.toolbarElement.style.display = "none";
            });

            // get the definition's id from the url
            $scope.processId = $routeParams.processId;

            // a variable that holds if the definition needs to be saved f
            $scope.saveFirst = false;

            // select the "process tab" during the first time open of the page
            $scope.selectedTab = "process";

            // show progress bar
            $scope.showProgress = { show: true };

            $scope.editTaskDetails = null;
            $scope.instances = null;
            $scope.selectedTask = null;
            $scope.userTaskFormElements = [];
            $scope.externalFormsActiveView = "formList";
            $scope.taskMetadataActiveView = "taskMetadata";

            $scope.isManager = false;
            $scope.isSupervisor = false;
            $scope.isOnlyUser = false;

            if (authProvider.getRoles().length == 1 && authProvider.getRoles().indexOf("ROLE_Manager") > -1)
                $scope.isManager = true;

            if(authProvider.getRoles().indexOf("ROLE_Supervisor") > -1)
                $scope.isSupervisor = true;

            if(authProvider.getRoles().indexOf("ROLE_Admin") < 0 &&
                    authProvider.getRoles().indexOf("ROLE_Supervisor") < 0 &&
                    authProvider.getRoles().indexOf("ROLE_ProcessAdmin") < 0 &&
                    authProvider.getRoles().indexOf("ROLE_User") > -1)
                $scope.isOnlyUser = true;

            /****************** Process tab ******************/

            // returns the process and its metadata such as registries, user groups
            function getProcess() {
                // get the process data
                processService.getProcess($scope.processId).then(
                    // success callback
                    function (response) {
                        $scope.workflowDefinition = response.data;

                        // check if definition has already been saved at least one time
                        if ($scope.workflowDefinition.owner == null)
                            $scope.saveFirst = true;

                        // get the active version of the definition
                        if ($scope.workflowDefinition.activeDeploymentId == null) {
                            var version = response.data.processVersions[0];
                            $scope.workflowDefinition.activeDeploymentId = version.deploymentId;
                        }

                        // set a default icon if hasn't been set
                        $scope.workflowDefinition.icon = $scope.workflowDefinition.icon || config.DEFAULT_AVATAR;

                        // substring the icon name in order to remove the ".svg" from its name
                        var endIndex = $scope.workflowDefinition.icon.indexOf(".");
                        $scope.iconName = $scope.workflowDefinition.icon.substring(0, endIndex);

                        // get all available registries
                        processService.getRegistries().then(
                            // success callback
                            function (response) {
                                $scope.registries = response.data;
                            }
                        );

                        //checking if user has role admin in order to show all groups/owners
                        if (authProvider.getRoles().indexOf("ROLE_Admin") >= 0 || authProvider.getRoles().indexOf("ROLE_Manager") > -1) {
                            processService.getGroups().then(
                                // success callback
                                function (response) {
                                    $scope.groups = response.data;
                                });
                        } else {
                            processService.getUserGroups().then(
                                function (response) {
                                    $scope.groups = response.data;
                                });
                        }
                    }
                    // error callback
                    , function (response) {
                        exceptionModal(response);
                    }
                ).finally(function () {
                    $scope.showProgress.show = false;
                });
            }

            getProcess();

            /**
             * Saves the Process Definition form
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
             * Click event Handler for the Delete Process button
             * @param {event} event
             */
            $scope.askDeleteProcess = function (event) {

                var confirm = $mdDialog.confirm()
                    .title($filter('translate')('deleteProcess'))
                    .content($filter('translate')('deleteProcessConf') + " " + $scope.workflowDefinition.name + " ?")
                    .ariaLabel($filter('translate')('deleteProcess'))
                    .targetEvent(event)
                    .ok($filter('translate')('confirm'))
                    .cancel($filter('translate')('cancel'));

                $mdDialog.show(confirm).then(function () {
                    processService.deleteProcess($scope.processId).then(
                        // success callback
                        function () {
                            $location.path('/process');
                        },
                        // error callback
                        function (response) {
                            exceptionModal(response);
                        });
                });
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
                            .title($filter('translate')('deleteVersionError'))
                            .content($filter('translate')('versionNotFound'))
                            .ariaLabel($filter('translate')('deleteVersionError'))
                            .ok($filter('translate')('confirm'))
                    );
                    return;
                }

                var confirm = $mdDialog.confirm()
                    .title($filter('translate')('deleteVersion'))
                    .content($filter('translate')('deleteVersionConf') + " " + version.version + " ?")
                    .ariaLabel($filter('translate')('deleteVersion'))
                    .targetEvent(event)
                    .ok($filter('translate')('confirm'))
                    .cancel($filter('translate')('cancel'));

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
                            exceptionModal(response);
                        });
                });
            };

            /**
             * Click event handler fot the Add Version button
             * @param event
             */
            $scope.showAddVersion = function (event) {
                $mdDialog.show({
                    controller: 'AddDefinitionController',
                    templateUrl: 'templates/adddefinition.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: false,
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
             * Shows dialog to select an icon for the process
             */
            $scope.showIconSelect = function (event) {
                $mdDialog.show({
                    controller: DialogController,
                    templateUrl: 'templates/iconselect.tmpl.html',
                    parent: document.body,
                    targetEvent: event,
                    clickOutsideToClose: true

                }).then(function (answer) {
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
                        return 'activate';
                    case 'active':
                        return 'deActivate';
                }
            };

            /**
             *
             */
            $scope.getVersionJustification = function () {
                var version = $scope.findActiveVersion() || {};

                return version.justification;
            }

            /**
             * Changes the status of the selected version
             * (new,inactive -> active  or active -> inactive)
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
                        }
                    );
                    // no save required	
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
                            $scope.setVersionStatus(version, version.status === 'active' ? 'inactive' : 'active');
                        }
                    );
                }
            };

            /**
             * Find the active version in the definition versions array
             * @return DefinitionVersion
             */
            $scope.findActiveVersion = function () {

                if ($scope.workflowDefinition == null)
                    return null;

                var versions = $scope.workflowDefinition.processVersions;

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
             * Sets the status to the definition version
             * @param {DefinitionVersion} version
             * @param {string} status
             */
            $scope.setVersionStatus = function (version, status) {
                $scope.showProgress.show = true;

                if (status === 'active') {
                    processService.setActiveVersion($scope.workflowDefinition.id, version.id).then(
                        // success callback
                        function (response) {
                            $scope.workflowDefinition = response.data;
                        },
                        // fail callback
                        function (response) {
                            alertStatusChangeFailed(response);
                        }
                    ).finally(function () {
                        $scope.showProgress.show = false;
                    });

                } else {
                    processService.deactivateVersion($scope.workflowDefinition.id, version.id).then(
                        // success callback
                        function (response) {
                            version.status = response.data.status;
                        },
                        // fail callback
                        function (response) {
                            alertStatusChangeFailed(response);
                        }

                    ).finally(function () {
                        $scope.showProgress.show = false;
                    });
                }
            };

            /****************** Tasks tab ******************/

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
                        // success callback
                        function (response) {
                            $scope.taskDetails = response.data;
                        },
                        // error callback
                        function (response) {
                            alertGenericResponseError(response);
                        }
                    );
                }
            };

            /**
             * Show modal window with the task description
             */
            $scope.editTaskDescription = function (id, event) {

                $scope.taskDetails.forEach(function (entry) {
                    if (entry.id == id) {
                        $scope.selectedTask = entry;

                    }
                });
                $mdDialog.show({
                    controller: function ($scope, $mdDialog, $http, task) {
                        $scope.editValue = task;


                        $scope.save = function () {
                            processService.updateTaskDetails($scope.editValue).then(
                                function () {
                                    $mdDialog.hide();

                                }, function (response) {
                                    $scope.exception = true;
                                    $scope.exceptionMessage = response.data;

                                });
                        };

                        $scope.cancel = function () {
                            $mdDialog.cancel();
                        };

                    },
                    templateUrl: 'templates/updatetaskdetails.tmpl.html',
                    parent: document.body,
                    locals: {
                        'task': $scope.selectedTask,
                        'exception': $scope.exception,
                        'exceptionMessage': $scope.exceptionMessage,
                    },
                    clickOutsideToClose: true
                })

            };


            function UpdateTaskController($scope, $mdDialog, $http, task) {
                $scope.editValue = task;
            }

            /**
             * Cancel an instance
             */
            $scope.cancelInstance = function (instance, event) {
                $mdDialog.show({
                    controller: CancelInstanceConfirmController,
                    templateUrl: 'templates/cancelinstancemodal.tmpl.html',
                    parent: document.body,
                    locals: {
                        'instance': instance,
                        'instances': $scope.instances
                    },
                    clickOutsideToClose: true
                })
            }

            function CancelInstanceConfirmController($scope, $mdDialog, $http, instance, instances) {
                $scope.instance = instance;
                $scope.instances = instances;

                $scope.confirm = function () {
                    processService.cancelInstance($scope.instance.id).then(
                        // success callback
                        function () {
                            getInstances();
                            $mdDialog.hide();
                            /*
                            var i = $scope.instances.indexOf($scope.instance);
                        	
                            if(i!=-1)
                                $scope.instances.splice(i,1);
                            	
                            */
                        }
                        // error callback
                        , function (response) {
                            alertGenericResponseError(response);
                        }
                    );
                };

                $scope.cancel = function () {
                    $mdDialog.cancel();
                };
            }

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
             * Act on instance (suspend/resume)
             */
            $scope.actOnInstance = function (instance, action, event) {
                $mdDialog.show({
                    controller: ActOnInstanceConfirmController,
                    templateUrl: 'templates/actoninstancemodal.tmpl.html',
                    parent: document.body,
                    locals: {
                        'instance': instance,
                        'action': action
                    },
                    clickOutsideToClose: true
                })
            }

            function ActOnInstanceConfirmController($scope, $mdDialog, $http, instance, action) {
                $scope.instance = instance;
                $scope.action = action;

                $scope.confirm = function () {
                    processService.actOnInstance($scope.instance.id, action)
                        .then(
                        function (response) {
                            $scope.instance.status = response.data.status;
                            $mdDialog.hide();
                        },
                        function (response) {
                            alertGenericResponseError(response);
                        }
                        );
                };
                $scope.cancel = function () {
                    $mdDialog.cancel();
                };
            }

            /**
             * Put here function that returns workflow instances
             */
            $scope.clickedInProgressTab = function () {
                getInstances();
            };

            function getInstances() {
                processService.getWorkflowInstances($scope.workflowDefinition.id).then(
                    // sucess callback
                    function (response) {
                        $scope.instances = response.data;

                        if ($scope.instances == null || $scope.instances.length == 0)
                            return;
                    }
                );
            }

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
                        'instance': $scope.instance,
                    }
                })
            };

            /*=============== External Forms ===============*/

            $scope.clickedExtFormsTab = function () {

                $scope.supervisors;
                $scope.xforms = null;
                $scope.registries;

                $scope.xform;

                processService.getExternalForms($scope.workflowDefinition.id).then(
                    // success callback
                    function (response) {
                        $scope.xforms = response.data;
                    },
                    // fail callback
                    function (response) {
                        alertGenericResponseError(response);
                    }
                );

                processService.getUsers().then(
                    // success callback
                    function (response) {
                        $scope.supervisors = response.data;
                    },
                    // fail callback
                    function (response) {
                        alertGenericResponseError(response);
                    }
                );

                processService.getExternalGroups().then(
                    // success callback
                    function (response) {
                        $scope.extFormGroups = response.data;
                    },
                    //error callback
                    function () {

                    }
                );
            };

            $scope.goToExtFormDetails = function (externalForm) {
                $scope.externalFormsActiveView = "formDetails";
                $scope.extForm = externalForm;
            };

            /**
             * Update an external form
             */
            $scope.updateExternalForm = function () {
                processService.updateExternalForm($scope.extForm).then(
                    // success callback
                    function () {
                        $scope.externalFormsActiveView = "formList";
                    }
                );
            };

            $scope.goBackToFormList = function () {
                $scope.externalFormsActiveView = "formList";
            };

            /**
             * Delete external form
             */
            $scope.deleteExternalForm = function () {

                var confirm = $mdDialog.confirm()
                    .title($filter('translate')('deleteExternalForm'))
                    .content($filter('translate')('deleteExternalFormConf'))
                    .ariaLabel($filter('translate')('deleteExternalForm'))
                    .targetEvent(event)
                    .ok($filter('translate')('confirm'))
                    .cancel($filter('translate')('cancel'));

                $mdDialog.show(confirm).then(
                    // agree
                    function () {
                        processService.deleteExternalForm($scope.extForm.formId).then(
                            // success callback
                            function (response) {
                                processService.getExternalForms($scope.workflowDefinition.id).then(
                                    // success callback
                                    function (response) {
                                        $scope.xforms = response.data;
                                        $scope.externalFormsActiveView = "formList";
                                    },
                                    // fail callback
                                    function (response) {
                                        alertGenericResponseError(response);
                                    }
                                );
                            },
                            // error callback
                            function (response) {
                                alertGenericResponseError(response);
                            });
                    });
            };

            /**
             * Add external form
             */
            $scope.addExternalForm = function (event) {

                $mdDialog.show({
                    controller: function ($scope, $mdDialog, workflowDefinition, supervisors) {

                        $scope.createFromGroup = false;
                        $scope.isNew = true;

                        $scope.extForm = null;

                        $scope.processes = [];
                        $scope.processes.push(workflowDefinition);

                        $scope.supervisors = supervisors;

                        function getGroups() {
                            processService.getGroupsFormsWrapped().then(
                                function (response) {
                                    $scope.wrappedGroups = ArrayUtil.mapByProperty(response.data, "groupName");

                                    var groups = {};
                                    response.data.forEach(function (o) {
                                        var group = (o["groupName"] || "!empty!").toString();
                                        if (!groups.hasOwnProperty(group)) {
                                            groups[group] = o["groupId"];
                                        }
                                    });

                                    if (!groups.hasOwnProperty("!empty!")) {
                                        groups["!empty!"] = null;
                                    }
                                    $scope.groups = Object.keys(groups).sort().map(function (group) {
                                        return { name: group, groupId: groups[group] };
                                    });

                                    $scope.groupNames = Object.keys($scope.wrappedGroups).sort();
                                    //error callback
                                }, function (response) {
                                    exceptionModal(response);
                                }
                            );
                        };

                        getGroups();

                        $scope.save = function () {
                            processService.saveExternalForm($scope.extForm).then(
                                function (response) {
                                    $mdDialog.hide();
                                    getForms();

                                    //error callback	
                                }, function (response) {
                                    $mdDialog.cancel();
                                    //exceptionModal(response);
                                });
                        };

                        function getForms() {
                            processService.getExternalForms(workflowDefinition.id).then(
                                // success callback
                                function (response) {
                                    $scope.xforms = response.data;
                                },
                                // fail callback
                                function (response) {
                                    alertGenericResponseError(response);
                                }
                            );
                        }


                        $scope.cancel = function () {
                            $mdDialog.cancel();
                        };

                    },
                    scope: $scope,
                    preserveScope: true,
                    templateUrl: 'templates/externalForm.tmpl.html',
                    parent: document.body,
                    clickOutsideToClose: true,
                    targetEvent: event,
                    locals: {
                        'workflowDefinition': $scope.workflowDefinition,
                        'supervisors': $scope.supervisors
                    }
                })
            };

            /**
             * Edit External form dialog
             */
            $scope.editXForm = function (xform, event) {

                $scope.xform = xform;

                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };

                        $scope.update = function () {
                            processService.updateExternalForm($scope.xform).then(
                                // success callback
                                function () {
                                    $mdDialog.hide();
                                },
                                // error callback
                                function (response) {
                                    alertGenericResponseError(response);
                                });
                        };


                        $scope.resumeExternalForm = function () {
                            var action = 'resume';
                            processService.actOnExternalForm($scope.xform.formId, action).then(
                                // success callback
                                function (response) {
                                    xform.enabled = response.data.enabled;
                                    $mdDialog.hide();
                                },
                                // error callback
                                function (response) {
                                    alertGenericResponseError(response);
                                }
                            );
                        };

                        $scope.suspendExternalForm = function () {
                            var action = 'suspend';
                            processService.actOnExternalForm(xform.formId, action).then(
                                // success callback
                                function (response) {
                                    xform.enabled = response.data.enabled;
                                    $mdDialog.hide();
                                },
                                // error callback
                                function (response) {
                                    alertGenericResponseError(response);
                                }
                            );
                        };
                    },
                    scope: $scope,
                    preserveScope: true,
                    templateUrl: 'templates/editxform.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: event,
                    locals: {
                        'xform': $scope.xform,
                        'supervisors': $scope.supervisors,
                    }
                })
            };

            /**
             * Helper function for showing error alert
             * @param response
             */
            function alertStatusChangeFailed(response) {
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


            /**
             * Helper function for showing generic error alert
             * @param response
             */
            function alertGenericResponseError(response) {
                $mdDialog.show($mdDialog.alert()
                    .parent(document.body)
                    .clickOutsideToClose(true)
                    .title($filter('translate')('error'))
                    .content(response.data.message)
                    .ok($filter('translate')('confirm'))
                );
            }

            /**
             * Tab change listener
             */
            $scope.onTabSelected = function (tab) {
                $scope.taskMetadataActiveView = "taskMetadata";
                $scope.selectedTab = tab;
            };

            /**
             * Change task metadata tab to show task form items/details
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
                );
            };

            $scope.saveTaskDetails = function (taskDetails) {
                processService.updateTaskDetails(taskDetails).then(
                    function () {
                        $mdDialog.hide();

                    }, function (response) {
                        $scope.exception = true;
                        $scope.exceptionMessage = response.data;

                    });

            };

            /**
             * Edit External form dialog
             */
            $scope.editFormItem = function (formItem, event) {

                $scope.formItem = formItem;

                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };

                        $scope.save = function () {

                            processService.saveTaskFormElement($scope.formItem, $scope.editTaskDetails.taskId, $scope.workflowDefinition.processDefinitionId).then(

                                //success callback
                                function (response) {
                                    $mdDialog.hide();

                                    //error callback	
                                }, function (response) { }
                            );
                        };

                        $scope.clear = function () {
                            $scope.formItem.description = "";
                        };

                    },

                    scope: $scope,
                    preserveScope: true,
                    templateUrl: 'templates/editFormItem.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: event,
                    locals: {
                        'formItem': $scope.formItem,
                    }
                })
            };


            /**
             * Return to task list
             */
            $scope.goBackToTaks = function () {
                $scope.taskMetadataActiveView = "taskMetadata";
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
                })
            }

        }

        angular.module('wfManagerControllers').controller('ProcessDetailCtrl', ['$scope', '$http', '$routeParams', '$location', '$mdDialog', '$filter', 'processService', 'auth', 'CONFIG', processDetailCtrl]);
    }
);

