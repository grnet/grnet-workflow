/**
 * @author nlyk
 */

/**
 * @typedef {Promise} HttpPromise
 */

/**
 * @typedef {Object} WfProcess
 * @property {number} id
 * @property {string} name
 * @property {string} description
 * @property {string} icon
 * @property {string} processDefinitionId
 * @property {boolean} active
 * @property {WfProcessVersion[]} processVersions
 * @property {FormProperty[]} processForm
 */

/**
 * @typedef {Object} WfProcessVersion
 * @property {number} id
 * @property {string} deploymentId
 * @property {number} version
 * @property {date} deploymentdate
 * @property {string} status
 */

/**
 * @typedef {Object} FormProperty
 * @property {string} id
 * @property {string} name
 * @property {boolean} readable
 * @property {boolean} required
 * @property {boolean} writable
 * @property {{mimeType: string, name: string}} type
 * @property value
 */

/**
 * @typedef {Object} WorkflowDefinition
 * @property {number} id
 * @property {string} key
 * @property {string} description
 * @property {string} name
 * @property {string} activeDeploymentId
 * @property {DefinitionVersion[]} definitionVersions
 */

/**
 * @typedef {Object} DefinitionVersion
 * @property {number} id
 * @property {string} deploymentId
 * @property {number} version
 * @property {date} deploymentdate
 * @property {string} status
 */