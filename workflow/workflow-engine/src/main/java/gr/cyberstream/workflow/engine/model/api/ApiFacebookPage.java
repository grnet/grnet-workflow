package gr.cyberstream.workflow.engine.model.api;

public class ApiFacebookPage {

	private String name;
	private boolean valid;
	private String profilePicUrl;
	private String coverPicUrl;

	public ApiFacebookPage() {}

	public ApiFacebookPage(String name, boolean valid, String profilePicUrl, String coverPicUrl){
		this.name = name;
		this.valid = valid;
		this.profilePicUrl = profilePicUrl;
		this.coverPicUrl = coverPicUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getProfilePicUrl() {
		return profilePicUrl;
	}

	public void setProfilePicUrl(String profilePicUrl) {
		this.profilePicUrl = profilePicUrl;
	}

	public String getCoverPicUrl() {
		return coverPicUrl;
	}

	public void setCoverPicUrl(String coverPicUrl) {
		this.coverPicUrl = coverPicUrl;
	}

}
