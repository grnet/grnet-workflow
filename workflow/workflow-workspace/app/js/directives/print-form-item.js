(function (angular) {

    angular.module('wfDirectives').directive('nlkPrintFormItem', ['$compile', '$filter',
		/**
		 * @name nlkPrintFormItem
		 * @ngDoc directives
		 * @memberof wfDirectives
		 * 
		 * @desc Directive used to render the user's form for printing
		 */
        function ($compile, $filter) {

            /**
             * @memberOf nlkPrintFormItem
			 * @desc Takes the type of the form's element and sets to a string variable the desired html template in order to render it
             * 
             * @param {String} type
             * @returns {String} - The html element
             */
            function itemTemplate(type) {

                var inputTmpl;

                switch (type) {
                    case 'number':
                        inputTmpl =
                            '<md-input-container class="md-block" style="padding-left: 0; margin-top: 0; margin-bottom: 5px;">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="number" ng-model="ngModel.value" ng-readonly="!ngModel.writable || wfPreview()" disabled="true"></input>' +
                            '</md-input-container>';
                        break;
                    case 'boolean':
                        inputTmpl =
                            '<md-input-container class="md-block" style="padding-left: 0; margin-top: 0; margin-bottom: 0;">' +
                            '<label style="padding-left: 40px;">{{ngModel.name}}</label> ' +
                            '<div ng-if="ngModel.value"><img ng-src="img/icons/check.svg"/></div> ' +
                            '<div ng-if="!ngModel.value"><img ng-src="img/icons/uncheck.svg"/></div> ' +
                            '</md-input-container>';
                        break;
                    case 'document':
						inputTmpl =
							'<wf-document-input id="{{ngModel.id}}" wf-variable-code="{{ngModel.id}}" style="padding: 0px;margin: 0px"' +
							'wf-document-label="{{ngModel.name}}" ng-model="ngModel.value" ' +
							'wf-document-path="{{wfDocumentPath}}" wf-callback="wfDocumentSaveCallback" ' +
							'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview()">' +
							'</wf-document-input>';
						break;
                    case 'position':
						inputTmpl =
							'<wf-position-print id="{{ngModel.id}}" style="padding-left: 0; margin-top: 0; margin-bottom: 5px;"' +
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
							'ng-readonly="!ngModel.writable || wfPreview()">' +
							'</wf-conversation>';
						break;
                    case 'approve':
						inputTmpl =
							'<input id="{{ngModel.id}}" type="hidden" ng-model="ngModel.value"></input>';
						break;
                    case 'email':
						inputTmpl =
							'<md-input-container class="md-block" style="padding-left: 0; margin-top: 0; margin-bottom: 5px;">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="email" ng-model="ngModel.value" ng-readonly="!ngModel.writable || wfPreview()" disabled="true"></input>' +
                            '</md-input-container>';
						break;
                    case 'textarea':
						inputTmpl =
							'<md-input-container class="md-block" style="margin-top: 0;">' +
                            '<label for="{{ngModel.id}}" style="font-size: 14px;">{{ngModel.name}}</label>' +
                            '<div id="{{ngModel.id}}" style="overflow:auto; margin-top: 30px; margin-bottom: 15px; border-bottom: 1px dotted #e1e1e1;padding-bottom: 1px;    border-width: 0px 0px 2px 0px;"> ' +
                            '{{ngModel.value}} </div>' +
                            '</md-input-container>';
						break;
                    case 'enum':
						inputTmpl =
							'<md-input-container class="md-block" style="padding-left: 0; margin-top: 0; margin-bottom: 5px;">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="email" ng-model="ngModel.value" ng-readonly="!ngModel.writable || wfPreview()" disabled="true"></input>' +
                            '</md-input-container>';
						break;
                    case 'date':
						inputTmpl =
							'<section layout="row" class="md-block" sstyle="padding-left: 0; margin-top: 0; margin-bottom: 5px;">' +
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
                            '<md-input-container class="md-block" style="padding-left: 0; margin-top: 0; margin-bottom: 5px;">' +
                            '<label for="{{ngModel.id}}">{{ngModel.name}}</label>' +
                            '<input id="{{ngModel.id}}" type="text" ng-model="ngModel.value" ng-readonly="!ngModel.writable || wfPreview()" disabled="true"></input>' +
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

					if (scope.ngModel.type == "string" || scope.ngModel.type == "textarea") {
						if (scope.ngModel.value == "null" || scope.ngModel.value == null) {
							scope.ngModel.value = "";
						}
					}

					switch (scope.ngModel.type) {
						case "boolean":
							scope.ngModel.value = scope.ngModel.value === "true";
							break;
						case "date":
							if (!scope.ngModel.value) {
								scope.ngModel.value = null;
							}
							else {
								scope.ngModel.value = new Date(scope.ngModel.value);
							}
							// check whether the date is datetime or date
							if (scope.ngModel.format != null && scope.ngModel.format.indexOf('T') >= 1) {
								scope.ngModel["time"] = true;
								scope.ngModel["datePattern"] = "DD/MM/YYYY HH:mm";
							} else {
								scope.ngModel["time"] = false;
								scope.ngModel["datePattern"] = "DD/MM/YYYY";
							}
							break;
						case "enum":
							scope.ngModel.value = $filter('translate')(scope.ngModel.value);

							break;
					}

                    iElement.html(itemTemplate(scope.ngModel.type));
                    $compile(iElement.contents())(scope);
                }
            }
        }]);

})(angular);