define(['angular'],

    function (angular) {

        'use strict';
        
        function processForm() {

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
        }

        angular.module('wfManagerDirectives').directive('nlkProcessForm', [processForm]);
    }
);