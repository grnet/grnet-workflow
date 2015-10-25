package gr.cyberstream.auth.util;

import gr.cyberstream.auth.backend.UserManagementBackend;
import gr.cyberstream.auth.model.Role;

import javax.el.ELContext;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
/**
 * @exclude
 */
public class RoleConverter implements Converter {

	private UserManagementBackend userManagementBackend;

	public Object getAsObject(FacesContext facesContext, UIComponent component, String s) {

		for (Role role : getUserManagementBackend(facesContext).getRolesList()) {

			if (role.getName().equals(s)) {
				return role;
			}
		}

		return null;
	}

	public String getAsString(FacesContext facesContext, UIComponent component, Object obj) {

		if (obj == null)
			return null;

		return ((Role) obj).getName();
	}

	private UserManagementBackend getUserManagementBackend(FacesContext facesContext) {

		if (userManagementBackend == null) {
			ELContext elContext = facesContext.getELContext();
			userManagementBackend = (UserManagementBackend) elContext.getELResolver().getValue(elContext, null, "userManagementBackend");
		}

		return userManagementBackend;
	}
}
