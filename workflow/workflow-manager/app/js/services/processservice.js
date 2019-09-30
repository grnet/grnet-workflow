define(['angular'],

    function (angular) {

        'use strict';

        function ProcessService($http, config) {

            /**
             * Creates a new process definition from a BPMN file
             * @param {File} file
             * @return {HttpPromise}
             *
                 * V1 API: @name ProcessController#createProcessDefinition
             * V2 API: @name DefinitionController#createProcessDefinition
             *
             * Note: On v2 api the url should be /api/v2/process
             */
            this.createProcess = function (file, justification) {
                var url = config.WORKFLOW_SERVICE_ENTRY + '/v2/process:'
                    + justification;

                var fd = new FormData();
                fd.append('file', file);

                return $http.post(url, fd, {
                    transformRequest: angular.identity,
                    headers: { 'Content-Type': undefined }
                });
            };

            /**
             * Returns all groups from realm
             *
             * V1 API: @name RealmController#getGroups
             * V2 API: @name RealmController#getGroups
             */
            this.getGroups = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/group');
            };

            /**
             * Deletes an owner/group by its name
             *
             */
            this.deleteOwner = function (ownerId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/owner/?ownerId=' + ownerId);
            };

            /**
             * Creates a new or updates an owner
             *
             * @param owner
             *
             */
            this.saveOwner = function (owner) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/owner', owner);
            };

            /**
             * Returns user groups from realm
             *
             * V1 API: @name RealmController#getUserGroups
             * V2 API: @name RealmController#getUserGroups
             */
            this.getUserGroups = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/user/group');
            };

            /**
             * Returns all available roles
             *
             */
            this.getRoles = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/roles');
            };

            this.saveRole = function (role) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/role', role);
            };

            this.deleteRole = function (roleId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/role/?roleId=' + roleId);
            };

            /**
             * Creates a user task form element and then saves it
             *
             * V1 API: @name ExecutionController#saveUserTaskFormElement
             * V2 API: @name TaskController#saveUserTaskFormElement
             */
            this.saveTaskFormElement = function (formItem, taskDefinitionKey, definitionVersion) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/' + encodeURIComponent(definitionVersion)
                    + '/task/' + taskDefinitionKey + '/formelement', formItem);
            };

            /**
             * Create a new version for the given process based on an uploaded BPMN file
             *
             * @param {number} processId The process id
             * @param {File} file The BPMN File
             * @return {HttpPromise}
             *
                     * V1 API: @name ProcessController#createProcessVersion
             * V2 API: @name DefinitionController#createProcessVersion
             *
             * Note: On v2 api the url should be /api/v2/process/{id}/version
             *
             */
            this.createProcessVersion = function (processId, file, justification) {
                var url = config.WORKFLOW_SERVICE_ENTRY + '/v2/process/'
                    + processId + "/version:"
                    + justification;

                var fd = new FormData();
                fd.append('file', file);

                return $http.post(url, fd, {
                    transformRequest: angular.identity,
                    headers: { 'Content-Type': undefined }
                });
            };

            /**
             * Delete the process with the given id
             * @param {number} processId       - the process (workflow definition) id
             * @return {HttpPromise}
             *
             * V1 API: @name ProcessController#deleteProcessDefinition
             * V2 API: @name DefinitionController#deleteProcessDefinition
             */
            this.deleteProcess = function (processId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/v2/process/' + processId+'/full');
            };

            /**
             * Delete the identified process version
             *
             * @param {number} processId       - the process (workflow definition) id
             * @param {string} deploymentId    - the process version deployment id
             * @return {HttpPromise}
             *
             * V1 API: @name ProcessController#deleteProcessDefinitionVersion
             * V2 API: @name DefinitionController#deleteProcessDefinitionVersion
             *
             * Note: On v2 api the url should be /api/v2/process/version/{processId}/{deploymentId}
             */
            this.deleteProcessVersion = function (processId, deploymentId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/v2/process/version/' + processId + '/' + deploymentId);
            };

            /**
             * Return a promise object for the list of all processes (workflow definition)
             * @return {HttpPromise}
             *
             * V1 API: @name ProcessController#getProcessDefinitions
             * V2 API: @name DefinitionController#getProcessDefinitions
             */
            this.getProcesses = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/process');
            };

            /**
             * Return a promise object for the list of processes by selected owners (workflow definition)
             * @return {HttpPromise}
             *
             * V1 API: @name ProcessController#getProcessDefinitionsByOwner
             * V2 API: @name DefinitionController#getProcessDefinitionsByOwner
             */
            this.getProcessesByOwners = function (selectedOwners) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/process/filter?owners=' + encodeURIComponent(selectedOwners));
            };

            /**
             * Return a promise object for the process (workflow definition) object
             * @param {number} processId
             * @return {HttpPromise}
             *
             * V1 API: @name ProcessController#getProcessDefinition
             * V2 API: @name DefinitionController#getProcessDefinition
             */
            this.getProcess = function (processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/process/' + processId);
            };

            /**
             * Updates the workflow definition
             * @param {WorkflowDefinition} process
             * @return {HttpPromise}
             *
             * V1 API: @name ProcessController#updateProcessDefinition
             * V2 API: @name DefinitionController#updateProcessDefinition
             */
            this.updateProcess = function (process) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/v2/process', process);
            };

            /**
             * Returns a list of wftasks based on instance id
             *
             * V1 API: @name ExecutionController#getTaskByInstanceId
             * V2 API: @name TaskController#getTaskByExecutionId
             *
             * Note: On v2 api the url should be /api/v2/task/execution/{id}
             */
            this.getTasksByInstanceId = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/instance/' + instanceId)
            }


            /**
             * NOT USED!!!
             *
             * Updates the process definition version
             * @param {number} processId
             * @param {ProcessVersion} version
             * @return {HttpPromise}
             *
             */
            this.updateProcessDefinitionVersion = function (processId, version) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/version', version);
            };

            /**
             * Get task by task id
             *
             * @param {string} taskId
             * @return {HttpPromise}
             *
             * V1 API: @name ExecutionController#getTask
             * V2 API: @name TaskController#getTask
             */
            this.getTask = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/task/' + taskId);
            };

            /**
             * Returns a task by task definition key
             *
             * @param {String} taskDefinitionKey
             * @param {String} processId
             *
             * V1 API: @name ExecutionController#getTask
             * V2 API: @name TaskController#getTask
             */
            this.getTaskFormProperties = function (taskDefinitionKey, processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/taskdefinition/' + taskDefinitionKey + '/process/' + processId);
            }

            /**
             * Not used. Deprecated by getTaskById
             *
             */
            this.getCompletedTask = function (taskId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId + '/completed');
            };

            /**
             * Sets the active version for the workflow definition
             * @param {number} processId
             * @param {number} versionId
             * @return {HttpPromise}
             *
             * V1 API: @name ProcessController#setActiveVersion
             * V2 API: @name DefinitionController#setActiveVersion
             *
             * Note: On v2 api the url should be /api/v2/process/{processId}/version/{versionId}/active
             */
            this.setActiveVersion = function (processId, versionId) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/version/active/' + versionId, null);
            };

            /**
             * Deactivate the version of the workflow definition
             * @param {number} processId
             * @param {number} versionId
             * @return {HttpPromise}
             *
             * V1 API: @name ProcessController#deactivateVersion
             * V2 API: @name DefinitionController#deactivateVersion
             *
             * Note: On v2 api the url should be /api/v2/process/{processId}/version/{versionId}/inactive
             */
            this.deactivateVersion = function (processId, versionId) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/version/inactive/' + versionId, null);
            };

            /**
             * Returns true if the selected version is active
             * @param {WorkflowDefinition} process
             *
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
             * Returns a promise object for the list of the task details of the
             * process specified version
             *
             * V1 API: @name ProcessController#getVersionsTaskDetails
             * V2 API: @name TaskController#getVersionsTaskDetails
             *
             * Note: On v2 api the url should be /api/v2/task/process/version/{id}
             */
            this.getVersionTaskDetails = function (versionid) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/version/' + versionid);
            };

            /**
             * Update a UserTaskDetails object
             *
             * V1 API: @name ProcessController#updateTaskDetails
             * V2 API: @name TaskController#updateTaskDetails
             *
             * Note: On v2 api the url should be /api/v2/task
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
             * Suspend / Resume a running instance.
             *
             * V1 API: @name ProcessController#modifyProcessInstanceStatus
             * V2 API: @name ExecutionController#modifyProcessInstanceStatus
             *
             * Note: On v2 api the url should be /api/v2/execution/{id}/{action}
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
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/process/active');
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
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/instances/ended/search:'
                    + definitionName + ","
                    + instanceTitle + ","
                    + dateAfterTime + ","
                    + dateBeforeTime
                );
            };

            /**
             * Get user activity, all tasks having the specified user as assignee
             *
             * V1 API: @name ProcessController#getUserActivity
             * V2 API: @name TaskController#getUserActivity
             *
             * Note: On v2 api the url should be /api/v2/task/search:{after:\\d+},{before:\\d+}/assignee/{userId}
             */
            this.getUserActivity = function (after, before, userId) {
                var url = config.WORKFLOW_SERVICE_ENTRY + '/task/user/search/' + after + "," + before + "," + userId;
                return $http.get(url);
            };

            /**
             * Retrieve all active tasks
             *
             * V1 API: @name ExecutionController#getAllActiveTasks
             * V2 API: @name TaskController#getAllActiveTasks
             */
            this.getActiveTasks = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/task');
            };

            /**
             * @memberof processService
             * @desc Retrieve active tasks by given criteria
             * API: @name ExecutionController#getActiveTasksByCriteria
             *
             * @returns {HttpPromise}
             */
            this.getActiveTasksByCriteria = function (definitionName, taskName, after, before) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/tasks/search:'
                    + definitionName + ","
                    + taskName + ","
                    + after + ","
                    + before);
            };

            /**
             * Retrieve all users
             *
             * V1 API: @name RealmController#getUsers
             * V2 API: @name RealmController#getUsers
             */
            this.getUsers = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/user');
            };

            /**
             * Get all registries
             *
             * V1 API: @name ProcessController#getRegistries
             * V2 API: @name PublicFormController#getRegistries
             */
            this.getRegistries = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/registry');
            };

            /**
             * Get the current settings
             *
             * V1 API: @name ProcessController#getSettings
             * V2 API: @name DefinitionController#getSettings
             */
            this.getSettings = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/settings');
            };

            /**
             * Update settings
             *
             * V1 API: @name ProcessController#updateSettings
             * V2 API: @name DefinitionController#updateSettings
             */
            this.updateSettings = function (settings) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/v2/settings', settings);
            };

            /**
             * Update registry
             *
             * V1 API: @name ProcessController#updateRegistry
             * V2 API: @name PublicFormController#getSettings
             */
            this.updateRegistry = function (registry) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/v2/registry', registry);
            };

            /**
             * Create a new registry
             *
             * V1 API: @name ProcessController#createRegistry
             * V2 API: @name PublicFormController#createRegistry
             *
             * Note: On v2 api the method is PUT
             */
            this.createRegistry = function (registry) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/v2/registry', registry);
            };

            /**
             * Delete registry
             *
             * V1 API: @name ProcessController#deleteRegistry
             * V2 API: @name PublicFormController#deleteRegistry
             */
            this.deleteRegistry = function (registryId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/v2/registry/' + registryId);
            };

            /**
             * Get external forms of the process specified by its id
             *
             * V1 API: @name ProcessController#getProcessExternalForms
             * V2 API: @name PublicFormController#getProcessPublicForms
             *
             * Note: On v2 api the url should be api/v2/form/process/{id}
             */
            this.getExternalForms = function (id) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/' + id + '/externalform');
            };

            /**
             * Create new external form
             *
             * V1 API: @name ProcessController#createExternalForm
             * V2 API: @name PublicFormController#createPublicForm
             *
             * Note: On v2 api the url should be api/v2/form method = POST
             */
            this.saveExternalForm = function (externalForm) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/externalform', externalForm);
            };

            /**
             * Update external form
             *
             * V1 API: @name ProcessController#updateExternalForm
             * V2 API: @name PublicFormController#updatePublicForm
             *
             * Note: On v2 api the url should be api/v2/form with method PUT
             */
            this.updateExternalForm = function (externalForm) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/externalform/update', externalForm);
            }

            /**
             * Delete external form
             *
             * V1 API: @name ProcessController#deleteExternalForm
             * V2 API: @name PublicFormController#deletePublicForm
             *
             * Note: On v2 api the url should be api/v2/form/{id}
             */
            this.deleteExternalForm = function (id) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/externalform/' + id);
            }

            /**
             * Suspend / Resume an external form.
             *
             * V1 API: @name ProcessController#modifyExternalFormStatus
             * V2 API: @name PublicFormController#modifyExternalFormStatus
             *
             * Note: On v2 api the url should be api/v2/form/{id}/{action}
             */
            this.actOnExternalForm = function (externalFormId, action) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/externalform/' + externalFormId + "/" + action);
            };

            /**
             * Post facebook access token to server
             */
            this.postAccessToken = function (fbResponse) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/facebook', fbResponse);
            };


            /**
             * Check if token exists for page
             */
            this.checkTokens = function (pages) {
                if (pages == null) pages = [];
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/facebook/check', pages);
            };


            /**
             * Delete access to facebook page
             */
            this.removeFacebookPageAccess = function (p) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/facebook/page/' + p.name);
            }

            /**
             * Authenticate to twitter
             */
            this.authenticateTwitter = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/twitter');
            }

            /**
             * Return all authorized twitter accounts
             */
            this.getTwitterAccounts = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/twitter/accounts');
            }

            /**
             * Delete access to twitter account
             */
            this.removeTwitterAccountAccess = function (a) {
                console.log("Remove twitter account");
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/twitter/account/' + a.screenName);
            }

            /**
             * Returns instances by process definition
             *
             *
             * V1 API: @name ProcessController#getProcessInstances
             * V2 API: @name ExecutionController#getExecutions
             *
             * Note: On v2 api the url should be api/v2/execution/process/version/{id}
             */
            this.getWorkflowInstances = function (workflowId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/' + workflowId + '/instance');
            };

            /**
             * Delete a process instance by id
             *
             * V1 API: @name ExecutionController#deleteProcessCompletedInstance
             * V2 API: @name ExecutionController#deleteCompletedExecution
             *
             * Note: On v2 api the url should be api/v2/execution/{id}
             */
            this.deleteProcessInstance = function (instanceId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/delete/completed/instance/' + instanceId);
            };

            /**
             * Get groups/forms wrapped
             *
             * V1 API: @name ExecutionController#getWrappedGroupsForms
             * V2 API: @name PublicFormController#getWrappedGroupsForms
             *
             * Note: On v2 api the url should be api/v2/form/group/wrapped
             */
            this.getGroupsFormsWrapped = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/external/groups/forms/wrapped');
            };

            /**
             * Creates new external group
             *
             * V1 API: @name ProcessController#createExternalGroup
             * V2 API: @name PublicFormController#createPublicGroup
             *
             * Note: On v2 api the url should be api/v2/form/group
             */
            this.createExternalGroup = function (externalGroup) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/external/group/create', externalGroup);
            };

            /**
             * Gets all available groups
             *
             * V1 API: @name ProcessController#getExternalGroups
             * V2 API: @name PublicFormController#getPublicGroups
             *
             * Note: On v2 api the url should be api/v2/form/group
             */
            this.getExternalGroups = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/external/groups');
            };

            /**
             * Deletes a public group
             *
             * V1 API: @name ProcessController#deletePublicGroup
             * V2 API: @name PublicFormController#deletePublicGroup
             *
             * Note: On v2 api the url should be api/v2/form/group/{groupId}
             */
            this.deletePublicGroup = function (groupId) {
                return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/form/delete/group/' + groupId);
            };

            /**
             * Edits public group
             *
             * V1 API: @name ProcessController#updatePublicGroup
             * V2 API: @name PublicFormController#updatePublicGroup
             *
             * Note: On v2 api the url should be api/v2/form/group
             */
            this.editPublicGroup = function (group) {
                return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/form/update/group', group);
            };

            /**
             * Gets supervisors by process id
             *
             * V1 API: @name ProcessController#getSupervirosByProcess
             * V2 API: @name RealmController#getSupervisorsByProcess
             *
             * Note: On v2 api the url should be api/v2/user/process/{processId}/supervisor
             */
            this.getSupervisorsByProcessId = function (processId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/supervisors/process/' + processId);
            };

            /**
             * Returns all in progress instances
             *
             * V1 API: @name ExecutionController#getInProgressInstances
             * V2 API: @name ExecutionController#getInProgressInstances
             */
            this.getInProgressInstances = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/inprogress/instances');
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
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/inprogress/instances/search:'
                    + definitionName + ","
                    + instanceTitle + ","
                    + dateAfterTime + ","
                    + dateBeforeTime
                );
            };

            /**
             * Returns instance's documents by id
             *
             * V1 API: @name ExecutionController#getDocumentsByInstanceId
             * V2 API: @name ExecutionController#getDocumentsByInstanceId
             */
            this.getDocumentsByInstance = function (instanceId) {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/instance/' + instanceId + '/documents')
            };

            /**
             * Returns users having role Supervisor
             *
             * V1 API: @name RealmController#getUsersByRole
             * V2 API: @name RealmController#getUsersByRole
             */
            this.getSupervisors = function () {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/v2/user/role/' + 'ROLE_Supervisor');
            };

            /**
             * Change instace's supervisor
             *
             * V1 API: @name ExecutionController#changeInstanceSupervisor
             * V2 API: @name ExecutionController#changeInstanceSupervisor
             */
            this.changeInstanceSupervisor = function (instanceId, supervisor) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/v2/instance/' + instanceId + '/supervisor?supervisor=' + supervisor);
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

            this.sendTaskDueDateNotification = function (taskId, content) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId + '/notification', content)
            };

            this.synchRoles = function() {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/syncroles');
            };

            this.synchOwners = function() {
                return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/syncowners');
            };

            this.importOwners = function(owners) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/importowners', owners);
            };

            this.importRoles = function(roles) {
                return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/importroles', roles);
            };
        }

        angular.module('wfManagerServices').service('processService', ['$http', 'CONFIG', ProcessService]);

    }
);
