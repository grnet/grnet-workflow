(function () {
    angular.module('nlkDirectives')
        .directive('nlkFileInput', ['$parse', function ($parse) {

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

        }]);
})();