define(['angular', 'services/dashboardService'],

	function (angular) {

		'use strict';

		function dashboardCtrl($scope, $mdDialog, dashboardService) {

			// grid options
			$scope.gridsterOptions = { margins: [15, 15], outerMargin: true, columns: 28, draggable: { handle: 'md-toolbar' }, resizable: { handles: ['se'] } };

			$scope.dashlets = [];

			$scope.predefinedDashlets = [
				{
					id: "ruInstByDef",
					name: "Διαδικασίες σε εξέλιξη/διαδικασία",
					baseType: "singleSeriesChart",
					type: "doughnut",
					src: "/stat/definition/running",
					label: "Διαδικασίες σε εξέλιξη/διαδικασία",
					legend: true,
					availableTypes: [
						{ title: "Ντόνατ", type: "doughnut" },
						{ title: "Γραμμή", type: "line" },
						{ title: "Bar", type: "bar" }
					]
				},

				{
					id: "runInstByOwner",
					name: "Διαδικασίες σε εξέλιξη/ ιδιοκτήτης",
					baseType: "singleSeriesChart",
					type: "doughnut",
					src: "/stat/owner/running",
					label: "Διαδικασίες σε εξέλιξη/ ιδιοκτήτης",
					legend: true,
					availableTypes: [
						{ title: "Ντόνατ", type: "doughnut" },
						{ title: "Γραμμή", type: "line" },
						{ title: "Bar", type: "bar" }
					]
				},

				{
					id: "completedInstances",
					name: "Ολοκληρωμένες εκτελέσεις",
					baseType: "singleTimeSeriesChart",
					type: "bar",
					src: "/stat/completed/{from}/{to}/{dateGroup}",
					label: "Ολοκληρωμένες εκτελέσεις",
					legend: true,
					dateGroup: 'week',
					isFromChanged: false,
					availableTypes: [
						{ title: "Ντόνατ", type: "doughnut" },
						{ title: "Γραμμή", type: "line" },
						{ title: "Bar", type: "bar" }
					]
				},

				{
					id: "groupedByOwnerCompletedInstances",
					name: "Ολοκληρωμένες εκτελέσεις ανά ιδιοκτήτη",
					baseType: "multiTimeSeriesChart",
					type: "line",
					src: "/stat/completed/owner/{from}/{to}/{dateGroup}",
					label: "Ολοκληρωμένες εκτελέσεις ανά ιδιοκτήτη",
					legend: true,
					dateGroup: 'week',
					isFromChanged: false,
					availableTypes: [
						{ title: "Ντόνατ", type: "doughnut" },
						{ title: "Γραμμή", type: "line" },
						{ title: "Bar", type: "bar" }
					]
				},

				{
					id: "groupedByDefCompletedInstances",
					name: "Ολοκληρωμένες εκτελέσεις ανά διαδικασία",
					baseType: "multiTimeSeriesChart",
					type: "line",
					src: "/stat/completed/definition/{from}/{to}/{dateGroup}",
					label: "Ολοκληρωμένες εκτελέσεις ανά διαδικασία",
					legend: true,
					isFromChanged: false,
					dateGroup: 'week',
					availableTypes: [
						{ title: "Ντόνατ", type: "doughnut" },
						{ title: "Γραμμή", type: "line" },
						{ title: "Bar", type: "bar" }
					]
				},

				{
					id: "overDueTasks",
					name: "Ληξιπρόθεσμες εργασίες",
					baseType: "taskList",
					src: "/stat/tasks/overdue",
					label: "Ληξιπρόθεσμες εργασίες"
				},

				{
					id: "unassignedTasks",
					name: "Μη ανατεθιμένες εργασίες",
					baseType: "taskList",
					src: "/stat/tasks/unassigned",
					label: "Μη ανατεθιμένες εργασίες"
				},

				{
					id: "startedInstancesByMobile",
					name: "Εκτελέσεις ανά συσκευή υποβολής",
					baseType: "multiTimeSeriesChart",
					type: "bar",
					src: "/stat/started/client/{from}/{to}/{dateGroup}",
					label: "Εκτελέσεις ανά συσκευή υποβολής",
					legend: true,
					isFromChanged: false,
					dateGroup: 'week',
					availableTypes: [
						{ title: "Ντόνατ", type: "doughnut" },
						{ title: "Γραμμή", type: "line" },
						{ title: "Bar", type: "bar" }
					]
				},

				{
					id: "activeUserTasks",
					name: "Ανατεθειμένες εργασίες σε υπαλλήλους",
					baseType: "list",
					src: "/stat/users/active/asc/50",
					label: "Ανατεθειμένες εργασίες σε υπαλλήλους"
				},

				{
					id: "completedInstancesMeanTimes",
					name: "Χρόνοι ολοκλήρωσης εκτελέσεων",
					baseType: "multiTimeSeriesChart",
					type: "bar",
					src: "/stat/completed/times/{from}/{to}/{dateGroup}",
					label: "Χρόνοι ολοκλήρωσης εκτελέσεων",
					legend: true,
					dateGroup: 'week',
					isFromChanged: false,
					valueToGet: 'decimalValue',
					availableTypes: [
						{ title: "Γραμμή", type: "line" },
						{ title: "Bar", type: "bar" }
					]
				}
			];

			if (localStorage.getItem('dashBoard') != "null" && localStorage.getItem('dashBoard') != null && localStorage.getItem('dashBoard').length > 0)
				$scope.dashlets = JSON.parse(localStorage.getItem('dashBoard'));

			else
				// get a default dashboard configuration
				$scope.dashlets = dashboardService.getSupervisorDefaultDashlets();

			// watch for changes in order to update the local storage
			$scope.$watch('dashlets', function (items) {

				for (var index = 0; index < $scope.dashlets.length; index++) {
					var dashlet = $scope.dashlets[index];

					if (dashlet.sizeX < 10 || dashlet.sizeY < 10)
						dashlet.legend = false;
				}

				updateLocalStorage();
			}, true);

			/**
			 * Add a new dashlet
			 */
			$scope.addDashlet = function (event) {
				$mdDialog.show({
					controller: addDashletController,
					templateUrl: 'templates/add-dashlet.tmpl.html',
					parent: document.body,
					clickOutsideToClose: true,
					scope: $scope,
					preserveScope: true,
					locals: {
						'predefinedDashlets': $scope.predefinedDashlets
					}
				});
			};

			/**
			 * New dashlet's modal controller
			 */
			function addDashletController($scope, $mdDialog, predefinedDashlets) {
				$scope.predefinedDashlets = predefinedDashlets;
				$scope.edit = false;

				$scope.cancel = function () {
					$mdDialog.cancel();
				};

				$scope.addPredefinedDashlet = function (dashlet) {
					var newDashlet = angular.copy(dashlet);

					// set a default size for the new dashlet
					newDashlet.sizeX = 8;
					newDashlet.sizeY = 8;

					for (var index = 0; index < $scope.dashlets.length; index++) {
						if ($scope.dashlets[index].id === newDashlet.id) {
							newDashlet.id = guid();
							break;
						}
					}

					$scope.dashlets.push(newDashlet);
					$mdDialog.cancel();
					updateLocalStorage();
				};
			};

			/**
			 * Generates a unique if used by dashlet's id
			 */
			function guid() {
				function s4() {
					return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
				}

				return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
			};

			$scope.$on('updateSrc', function (event, saveObject) {
				var dashletId = saveObject.dashletId;
				var searchCriteria = saveObject.searchCriteria;

				for (var index = 0; index < $scope.dashlets.length; index++) {
					if ($scope.dashlets[index].id == dashletId) {

						$scope.dashlet = $scope.dashlets[index];

						if ($scope.dashlet.isFromChanged === true)
							$scope.dashlet.from = searchCriteria.from;

						$scope.dashlet.dateGroup = searchCriteria.dateGroup;
						updateLocalStorage();

						return;
					}
				}
			});

			/**
			 * Catches the delete event emited by the dashlet directive in
			 * order to delete the dashlet from the dashlets list
			 */
			$scope.$on('deleteDashlet', function (event, dashletId) {
				for (var index = 0; index < $scope.dashlets.length; index++) {

					if ($scope.dashlets[index].id == dashletId)
						$scope.dashlets.splice(index, 1);
				}
			});

			/**
			 * Updates localstorage when a change is occured on the dashboard.
			 * Such as a new dashlet, move an existing one, etc.
			 */
			function updateLocalStorage() {
				localStorage.setItem("dashBoard", JSON.stringify($scope.dashlets));
			};

		}

		angular.module('wfManagerControllers').controller('DashboardCtrl', ['$scope', '$mdDialog', 'dashboardService', dashboardCtrl]);
	}
);