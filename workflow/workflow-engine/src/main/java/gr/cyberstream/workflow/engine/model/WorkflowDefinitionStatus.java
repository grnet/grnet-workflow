package gr.cyberstream.workflow.engine.model;

/**
 * Enumerated type class for definition version status values
 * 
 * @author nlyk
 *
 */
public enum WorkflowDefinitionStatus {

	// @formatter:off
	//
	NEW("new"),				// when a new workflow definition version is uploaded has a status of "new"
	ACTIVE("active"), 		// a workflow definition version can be activated. Only one version can be active at a time (to be used for instantiation)
	INACTIVE("inactive"), 	// a workflow definition version can be deactivated (get the status of inactive)
	RETIRED("retired");		// an "old" workflow definition version
	//
	// @formatter:on

	private final String name;

	private WorkflowDefinitionStatus(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		return (otherName == null) ? false : name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}
}
