(function () {
    angular.module('wfmanagerServices').service('processService', ['$http', 'CONFIG',

		/**
		 * @name processService
		 * @ngDoc services
		 * @memberof wfmanagerServices
		 * @desc Implements communication between the API and the client
		 */
        function ($http, config) {

            /**
             * @memberOf processService
             * @desc Creates a new process definition from a BPMN file
             * 
             * @function createProcess
             * @param {File} file - The BPMN file of the process
             * @returns {HttpPromise}
             */
            this.createProcess = function (file) {
                var url = config.WORKFLOW_SERVICE_ENTRY + '/processbpmn';

                var fd = new FormData();
                fd.append('file', file);

                return $http.post(url, fd, {
                    transformRequest: angular.identity,
                    headers: { 'Content-Type': undefined }
                });
            };

            /**
             * @memberof processService
             * @desc Returns all groups from realm 
             * 
             * @returns {HttpPromise}
             */
            this.getGroups = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/group');
            };

            /**
             * @memberof processService
             * @desc Returns user groups from realm
             * 
             * @returns {HttpPromise}
             */
            this.getUserGroups = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/user/group');
            };

            /**
             * @memberof processService
             * @desc Creates a user task form element and then saves it
             * 
             * 
             * @param {any} formItem
             * @param {String} taskDefinitionKey
             * @param {any} definitionVersion
             * 
             * @returns {HttpPromise}
             */
            this.saveTaskFormElement = function (formItem, taskDefinitionKey, definitionVersion) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/' + encodeURIComponent(definitionVersion)
                    + '/task/' + taskDefinitionKey + '/formelement', formItem);
            };

            /**
             * @memberof processService
             * @desc Create a new version for the given process based on an uploaded BPMN file
             * 
             * @param {number} processId - processId The process id
             * @param {File} file - The BPMN File
             * @returns {HttpPromise}
             */
            this.createProcessVersion = function (processId, file) {
                var url = config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId;

                var fd = new FormData();
                fd.append('file', file);

                return $http.post(url, fd, {
                    transformRequest: angular.identity,
                    headers: { 'Content-Type': undefined }
                });
            };

            /**
             * @memberof processService
             * @desc Deletes a process with the given id
             * 
             * @param {String} processId - The process (workflow definition) id
             * @returns {HttpPromise}
             */
            this.deleteProcess = function (processId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId);
            };

            /**
             * @memberof processService
             * @desc Delete the identified process version
             * 
             * @param {String} processId - The process (workflow definition) id
             * @param {String} deploymentId - The process version deployment id
             * @returns {HttpPromise}
             */
            this.deleteProcessVersion = function (processId, deploymentId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/' + deploymentId);
            };

            /**
             * @memberof processService
             * @desc Return a promise object for the list of all processes (workflow definition) 
             * 
             * @returns {HttpPromise}
             */
            this.getProcesses = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process');
            };

            /**
            * @memberof processService
            * @desc Return a promise object for the list of processes by selected owners (workflow definition)
            * 
            * @param {list} selectedOwners
            * @returns {HttpPromise}
            */
            this.getProcessesByOwners = function (selectedOwners) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/filter?owners=' + encodeURIComponent(selectedOwners));
            };

            /**
             * @memberof processService
             * @desc Return a promise object for the process (workflow definition) object
             * 
             * @param {number} processId
             * @returns {HttpPromise}
             */
            this.getProcess = function (processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId);
            };

            /**
             * @memberof processService
             * @desc Updates the workflow definition 
             * 
             * @param {WorkflowDefinition} workflowDefinition - The updated WorkflowDefinition
             * @returns {HttpPromise}
             */
            this.updateProcess = function (workflowDefinition) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process', workflowDefinition);
            };

            /**
             * @memberof processService
             * @desc Returns a list of wftasks based on instance id 
             * 
             * @param {number} instanceId - Instance's id to get tasks
             * @returns
             */
            this.getTasksByInstanceId = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/instance/' + instanceId)
            };

            /**
             * @memberof processService
             * @desc Get task by task id
             * API: @name ExecutionController#getTask
             * 
             * @param {String} taskId
             * @returns {HttpPromise}
             */
            this.getTask = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId);
            };

            /**
             * @memberof processService
             * @desc Returns a task by task definition key
             * API: @name ExecutionController#getTask 
             * 
             * @param {String} taskDefinitionKey
             * @param {String} processId
             * @returns {HttpPromise}
             */
            this.getTaskFormProperties = function (taskDefinitionKey, processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/taskdefinition/' + taskDefinitionKey + '/process/' + processId);
            };

            /**
             * @memberof processService
             * @desc Sets the active version for the workflow definition
             * API: @name ProcessController#setActiveVersion 
             * 
             * @param {number} processId
             * @param {number} versionId
             * @returns {HttpPromise}
             */
            this.setActiveVersion = function (processId, versionId) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/version/active/' + versionId, null);
            };

            /**
             * @memberof processService
             * @desc Deactivate the version of the workflow definition
             * API: @name ProcessController#deactivateVersion 
             * 
             * @param {number} processId
             * @param {number} versionId
             * @returns {HttpPromise}
             */
            this.deactivateVersion = function (processId, versionId) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/version/inactive/' + versionId, null);
            };

            /**
             * @memberof processService
             * @desc Returns true if the selected version is active 
             * 
             * @param {WorkflowDefinition} process
             * @returns {Boolean} If the given Workflow Definition is active
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
             * @memberof processService
             * @desc Returns a promise object for the list of the task details of the process specified version
             * API: @name ProcessController#getVersionsTaskDetails 
             * 
             * @param {Number} versionid
             * @returns {HttpPromise}
             */
            this.getVersionTaskDetails = function (versionid) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/version/' + versionid);
            };

            /**
             * @memberof processService
             * @desc Update a UserTaskDetails object
             * API: @name ProcessController#updateTaskDetails 
             * 
             * @param {Task} task
             * @returns {HttpPromise}
             */
            this.updateTaskDetails = function (task) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/taskdetails', task);
            };

            /**
             * @memberof processService
             * @desc Cancel a running instance
             * API: @name ProcessController#cancelProcessInstance
             * 
             * @param {String} instanceid
             * @returns {HttpPromise}
             */
            this.cancelInstance = function (instanceid) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/process/instance/' + instanceid);
            };

            /**
             * @memberof processService
             * @desc Deletes an instance by a given id
             * 
             * @param {String} instanceid
             * @returns {HttpPromise}
             */
            this.deleteInstance = function (instanceid) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/process/instance/' + instanceid);
            };

            /**
             * @memberof processService
             * @desc Suspend / Resume a running instance.
             * API: @name ProcessController#modifyProcessInstanceStatus
             * 
             * @param {String} instanceid - Instace's id to be suspened or resusmed
             * @param {String} action - A string represents the action to be done in the instance (suspend/resume)
             * @returns {HttpPromise}
             */
            this.actOnInstance = function (instanceid, action) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/instance/' + instanceid + "/" + action);
            };

            /**
             * @memberOf processService
             * @function getActiveProcesses
             * @desc Gets all active processes
             *
             * @returns {HttpPromise}
             */
            this.getActiveProcesses = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/active');
            };

            /**
             * @memberOf processService
             * @function getEndedInstances
             * @desc Searches for ended instances based on given criteria
             *
             * @param {String} definitionName
             * @param {String} instanceTitle
             * @param {Number} dateAfterTime
             * @param {Number} dateBeforeTime
             * @returns {HttpPromise}
             */
            this.getEndedInstances = function (definitionName, instanceTitle, dateAfterTime, dateBeforeTime) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/instances/ended/search:'
                    + definitionName + ","
                    + instanceTitle + ","
                    + dateAfterTime + ","
                    + dateBeforeTime
                );
            };

            /**
             * @memberof processService
             * @desc Get user activity, all tasks having the specified user as assignee
             * API: @name ProcessController#getUserActivity 
             * 
             * @param {Number} after
             * @param {Number} before
             * @param {String} userId
             * @returns {HttpPromise}
             */
            this.getUserActivity = function (after, before, userId) {
                var url = config.WORKFLOW_SERVICE_ENTRY + '/task/user/search/' + after + "," + before + "," + userId;

                return $http.get(url);
            };

            /**
             * @memberof processService
             * @desc Retrieve all active tasks
             * API: @name ExecutionController#getAllActiveTasks
             *
             * @returns {HttpPromise}
             */
            this.getActiveTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task');
            };

            /**
             * @memberof processService
             * @desc Retrieve active tasks by given criteria
             * API: @name ExecutionController#getActiveTasksByCriteria
             *
             * @returns {HttpPromise}
             */
            this.getActiveTasksByCriteria = function (definitionName, taskName, after, before) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/search:'
                    + definitionName + ","
                    + taskName + ","
                    + after + ","
                    + before);
            };

            /**
             * @memberof processService
             * @desc Retrieve all users
             * API: @name RealmController#getUsers
             * 
             * @returns {HttpPromise}
             */
            this.getUsers = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/user');
            };

            /**
             * @memberof processService
             * @desc Get the current settings
             * API: @name ProcessController#getSettings
             * 
             * @returns {HttpPromise}
             */
            this.getSettings = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/settings');
            };

            /**
             * @memberof processService
             * @desc Update system
             * API: @name ProcessController#updateSettings
             * 
             * @param {Settings} settings
             * @returns {HttpPromise}
             */
            this.updateSettings = function (settings) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/settings', settings);
            };

            /**
             * @memberof processService
             * @desc Returns instances by process definition
             * API: @name ProcessController#getProcessInstances 
             * 
             * @param {String} workflowDefinitionId
             * @returns {HttpPromise}
             */
            this.getWorkflowInstances = function (workflowDefinitionId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/' + workflowDefinitionId + '/instance');
            };

            /**
             * @memberof processService
             * @desc Delete a process instance by id
             * API: @name ExecutionController#deleteProcessCompletedInstance 
             * 
             * @param {String} instanceId - Instance's id to be deleted
             * @returns {HttpPromise}
             */
            this.deleteProcessInstance = function (instanceId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/delete/completed/instance/' + instanceId);
            };

            /**
             * @memberof processService
             * @desc Gets supervisors by process id
             * API: @name ProcessController#getSupervirosByProcess 
             * 
             * @param {String} processId
             * @returns {HttpPromise}
             */
            this.getSupervisorsByProcessId = function (processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/supervisors/process/' + processId);
            };

            /**
             * @memberof processService
             * @desc Returns all in progress instances
             * API: @name ExecutionController#getInProgressInstances 
             * 
             * @returns {HttpPromise}
             */
            this.getInProgressInstances = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/inprogress/instances');
            };

            /**
             * @memberOf processService
             * @function getInProgressInstancesByCriteria
             * @desc Searches for in progress instances based on given criteria
             *
             * @param {String} definitionName
             * @param {String} instanceTitle
             * @param {Number} dateAfterTime
             * @param {Number} dateBeforeTime
             * @returns {HttpPromise}
             */
            this.getInProgressInstancesByCriteria = function (definitionName, instanceTitle, dateAfterTime, dateBeforeTime) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/inprogress/instances/search:'
                    + definitionName + ","
                    + instanceTitle + ","
                    + dateAfterTime + ","
                    + dateBeforeTime
                );
            };

            /**
             * @memberof processService
             * @desc Returns instance's documents by id
             * API: @name ExecutionController#getDocumentsByInstanceId
             * 
             * @param {String} instanceId
             * @returns {HttpPromise}
             */
            this.getDocumentsByInstance = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/instance/' + instanceId + '/documents')
            };

            /**
             * @memberof processService
             * @desc Returns users having role Supervisor
             * API: @name RealmController#getUsersByRole
             * 
             * @returns {HttpPromise}
             */
            this.getSupervisors = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/user/role/' + 'ROLE_Supervisor');
            };

            /**
             * @memberof processService
             * @desc Change instace's supervisor
             * API: @name ExecutionController#changeInstanceSupervisor 
             * 
             * @param {String} instanceId
             * @param {String} supervisor
             * @returns {HttpPromise}
             */
            this.changeInstanceSupervisor = function (instanceId, supervisor) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/instance/' + instanceId + '/supervisor?supervisor=' + supervisor);
            };

            /**
             * @memberof processService
             * @desc Returns an instnace by its id 
             * 
             * @param {String} instanceId
             * @returns {HttpPromise}
             */
            this.getInstanceById = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/public/instance/' + instanceId);
            };
        }]
    );
})(angular);