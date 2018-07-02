define(['angular'],

    function (angular) {

        'use strict';
        
        function ProcessService($http, config) {


            this.getSupervisors = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/user/role/' + 'ROLE_Supervisor');
            };

            /**
             * Returns an instnace by its id
             * 
             */
            this.getInstanceById = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/public/instance/' + instanceId);
            };

            /**
             * Returns true if the selected version is active
             * @param {WorkflowDefinition} process
             * @return {HttpPromise}
             * @name ProcessService#isProcessActive
             */
            this.isProcessActive = function (process) {
                var versions = process.processVersions;

                for (var index = 0; index < versions.length; index++) {
                    if (versions[index].deploymentId === process.activeDeploymentId) {
                        return (versions[index].status === 'active');
                    }
                }
                return false;
            };

            /**
             * Get Candidates for task
             */
            this.getCandidatesForTask = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId + '/candidates');
            };


            /**
             * Get all Candidates
             */
            this.getAllCandidates = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/candidates/all');
            };

            /**
             * @memberOf processService
             * @function notifyNoCandidates
             * @desc Notifies administrator for no available candidates
             *
             * @returns {HttpPromise}
             */
            this.notifyNoCandidates = function (taskId, user) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/v2/task/'+ taskId + '/candidates/nocandidates/'+ user);
            };

            /**
             * Returns all active process definitions
             */
            this.getActiveProcessDefinitions = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/process/active');
            };

            /**
             * Get completed tasks for user for supervisor
             */
            this.getCompletedTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/completed');
            };

            /**
             * Get completed instances for user
             */
            this.getCompletedInstances = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/instances/completed');
            }

            /**
             * Get user's completed tasks by instance ids
             */
            this.getCompletedTasksByInstanceIds = function (instancesId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/completed/instances?i=' + instancesId);
            };

            /**
             * Temporary saves task's form data
             */
            this.temporarySave = function (taskData) {

                var containsDocuments = false;
                var filesMap = new Map();

                var vars = taskData.taskForm;
                for (var i = 0; i < vars.length; i++) {

                    if (vars[i].type == 'document' && vars[i].value.file) {
                        containsDocuments = true;

                        var blob = new Blob([vars[i].value.file], { type: vars[i].value.file.type });
                        filesMap.set(vars[i].id, blob);
                        delete vars[i].value['file'];
                    }
                }

                if (containsDocuments) {
                    var url = config.WORKFLOW_SERVICE_ENTRY + '/task/tempsave';
                    return $http.post(url, { 'data': taskData, 'file': filesMap }, {
                        headers: { 'Content-Type': undefined },
                        transformRequest: function (data) {

                            var formData = new FormData();

                            formData.append('json', angular.toJson(data.data));

                            /*
                            for (var key of data.file.keys()) {
                                console.log()
                                formData.append('file', data.file.get(key), key);
                            }
                            */

                            data.file.forEach(function (v, k) {
                                formData.append('file', v, k);
                            });

                            return formData;
                        }
                    });

                } else {
                    return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/task/tempsave', taskData);
                }
            };

            /**
             * Get completed tasks by instance id
             */
            this.getCompletedTasksByInstances = function (instanceIds) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/instances?i=' + instanceIds);

            }

            /**
             * Get tasks by instance id
             */
            this.getTasksByInstanceId = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/instance/' + instanceId);
            }

            /**
             * Get user's completed tasks
             */
            this.getUserCompletedTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/completed/user');
            };

            this.getSearchedUserTasks = function (definitionKey, instanceTitle, after, before, isSupervisor) {

                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/completed/search:'
                    + definitionKey + ","
                    + instanceTitle + ","
                    + after + ","
                    + before + ","
                    + isSupervisor
                );
            };

            /**
             * Return a promise object for the list of all processes (workflow definition)
             * @return {HttpPromise}
             *
             * @name ProcessService#getProcesses
             */
            this.getProcesses = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process');
            };


            /**
             * Returns list of tasks that belong to instances supervised by the user in context
             */
            this.getSupervisedTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/supervised');
            };


            /**
             * Get tasks to be claimed by user according to user role
             */
            this.getClaimTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/claim');
            };

            /**
             * Unclaims a task 
             */
            this.unclaimTask = function (taskId) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/v2/task/' + taskId + '/unclaim');
            };

            /**
             * Claims a task
             */
            this.claimTask = function (taskId) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/v2/task/' + taskId + '/claim');
            }

            /**
             * Assign to a task assignee
             */
            this.setAssigneeToTask = function (taskData, assignee) {

                var containsDocuments = false;
                var filesMap = new Map();

                var vars = taskData.taskForm;
                for (var i = 0; i < vars.length; i++) {

                    if (vars[i].type == 'document' && vars[i].value && vars[i].value.file) {
                        containsDocuments = true;

                        var blob = new Blob([vars[i].value.file], { type: vars[i].value.file.type });
                        filesMap.set(vars[i].id, blob);
                        delete vars[i].value['file'];
                    }
                }

                if (containsDocuments) {

                    var url = config.WORKFLOW_SERVICE_ENTRY + '/task/assignee/' + assignee + '/';

                    return $http.post(url, { 'data': taskData, 'file': filesMap }, {
                        headers: { 'Content-Type': undefined },
                        transformRequest: function (data) {

                            var formData = new FormData();

                            formData.append('json', angular.toJson(data.data));

                            /*
                            for (var key of data.file.keys()) {
                                formData.append('file', data.file.get(key), key);
                            }
                            */

                            data.file.forEach(function (v, k) {
                                formData.append('file', v, k);
                            });

                            return formData;
                        }
                    });

                } else {

                    return $http.post(config.WORKFLOW_SERVICE_ENTRY
                        + '/task/assignee/' + assignee + '/', taskData);
                }
            };

            /**
             * Return a promise object for the process (workflow definition) object
             * @param {number} processId
             * @return {HttpPromise}
             *
             * @name ProcessService#getProcess
             */
            this.getProcess = function (processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId);
            };

            /**
             * Return a promise object for the process metadata
             * @param {number} processId
             * @return {HttpPromise}
             *
             * @name ProcessService#getProcessMetadata
             */
            this.getProcessMetadata = function (processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/form');
            };

            this.getTasksInProgress = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/inprogress/user');
            };

            /**
             * Updates the workflow definition
             * @param {WorkflowDefinition} process
             * @return {HttpPromise}
             *
             * @name ProcessService#updateProcess
             */
            this.updateProcess = function (process) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process', process);
            };

            /**
             * Updates the process definition version
             * @param {number} processId
             * @param {DefinitionVersion} version
             * @return {HttpPromise}
             *
             * @name ProcessService#updateProcessDefinitionVersion
             */
            this.updateProcessDefinitionVersion = function (processId, version) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY
                    + '/process/'
                    + processId
                    + '/version',
                    version
                );
            };

            /**
             * Sets the active version for the workflow definition
             * @param {number} processId
             * @param {number} versionId
             * @return {HttpPromise}
             *
             * @name ProcessService#setActiveVersion
             */
            this.setActiveVersion = function (processId, versionId) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY
                    + '/process/'
                    + processId
                    + '/version/active/'
                    + versionId,
                    null
                );
            };

            /**
             * Deactivate the version of the workflow definition
             * @param {number} processId
             * @param {number} versionId
             * @return {HttpPromise}
             *
             * @name ProcessService#deactivateVersion
             */
            this.deactivateVersion = function (processId, versionId) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY
                    + '/process/'
                    + processId
                    + '/version/inactive/'
                    + versionId,
                    null
                );
            };

            /**
             * creates a new process instance using the form data
             * @param {number} processId
             * @param {FormProperty[]} processForm
             * @return {HttpPromise}
             * @name ProcessService#startProcess
             */
            this.startProcess = function (processId, instanceData) {
                var containsDocuments = false;
                var filesMap = new Map();
                var vars = instanceData.processForm;

                for (var i = 0; i < vars.length; i++) {

                    if (vars[i].type == 'document') {

                        if (!vars[i].value.file) {
                            vars[i].value = null;
                            continue;
                        }

                        containsDocuments = true;

                        var blob = new Blob([vars[i].value.file], { type: vars[i].value.file.type });
                        filesMap.set(vars[i].id, blob);
                        delete vars[i].value['file'];
                    }
                }

                if (containsDocuments) {

                    var url = config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/document/start';

                    return $http.post(url, { 'data': instanceData, 'file': filesMap }, {
                        headers: { 'Content-Type': undefined },
                        transformRequest: function (data) {

                            var formData = new FormData();

                            formData.append('json', angular.toJson(data.data));
                            
                            /*
                            for (var key of data.file.keys()) {
                                formData.append('file', data.file.get(key), key);
                            }
                            */

                            data.file.forEach(function (v, k) {
                                formData.append('file', v, k);
                            });

                            return formData;
                        }
                    });

                } else {

                    return $http.post(config.WORKFLOW_SERVICE_ENTRY
                        + '/process/'
                        + processId
                        + '/start',
                        instanceData
                    );
                }
            };

            /**
             * Return a list of the processes, supervised by the authenticated user
             * @return {HttpPromise}
             * @name ProcessService#getSupervisedProcesses
             */
            this.getSupervisedProcesses = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/supervised');
            };

            /**
             * Get the task
             * @name ProcessService#getTask
             *
             * @param {string} taskId
             * @return {HttpPromise}
             */
            this.getTask = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/task/' + taskId);
            };

            /**
             * Get the task
             * @name ProcessService#getTask
             *
             * @param {string} taskId
             * @return {HttpPromise}
             */
            this.getCompletedTask = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId + '/completed');
            };

            /**
             * Returns the start event form by selected instance
             */
            this.getStartEventForm = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/instance/' + instanceId + '/startform');
            };

            /**
             * Retrieve all active tasks
             */
            this.getActiveTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/task');
            };

            /**
             * Completes a task and setting formdata to it
             */
            this.completeTask = function (taskData) {
                var containsDocuments = false;
                var filesMap = new Map();

                var vars = taskData.taskForm;
                for (var i = 0; i < vars.length; i++) {

                    if (vars[i].type == 'document' && vars[i].value.file) {
                        containsDocuments = true;
                        var blob = new Blob([vars[i].value.file], { type: vars[i].value.file.type });
                        filesMap.set(vars[i].id, blob);
                        delete vars[i].value['file'];
                    }
                }

                if (containsDocuments) {
                    var url = config.WORKFLOW_SERVICE_ENTRY + '/task/complete';
                    return $http.post(url, { 'data': taskData, 'file': filesMap }, {
                        headers: { 'Content-Type': undefined },
                        transformRequest: function (data) {

                            var formData = new FormData();

                            formData.append('json', angular.toJson(data.data));

                            /*
                            for (var key of data.file.keys()) {
                                formData.append('file', data.file.get(key), key);
                            }
                            */

                            data.file.forEach(function (v, k) {
                                formData.append('file', v, k);
                            });

                            return formData;
                        }
                    });

                } else {
                    return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/task/complete', taskData);
                }
            };

            /**
             * Save or update a document in the repository
             * 
             * @param {Document} wfDocument
             * @param {File} file
             *
             * @name ProcessService#saveDocument
             */
            this.saveDocument = function (instanceId, wfDocument, file) {
                var url = config.WORKFLOW_SERVICE_ENTRY + '/process/exec/' + instanceId + '/file';

                if (file !== null) {

                    return $http.post(url, { 'data': wfDocument, 'file': file }, {
                        headers: { 'Content-Type': undefined },
                        transformRequest: function (data) {

                            var formData = new FormData();

                            formData.append('json', new Blob([angular.toJson(data.data)], {
                                type: "application/json"
                            }));

                            formData.append("file", data.file);

                            return formData;
                        }
                    });

                } else {

                    return $http.put(url, wfDocument);
                }
            };

            /**
             * Get a list of documents for the process instance specified by the task id
             * 
             * @param {number} taskId
             *
             * @name ProcessService#getProcessInstanceDocuments
             */
            this.getProcessInstanceDocuments = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/task/' + taskId + '/document');
            };

            /**
             * @memberOf processService
             * @function getInProgressInstances
             * @desc Returns in progress instances by given criteria
             *
             * @returns {HttpPromise}
             */
            this.getInProgressInstances = function (definitionName, instanceTitle, after, before) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/inprogress/instances/search:'
                    + definitionName + ","
                    + instanceTitle + ","
                    + after + ","
                    + before
                );
            };

            /**
             * Returns instance's documents by id
             * 
             * @param {String} instanceId
             */
            this.getDocumentsByInstance = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/instance/' + instanceId + '/documents')
            };

            this.sendTaskDueDateNotification = function (taskId, content) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId + '/notification', content)
            };

        }

        angular.module('wfWorkspaceServices').service('processService', ['$http', 'CONFIG', ProcessService]);
    }
);