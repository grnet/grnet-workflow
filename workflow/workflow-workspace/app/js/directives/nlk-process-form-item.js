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
                            '<md-input-container ng-if="ngModel.readable" class="md-block">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="number" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required"' +
                            'ng-readonly="!ngModel.writable || wfPreview">' +
                            '</input>' +
                            '</md-input-container>';
                        break;
                    case 'boolean':
                        inputTmpl =
                            '<md-input-container class="md-block">' +
                            '<md-checkbox id="{{ngModel.id}}" ng-model="ngModel.value" ' +
                            'ng-readonly="!ngModel.writable || wfPreview" ' +
                            'ng-disabled="!ngModel.writable || wfPreview"> ' +
                            '{{ngModel.name}}' +
                            '</md-checkbox>' +
                            '</md-input-container>';
                        break;
                    case 'document':
                    	inputTmpl = 
                    		'<wf-document-input id="{{ngModel.id}}" wf-variable-code="{{ngModel.id}}" ' +
                    			'wf-document-label="{{ngModel.name}}" ng-model="ngModel.value" ' +
                    			'wf-document-path="{{wfDocumentPath}}" wf-callback="wfDocumentSaveCallback" ' + 
                    			'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview">' +
                    		'</wf-document-input>';
                    	break;
                    case 'position':
                    	inputTmpl = 
                    		'<wf-position-input id="{{ngModel.id}}" ' +
                				'wf-position-label="{{ngModel.name}}" ng-model="ngModel.value" ' +
                				'wf-position-center-lat="{{wfPositionCenterLat}}" ' +
                				'wf-position-center-lng="{{wfPositionCenterLng}}" ' +
                				'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview">' +
                			'</wf-position-input>';
                    	break;
                    case 'conversation':
                    	inputTmpl = 
                    		'<wf-conversation id="{{ngModel.id}}" ' +
                				'wf-conversation-label="{{ngModel.name}}" ng-model="ngModel.value" ' +
                				'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview">' +
                			'</wf-conversation>';
                    	break;
                    case 'approve':
                    	inputTmpl = 
                    		'<input id="{{ngModel.id}}" type="hidden" ng-model="ngModel.value" ' + 
                    		'ng-required="true"></input>';
                    	break;
                    case 'email':
                    	inputTmpl = 
                    		'<md-input-container class="md-block {{ngModel.required ? \'wf-required\' : \'\'}}">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="email" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required" ' +
                            'ng-readonly="!ngModel.writable || wfPreview">' +
                            '</input>' +
                            '</md-input-container>';
                    	break;
                    case 'textarea':
                    	inputTmpl = 
                    		'<md-input-container class="md-block {{ngModel.required ? \'wf-required\' : \'\'}}">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<textarea id="{{ngModel.id}}" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required" ' +
                            'ng-readonly="!ngModel.writable || wfPreview">' +
                            '</textarea>' +
                            '</md-input-container>';
                    	break;
                    case 'enum':
                    	inputTmpl = 
                    		'<md-input-container class="md-block {{ngModel.required ? \'wf-required\' : \'\'}}">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<md-select id="{{ngModel.id}}" ' +
                            'ng-model="ngModel.value" ' +
                            'style="font-size:15px" ' +
                            'ng-required="ngModel.required" ' +
                            'ng-readonly="!ngModel.writable || wfPreview"' +
                            'ng-disabled="!ngModel.writable || wfPreview">' +
                            '<md-option ng-repeat="(key, value) in ngModel.formValues" value="{{key}}">{{value}}</md-option>' +
                            '</md-select>' +
                            '</md-input-container>';
                    	break;
                    case 'string':
                    default:
                        inputTmpl =
                            '<md-input-container class="md-block {{ngModel.required ? \'wf-required\' : \'\'}}">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="text" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required" ' +
                            'ng-readonly="!ngModel.writable || wfPreview">' +
                            '</input>' +
                            '</md-input-container>';
                }

                return inputTmpl;
            }

            return {
                require: 'ngModel',
                restrict: 'E',
                scope: {
                	wfDocumentPath: '@wfDocumentPath',
                	wfPositionCenterLat: '@wfPositionCenterLat',
                	wfPositionCenterLng: '@wfPositionCenterLng',
                	wfPreview: '@wfPreview',
                    ngModel: '=',
                    wfDocumentSaveCallback: '='
                },
                link: function (scope, iElement, iAttributes, ngModelCtrl) {

                	switch (scope.ngModel.type) {
                	case "boolean":
                		scope.ngModel.value = scope.ngModel.value === "true";
                		break;
                	}
                    iElement.html(itemTemplate(scope.ngModel.type));
                    $compile(iElement.contents())(scope);
                }
            }
        }]);

})(angular);