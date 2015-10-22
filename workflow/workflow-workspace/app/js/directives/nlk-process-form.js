/**
 * @author nlyk
 * Created by nlyk on 6/10/2015.
 */
(function (angular) {

    angular.module('nlkDirectives').directive('nlkProcessForm',
        [
        function () {

            return {
                require: 'ngModel',
                restrict: 'E',
                transclude: true,
                scope: {
                    name: '@form-name',
                    ngModel: '='
                },
                template: '<ng-form name="{{name}}" ng-transclude></ng-form>'
            }
        }]);

})(angular);