package gr.cyberstream.workflow.engine.model.dashboard;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.MappedSuperclass;
import javax.persistence.SqlResultSetMapping;

@SqlResultSetMapping(
        name = "SimpleResultMapping",
        classes = @ConstructorResult(
                targetClass = DashboardSimpleResult.class,
                columns = {
                	@ColumnResult(name = "label"),
                    @ColumnResult(name = "value", type = Long.class)
                    }))

@MappedSuperclass
public class DashboardSimpleResult {

	private String label;
	private long value;
	
	public DashboardSimpleResult() {
	}

	public DashboardSimpleResult(String label, long value) {
		
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
	
		return label;
	}

	public void setLabel(String label) {
	
		this.label = label;
	}

	public long getValue() {
	
		return value;
	}

	public void setValue(long value) {
	
		this.value = value;
	}
	
	@Override
	public String toString() {
		
		return label + " - " + value;
	}
}
