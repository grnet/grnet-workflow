(function () {
    angular.module('wfworkspaceServices').service(
        'processService', ['$http', 'CONFIG',

            /**
             * @class ProcessService
             * @param {$http} $http
             * @param config
             */
                function ($http, config) {

                /**
                 * Creates a new process definition from a BPMN file
                 * @param {File} file
                 * @return {HttpPromise}
                 *
                 * @name ProcessService#createProcess
                 */
                this.createProcess = function (file) {
                    var url = config.WORKFLOW_SERVICE_ENTRY
                        + '/processbpmn';

                    var fd = new FormData();
                    fd.append('file', file);

                    return $http.post(url, fd, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    });
                };

                /**
                 * Creates a new process version
                 * @param {number} processId
                 * @param {File} file
                 * @return {HttpPromise}
                 *
                 * @name ProcessService#createProcessVersion
                 */
                this.createProcessVersion = function (processId, file) {
                    var url = config.WORKFLOW_SERVICE_ENTRY
                        + '/process/'
                        + processId;

                    var fd = new FormData();
                    fd.append('file', file);

                    return $http.post(url, fd, {
                        transformRequest: angular.identity,
                        headers: {'Content-Type': undefined}
                    });
                };

                /**
                 * Delete the process with the given id
                 * @param {number} processId       - the process (workflow definition) id
                 * @return {HttpPromise}
                 *
                 * @name ProcessService#deleteProcess
                 */
                this.deleteProcess = function (processId) {
                    return $http.delete(config.WORKFLOW_SERVICE_ENTRY
                        + '/process/'
                        + processId
                    );
                };

                /**
                 * Delete the identified process version
                 *
                 * @param {number} processId       - the process (workflow definition) id
                 * @param {string} deploymentId    - the process version deployment id
                 * @return {HttpPromise}
                 *
                 * @name ProcessService#deleteProcessVersion
                 */
                this.deleteProcessVersion = function (processId, deploymentId) {
                    return $http.delete(config.WORKFLOW_SERVICE_ENTRY
                        + '/process/'
                        + processId
                        + '/'
                        + deploymentId
                    );
                };

                /**
                 * Return a promise object for the list of all processes (workflow definition)
                 * @return {HttpPromise}
                 *
                 * @name ProcessService#getProcesses
                 */
                this.getProcesses = function () {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY +
                        '/process'
                    );
                };

                /**
                 * Return a promise object for the process (workflow definition) object
                 * @param {number} processId
                 * @return {HttpPromise}
                 *
                 * @name ProcessService#getProcess
                 */
                this.getProcess = function (processId) {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY
                        + '/process/'
                        + processId
                    );
                };

                /**
                 * Return a promise object for the process metadata
                 * @param {number} processId
                 * @return {HttpPromise}
                 *
                 * @name ProcessService#getProcessMetadata
                 */
                this.getProcessMetadata = function (processId) {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY
                        + '/process/'
                        + processId
                        + '/form'
                    );
                };

                /**
                 * Updates the workflow definition
                 * @param {WorkflowDefinition} process
                 * @return {HttpPromise}
                 *
                 * @name ProcessService#updateProcess
                 */
                this.updateProcess = function (process) {
                    return $http.put(config.WORKFLOW_SERVICE_ENTRY
                        + '/process',
                        process
                    );
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
                 * Returns true if the selected version is active
                 * @param {WorkflowDefinition} process
                 * @return {HttpPromise}
                 * @name ProcessService#isProcessActive
                 */
                this.isProcessActive = function (process) {
                    var versions = process.definitionVersions;
                    for (var index = 0; index < versions.length; index++) {
                        if (versions[index].deploymentId === process.activeDeploymentId) {
                            return (versions[index].status === 'active');
                        }
                    }
                    return false;
                };

                /**
                 * creates a new process instance using the form data
                 * @param {number} processId
                 * @param {FormProperty[]} processForm
                 * @return {HttpPromise}
                 * @name ProcessService#startProcess
                 */
                this.startProcess = function (processId, processForm) {

                    var formData = {};
                    for (var i = 0; i < processForm.length; i++) {
                        formData[processForm[i].id] = processForm[i].value;
                    }

                    return $http.post(config.WORKFLOW_SERVICE_ENTRY
                        + '/process/'
                        + processId
                        + '/start',
                        formData
                    );
                };

                /**
                 * Return a list of the processes, supervised by the authenticated user
                 * @return {HttpPromise}
                 * @name ProcessService#getSupervisedProcesses
                 */
                this.getSupervisedProcesses = function () {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY
                        + '/process/supervised'
                    );
                };

                /**
                 * Gets all unassigned tasks that belong to the list of supplied processes
                 * @name ProcessService#getUnassignedTasks
                 *
                 * @param {number[]} processes
                 * @return {HttpPromise}
                 */
                this.getUnassignedTasks = function (processes) {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY
                        + '/task/unassigned',
                        {params: {'p': processes}});
                };

                /**
                 * Get the task
                 * @name ProcessService#getTask
                 *
                 * @param {string} taskId
                 * @return {HttpPromise}
                 */
                this.getTask = function (taskId) {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY
                        + '/task/'
                        + taskId
                    );
                }

            }]
    );
})(angular);