define(['angular'],

	function (angular) {

		'use strict';

		function singleTimeSeriesChart($compile) {
			return {
				require: 'ngModel', // the ng-model is required
				restrict: 'E', // only matches element name
				controller: 'wfSingleTimeSeriesChartCtrl', // directive's controller
				transclude: true, // directive wraps other elements
				scope: { // is an Angular scope object. It is somehow a link between the view and the controller part 
					ngModel: '=', // its a shorthand for ng-model
					wfChartType: '&wfChartType',
					wfLegend: '&wfLegend'
				},
				templateUrl: 'templates/wfSingleTimeSeries.tmpl.html',
				link: function (scope, element, attributes, ngModel) {

					scope.ngModel = scope.ngModel || {};

					if (typeof scope.ngModel !== 'object') {
						scope.ngModel = angular.fromJson(scope.ngModel);
					}

					var chartContainer = '<div ng-show="!showProgressBar && !showNoData" style="width: 100%; height: 100%" class="chartContainer"></div>';
					angular.element(element[0].querySelector(".chartContainer")).append(chartContainer);
				}
			};
		}

		function singleTimeSeriesChartCtrl($scope, $compile, $filter, $element, dashboardService) {

			$scope.dashlet = $scope.ngModel;

			// show the progress bar till request is done
			$scope.showProgressBar = true;

			// constrains for datepickers
			$scope.nextDay = new Date();
			$scope.nextDay.setDate($scope.nextDay.getDate() + 1);
			$scope.today = new Date();

			// object that holds the search criteria
			$scope.criteria = { from: null, to: null, dateGroup: null };

			if ($scope.dashlet.isFromChanged === true)
				$scope.criteria.from = ($scope.dashlet.from != null) ? new Date($scope.dashlet.from) : null;
			else 
				$scope.criteria.from = new Date($scope.today.setMonth($scope.today.getMonth() - 3));

			$scope.criteria.to = null;
			$scope.criteria.dateGroup = $scope.dashlet.dateGroup;

			// options for doughnut chart
			$scope.doughnutOptions = {
				title: {
					display: true,
					text: $scope.ngModel.name,
					fontSize: 13,
					fontFamily: "sans-serif",
					fontStyle: "normal"
				},
				legend: {
					display: true,
					fontSize: 11,
					fontFamily: "sans-serif",
					fontStyle: "normal"
				}
			};

			// options for line chart
			$scope.lineOptions = {
				title: {
					display: true,
					text: $scope.ngModel.name,
					fontSize: 13,
					fontFamily: "sans-serif",
					fontStyle: "normal"
				},
				legend: {
					display: true,
					fontSize: 11,
					fontFamily: "sans-serif",
					fontStyle: "normal"
				},
				scales: {
					yAxes: [{
						stacked: false,
						ticks: {
							min: 0,
							fontColor: '#3f51b5',
						}
					}],
					xAxes: [{
						ticks: {
							display: true,
							fontColor: '#3f51b5',
							fontSize: 12,
							padding: 5,
							autoSkip: false,
							maxRotation: 45,
						},
						display: true,
						position: 'bottom'
					}],
				},
				tooltips: {
					enabled: true,
					backgroundColor: '#3f51b5',
					titleFontSize: 13,
					cornerRadius: 15,
					callbacks: {
						label: function (tooltipItems, data) {
							return tooltipItems.yLabel;
						},
						title: function (tooltipItem) {
							var splitted = tooltipItem[0].xLabel.split("-")
							var year = splitted[0];

							if ($scope.criteria.dateGroup === "week") {
								var week = splitted[1];

								var startDate = moment().year(year).day("Monday").week(week);

								return startDate.format('DD/MM/YYYY') + " - " + startDate.add(6, "days").format('DD/MM/YYYY');

							} else if ($scope.criteria.dateGroup === "month") {
								var month = splitted[1];

								var startDate = moment([year, month]).add(-1, "month");
								var endDate = moment(startDate).endOf('month');

								return startDate.format('DD/MM/YYYY') + " - " + endDate.format('DD/MM/YYYY');

							} else
								return tooltipItem[0].xLabel;
						}
					}
				},
				elements: { line: { tension: 0 } }
			};

			// chart templates
			var doughnutTmpl = '<canvas id="' + $scope.ngModel.id + '" class="chart chart-doughnut" chart-options="doughnutOptions" chart-data="data" chart-labels="labels"></canvas>';
			var lineTmpl = '<canvas id="' + $scope.ngModel.id + '" class="chart chart-line" chart-options="lineOptions" chart-data="data" chart-labels="labels"></canvas>';
			var barTmpl = '<canvas id="' + $scope.ngModel.id + '" class="chart chart-bar" chart-options="lineOptions" chart-data="data" chart-labels="labels"></canvas>';

			// add templates to a map in order to access them easier
			var chartTemplates = {};
			chartTemplates['doughnut'] = doughnutTmpl;
			chartTemplates['line'] = lineTmpl;
			chartTemplates['bar'] = barTmpl;

			// returns the labels/data for the charts
			$scope.getChartData = function () {
				$scope.showProgressBar = true;
				$scope.showNoData = false;

				$scope.labels = [];
				$scope.data = [];

				// prepare the url for the request
				var dataUrl = angular.copy($scope.ngModel.src);

				dataUrl = dataUrl.replace("{from}", $filter('date')($scope.criteria.from, "yyyy-MM-dd"));
				dataUrl = dataUrl.replace("{to}", $filter('date')($scope.criteria.to, "yyyy-MM-dd"));
				dataUrl = dataUrl.replace("{dateGroup}", $scope.criteria.dateGroup);

				//emit an event to controller
				$scope.$emit("updateSrc", { dashletId: $scope.ngModel.id, searchCriteria: $scope.criteria });

				$scope.$emit("disableLegend", { dashletId: $scope.ngModel.id });

				dashboardService.getData(dataUrl).then(
					function (response) {
						var tempData = [];

						for (var data in response.data) {
							$scope.labels.push(response.data[data].label);
							tempData.push(response.data[data].intValue);
						}

						$scope.labels = $scope.labels.sort(function (a, b) {
							return a.localeCompare(b)
						});

						$scope.data.push(tempData);

						if (response.data.length == 0)
							$scope.showNoData = true;
					}
					// error response
					, function (response) {
						console.log("Error getting the chart data");
						console.log(response);
					}

				).finally(function () {
					$scope.showProgressBar = false;
				});
			};

			$scope.getChartData();

			/**
			 * Watches for changes at chart type and re-renders the view in
			 * order to change the type of the chart
			 */
			$scope.$watch($scope.wfChartType, function () {
				compileChart();
			});

			$scope.$watch($scope.wfLegend, function () {
				$scope.doughnutOptions.legend.display = $scope.wfLegend();
				$scope.lineOptions.legend.display = $scope.wfLegend();
			}, true);

			$scope.$on('refreshData', function (event) {
				$scope.getChartData();
			});

			$scope.clearFormCriteria = function () {
				$scope.dashlet.isFromChanged = true;
				$scope.criteria.from = null;
				$scope.getChartData();
			};

			$scope.clearToCriteria = function () {
				$scope.criteria.to = null;
				$scope.getChartData();
			};

			$scope.fromChanged = function () {
				$scope.dashlet.isFromChanged = true;
			};

			/**
			 * Compliles the actual chart based on type
			 */
			function compileChart() {
				var chartElement = "";

				chartElement = angular.element(chartTemplates[$scope.wfChartType()]);

				// compile the chart template
				$compile(chartElement)($scope);

				var hostElement = $element[0].querySelector('.chartContainer');

				if (!hostElement)
					return;

				// add the chart element to the chart container
				angular.element(hostElement).empty();
				angular.element(hostElement).append(chartElement);
			}
		}

		angular.module('wfManagerDirectives').directive('wfSingleTimeSeriesChart', ['$compile', singleTimeSeriesChart]);

		// Directive's controller
		angular.module('wfManagerControllers').controller('wfSingleTimeSeriesChartCtrl', ['$scope', '$compile', '$filter', '$element', 'dashboardService', singleTimeSeriesChartCtrl]);
	}
);