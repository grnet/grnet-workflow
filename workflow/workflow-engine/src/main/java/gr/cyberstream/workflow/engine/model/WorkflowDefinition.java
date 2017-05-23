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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gr.cyberstream.workflow.engine.model.api.WfProcess;

/**
 * The persistent class for the WorkflowDefinition database table.
 * 
 * TODO: Needs review (properties mostly)
 * 
 * @author nlyk
 */
@Entity
@Table(name = "WorkflowDefinition")
public class WorkflowDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	// TODO: eliminate key - we could get the key from the version
	// the name "key" is invalid for column name
	@Column(name = "process_key")
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

	@Column(name = "start_form")
	private boolean startForm;

	/**
	 * Helper function to add a new dependent DefinitionVersion object and set
	 * the reference to the current WorkflowDefinition object.
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

	/**
	 * Default constructor
	 */
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

	public boolean hasStartForm() {
		return startForm;
	}

	public void setStartForm(boolean startForm) {
		this.startForm = startForm;
	}

	@Transient
	public DefinitionVersion getVersion(int versionId) {
		for (DefinitionVersion version : this.definitionVersions) {
			if (version.getId() == versionId) {
				return version;
			}
		}
		return null;
	}

	@Transient
	public boolean isSelectedVersionActive() {
		for (DefinitionVersion version : this.definitionVersions) {
			if (version.getDeploymentId().equals(this.getActiveDeploymentId())) {
				return WorkflowDefinitionStatus.ACTIVE.equalsName(version.getStatus());
			}
		}
		return false;
	}

	@Transient
	public DefinitionVersion getActiveVersion() {
		for (DefinitionVersion version : this.definitionVersions) {
			if (version.getDeploymentId().equals(this.getActiveDeploymentId())
					&& WorkflowDefinitionStatus.ACTIVE.equalsName(version.getStatus())) {
				return version;
			}
		}
		return null;
	}

	/**
	 * Copy constructor
	 * 
	 * TODO: Needs review may overlap with other constructors
	 * 
	 * @param process
	 */
	public void updateFrom(WfProcess process) {
		this.description = process.getDescription();
		this.name = process.getName();
		this.icon = process.getIcon();
		this.owner = process.getOwner();
		this.assignBySupervisor = process.isAssignBySupervisor();
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof WorkflowDefinition) {
			WorkflowDefinition that = (WorkflowDefinition) other;
			result = (this.getId() == that.getId());
		}
		return result;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(id);
		builder.append(key);
		builder.append(description);
		builder.append(name);
		builder.append(icon);
		builder.append(owner);
		builder.append(assignBySupervisor);
		builder.append(folderId);
		builder.append(registry);
		builder.append(activeDeploymentId);
		builder.append(definitionVersions);
		builder.append(externalForms);
		builder.append(startForm);
		return builder.toHashCode();
	}

}