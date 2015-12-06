/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

/**
 * The persistent class for the process database table.
 * 
 * @author nlyk
 */
@Entity
public class WorkflowDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "process_key")
	// the name "key" is invalid for column name
	private String key;
	private String description;
	private String name;
	private String icon;

	@Column(name = "active_deployment_id")
	private String activeDeploymentId;

	@OneToMany(mappedBy = "workflowDefinition", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("deploymentdate DESC")
	private List<DefinitionVersion> definitionVersions;

	/**
	 * Helper function to add a new dependent DefinitionVersion object and set the reference to the current
	 * WorkflowDefinition object.
	 * 
	 * @param version
	 */
	public void addDefinitionVersion(DefinitionVersion version) {
		if (definitionVersions == null) {
			definitionVersions = new ArrayList<DefinitionVersion>();
		}
		version.setWorkflowDefinition(this);
		definitionVersions.add(version);
	}

	public List<DefinitionVersion> getDefinitionVersions() {
		return definitionVersions;
	}

	public void setDefinitionVersions(List<DefinitionVersion> definitionVersions) {
		this.definitionVersions = definitionVersions;
	}

	public WorkflowDefinition() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getKey() {
		return this.key;
	}

	public void setKey(String code) {
		this.key = code;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getActiveDeploymentId() {
		return activeDeploymentId;
	}

	public void setActiveDeploymentId(String activeDeploymentId) {
		this.activeDeploymentId = activeDeploymentId;
	}

	public String getIcon() {
		 return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Transient
	public DefinitionVersion getVersion(int versionId) {
		for(DefinitionVersion version : this.definitionVersions) {
			if (version.getId() == versionId) {
				return version;
			}
		}
		return null;
	}

	@Transient
	public boolean isSelectedVersionActive() {
		for(DefinitionVersion version : this.definitionVersions) {
			if (version.getDeploymentId() == this.getActiveDeploymentId()) {
				return version.getStatus().equals(WorkflowDefinitionStatus.ACTIVE);
			}
		}
		return false;
	}

}