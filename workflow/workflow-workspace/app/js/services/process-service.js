(function () {
    angular.module('wfworkspaceServices').service('processService', ['$http', 'CONFIG',
        /**
         * @name processService
         * @ngDoc services
         * @memberof wfworkspaceServices
         * @desc Implements communication between the API and the client
         */
        function ($http, config) {

            /**
             * @memberOf processService
             * @function getSupervisors
             * @desc Returns all available supervisors
             * 
             * @returns {HttpPromise}
             */
            this.getSupervisors = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/user/role/' + 'ROLE_Supervisor');
            };

            /**
             * @memberOf processService
             * @function getInstanceById
             * @desc Returns an instnace by its id
             * 
             * @param {String} instanceId
             * @returns {HttpPromise}
             */
            this.getInstanceById = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/public/instance/' + instanceId);
            };

            /**
             * @memberOf processService
             * @function isProcessActive
             * @desc Returns true if the selected version is active
             * 
             * @param {WorkflowDefinition} process
             * @returns {Boolean}
             */
            this.isProcessActive = function (process) {
                var versions = process.processVersions;

                for (var index = 0; index < versions.length; index++) {

                    if (versions[index].deploymentId === process.activeDeploymentId)
                        return (versions[index].status === 'active');
                }
                return false;
            };

            /**
             * @memberOf processService
             * @function getCandidatesForTask
             * @desc Returns all available candidates for the given task
             * 
             * @param {String} taskId - Task's id to get candidates
             * @returns {HttpPromise}
             */
            this.getCandidatesForTask = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId + '/candidates');
            };

            /**
             * @memberOf processService
             * @function getAllCandidates
             * @desc Returns all users as candidates
             *
             * @returns {HttpPromise}
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
            this.notifyNoCandidates = function (taskId) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/task/'+ taskId + '/candidates/nocandidates');
            };

            /**
             * @memberOf processService
             * @function getActiveProcessDefinitions
             * @desc Returns all active process definitions
             * 
             * @returns {HttpPromise}
             */
            this.getActiveProcessDefinitions = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/active');
            };

            /**
             * @memberOf processService
             * @function getCompletedTasks
             * @desc Get all completed tasks (used by supervisor)
             * 
             * @returns {HttpPromise}
             */
            this.getCompletedTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/completed');
            };

            /**
             * @memberOf processService
             * @function getCompletedInstances
             * @desc Get completed instances for user
             * 
             * @returns {HttpPromise}
             */
            this.getCompletedInstances = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/instances/completed');
            };

            /**
             * 
             * @memberOf processService
             * @function getCompletedTasksByInstanceIds
             * @desc Get user's completed tasks by instance ids
             * 
             * @param {String} [instancesId]
             * @returns {HttpPromise}
             */
            this.getCompletedTasksByInstanceIds = function (instancesId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/completed/instances?i=' + instancesId);
            };

            /**
             * @memberOf processService
             * @function temporarySave
             * @desc Temporary saves a task 
             * 
             * @param {Task} taskData
             * @returns {HttpPromise}
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

                            for (var key of data.file.keys()) {
                                formData.append('file', data.file.get(key), key);
                            }
                            return formData;
                        }
                    });

                } else {
                    return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/task/tempsave', taskData);
                }
            };

            /**
             * @memberOf processService
             * @function getCompletedTasksByInstances
             * @desc Get completed tasks by instance id
             * 
             * @param {any} instanceIds
             * @returns {HttpPromise}
             */
            this.getCompletedTasksByInstances = function (instanceIds) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/instances?i=' + instanceIds);
            };

            /**
             * @memberOf processService
             * @function getTasksByInstanceId
             * @desc Get tasks by instance id
             * 
             * @param {String} instanceId
             * @returns {HttpPromise}
             */
            this.getTasksByInstanceId = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/instance/' + instanceId);
            };

            /**
             * @memberOf processService
             * @function getUserCompletedTasks
             * @desc Get user's completed tasks
             * 
             * @returns {HttpPromise}
             */
            this.getUserCompletedTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/completed/user');
            };

            /**
             * @memberOf processService
             * @function getSearchedUserTasks
             * @desc Searches for completed tasks based on given criteria
             * 
             * @param {String} definitionKey
             * @param {String} instanceTitle
             * @param {Number} after
             * @param {Number} before
             * @param {Boolean} isSupervisor
             * @returns {HttpPromise}
             */
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
             * @memberOf processService
             * @function getProcesses
             * @desc Return all processes
             * 
             * @returns {HttpPromise}
             */
            this.getProcesses = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process');
            };

            /**
             * @memberOf processService
             * @function getTasksByDefinitionsId
             * @desc Return a promise object for the tasks by an array of instances id
             * 
             * @param {any} instancesId
             * @returns {HttpPromise}
             */
            this.getTasksByDefinitionsId = function (instancesId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/unassigned?i=' + instancesId);
            };

            /**
             * @memberOf processService
             * @function getSupervisedTasks
             * @desc Returns list of tasks that belong to instances supervised by the user in context
             * 
             * @returns {HttpPromise}
             */
            this.getSupervisedTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/supervised');
            };

            /**
             * @memberOf processService
             * @function getClaimTasks
             * @desc Get tasks to be claimed by user according to user role
             * 
             * @returns {HttpPromise}
             */
            this.getClaimTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/claim');
            };

            /**
             * @memberOf processService
             * @function unclaimTask
             * @desc User unclaims himself from a task
             * 
             * @param {String} taskId
             * @returns {HttpPromise}
             */
            this.unclaimTask = function (taskId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId + '/unclaim');
            };

            /**
             * @memberOf processService
             * @function claimTask
             * @desc User claims an unassigned task
             * 
             * @param {String} taskId
             * @returns {HttpPromise}
             */
            this.claimTask = function (taskId) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId + '/claim');
            };

            /**
             * @memberOf processService
             * @function setAssigneeToTask
             * @desc Assign a user as assignee to a task
             * 
             * @param {Task} taskData
             * @param {String} assignee - Assignee's email
             * @returns {HttpPromise}
             */
            this.setAssigneeToTask = function (taskData, assignee) {

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

                    var url = config.WORKFLOW_SERVICE_ENTRY + '/task/assignee/' + assignee + '/';

                    return $http.post(url, { 'data': taskData, 'file': filesMap }, {
                        headers: { 'Content-Type': undefined },
                        transformRequest: function (data) {

                            var formData = new FormData();

                            formData.append('json', angular.toJson(data.data));

                            for (var key of data.file.keys()) {
                                formData.append('file', data.file.get(key), key);
                            }

                            return formData;
                        }
                    });

                } else
                    return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/task/assignee/' + assignee + '/', taskData);
            };

            /**
             * @memberOf processService
             * @function getProcess
             * @desc Returns a workflow definition by its id
             * 
             * @param {String} processId
             * @returns {HttpPromise}
             */
            this.getProcess = function (processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId);
            };

            /**
             * @memberOf processService
             * @function getProcessMetadata
             * @desc Returns a definition's form data
             * 
             * 
             * @param {String} processId
             * @returns {HttpPromise}
             */
            this.getProcessMetadata = function (processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/form');
            };

            /**
             * @memberOf processService
             * @function getTasksInProgress
             * @desc Returns all available in progress tasks
             * 
             * @returns {HttpPromise}
             */
            this.getTasksInProgress = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/inprogress/user');
            };

            /**
             * @memberOf processService
             * @function startProcess
             * @desc Creates a new process instance using the form data
             * 
             * @param {String} processId
             * @param {ProcessInstance} instanceData
             * @returns {HttpPromise}
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

                            for (var key of data.file.keys()) {
                                formData.append('file', data.file.get(key), key);
                            }

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
             * @memberOf processService
             * @function getUnassignedTasks
             * @desc Get all unassigned tasks by given processes
             * 
             * @param {WorkflowDefinition} [processes]
             * @returns {HttpPromise}
             */
            this.getUnassignedTasks = function (processes) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/unassigned', { params: { 'p': processes } });
            };

            /**
             * @memberOf processService
             * @function getTask
             * @desc Returns a task by given id
             * 
             * @param {String} taskId
             * @returns {HttpPromise}
             */
            this.getTask = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId);
            };

            /**
             * @memberOf processService
             * @function getStartEventForm
             * @desc Returns instance's start form by a given instance id
             * 
             * @param {String} instanceId
             * @returns {HttpPromise}
             */
            this.getStartEventForm = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/instance/' + instanceId + '/startform');
            };

            /**
             * @memberOf processService
             * @function completeTask
             * @desc Completes a given task
             * 
             * @param {Task} taskData
             * @returns {HttpPromise}
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

                            for (var key of data.file.keys()) {
                                formData.append('file', data.file.get(key), key);
                            }

                            return formData;
                        }
                    });

                } else {
                    return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/task/complete', taskData);
                }
            };

            /**
             * @memberOf processService
             * @function saveDocument
             * @desc Save or update a document in the repository
             * 
             * @param {String} instanceId
             * @param {Document} wfDocument
             * @param {File} file
             * @returns {HttpPromise}
             */
            this.saveDocument = function (instanceId, wfDocument, file) {
                var url = config.WORKFLOW_SERVICE_ENTRY
                    + '/process/exec/'
                    + instanceId
                    + '/file';

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
             * @memberOf processService
             * @function getProcessInstanceDocuments
             * @desc Get a list of documents for the process instance specified by the task id
             * 
             * @param {String} taskId
             * @returns {HttpPromise}
             */
            this.getProcessInstanceDocuments = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId + '/document');
            };

            /**
             * @memberOf processService
             * @function getInProgressInstances
             * @desc Returns all in progress instances
             * 
             * @returns {HttpPromise}
             */
            this.getInProgressInstances = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/inprogress/instances');
            };

            /**
             * @memberOf processService
             * @function getDocumentsByInstance
             * @desc Returns instance's documents by id
             * 
             * @param {String} instanceId
             * @returns {HttpPromise}
             */
            this.getDocumentsByInstance = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/instance/' + instanceId + '/documents')
            };

        }]
    );
})(angular);