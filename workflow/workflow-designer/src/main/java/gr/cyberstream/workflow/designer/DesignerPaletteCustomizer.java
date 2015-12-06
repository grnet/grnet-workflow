/**
 * @author nlyk
 */
package gr.cyberstream.workflow.designer;

import java.util.ArrayList;
import java.util.List;

import org.activiti.designer.integration.palette.AbstractDefaultPaletteCustomizer;
import org.activiti.designer.integration.palette.PaletteEntry;

public class DesignerPaletteCustomizer extends AbstractDefaultPaletteCustomizer {

	@Override
	public List<PaletteEntry> disablePaletteEntries() {
	    List<PaletteEntry> result = new ArrayList<PaletteEntry>();
	    
	    // remove Alfresco proprietary tasks and events
	    result.add(PaletteEntry.ALFRESCO_MAIL_TASK);
	    result.add(PaletteEntry.ALFRESCO_SCRIPT_TASK);
	    result.add(PaletteEntry.ALFRESCO_START_EVENT);
	    result.add(PaletteEntry.ALFRESCO_USER_TASK);
	    
	    return result;
	}

}
