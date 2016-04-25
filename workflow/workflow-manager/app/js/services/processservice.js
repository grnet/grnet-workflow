(function () {
    angular.module('wfmanagerServices').service('processService', ['$http', 'CONFIG',

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
		    	 * Returns all groups from realm
		    	 * 
		    	 * @name RealmService#getGroups
		    	 */
			
			    this.getGroups = function () {
			        return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/group');
			    };
			    
			    /**
		    	 * Returns user groups from realm
		    	 * 
		    	 * @name RealmService#getUserGroups
		    	 */
			
			    this.getUserGroups = function () {
			        return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/user/group');
			    };
			    
			    /**
			     * Creates an user task form element and then saves it
			     */
			    this.saveTaskFormElement = function (formItem, taskDefinitionKey, definitionVersion){
			    	
			    	return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/process/' + encodeURIComponent(definitionVersion) + '/task/' + taskDefinitionKey + '/formelement' , formItem);
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
                
//                /**
//                 * Return a promise object for the list of processes by selected owners (workflow definition)
//                 * @return {HttpPromise}
//                 *
//                 * @name ProcessService#getProcessesByOwners
//                 */
//                this.getProcessesByOwners = function (selectedOwners) {
//                	
//                	if(selectedOwners.length == 0){
//                		return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/filter/all');
//                	}else{
//                		return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/filter/owners;owners=' + encodeURIComponent(selectedOwners));
//                	}
//                	
//                };
                
                /**
                 * Return a promise object for the list of processes by selected owners (workflow definition)
                 * @return {HttpPromise}
                 *
                 * @name ProcessService#getProcessesByOwners
                 */
                this.getProcessesByOwners = function (selectedOwners) {
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/filter?owners=' + encodeURIComponent(selectedOwners));
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
                 * Returns a list of wftasks based on instance id
                 */
                this.getTasksByInstanceId = function (instanceId){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/instance/' + instanceId)
                }
                

                /**
                 * Updates the process definition version
                 * @param {number} processId
                 * @param {ProcessVersion} version
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
                };
                
                /**
                 * Returns a task by task definition key
                 * 
                 * @param {String} taskDefinitionKey
                 * @param {String} processId
                 */
                this.getTaskFormProperties = function (taskDefinitionKey,processId){
                	
                	 return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/taskdefinition/' + taskDefinitionKey + '/process/' + processId );
                }
                
                this.getCompletedTask = function (taskId) {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY
                        + '/task/'
                        + taskId
                        + '/completed'
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
                 */
                this.getVersionTaskDetails = function (versionid) {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY +
                        '/process/version/' 
                    	+ versionid
                    );
                };
                
                /**
                 * Update a UserTaskDetails object.
                 */
                this.updateTaskDetails = function (task) {
                    return $http.put(config.WORKFLOW_SERVICE_ENTRY
                        + '/process/taskdetails',
                        task
                    );
                };
                
                /**
                 * Cancel a running instance.
                 */
                this.cancelInstance = function(instanceid){
                	return $http.delete(config.WORKFLOW_SERVICE_ENTRY
                			+ '/process/instance/' 
                			+ instanceid
                	);
                };

                /**
                 * Suspend / Resume a running instance.
                 */
                this.actOnInstance = function(instanceid, action){
                	return $http.put(config.WORKFLOW_SERVICE_ENTRY
                			+ '/process/instance/' 
                			+ instanceid
                			+ "/"
                			+ action
                	);
                };

                
                /**
                 * Retrieve all ended instances
                 */
                this.getEndedInstancesTasks = function(title, after, before, anonymous){              	
                	
                	var url = config.WORKFLOW_SERVICE_ENTRY
        			+ '/process/instance/ended/search:'           			
        			+ title
        			+ ","
        			+ after
        			+ ","
        			+ before
        			+ ","
        			+ anonymous
        			;
                	
                	return $http.get(url);
                };

                
                /**
                 * Get user activity, all tasks having the specified user as assignee.
                 */
                this.getUserActivity = function(after, before, userId){           	
                	
                	var url = config.WORKFLOW_SERVICE_ENTRY
        			+ '/task/search:'           			
        			+ after
        			+ ","
        			+ before
        			+ "/assignee/"
        			+ userId
        			;
                	
                	return $http.get(url);
                };
                
                
                
                /**
                 * Retrieve all active tasks
                 */
                this.getActiveTasks = function(){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY
                			+ '/task' 
                	);
                };
                
                
                /**
                 * Retrieve all users
                 */
                this.getUsers = function(){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/user'
                	);
                };
                
                
                /**
                 * Get all registries
                 */
                this.getRegistries = function(){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/registry'
                	);
                };
                
                
                /**
                 * Get the current settings
                 */
                this.getSettings = function(){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/settings'
                	);
                };
                
                
                /**
                 * Update settings
                 */
                this.updateSettings = function(settings){
                	return $http.put(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/settings',
                			settings
                	);
                };
                
                /**
                 * Update registry
                 */
                this.updateRegistry = function (registry){
                	return $http.put(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/registry', registry
                	);
                };
                
                /**
                 * create registry
                 */
                this.createRegistry = function (registry){
                	return $http.post(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/registry', registry
                	);
                };
                
                /**
                 * Delete registry
                 */
                this.deleteRegistry = function (registryId){
                	return $http.delete(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/registry/' + registryId
                			);
                };
                
                /**
                 * Get external forms of the process specified by its id
                 */
                this.getExternalForms = function(id){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/process/'
                			+ id
                			+ '/externalform'
                	);
                };
                
                
                /**
                 * Create new external form
                 */
                this.saveExternalForm = function(xform){
                	return $http.post(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/externalform', xform             			
                	);
                }
                
                
                /**
                 * Update external form
                 */
                this.updateExternalForm = function(xform){
                	return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/externalform/update', xform);
                }
 
                
                /**
                 * Delete external form
                 */
                this.deleteExternalForm = function(id){
                	return $http.delete(config.WORKFLOW_SERVICE_ENTRY 
                			+ '/externalform/'
                			+ id                			
                	);
                }
                
                
                /**
                 * Suspend / Resume an external form.
                 */
                this.actOnExternalForm = function(xformId, action){
                	return $http.put(config.WORKFLOW_SERVICE_ENTRY
                			+ '/externalform/' 
                			+ xformId
                			+ "/"
                			+ action
                	);
                };   
                
                
                
                
                /**
                 * Post facebook access token to server
                 */
                this.postAccessToken=function(fbResponse){
                	//alert(fbResponse.userID + ', ' + fbResponse.accessToken);                	
                	return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/facebook', fbResponse);
                };

                
                
                
                /**
                 * Returns a promise object for the list of workflow instances
                 */
                this.getWorkflowInstances = function (workflowId) {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY +
                    		'/process/'
                    		+ workflowId
                    		+'/instance'
                    );
                };
                
                /**
                 * Delete a process instance by id
                 */
                this.deleteProcessInstance = function(instanceId) {
                	return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/delete/completed/instance/' + instanceId);
                };
                
                /**
                 * Get groups/forms wrapped
                 */
                this.getGroupsFormsWrapped = function() {
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/external/groups/forms/wrapped');
                };
                
                /**
                 * Creates new external group
                 */
                this.createExternalGroup = function(externalGroup) {
                	return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/external/group/create', externalGroup);
                };
                
                /**
                 * Gets all available groups
                 */
                this.getExternalGroups = function() {
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/external/groups');
                };
                
                /**
                 * Deletes a public group
                 */
                this.deletePublicGroup = function(groupId) {
                	return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/form/delete/group/' + groupId);		
                };
                
                /**
                 * Edits public group
                 */
                this.editPublicGroup = function(group) {
                	return $http.put(config.WORKFLOW_SERVICE_ENTRY + '/form/update/group', group);	
                };
                
                /**
                 * Gets supervisors by process id
                 */
                this.getSupervisorsByProcessId = function(processId) {
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/supervisors/process/' + processId);
                };
                
            }]
    );
})(angular);