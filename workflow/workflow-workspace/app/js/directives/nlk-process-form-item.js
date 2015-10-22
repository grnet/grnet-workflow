/**
 * @author nlyk
 */
(function (angular) {

    angular.module('nlkDirectives').directive('nlkProcessFormItem', ['$compile',
        function ($compile) {

            /**
             * Returns the appropriate template for the type of the form item
             * @param {string} type
             * @return {*}
             */
            function itemTemplate(type) {

                var inputTmpl;

                switch (type) {
                    case 'number':
                        inputTmpl =
                            '<md-input-container ng-if="ngModel.readable">' +
                            '<label for="ngModel.id">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="number" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required"' +
                            'ng-readonly="!ngModel.writable">' +
                            '</md-input-container>';
                        break;
                    case 'boolean':
                        inputTmpl =
                            '<md-input-container>' +
                            '<md-checkbox id="{{ngModel.id}}" ng-model="ngModel.value" ' +
                            'ng-readonly="!ngModel.writable">' +
                            '{{ngModel.name}}' +
                            '</md-checkbox>' +
                            '</md-input-container>';
                        break;
                    case 'string':
                    default:
                        inputTmpl =
                            '<md-input-container>' +
                            '<label for="ngModel.id">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="text" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required" ' +
                            'ng-readonly="!ngModel.writable">' +
                            '</md-input-container>';
                }

                return inputTmpl;
            }

            return {
                require: 'ngModel',
                restrict: 'E',
                scope: {
                    ngModel: '='
                },
                link: function (scope, iElement, iAttributes, ngModelCtrl) {

                    iElement.html(itemTemplate(scope.ngModel.type.name));
                    $compile(iElement.contents())(scope);
                }
            }
        }]);

})(angular);