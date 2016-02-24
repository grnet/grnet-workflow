/**
 * @author nlyk
 */
package gr.cyberstream.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gr.cyberstream.workflow.engine.model.api.WfExternalForm;
import gr.cyberstream.workflow.engine.model.api.WfProcess;

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

	// TODO: eliminate key - we could get the key from the version
	@Column(name = "process_key")
	// the name "key" is invalid for column name
	private String key;
	private String description;
	private String name;
	private String icon;
	private String owner;
	
	// TODO: to be changed column in Database!!!!!!
	@Column(name = "tasks_assign")
	private boolean assignBySupervisor;
	
	@Column(name = "folder_id")
	private String folderId;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "registry_id")
	private Registry registry;
	
	// TODO: change with selected version id
	@Column(name = "active_deployment_id")
	private String activeDeploymentId;

	@OneToMany(mappedBy = "workflowDefinition", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("version ASC")
	private List<DefinitionVersion> definitionVersions;
	
	@OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ExternalForm> externalForms;

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
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}
	
	public boolean isAssignBySupervisor() {
		return assignBySupervisor;
	}

	public void setAssignBySupervisor(boolean assignBySupervisor) {
		this.assignBySupervisor = assignBySupervisor;
	}
	
	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	public List<ExternalForm> getExternalForms() {
		return externalForms;
	}

	public void setExternalForms(List<ExternalForm> externalForms) {
		this.externalForms = externalForms;
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
			if (version.getDeploymentId().equals(this.getActiveDeploymentId())) {
				return WorkflowDefinitionStatus.ACTIVE.equalsName(version.getStatus());
			}
		}
		return false;
	}
	
	@Transient
	public DefinitionVersion getActiveVersion() {
		for(DefinitionVersion version : this.definitionVersions) {
			if (version.getDeploymentId().equals(this.getActiveDeploymentId()) &&
					WorkflowDefinitionStatus.ACTIVE.equalsName(version.getStatus())) {
				return version;
			}
		}
		return null;
	}
	
	public void updateFrom(WfProcess process) {
		this.setDescription(process.getDescription());
		this.setName(process.getName());
		this.setIcon(process.getIcon());
		this.setOwner(process.getOwner());
		this.setAssignBySupervisor(process.isAssignBySupervisor());
	}
}