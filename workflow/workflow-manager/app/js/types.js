/**
 * @ngDoc types
 * @name WorkflowDefinition
 * @desc Represents the WorkflowDefinition Entity
 * 
 * @property {Number} id
 * @property {String} name
 * @property {String} description
 * @property {String} icon
 * @property {String} processDefinitionId
 * @property {Boolean} active
 * @property {FormProperty[]} [processForm] - an array of form properties
 * @property {ProcessVersion[]} [processVersions] - an array of versions for the process
 * @property {String} owner
 * @property {Boolean} assignBySupervisor
 * @property {String} activeDeploymentId
 * @property {Boolean} startForm
 */

/**
 * @ngDoc types
 * @name Task
 * @desc Represents the Task Entity
 * 
 * @property {String} id
 * @property {String} name
 * @property {String} description
 * @property {String} processDefinitionId
 * @property {String} deploymentId
 * @property {FormProperty[]} [taskForm] - an array of form properties
 * @property {Number} processId
 * @property {Date} dueDate
 * @property {Date} startDate
 * @property {ProcessInstance} processInstance
 * @property {String} assignee
 * @property {String} icon
 * @property {String} definitionName
 * @property {TaskDetails} taskDetails
 * @property {Boolean} completed
 * @property {Date} endDate
 * @property {Boolean} startForm
 */

/**
 * @ngDoc types
 * @name ProcessVersion
 * @desc Represents the ProcessVersion Entity
 * 
 * @property {number} id
 * @property {number} version
 * @property {String} status
 * @property {Date} deploymentDate
 * @property {String} deploymentId
 * @property {String} processDefinitionId
 * @property {Number} workflowDefinitionId
 */

/**
 * @ngDoc types
 * @name Settings
 * @desc Represents the Settings Entity
 * 
 * @property {number} id
 * @property {Boolean} autoAssignment
 * @property {Number} duedateAlertPeriod
 * @property {Boolean} assignmentNotification
 */

/**
 * @ngDoc types
 * @name FormProperty
 * @desc Represents the FormProperty Entity
 * 
 * @property {String} id
 * @property {String} name
 * @property {String} type
 * @property {String} value
 * @property {Boolean} readable
 * @property {Boolean} writable
 * @property {Boolean} required
 * @property {String} format
 * @property {String} description
 * @property {String} formValues
 * @property {String} device
 */

/**
 * @ngDoc types
 * @name TaskDetails
 * @desc Represents the TaskDetails Entity
 * 
 * @property {Number} id
 * @property {String} name
 * @property {String} description
 * @property {Number} definitionVersionId
 * @property {String} taskId
 * @property {boolean} assign
 * @property {UserTaskFormElement[]} [userTaskFormElements]
 */

/**
 * @ngDoc types
 * @name UserTaskFormElement
 * @desc Represents the UserTaskFormElement Entity
 * 
 * @property {Number} id
 * @property {String} description
 * @property {String} format
 * @property {String} elementId
 */

/**
 * @ngDoc types
 * @name User
 * @desc Represents the User Entity
 * 
 * @property {String} id
 * @property {String} username
 * @property {String} firstName
 * @property {String} lastName
 * @property {String} email
 * @property {String[]} [groups]
 * @property {String[]} [userRoles]
 * @property {Number} pendingTasks
 */

/**
 * @ngDoc types
 * @name ProcessInstance
 * @desc Represents the ProcessInstance Entity
 * 
 * @property {String} id
 * @property {String} title
 * @property {String} folderId
 * @property {String} definitionVersionId
 * @property {String} supervisor
 * @property {FormProperty[]} [processForm]
 * @property {Number} version
 * @property {Date} startDate
 * @property {String} status
 * @property {Date} endDate
 * @property {String} definitionIcon
 * @property {String} definitionName
 */

// Misc type defs

/**
 * @ngDoc types
 * @name HttpPromise
 * @desc Http Promise interface </br>
 * Viist <a href="https://docs.angularjs.org/api/ng/service/$q" target="_blank">https://docs.angularjs.org/api/ng/service/$q</a>
 *
 */

/**
 * @ngDoc types
 * @name File
 * @desc File element </br>
 * Viist <a href="https://developer.mozilla.org/en-US/docs/Web/API/File" target="_blank">https://developer.mozilla.org/en-US/docs/Web/API/File</a>
 */

/**
 * @ngDoc types
 * @name Date
 * @desc Date Object </br>
 * Viist <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Data_structures#Dates" target="_blank">https://developer.mozilla.org/en-US/docs/Web/JavaScript/Data_structures#Dates</a>
 */

/**
 * @ngDoc types
 * @name scope
 * @desc $scope Object </br>
 * Viist <a href="https://docs.angularjs.org/guide/scope" target="_blank">https://docs.angularjs.org/guide/scope</a>
 */

/**
 * @ngDoc types
 * @name $location
 * @desc $location Object </br>
 * Viist <a href="https://docs.angularjs.org/api/ng/service/$location" target="_blank">https://docs.angularjs.org/api/ng/service/$location</a>
 */

/**
 * @ngDoc types
 * @name $mdDialog
 * @desc $mdDialog Service </br>
 * Viist <a href="https://material.angularjs.org/latest/api/service/$mdDialog" target="_blank">https://material.angularjs.org/latest/api/service/$mdDialog</a>
 */