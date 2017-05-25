define(['angular'],

	function (angular) {

		'use strict';

		function positionPrint($mdDialog, config) {
			return {
				require: 'ngModel',
				restrict: 'E',
				scope: {
					wfPositionLabel: '@wfPositionLabel',
					wfPositionCenterLat: '@wfPositionCenterLat',
					wfPositionCenterLng: '@wfPositionCenterLng',
					ngRequired: '=',
					ngReadonly: '=',
					ngModel: '='
				},
				templateUrl: 'templates/wfPositionPrint.tmpl.html',
				link: function (scope, element, attributes, controller) {

					scope.ngModel = scope.ngModel || {};

					scope.map = { src: null };

					scope.map.src = "https://maps.googleapis.com/maps/api/staticmap?center="
						+ JSON.parse(scope.ngModel).latitude + ","
						+ JSON.parse(scope.ngModel).longitude +
						"&size=550x450&zoom=17&markers=color:red|" +
						+JSON.parse(scope.ngModel).latitude + ","
						+ JSON.parse(scope.ngModel).longitude +
						"&key=AIzaSyCUqs0276PPZwQIQhLfraUTv3NuAnjXu0k";

				}
			};
		}

		angular.module('wfWorkspaceDirectives').directive('wfPositionPrint', ['$mdDialog', 'CONFIG', positionPrint]);

	}
);
