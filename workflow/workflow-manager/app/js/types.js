/**
 * @author nlyk
 */

/**
 * @typedef {Promise} HttpPromise
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