package gr.cyberstream.workflow.engine.customservicetasks;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import gr.cyberstream.workflow.engine.service.InvalidRequestException;

public class DbAppend implements JavaDelegate{

	private Expression datasource;
	private Expression table;
	private Expression columns;
	private Expression values;
		
	// holds model for data source and table so that the process is not repeated
	private Map<String, Column[]> dataSourceMap; 
	
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		String ds = (String) datasource.getValue(execution);
		String dbTable = (String) table.getValue(execution);
		String dbColumns = (String) columns.getValue(execution);
		String columnValues = (String) values.getValue(execution);
		
		String[] columnParts = dbColumns.split(",");
		String[] columnValuesParts = columnValues.split(",");
		
		if(columnParts.length != columnValuesParts.length){
			System.out.println("Columns and values do not match");
			throw new InvalidRequestException("Columns and values do not match");
		}
		
		// column name-value mapping
		Map<String, Object> columnsMap = new HashMap<String, Object>();
		for(int i=0;i<columnParts.length;i++){
			columnsMap.put(columnParts[i], columnValuesParts[i]);
		}

		// column name-type mapping initialization
		Map<String, String> columnTypesMap = new HashMap<String, String>();
		
		// get data source and connection
		Connection connection = null;
		DataSource dataSource = null;
		try{
			InitialContext context = new InitialContext();
			dataSource = (DataSource) context.lookup(ds);
			connection = dataSource.getConnection();
		}		
		catch (NamingException e) {
			System.out.println("Data source name not found");
			throw new InvalidRequestException("Data source name not found");
		} 
		catch (SQLException e) {
			System.out.println("Connection Error: " + e.getMessage());
			throw new InvalidRequestException("Connection Error: " + e.getMessage());
		}
		
		
		// ddlutils: to find the types of the accumulator table columns 
		Column[] columns;
		
		// check if the model already exists
		if(dataSourceMap != null) 	columns = dataSourceMap.get(ds+":"+dbTable);
		else{
			Platform platform = PlatformFactory.createNewPlatformInstance(dataSource);
			Database database = platform.readModelFromDatabase(dataSource.getConnection().getSchema());
			Table table = database.findTable(dbTable);
			columns = table.getColumns();
			
			dataSourceMap = new HashMap<String, Column[]>();
			dataSourceMap.put(ds+":"+dbTable, columns);
		}
				
		// check the column names and types
		for(Column col : columns){
			
			Object colVal = columnsMap.get(col.getName());
			
			if(colVal==null)	continue;
						
			switch(col.getType()){						
				case "VARCHAR": columnsMap.put(col.getName(),""+colVal);
					columnTypesMap.put(col.getName(), col.getType());
					break;
					
				case "INTEGER": 
					try{
						int intVal = Integer.parseInt(""+colVal);   
						columnsMap.put(col.getName(),intVal);
						columnTypesMap.put(col.getName(), col.getType());
					}
					catch(NumberFormatException e){
						System.out.println("Column format error");
						throw new InvalidRequestException("Column format error");
					}
					break;
					
				/*default: System.out.println("Column type not supported");
					throw new InvalidRequestException("Column type not supported");*/
			}
		}		
		

		// create and execute the query
		Statement statement = connection.createStatement();		
		String query = "INSERT INTO " + dbTable + " (" + dbColumns + ") VALUES (";

		int count=1;
		for(String col : columnParts){			
			if(columnTypesMap.get(col).equals("VARCHAR")) 	query += "'" + columnsMap.get(col) + "'";
			else query += columnsMap.get(col);
			query += (columnValuesParts.length > count) ? "," : ")";
			count++;
		}
	
		System.out.println("Accumulator Query:: " + query);
		
		try{
			statement.executeUpdate(query);		
		}
		catch(SQLException e){
			System.out.println(e.getMessage());
			throw new InvalidRequestException(e.getMessage());
		}
		
		
	}

}
