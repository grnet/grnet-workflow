define(['angular'],

	function (angular) {

		'use strict';

		function multiSeriesChart($compile) {
			return {
				require: 'ngModel', // the ng-model is required
				restrict: 'E', // only matches element name
				controller: 'wfMultiSeriesChartCtrl', // directive's controller
				transclude: true, // directive wraps other elements
				scope: { // is an Angular scope object. It is somehow a link between the view and the controller part 
					ngModel: '=', // its a shorthand for ng-model
					wfChartType: '&wfChartType',
					wfLegend: '&wfLegend'
				},
				templateUrl: 'templates/wfMultiSeriesChart.tmpl.html',
				link: function (scope, element, attributes, ngModel) {

					scope.ngModel = scope.ngModel || {};

					if (typeof scope.ngModel !== 'object') {
						scope.ngModel = angular.fromJson(scope.ngModel);
					}

					var chartContainer = '<div ng-show="!showNoData && !showProgressBar" style="width: 100%; height: 100%" class="chartContainer"></div>';
					angular.element(element[0].querySelector(".chartContainer")).append(chartContainer);
				}
			};
		}

		function multiSeriesChartCtrl($scope, $compile, dashboardService) {

			// options for line chart
			$scope.lineOptions = {
				title: {
					display: true,
					text: $scope.ngModel.name,
					fontSize: 13,
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
							maxRotation: 45
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
							return data.datasets[tooltipItems.datasetIndex].label + ': ' + tooltipItems.yLabel;
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
				legend: {
					display: true,
					fontSize: 11,
					fontFamily: "sans-serif",
					fontStyle: "normal"
				}
			};

			// chart templates
			var lineTmpl = '<canvas id="' + $scope.ngModel.id + '" class="chart chart-line" chart-options="lineOptions" chart-data="data" chart-labels="labels"></canvas>';

			$scope.getChartData = function () {
				$scope.showProgressBar = true;

				$scope.showNoData = false;

				// variables that holds the labels/data for the charts
				$scope.labels = [];
				$scope.data = [];
				$scope.series = [];

				// returns the labels/data for the charts
				dashboardService.getData($scope.ngModel.src).then(
					function (response) {
						for (var data in response.data) {

							$scope.labels.push(response.data[data].label);
							$scope.data.push(response.data[data].intValue);
						}

						if ($scope.data.length == 0)
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

		angular.module('wfManagerDirectives').directive('wfMultiSeriesChart', ['$compile', multiSeriesChart]);

		// Directive's controller
		angular.module('wfManagerControllers').controller('wfMultiSeriesChartCtrl', ['$scope', '$compile', 'dashboardService', multiSeriesChartCtrl]);
	}
);