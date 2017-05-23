package gr.cyberstream.workflow.engine.model.api;

public class ApiTwitterAccount {
	
	private String screenName;
	private String name;
	private String profilePicUrl;
	private String profileBannerUrl;
	
	public ApiTwitterAccount(){}
	
	public ApiTwitterAccount(String screenName, String name, String profilePicUrl, String profileBannerUrl){
		this.screenName = screenName;
		this.name = name;
		this.profilePicUrl = profilePicUrl;
		this.profileBannerUrl = profileBannerUrl;

	}
	
	
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getProfilePicUrl() {
		return profilePicUrl;
	}
	public void setProfilePicUrl(String profilePicUrl) {
		this.profilePicUrl = profilePicUrl;
	}

	public String getProfileBannerUrl() {
		return profileBannerUrl;
	}

	public void setProfileBannerUrl(String profileBannerUrl) {
		this.profileBannerUrl = profileBannerUrl;
	}


}
