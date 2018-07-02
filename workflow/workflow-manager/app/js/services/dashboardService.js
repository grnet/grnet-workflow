define(['angular'],

	function (angular) {

		function DashboardService($http, config, authProvider) {

			var isSupervisor = false;

			if (authProvider.getRoles().indexOf('ROLE_Supervisor') > -1 && authProvider.getRoles().indexOf('ROLE_ProcessAdmin') === -1)
				isSupervisor = true;

			/**
			 * Makes an http request to the server based on the given url in
			 * order to return the stats
			 */
			this.getData = function (src) {

				var supervisorOption = '?supervisor=' + isSupervisor;
				src += supervisorOption;

				return $http.get(config.DASHBOARD_SERVICE_ENTRY + src);
			};

			this.getSupervisorDefaultDashlets = function () {
				var dashlets = [
					{ 'id': 'groupedByDefCompletedInstances', 'name': 'Ολοκληρωμένες εκτελέσεις ανά διαδικασία', 'baseType': 'multiTimeSeriesChart', 'type': 'bar', 'src': '/stat/completed/definition/{from}/{to}/{dateGroup}', 'label': 'Ολοκληρωμένες εκτελέσεις ανά διαδικασία', 'legend': true, 'dateGroup': 'week', 'availableTypes': [{ 'title': 'Ντόνατ', 'type': 'doughnut', '$$hashKey': 'object:374' }, { 'title': 'Γραμμή', 'type': 'line', '$$hashKey': 'object:375' }, { 'title': 'Bar', 'type': 'bar', '$$hashKey': 'object:376' }], 'sizeX': 28, 'sizeY': 12, '$$hashKey': 'object:340', 'row': 0, 'col': 0 },
					{ 'id': 'completedInstances', 'name': 'Ολοκληρωμένες εκτελέσεις', 'baseType': 'singleTimeSeriesChart', 'type': 'bar', 'src': '/stat/completed/{from}/{to}/{dateGroup}', 'label': 'Ολοκληρωμένες εκτελέσεις', 'legend': false, 'dateGroup': 'week', 'availableTypes': [{ 'title': 'Ντόνατ', 'type': 'doughnut', '$$hashKey': 'object:488' }, { 'title': 'Γραμμή', 'type': 'line', '$$hashKey': 'object:489' }, { 'title': 'Bar', 'type': 'bar', '$$hashKey': 'object:490' }], 'sizeX': 28, 'sizeY': 10, '$$hashKey': 'object:463', 'row': 12, 'col': 0 },
					{ 'id': 'ruInstByDef', 'name': 'Διαδικασίες σε εξέλιξη/διαδικασία', 'baseType': 'singleSeriesChart', 'type': 'doughnut', 'src': '/stat/definition/running', 'label': 'Διαδικασίες σε εξέλιξη/διαδικασία', 'legend': false, 'availableTypes': [{ 'title': 'Ντόνατ', 'type': 'doughnut', '$$hashKey': 'object:597' }, { 'title': 'Γραμμή', 'type': 'line', '$$hashKey': 'object:598' }, { 'title': 'Bar', 'type': 'bar', '$$hashKey': 'object:599' }], 'sizeX': 14, 'sizeY': 10, '$$hashKey': 'object:571', 'row': 22, 'col': 0 },
					{ 'id': 'unassignedTasks', 'name': 'Μη ανατεθιμένες εργασίες', 'baseType': 'taskList', 'src': '/stat/tasks/unassigned', 'label': 'Μη ανατεθιμένες εργασίες', 'sizeX': 14, 'sizeY': 10, 'legend': false, '$$hashKey': 'object:664', 'row': 22, 'col': 14 },
					{ 'id': 'overDueTasks', 'name': 'Ληξιπρόθεσμες εργασίες', 'baseType': 'taskList', 'src': '/stat/tasks/overdue', 'label': 'Ληξιπρόθεσμες εργασίες', 'sizeX': 14, 'sizeY': 9, 'legend': false, '$$hashKey': 'object:467', 'row': 32, 'col': 0 },
					{ 'id': 'runInstByOwner', 'name': 'Διαδικασίες σε εξέλιξη/ ιδιοκτήτης', 'baseType': 'singleSeriesChart', 'type': 'doughnut', 'src': '/stat/owner/running', 'label': 'Διαδικασίες σε εξέλιξη/ ιδιοκτήτης', 'legend': false, 'availableTypes': [{ 'title': 'Ντόνατ', 'type': 'doughnut', '$$hashKey': 'object:824' }, { 'title': 'Γραμμή', 'type': 'line', '$$hashKey': 'object:825' }, { 'title': 'Bar', 'type': 'bar', '$$hashKey': 'object:826' }], 'sizeX': 14, 'sizeY': 9, '$$hashKey': 'object:798', 'row': 32, 'col': 14 }
				];

				return dashlets;
			};

		}

		angular.module('wfManagerServices').service('dashboardService', ['$http', 'CONFIG', 'auth', DashboardService]);
	}
);