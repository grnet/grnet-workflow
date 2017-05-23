package gr.cyberstream.workflow.engine.model.dashboard;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.MappedSuperclass;
import javax.persistence.SqlResultSetMapping;

@SqlResultSetMapping(
		name = "DatePartResultMapping",
        classes = @ConstructorResult(
        		targetClass = DashboardDatePartResult.class,
        		columns = {
        				@ColumnResult(name = "label"),
        				@ColumnResult(name = "week", type = Long.class),
        				@ColumnResult(name = "month", type = Long.class),
        				@ColumnResult(name = "year", type = Long.class),
        				@ColumnResult(name = "intValue", type = Long.class),
        				@ColumnResult(name = "decimalValue", type = Double.class)
        		})
		)

@MappedSuperclass
public class DashboardDatePartResult {

	private String label;
	private int week = -1;
	private int month = -1;
	private int year = -1;
	private long intValue;
	private double decimalValue;
	
	public DashboardDatePartResult() {
	}

	public DashboardDatePartResult(String label, long week, long month, long year, long intValue, double decimalValue) {
		
		this.label = label;
		this.week = (int) week;
		this.month = (int) month;
		this.year = (int) year;
		this.intValue = intValue;
		this.decimalValue = decimalValue;
	}

	public String getLabel() {
	
		return label;
	}

	public void setLabel(String label) {
	
		this.label = label;
	}

	public long getIntValue() {
	
		return intValue;
	}

	public void setIntValue(long intValue) {
	
		this.intValue = intValue;
	}
	
	public double getDecimalValue() {
		
		return decimalValue;
	}

	public void setDecimalValue(double decimalValue) {
	
		this.decimalValue = decimalValue;
	}

	public int getWeek() {
	
		return week;
	}

	public void setWeek(int week) {
	
		this.week = week;
	}

	public int getMonth() {
	
		return month;
	}

	public void setMonth(int month) {
	
		this.month = month;
	}

	public int getYear() {
	
		return year;
	}

	public void setYear(int year) {
	
		this.year = year;
	}
	
	@Override
	public String toString() {
		
		return label + " - " + (week == -1 ? "" : week + "/") + (month == -1 ? "" : month + "/") 
				+ (year == -1 ? "" : year) + (intValue == 0 ? "" : " - " + intValue) 
				+ (decimalValue == 0.0 ? "" : " - " + decimalValue);
	}
}
