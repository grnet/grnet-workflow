package gr.cyberstream.workflow.engine.persistence.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gr.cyberstream.workflow.engine.model.dashboard.DashboardDatePartResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardSimpleResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardTaskResult;
import gr.cyberstream.workflow.engine.persistence.Dashboard;

@Repository
public class JPADashboard implements Dashboard {

	@PersistenceContext
	private EntityManager entityManager;
		
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardSimpleResult> getDefinitionRunningInstances(String supervisor) {

		String whereClause = "WHERE i.status = 'running' ";
		
		if (supervisor != null) {
			
			whereClause += " and i.supervisor = :supervisor ";
		}
		
		String queryString = "SELECT d.name as label, count(*) as value FROM WorkflowDefinition as d "
				+ "inner join DefinitionVersion as v on d.id = v.workflow_definition_id "
				+ "inner join WorkflowInstance as i on v.id = i.definition_version_id " 
				+ whereClause
				+ "GROUP BY d.name";
		
		Query query = entityManager.createNativeQuery(queryString, "SimpleResultMapping");

		if (supervisor != null) {
			
			query.setParameter("supervisor", supervisor);
		}

		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardSimpleResult> getOwnerRunningInstances() {

		Query query = entityManager.createNativeQuery("SELECT o.ownerName as label, count(*) as value FROM WorkflowDefinition as d "
				+ "inner join DefinitionVersion as v on d.id = v.workflow_definition_id "
				+ "inner join WorkflowInstance as i on v.id = i.definition_version_id " 
				+ "inner join Owner as o on d.owner = o.ownerId "
				+ "WHERE i.status = 'running'"
				+ "GROUP BY d.owner", "SimpleResultMapping");

		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardDatePartResult> getCompletedInstances(String supervisor, Date from, Date to, String dateGroup) {
		
		String selectClause = "SELECT ";
		String groupByClause = "GROUP BY ";
		String orderByClause = "ORDER BY ";
		
		switch(dateGroup) {
		
		case "week":
			selectClause += "concat(cast(year(end_date) as char),'-',cast(week(end_date) as char)) as label, weekofyear(end_date) as week, -1 as month, year(end_date) as year";
			groupByClause += "weekofyear(end_date), year(end_date)";
			orderByClause += "weekofyear(end_date), year(end_date)";
			break;
		case "month":
			selectClause += "concat(cast(year(end_date) as char),'-',cast(month(end_date) as char)) as label, -1 as week, month(end_date) as month, year(end_date) as year";
			groupByClause += "month(end_date), year(end_date)";
			orderByClause += "month(end_date), year(end_date)";
			break;
		case "year": default:
			selectClause += "cast(year(end_date) as char) as label, -1 as week, -1 as month, year(end_date) as year";
			groupByClause += "year(end_date)";
			orderByClause += "year(end_date)";			
		}
		
		selectClause += ", count(*) as intValue, 0.0 as decimalValue";
		
		String whereClause = "WHERE ";
		
		if (from == null && to != null) {
			whereClause += "end_date < :to";
			
		} else if (from != null && to == null) {
			whereClause += "end_date > :from";
			
		} else if (from != null && to != null) {
			whereClause += "end_date between :from and :to";
		}
		
		if (supervisor != null) {
			
			whereClause += " and supervisor = :supervisor";
		}
		
		whereClause += " and status = 'ended'";
		
		Query query = entityManager.createNativeQuery(selectClause + " FROM WorkflowInstance " 
				+ whereClause + " " + groupByClause + " " + orderByClause, "DatePartResultMapping");
		
		if (from == null && to != null) {
			query.setParameter("to", to);
			
		} else if (from != null && to == null) {
			query.setParameter("from", from);
			
		} else if (from != null && to != null) {
			query.setParameter("from", from);
			query.setParameter("to", to);
		}
		
		if (supervisor != null) {
			
			query.setParameter("supervisor", supervisor);
		}
		
		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardDatePartResult> getDefinitionCompletedInstances(String supervisor, Date from, Date to, String dateGroup, String... definitions) {
	
		String selectClause = "SELECT d.name as label, ";
		String groupByClause = "GROUP BY d.name, ";
		String orderByClause = "ORDER BY d.name, ";
		
		switch(dateGroup) {
		
		case "week":
			selectClause += "weekofyear(end_date) as week, -1 as month, year(end_date) as year";
			groupByClause += "weekofyear(end_date), year(end_date)";
			orderByClause += "weekofyear(end_date), year(end_date)";
			break;
		case "month":
			selectClause += "-1 as week, month(end_date) as month, year(end_date) as year";
			groupByClause += "month(end_date), year(end_date)";
			orderByClause += "month(end_date), year(end_date)";
			break;
		case "year": default:
			selectClause += "-1 as week, -1 as month, year(end_date) as year";
			groupByClause += "year(end_date)";
			orderByClause += "year(end_date)";
		}
		
		selectClause += ", count(*) as intValue, 0.0 as decimalValue";
		
		String whereClause = "WHERE ";
		
		if (from == null && to != null) {
			whereClause += "end_date < :to";
			
		} else if (from != null && to == null) {
			whereClause += "end_date > :from";
			
		} else if (from != null && to != null) {
			whereClause += "end_date between :from and :to";
		}
		
		if (supervisor != null) {
			
			whereClause += " and i.supervisor = :supervisor";
		}
		
		if (definitions != null && definitions.length > 0) {
			
			whereClause += " and d.name in (";
			
			for (int i = 0; i < definitions.length; i++) {
				
				whereClause += "'" + definitions[i] + "'";
				
				if (i < definitions.length - 1)
					whereClause += ", ";
			}
			
			whereClause += ")";
		}
		
		whereClause += " and i.status = 'ended'";
		
		Query query = entityManager.createNativeQuery(
				selectClause + " FROM WorkflowDefinition as d "
				+ "inner join DefinitionVersion as v on d.id = v.workflow_definition_id "
				+ "inner join WorkflowInstance as i on v.id = i.definition_version_id " 
				+ whereClause + " " + groupByClause + " " + orderByClause, "DatePartResultMapping");
		
		if (from == null && to != null) {
			query.setParameter("to", to);
			
		} else if (from != null && to == null) {
			query.setParameter("from", from);
			
		} else if (from != null && to != null) {
			query.setParameter("from", from);
			query.setParameter("to", to);
		}
		
		if (supervisor != null) {
			
			query.setParameter("supervisor", supervisor);
		}
		
		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardDatePartResult> getOwnerCompletedInstances(Date from, Date to, String dateGroup, String... owners) {
		
		String selectClause = "SELECT o.ownerName as label, ";
		String groupByClause = "GROUP BY o.ownerName, ";
		String orderByClause = "ORDER BY o.ownerName, ";
		
		switch(dateGroup) {
		
		case "week":
			selectClause += "weekofyear(end_date) as week, -1 as month, year(end_date) as year";
			groupByClause += "weekofyear(end_date), year(end_date)";
			orderByClause += "weekofyear(end_date), year(end_date)";
			break;
		case "month":
			selectClause += "-1 as week, month(end_date) as month, year(end_date) as year";
			groupByClause += "month(end_date), year(end_date)";
			orderByClause += "month(end_date), year(end_date)";
			break;
		case "year": default:
			selectClause += "-1 as week, -1 as month, year(end_date) as year";
			groupByClause += "year(end_date)";
			orderByClause += "year(end_date)";
		}
		
		selectClause += ", count(*) as intValue, 0.0 as decimalValue";
		
		String whereClause = "WHERE ";
		
		if (from == null && to != null) {
			whereClause += "end_date < :to";
			
		} else if (from != null && to == null) {
			whereClause += "end_date > :from";
			
		} else if (from != null && to != null) {
			whereClause += "end_date between :from and :to";
		}
		
		if (owners != null && owners.length > 0) {
			
			whereClause += " and o.ownerName in (";
			
			for (int i = 0; i < owners.length; i++) {
				
				whereClause += "'" + owners[i] + "'";
				
				if (i < owners.length - 1)
					whereClause += ", ";
			}
			
			whereClause += ")";
		}
		
		whereClause += " and i.status = 'ended'";
		
		Query query = entityManager.createNativeQuery(
				selectClause + " FROM WorkflowDefinition as d "
				+ "inner join DefinitionVersion as v on d.id = v.workflow_definition_id "
				+ "inner join WorkflowInstance as i on v.id = i.definition_version_id " 
				+ "inner join Owner as o on d.owner = o.ownerId "
				+ whereClause + " " + groupByClause + " " + orderByClause, "DatePartResultMapping");
		
		if (from == null && to != null) {
			query.setParameter("to", to);
			
		} else if (from != null && to == null) {
			query.setParameter("from", from);
			
		} else if (from != null && to != null) {
			query.setParameter("from", from);
			query.setParameter("to", to);
		}
		
		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardDatePartResult> getClientStartedInstances(String supervisor, Date from, Date to, String dateGroup, String... clients) {
		
		String selectClause = "SELECT i.client as label, ";
		String groupByClause = "GROUP BY i.client, ";
		String orderByClause = "ORDER BY i.client, ";
		
		switch(dateGroup) {
		
		case "week":
			selectClause += "weekofyear(start_date) as week, -1 as month, year(start_date) as year";
			groupByClause += "weekofyear(start_date), year(start_date)";
			orderByClause += "weekofyear(start_date), year(start_date)";
			break;
		case "month":
			selectClause += "-1 as week, month(start_date) as month, year(start_date) as year";
			groupByClause += "month(start_date), year(start_date)";
			orderByClause += "month(start_date), year(start_date)";
			break;
		case "year": default:
			selectClause += "-1 as week, -1 as month, year(start_date) as year";
			groupByClause += "year(start_date)";
			orderByClause += "year(start_date)";
		}
		
		selectClause += ", count(*) as intValue, 0.0 as decimalValue";
		
		String whereClause = "WHERE ";
		
		if (from == null && to != null) {
			whereClause += "start_date < :to";
			
		} else if (from != null && to == null) {
			whereClause += "start_date > :from";
			
		} else if (from != null && to != null) {
			whereClause += "start_date between :from and :to";
		}
		
		if (supervisor != null) {
			
			whereClause += " and i.supervisor = :supervisor";
		}
		
		if (clients != null && clients.length > 0) {
			
			whereClause += " and i.client in (";
			
			for (int i = 0; i < clients.length; i++) {
				
				whereClause += "'" + clients[i] + "'";
				
				if (i < clients.length - 1)
					whereClause += ", ";
			}
			
			whereClause += ")";
		}
		
		Query query = entityManager.createNativeQuery(
				selectClause + " FROM WorkflowInstance as i "
				+ whereClause + " " + groupByClause + " " + orderByClause, "DatePartResultMapping");
		
		if (from == null && to != null) {
			query.setParameter("to", to);
			
		} else if (from != null && to == null) {
			query.setParameter("from", from);
			
		} else if (from != null && to != null) {
			query.setParameter("from", from);
			query.setParameter("to", to);
		}
		
		if (supervisor != null) {
			
			query.setParameter("supervisor", supervisor);
		}
		
		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardTaskResult> getOverdueTasks(String supervisor, String taskTableName) {
		
		String whereClause = "WHERE DUE_DATE_ is not null and DUE_DATE_ < :now ";
		
		if (supervisor != null) {
			
			whereClause += "and i.supervisor = :supervisor ";
		}
		
		Query query = entityManager.createNativeQuery("SELECT i.title as instanceName, t.ID_ as taskId, "
				+ "t.NAME_ as taskName, t.ASSIGNEE_ as assignee, t.DUE_DATE_ dueDate "
				+ "FROM " + taskTableName + " t inner join WorkflowInstance i on t.PROC_INST_ID_ = i.id " 
				+ whereClause + "ORDER BY t.DUE_DATE_ asc", "TaskResultMapping");

		query.setParameter("now", new Date());
		
		if (supervisor != null) {
			
			query.setParameter("supervisor", supervisor);
		}

		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardTaskResult> getUnassignedTasks(String supervisor, String taskTableName) {
		
		String whereClause = "WHERE ASSIGNEE_ is null ";
		
		if (supervisor != null) {
			
			whereClause += "and i.supervisor = :supervisor ";
		}
		
		Query query = entityManager.createNativeQuery("SELECT i.title as instanceName, t.ID_ as taskId, "
				+ "t.NAME_ as taskName, t.ASSIGNEE_ as assignee, t.DUE_DATE_ dueDate "
				+ "FROM " + taskTableName + " t inner join WorkflowInstance i on t.PROC_INST_ID_ = i.id " 
				+ whereClause + "ORDER BY t.CREATE_TIME_ asc", "TaskResultMapping");

		if (supervisor != null) {
			
			query.setParameter("supervisor", supervisor);
		}

		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardDatePartResult> getCompletedInstancesMeanTime(String supervisor, Date from, Date to, String dateGroup) {
		
		String selectClause = "SELECT ";
		String groupByClause = "GROUP BY ";
		String orderByClause = "ORDER BY ";
		
		switch(dateGroup) {
		
		case "week":
			selectClause += "d.name as label, weekofyear(end_date) as week, -1 as month, year(end_date) as year";
			groupByClause += "d.name, weekofyear(end_date), year(end_date)";
			orderByClause += "d.name, weekofyear(end_date), year(end_date)";
			break;
		case "month":
			selectClause += "d.name as label, -1 as week, month(end_date) as month, year(end_date) as year";
			groupByClause += "d.name, month(end_date), year(end_date)";
			orderByClause += "d.name, month(end_date), year(end_date)";
			break;
		case "year": default:
			selectClause += "d.name as label, -1 as week, -1 as month, year(end_date) as year";
			groupByClause += "d.name, year(end_date)";
			orderByClause += "d.name, year(end_date)";			
		}
		
		selectClause += ", avg(timestampdiff(minute, start_date, end_date) / 1440) as decimalValue, count(*) as intValue";
		
		String whereClause = "WHERE ";
		
		if (from == null && to != null) {
			whereClause += "end_date < :to";
			
		} else if (from != null && to == null) {
			whereClause += "end_date > :from";
			
		} else if (from != null && to != null) {
			whereClause += "end_date between :from and :to";
		}
		
		if (supervisor != null) {
			
			whereClause += " and i.supervisor = :supervisor";
		}	
		
		whereClause += " and i.status = 'ended'";
		
		Query query = entityManager.createNativeQuery(selectClause + " FROM WorkflowDefinition as d "
				+ "inner join DefinitionVersion as v on d.id = v.workflow_definition_id "
				+ "inner join WorkflowInstance as i on v.id = i.definition_version_id " 
				+ whereClause + " " + groupByClause + " " + orderByClause, "DatePartResultMapping");
		
		if (from == null && to != null) {
			query.setParameter("to", to);
			
		} else if (from != null && to == null) {
			query.setParameter("from", from);
			
		} else if (from != null && to != null) {
			query.setParameter("from", from);
			query.setParameter("to", to);
		}
		
		if (supervisor != null) {
			
			query.setParameter("supervisor", supervisor);
		}
		
		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardSimpleResult> getActiveUserTasks(String supervisor, String taskTableName, String mode, int count) {
		
		String fromClause = "FROM " + taskTableName + " t ";
		String whereClause = "WHERE assignee_ is not null ";
		
		if (supervisor != null) {
			
			fromClause += "inner join WorkflowInstance i on t.PROC_INST_ID_ = i.id ";
			whereClause += "and i.supervisor = :supervisor ";
		}
		
		Query query = entityManager.createNativeQuery("SELECT assignee_ as label, count(*) as value "
				+ fromClause + whereClause
				+ "GROUP BY assignee_ "
				+ "ORDER BY count(*) " + (mode.equals("top") ? "desc " : "asc ")
				+ "LIMIT " + count, "SimpleResultMapping");

		if (supervisor != null) {
			
			query.setParameter("supervisor", supervisor);
		}

		return query.getResultList();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<DashboardSimpleResult> getCompletedUserTasks(String supervisor, String taskTableName, Date from, Date to, 
			String mode, int count) {
		
		String fromClause = "FROM " + taskTableName + " t ";
		String whereClause = "WHERE assignee_ is not null and delete_reason_ = 'completed' ";
		
		if (supervisor != null) {
			
			fromClause += "inner join WorkflowInstance i on t.PROC_INST_ID_ = i.id ";
			whereClause += "and i.supervisor = :supervisor ";
		}
		
		if (from == null && to != null) {
			whereClause += "and end_time_ < :to ";
			
		} else if (from != null && to == null) {
			whereClause += "and end_time_ > :from ";
			
		} else if (from != null && to != null) {
			whereClause += "and end_time_ between :from and :to";
		}
		
		String queryString = "SELECT assignee_ as label, count(*) as value "
				+ fromClause + whereClause
				+ "GROUP BY assignee_ "
				+ "ORDER BY count(*) " + (mode.equals("top") ? "desc " : "asc ")
				+ "LIMIT " + count;
		
		Query query = entityManager.createNativeQuery(queryString, "SimpleResultMapping");
		
		if (from == null && to != null) {
			query.setParameter("to", to);
			
		} else if (from != null && to == null) {
			query.setParameter("from", from);
			
		} else if (from != null && to != null) {
			query.setParameter("from", from);
			query.setParameter("to", to);
		}
		
		if (supervisor != null) {
			
			query.setParameter("supervisor", supervisor);
		}

		return query.getResultList();
	}
}
