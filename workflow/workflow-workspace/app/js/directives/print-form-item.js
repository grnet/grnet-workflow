/**
 * @author nlyk
 */
(function (angular) {

    angular.module('nlkDirectives').directive('nlkPrintFormItem', ['$compile',
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
                            '<md-input-container ng-if="ngModel.readable" class="md-block" style="padding: 0px;margin: 5px 5px;">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="number" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required"' +
                            'ng-readonly="!ngModel.writable || wfPreview()">' +
                            '</input>' +
                            '</md-input-container>';
                        break;
                    case 'boolean':
                        inputTmpl =
                            '<md-input-container class="md-block" style="padding: 0px;margin: 5px 5px;">' +
                            '<label style="padding-left: 40px;">{{ngModel.name}}</label> ' +
                            '<div ng-if="ngModel.value"><img ng-src="img/icons/check.svg"/></div> ' +
                            '<div ng-if="!ngModel.value"><img ng-src="img/icons/uncheck.svg"/></div> ' +
                            '</md-input-container>';
                        break;
                    case 'document':
                    	inputTmpl = 
                    		'<wf-document-input id="{{ngModel.id}}" wf-variable-code="{{ngModel.id}}" style="padding: 0px;margin: 5px 5px;"' +
                    			'wf-document-label="{{ngModel.name}}" ng-model="ngModel.value" ' +
                    			'wf-document-path="{{wfDocumentPath}}" wf-callback="wfDocumentSaveCallback" ' + 
                    			'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview()">' +
                    		'</wf-document-input>';
                    	break;
                    case 'position':
                    	inputTmpl = 
                    		'<wf-position-print id="{{ngModel.id}}" style="padding: 0px;margin: 5px 5px;"' +
                				'wf-position-label="{{ngModel.name}}" ng-model="ngModel.value" ' +
                				'wf-position-center-lat="{{wfPositionCenterLat}}" ' +
                				'wf-position-center-lng="{{wfPositionCenterLng}}" ' +
                				'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview()">' +
                			'</wf-position-print>';
                    	break;
                    case 'conversation':
                    	inputTmpl = 
                    		'<wf-conversation id="{{ngModel.id}}" ' +
                				'wf-conversation-label="{{ngModel.name}}" ng-model="ngModel.value" ' +
                				'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview()">' +
                			'</wf-conversation>';
                    	break;
                    case 'approve':
                    	inputTmpl = 
                    		'<input id="{{ngModel.id}}" type="hidden" ng-model="ngModel.value" ' + 
                    		'ng-required="true"></input>';
                    	break;
                    case 'email':
                    	inputTmpl = 
                    		'<md-input-container class="md-block {{ngModel.required ? \'wf-required\' : \'\'}}" style="padding: 0px;margin: 5px 5px;">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="email" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required" ' +
                            'ng-readonly="!ngModel.writable || wfPreview()">' +
                            '</input>' +
                            '</md-input-container>';
                    	break;
                    case 'textarea':
                    	inputTmpl = 
                    		'<md-input-container class="md-block {{ngModel.required ? \'wf-required\' : \'\'}}" style="padding: 0px;margin: 5px 5px;">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<textarea id="{{ngModel.id}}" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required" ' +
                            'ng-readonly="!ngModel.writable || wfPreview()">' +
                            '</textarea>' +
                            '</md-input-container>';
                    	break;
                    case 'enum':
                    	inputTmpl = 
                    		'<md-input-container class="md-block {{ngModel.required ? \'wf-required\' : \'\'}}" style="padding: 0px;margin: 5px 5px;">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<md-select id="{{ngModel.id}}" ' +
                            'ng-model="ngModel.value" ' +
                            'style="font-size:15px" ' +
                            'ng-required="ngModel.required" ' +
                            'ng-readonly="!ngModel.writable || wfPreview()"' +
                            'ng-disabled="!ngModel.writable || wfPreview()">' +
                            '<md-option ng-repeat="(key, value) in ngModel.formValues" value="{{key}}">{{value}}</md-option>' +
                            '</md-select>' +
                            '</md-input-container>';
                    	break;
//                    case 'date':
//                    	inputTmpl = 
//            				'<section layout="row" class="md-block">' +
//            				'<label for="{{ngModel.id}}" class="wf-label">{{ ngModel.name }}</label> ' +
//            				'<md-datepicker id="{{ngModel.id}}" ng-model="ngModel.value" md-placeholder="{{ngModel.name}}" style="margin-right: 0px;" ' +
//            				'ng-disabled="!ngModel.writable || wfPreview"></md-datepicker>' +
//            				'<md-button class="md-icon-button" aria-label="Clear" ng-click="ngModel.value = null" ng-disabled="!ngModel.writable || wfPreview">' +
//            				'<md-icon md-svg-icon="img/icons/clear.svg" style="margin-left: 8px;color: red;"></md-icon>' +
//        					'</md-button>' +
//        					'</section>';
//                    	break;
                    	
                    case 'date':
                    	inputTmpl = 
                    		'<section layout="row" class="md-block" style="padding: 0px;margin: 5px 5px;">' +
            				'<md-input-container style="padding-left:0px;">' +
            				'<label class="wf-label">{{ ngModel.name }}</label> ' +
            				'<input mdc-datetime-picker date="true" time="ngModel.time" type="text" id="{{ngModel.id}}" short-time="true" min-date="minDate" ' +
            				'ng-disabled="!ngModel.writable || wfPreview()" ng-required="ngModel.required" ' +
            				'placeholder="{{ngModel.name}}" format="{{ngModel.datePattern}}" ng-model="ngModel.value"></input> ' +
            				'</md-input-container> ' +
            				'<md-button class="md-icon-button" aria-label="Clear" ng-click="ngModel.value = null" ng-disabled="!ngModel.writable || wfPreview()">' +
            				'<md-icon md-svg-icon="img/icons/clear.svg" style="margin-left: 8px;color: red;"></md-icon>' +
            				'</md-button>' +
            				'</section>';
                    	break;
                    	
                    case 'string':
                    default:
                        inputTmpl =
                            '<md-input-container class="md-block {{ngModel.required ? \'wf-required\' : \'\'}}" style="padding: 0px;margin: 5px 5px;">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="text" ' +
                            'ng-model="ngModel.value" ' +
                            'ng-required="ngModel.required" ' +
                            'ng-readonly="!ngModel.writable || wfPreview()">' +
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
                	wfPreview: '&wfPreview',
                    ngModel: '=',
                    wfDocumentSaveCallback: '='
                },
                link: function (scope, iElement, iAttributes, ngModelCtrl) {

                	switch (scope.ngModel.type) {
                	case "boolean":
                		scope.ngModel.value = scope.ngModel.value === "true";
                		break;
                	case "date":
                		if( ! scope.ngModel.value ) {
                			scope.ngModel.value = null;
                		}
                		else {
                    		scope.ngModel.value = new Date(scope.ngModel.value);
                		}
                		// check whether the date is datetime or date
                		if(scope.ngModel.format != null && scope.ngModel.format.indexOf('T') >= 1 ){
                			scope.ngModel["time"] = true;
                			scope.ngModel["datePattern"] = "DD/MM/YYYY HH:mm";
                		}else {
                			scope.ngModel["time"] = false;
                			scope.ngModel["datePattern"] = "DD/MM/YYYY";
                		}
                		                		
                		break;
                	}
                	
                    iElement.html(itemTemplate(scope.ngModel.type));
                    $compile(iElement.contents())(scope);
                }
            }
        }]);

})(angular);