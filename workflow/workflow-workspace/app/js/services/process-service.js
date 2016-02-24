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
                
                this.getSupervisors = function (){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/user/role/' + 'ROLE_Supervisor');
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
                 * Get completed tasks for user for supervisor
                 */
                this.getCompletedTasks = function () {
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/completed');
                };
                
                /**
                 * Get completed instances for user
                 */
                this.getCompletedInstances = function (){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/instances/completed');
                }
                
                /**
                 * Get user's completed tasks by instance ids
                 */
                this.getCompletedTasksByInstanceIds = function (instancesId) {
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/completed/instances?i=' + instancesId );
                };
                
                /**
                 * Temporary saves task's form data
                 */
                this.temporarySave = function (taskData){
                	
                	var containsDocuments = false;
                	var filesMap = new Map();
                	
                    var vars = taskData.taskForm;
                    for (var i = 0; i < vars.length; i++) {
                        
                        if (vars[i].type == 'document' && vars[i].value.file) {
                        	containsDocuments = true;
                        	
                        	var blob = new Blob([vars[i].value.file], {type: vars[i].value.file.type});
                        	filesMap.set(vars[i].id, blob);
                        	delete vars[i].value['file'];
                        }
                    }
                    
                    if (containsDocuments) {
                    	
                    	var url = config.WORKFLOW_SERVICE_ENTRY + '/task/tempsave';
                    	
                    	return $http.post(url, {'data': taskData, 'file': filesMap}, {
                    		headers: {'Content-Type': undefined},
	                        transformRequest: function(data) {
	                            
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
                 * Get completed tasks by instance id
                 */
                this.getCompletedTasksByInstances = function (instanceIds){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/instances?i=' + instanceIds );
                	
                }
                
                /**
                 * Get tasks by instance id
                 */
                this.getTasksByInstanceId = function (instanceId){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/instance/' + instanceId);
                }
                
                
                /**
                 * Get user's completed tasks
                 */
                this.getUserCompletedTasks = function () {
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/completed/user' );
                };
                
                this.getSearchedUserTasks = function (definitionKey, instanceTitle, after, before, isSupervisor) {
                	
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/completed/search:'
                			+ definitionKey + "," 
                			+ instanceTitle + ","
                			+ after + ","
                			+ before + ","
                			+ isSupervisor);
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
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process'
                    );
                };
                
                /**
                 * Return a promise object for the tasks by an array of instances id
                 * @param {array} definitionsId - array of selected instances id
                 * @return {HttpPromise}
                 *
                 */
                this.getTasksByDefinitionsId = function (instancesId) {
                	
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/unassigned?i=' + instancesId );
                	
                };
                
                
                /**
                 * Returns a list of assigned tasks based on instances
                 */
                this.getAssignedTasksByInstances = function (instancesId) {
                	
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/assigned?i=' + instancesId );
                };
                
                
                /**
                 * Returns list of tasks that belong to instances supervised by the user in context
                 */
                this.getSupervisedTasks = function (){
                	
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/supervised');
                }
                
                
                /**
                 * Get tasks to be claimed by user according to user role
                 */
                this.getClaimTasks = function() {
                	
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/tasks/claim');
                }
                
                /**
                 * Unclaims a task 
                 */
                this.unclaimTask = function(taskId) {
                	return $http.delete(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId +'/unclaim');
                }
                
                /**
                 * Claims a task
                 */
                this.claimTask = function (taskId){
                	
                	return $http.post(config.WORKFLOW_SERVICE_ENTRY + '/task/' + taskId +'/claim');
                }
                
                /**
                 * Assign to a task assignee
                 */
                this.setAssigneeToTask = function (taskData, assignee){
                	
                	var containsDocuments = false;
                	var filesMap = new Map();
                	
                    var vars = taskData.taskForm;
                    for (var i = 0; i < vars.length; i++) {
                        
                        if (vars[i].type == 'document' && vars[i].value.file) {
                        	containsDocuments = true;
                        	
                        	var blob = new Blob([vars[i].value.file], {type: vars[i].value.file.type});
                        	filesMap.set(vars[i].id, blob);
                        	delete vars[i].value['file'];
                        }
                    }
                    
                    if (containsDocuments) {
                    	
                    	var url = config.WORKFLOW_SERVICE_ENTRY + '/task/assignee/' + assignee + '/';
                    	
                    	return $http.post(url, {'data': taskData, 'file': filesMap}, {
                    		headers: {'Content-Type': undefined},
	                        transformRequest: function(data) {
	                            
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
                
                this.getTasksInProgress = function (){
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/task/inprogress/user');
                }

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
                        	containsDocuments = true;
                        	
                        	var blob = new Blob([vars[i].value.file], {type: vars[i].value.file.type});
                        	filesMap.set(vars[i].id, blob);
                        	delete vars[i].value['file'];
                        }
                    }
                    
                    if (containsDocuments) {
                    	
                    	var url = config.WORKFLOW_SERVICE_ENTRY + '/process/' + processId + '/document/start';
                    	
                    	return $http.post(url, {'data': instanceData, 'file': filesMap}, {
                    		headers: {'Content-Type': undefined},
	                        transformRequest: function(data) {
	                            
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
                 * Return a list of the processes, supervised by the authenticated user
                 * @return {HttpPromise}
                 * @name ProcessService#getSupervisedProcesses
                 */
                this.getSupervisedProcesses = function () {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY + '/process/supervised');
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
                };
   
                
                /**
                 * Get the task
                 * @name ProcessService#getTask
                 *
                 * @param {string} taskId
                 * @return {HttpPromise}
                 */
                this.getCompletedTask = function (taskId) {
                    return $http.get(config.WORKFLOW_SERVICE_ENTRY
                        + '/task/'
                        + taskId
                        + '/completed'
                    );
                };
                
                /**
                 * Completes a task and setting formdata to it
                 */
                this.completeTask = function (taskData){
                	
                	var containsDocuments = false;
                	var filesMap = new Map();
                	
                    var vars = taskData.taskForm;
                    for (var i = 0; i < vars.length; i++) {
                        
                        if (vars[i].type == 'document' && vars[i].value.file) {
                        	containsDocuments = true;
                        	
                        	var blob = new Blob([vars[i].value.file], {type: vars[i].value.file.type});
                        	filesMap.set(vars[i].id, blob);
                        	delete vars[i].value['file'];
                        }
                    }
                    
                    if (containsDocuments) {
                    	
                    	var url = config.WORKFLOW_SERVICE_ENTRY + '/task/complete';
                    	
                    	return $http.post(url, {'data': taskData, 'file': filesMap}, {
                    		headers: {'Content-Type': undefined},
	                        transformRequest: function(data) {
	                            
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
                 * Save or update a document in the repository
                 * 
                 * @param {Document} wfDocument
                 * @param {File} file
                 *
                 * @name ProcessService#saveDocument
                 */
                this.saveDocument = function (instanceId, wfDocument, file) {
                    var url = config.WORKFLOW_SERVICE_ENTRY
                        + '/process/exec/'
                        + instanceId
                        + '/file';

                    if (file !== null) {
                    
	                    return $http.post(url, {'data': wfDocument, 'file': file}, {
	                        headers: {'Content-Type': undefined},
	                        transformRequest: function(data) {
	                            
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
                this.getProcessInstanceDocuments = function(taskId) {
                	
                	return $http.get(config.WORKFLOW_SERVICE_ENTRY
                			+ '/task/'
                			+ taskId
                			+ '/document'
                			);
                }

            }]
    );
})(angular);