package gr.cyberstream.workflow.engine.controller.v2;

import gr.cyberstream.workflow.engine.model.dashboard.DashboardDatePartResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardSimpleResult;
import gr.cyberstream.workflow.engine.model.dashboard.DashboardTaskResult;
import gr.cyberstream.workflow.engine.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/v2")
public class DashboardController {

	final static Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@Autowired
	private DashboardService dashboardService;

	@RequestMapping(value = "/stat/definition/running", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardSimpleResult> getDefinitionRunningInstances(
			@RequestParam(name = "supervisor", required = false) boolean supervisor) {

		return dashboardService.getDefinitionRunningInstances(supervisor);
	}

	@RequestMapping(value = "/stat/owner/running", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardSimpleResult> getOwnerRunningInstances() {

		return dashboardService.getOwnerRunningInstances();
	}

	/**
	 * [GET] /api/v2/stat/completed/2016-04-01/-/week
	 */
	@RequestMapping(value = "/stat/completed/{from}/{to}/{dateGroup}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardDatePartResult> getCompletedInstances(@PathVariable String from, @PathVariable String to,
			@PathVariable String dateGroup, @RequestParam(name = "supervisor", required = false) boolean supervisor) {

		// Validate date strings
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date fromDate = null;
		Date toDate = null;

		try {

			if (from.equals("null") || from.equals("-"))
				fromDate = new Date(0);

			else if (!from.equals("null") && !from.equals("-"))
				fromDate = dateFormat.parse(from);

			if (to.equals("null") || to.equals("-"))
				toDate = new Date(2524600800000L);

			else if (!to.equals("null") && !to.equals("-"))
				toDate = dateFormat.parse(to);

		} catch (ParseException e) {
			logger.warn("Unable to parse from/to date. " + e.getMessage());
			return null;
		}

		return dashboardService.getCompletedInstances(supervisor, fromDate, toDate, dateGroup);
	}

	/**
	 * [GET] /api/v2/stat/completed/definition/2016-04-01/-/week [GET]
	 * /api/v2/stat/completed/definition;definition=A,B/2016-04-01/-/week [GET]
	 * /api/v2/stat/completed/owner/2016-04-01/-/month [GET]
	 * /api/v2/stat/completed/owner;owner=A,B/2016-04-01/-/month
	 */
	@RequestMapping(value = "/stat/completed/{groupBy}/{from}/{to}/{dateGroup}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardDatePartResult> getGroupedCompletedInstances(@PathVariable String groupBy,
			@MatrixVariable(pathVar = "groupBy", value = "definition", required = false) List<String> definitions,
			@MatrixVariable(pathVar = "groupBy", value = "owner", required = false) List<String> owners,
			@PathVariable String from, @PathVariable String to, @PathVariable String dateGroup,
			@RequestParam(name = "supervisor", required = false) boolean supervisor) {

		// Validate date strings
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date fromDate = null;
		Date toDate = null;

		try {

			if (from.equals("null") || from.equals("-"))
				fromDate = new Date(0);

			else if (!from.equals("null") && !from.equals("-"))
				fromDate = dateFormat.parse(from);

			if (to.equals("null") || to.equals("-"))
				toDate = new Date(2524600800000L);

			else if (!to.equals("null") && !to.equals("-"))
				toDate = dateFormat.parse(to);

		} catch (ParseException e) {
			logger.warn("Unable to parse from/to date. " + e.getMessage());
			return null;
		}

		switch (groupBy) {

		case "definition":

			String[] definitionsArray = null;

			if (definitions != null) {

				definitionsArray = definitions.toArray(new String[definitions.size()]);
			}

			return dashboardService.getDefinitionCompletedInstances(supervisor, fromDate, toDate, dateGroup,
					definitionsArray);

		case "owner":

			String[] ownersArray = null;

			if (owners != null) {

				ownersArray = owners.toArray(new String[owners.size()]);
			}

			return dashboardService.getOwnerCompletedInstances(fromDate, toDate, dateGroup, ownersArray);

		default:

		}

		return null;
	}

	/**
	 * [GET] /api/v2/stat/started/client/2016-04-01/-/week [GET]
	 * /api/v2/stat/started/client;client=A,B/2016-04-01/-/week
	 * 
	 */
	@RequestMapping(value = "/stat/started/{groupBy}/{from}/{to}/{dateGroup}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardDatePartResult> getGroupedStartedInstances(@PathVariable String groupBy,
			@MatrixVariable(pathVar = "groupBy", value = "client", required = false) List<String> clients,
			@PathVariable String from, @PathVariable String to, @PathVariable String dateGroup,
			@RequestParam(name = "supervisor", required = false) boolean supervisor) {

		// Validate date strings
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date fromDate = null;
		Date toDate = null;

		try {

			if (from.equals("null") || from.equals("-"))
				fromDate = new Date(0);

			else if (!from.equals("null") && !from.equals("-"))
				fromDate = dateFormat.parse(from);

			if (to.equals("null") || to.equals("-"))
				toDate = new Date(2524600800000L);

			else if (!to.equals("null") && !to.equals("-"))
				toDate = dateFormat.parse(to);

		} catch (ParseException e) {

			logger.warn("Unable to parse from/to date. " + e.getMessage());
			return null;
		}

		switch (groupBy) {

		case "client":

			String[] clientsArray = null;

			if (clients != null) {

				clientsArray = clients.toArray(new String[clients.size()]);
			}

			return dashboardService.getClientStartedInstances(supervisor, fromDate, toDate, dateGroup, clientsArray);

		default:

		}

		return null;
	}

	@RequestMapping(value = "/stat/tasks/overdue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardTaskResult> getOverdueTasks(
			@RequestParam(name = "supervisor", required = false) boolean supervisor) {

		return dashboardService.getOverdueTasks(supervisor);
	}

	@RequestMapping(value = "/stat/tasks/unassigned", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardTaskResult> getUnassignedTasks(
			@RequestParam(name = "supervisor", required = false) boolean supervisor) {

		return dashboardService.getUnassignedTasks(supervisor);
	}

	/**
	 * [GET] /api/v2/stat/completed/2016-04-01/-/week
	 */
	@RequestMapping(value = "/stat/completed/times/{from}/{to}/{dateGroup}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardDatePartResult> getCompletedInstancesMeanTimes(@PathVariable String from,
			@PathVariable String to, @PathVariable String dateGroup,
			@RequestParam(name = "supervisor", required = false) boolean supervisor) {

		// Validate date strings
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date fromDate = null;
		Date toDate = null;

		try {

			if (from.equals("null") || from.equals("-"))
				fromDate = new Date(0);

			else if (!from.equals("null") && !from.equals("-"))
				fromDate = dateFormat.parse(from);

			if (to.equals("null") || to.equals("-"))
				toDate = new Date(2524600800000L);

			else if (!to.equals("null") && !to.equals("-"))
				toDate = dateFormat.parse(to);

		} catch (ParseException e) {

			logger.warn("Unable to parse from/to date. " + e.getMessage());
			return null;
		}

		return dashboardService.getCompletedInstancesMeanTime(supervisor, fromDate, toDate, dateGroup);
	}

	@RequestMapping(value = "/stat/users/active/{mode}/{count}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardSimpleResult> getActiveUserTasks(@PathVariable String mode, @PathVariable int count,
			@RequestParam(name = "supervisor", required = false) boolean supervisor) {

		return dashboardService.getActiveUserTasks(supervisor, mode, count);
	}

	@RequestMapping(value = "/stat/users/completed/{from}/{to}/{mode}/{count}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<DashboardSimpleResult> getCompletedUserTasks(@PathVariable String from, @PathVariable String to,
			@PathVariable String mode, @PathVariable int count,
			@RequestParam(name = "supervisor", required = false) boolean supervisor) {

		// Validate date strings
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Date fromDate = null;
		Date toDate = null;

		try {

			if (!from.equals("-"))
				fromDate = dateFormat.parse(from);

			if (!to.equals("-"))
				toDate = dateFormat.parse(to);

		} catch (ParseException e) {

			logger.warn("Unable to parse from/to date. " + e.getMessage());
			return null;
		}

		return dashboardService.getCompletedUserTasks(supervisor, fromDate, toDate, mode, count);
	}
}