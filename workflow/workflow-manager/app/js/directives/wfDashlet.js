define(['angular'],

	function (angular) {

		'use strict';

		function dashlet($compile) {
			return {
				require: 'ngModel',
				restrict: 'E',
				transclude: true,
				controller: 'wfDashletCtrl',
				scope: {
					ngModel: '=',
					wfBaseType: '&wfBaseType'
				},
				templateUrl: 'templates/wfDashlet.tmpl.html',
				link: function (scope, iElement, iAttributes, ngModelCtrl) {

				}
			}
		}

		function dashletCtrl($scope, $mdDialog) {

			// edit options for dashlets
			$scope.editOptions = [{ name: "resetDefauls", action: "resetDefauls" }, { name: "delete", action: "delete" }];

			// the dashlet
			$scope.dashlet = $scope.ngModel;

			$scope.hideLegend = false;

			/**
			 * Edit a dashlet
			 */
			$scope.editDashlet = function (event) {
				$mdDialog.show({
					controller: editDashletController,
					templateUrl: 'templates/edit-dashlet.tmpl.html',
					parent: document.body,
					clickOutsideToClose: true,
					locals: {
						'dashlet': $scope.dashlet
					}
				});
			};

			/**
			 * Edit dashlet's modal controller
			 */
			function editDashletController($scope, $mdDialog, dashlet) {
				$scope.userProperties = { label: "" };

				$scope.userProperties.label = dashlet.label;

				$scope.cancel = function () {
					$scope.userProperties.label = "";
					$mdDialog.cancel();
				};

				$scope.updateDashlet = function () {
					dashlet.label = $scope.userProperties.label;
					$mdDialog.cancel();
				};
			};

			/**
			 * Changes the chart type
			 * 
			 * @param dashletId The dashlet's id to be changed
			 * @param type The new type for the dashlet
			 */
			$scope.changeChartType = function (type) {
				$scope.dashlet.type = type;
			};

			/**
			 * Emits an event to dashboard controller in order to remove the
			 * selected dashlet from the dashlets list
			 */
			$scope.deleteDashlet = function () {
				$scope.$emit("deleteDashlet", $scope.dashlet.id);
			};

			$scope.refreshData = function () {
				$scope.$broadcast("refreshData");
			};

			$scope.$on('disableLegend', function (event, dashletId) {
				$scope.hideLegend = true;
			});
		}

		angular.module('wfManagerDirectives').directive('wfDashlet', ['$compile', dashlet]);

		//Directive's controller
		angular.module('wfManagerControllers').controller('wfDashletCtrl', ['$scope', '$mdDialog', dashletCtrl]);
	}
);