define(['angular'],

    function (angular) {

        'use strict';
        
        function fileInput($parse) {

            return {
                restrict: 'AE',

                link: function (scope, element, attributes) {

                    var model = $parse(attributes.nlkFileInput);
                    var modelSetter = model.assign;

                    element.bind('change', function () {

                        scope.$apply(function () {
                            modelSetter(scope, element[0].files[0]);
                        });
                    });
                }

            };

        }

        angular.module('wfManagerDirectives').directive('nlkFileInput', ['$parse', fileInput]);
    }
);