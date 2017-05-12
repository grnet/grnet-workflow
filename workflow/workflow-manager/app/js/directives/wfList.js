define(['angular'],

	function (angular) {

		'use strict';

		function listDashlet($compile) {
			return {
				require: 'ngModel', // the ng-model is required
				restrict: 'E', // only matches element name
				controller: 'wfListCtrl', // directive's controller
				transclude: true, // directive wraps other elements
				scope: { // is an Angular scope object. It is somehow a link between the view and the controller part 
					ngModel: '=' // its a shorthand for ng-model
				},
				templateUrl: 'templates/wfList.tmpl.html',
				link: function (scope, element, attributes, ngModel) {

					scope.ngModel = scope.ngModel || {};

					if (typeof scope.ngModel !== 'object')
						scope.ngModel = angular.fromJson(scope.ngModel);
				}
			};
		}

		function listDashletCtrl($scope, $window, dashboardService, config) {

			$scope.workspaceUrl = config.WORKFLOW_WORKSPACE_URL;

			$scope.showProgressBar = true;
			$scope.showNoData = false;

			$scope.isOverdueList = false;

			if ($scope.ngModel.src.indexOf('overdue') > -1)
				$scope.isOverdueList = true;

			$scope.$emit("disableLegend", { dashletId: $scope.ngModel.id });

			$scope.getData = function () {
				$scope.showProgressBar = true;
				$scope.showNoData = false;

				// returns the labels/data for the charts
				dashboardService.getData($scope.ngModel.src).then(
					function (response) {
						$scope.data = response.data;

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

			$scope.getData();

			$scope.$on('refreshData', function (event) {
				$scope.getData();
			});

			$scope.goTo = function (path) {
				$window.open(path, '_blank');
			};
		}

		angular.module('wfManagerDirectives').directive('wfList', ['$compile', listDashlet]);

		// Directive's controller
		angular.module('wfManagerControllers').controller('wfListCtrl', ['$scope', '$window', 'dashboardService', 'CONFIG', listDashletCtrl]);
	}
);