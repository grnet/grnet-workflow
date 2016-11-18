/**
 * @memberOf workflow-workspace
 */
angular.module('wfDirectives').directive('wfPositionPrint', ['$mdDialog', 'CONFIG',
	/**
	 * @name wfPositionPrint
	 * @ngDoc directives
	 * @memberof wfDirectives
	 * @desc Directive used to render the Position form item (printing only)
	 */
	function ($mdDialog, config) {
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
			templateUrl: 'directives/wfPositionPrint/wfPositionPrint.tmpl.html',
			link: function (scope, element, attributes, controller) {

				scope.ngModel = scope.ngModel || {};

				scope.map = { src: null };

				scope.map.src = "https://maps.googleapis.com/maps/api/staticmap?center="
					+ JSON.parse(scope.ngModel).latitude + ","
					+ JSON.parse(scope.ngModel).longitude +
					"&size=550x450&zoom=17&markers=color:red|" +
					+JSON.parse(scope.ngModel).latitude + ","
					+ JSON.parse(scope.ngModel).longitude +
					"|key=AIzaSyCUqs0276PPZwQIQhLfraUTv3NuAnjXu0k";

			}
		};
	}]);