define(['angular'],

	function (angular) {

		'use strict';

		function processFormItem($compile, $mdToast, $window, $filter, config) {

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
							'<section layout="row" class="md-block"> ' +

								'<md-input-container ng-if="ngModel.readable" class="md-block" flex="50"> ' +
									'<label for="{{ngModel.id}}">{{ngModel.name}}</label> ' +
									'<input id="{{ngModel.id}}" type="number" ng-model="ngModel.value" ng-required="ngModel.required" ' + 
										'ng-readonly="!ngModel.writable || wfPreview()" ng-disabled="!ngModel.writable || wfPreview()"/> ' +
								'</md-input-container> ' +
								
								'<md-button class="md-icon-button formItem-info" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()"> ' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
								'</md-button> ' +

							'</section>';
						break;
					case 'boolean':
						inputTmpl =
							'<section layout="row" class="md-block"> ' +

								'<md-input-container class="md-block"> ' +
									'<md-checkbox id="{{ngModel.id}}" ng-model="ngModel.value" ng-readonly="!ngModel.writable || wfPreview()" ' +
										'ng-disabled="!ngModel.writable || wfPreview()"> {{ngModel.name}} ' +
									'</md-checkbox> ' +
								'</md-input-container> ' +
								
								'<md-button class="md-icon-button formItem-info" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()"> ' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
								'</md-button> ' +

							'</section>';
						break;
					case 'document':
						inputTmpl =
							'<section layout="row" class="md-block">' +

								'<wf-document-input id="{{ngModel.id}}" wf-variable-code="{{ngModel.id}}" wf-document-label="{{ngModel.name}}" ' +
									'ng-model="ngModel.value" flex="50" wf-document-path="{{wfDocumentPath}}" wf-callback="wfDocumentSaveCallback" ' +
									'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview()">' +
								'</wf-document-input> ' +
								
								'<md-button class="md-icon-button formItem-info" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()">' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
								'</md-button>' +

							'</section>';
					break;
					case 'documentList':
						inputTmpl = 
							'<section layout="row" class="md-block">' +
								'<md-list flex style="padding-top: 0; margin-bottom: 2em;">' +
									'<md-list-item ng-repeat="document in ngModel.value.list" class="md-2-line" ng-click="goToDocument(document.documentId)" style="border-bottom: 1px solid #d3d3d3">' +
									
										'<div class="md-list-item-text" layout="column"> ' +
											'<span> ' +
												'<h3 style="display: inline">{{document.title}}</h3> ' +
												'<md-button id="back-button" class="md-icon-button" aria-label="Back" ng-click="copyLink(document.documentId)" class="md-secondary"> ' +
													'<md-tooltip md-direction="right">{{ "copyLink" | translate }}</md-tooltip> ' +
													'<md-icon md-svg-icon="img/icons/link.svg"></md-icon> ' +
												'</md-button> ' +
											'</span> ' +
											'<p style="margin-bottom: 8px">{{ "version" | translate }}: {{document.version}} ' +
												'<span class="cardInline">{{ "submittedDate" | translate }} {{getDateString(document.submittedDate)}}</span> '+
											'</p> ' +
										'</div> '+
									'</md-list-item> ' +
								'</list> ' +
							'</section>';
					break;
					case 'position':
						inputTmpl =
							'<section layout="row" class="md-block"> ' +
							
								'<wf-position-input id="{{ngModel.id}}" wf-position-label="{{ngModel.name}}" ng-model="ngModel.value" ' +
									'wf-position-center-lat="{{wfPositionCenterLat}}" wf-position-center-lng="{{wfPositionCenterLng}}" wf-id-prefix="{{wfIdPrefix}}" ' +
									'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview()" flex="50"> ' +
								'</wf-position-input> ' +
								
								'<md-button class="md-icon-button" style="top:-12px;padding:0px;margin:0px;right: 900px;" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()"> ' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
								'</md-button> ' +

							'</section>';
						break;
					case 'conversation':
						inputTmpl =
							'<wf-conversation id="{{ngModel.id}}" wf-conversation-label="{{ngModel.name}}" ng-model="ngModel.value" ' +
								'ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview()">' +
							'</wf-conversation>';
						break;
					case 'approve':
						inputTmpl =
							'<section layout="row" class="md-block">' +

								'<input id="{{ngModel.id}}" type="hidden" ng-model="ngModel.value" ng-required="true"/>' +

								'<md-button class="md-icon-button" style="top: 16px; padding:0px; margin:0px" ng-if="ngModel.description" ' +
									'aria-label="Info" ng-click="showElementInfo()">' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary" ></md-icon> ' +
								'</md-button> ' +

							'</section>';
						break;
					case 'email':
						inputTmpl =
							'<section layout="row" class="md-block"> ' +

								'<md-input-container class="md-block" flex="50"> ' +
									'<label for="{{ngModel.id}}">{{ngModel.name}}</label> ' +
									'<input id="{{ngModel.id}}" type="email" ng-model="ngModel.value" ng-required="ngModel.required" ' +
										'ng-readonly="!ngModel.writable || wfPreview()" ng-disabled="!ngModel.writable || wfPreview()"/> ' +
								'</md-input-container> ' +
								
								'<md-button class="md-icon-button formItem-info" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()"> ' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
								'</md-button> ' +

							'</section>';
						break;
					case 'textarea':
						inputTmpl =
							'<section layout="row" class="md-block"> ' +
							
								'<md-input-container class="md-block" flex="50"> ' +
									'<label for="{{ngModel.id}}">{{ngModel.name}}</label> ' +
									'<textarea id="{{ngModel.id}}" ng-model="ngModel.value" ng-required="ngModel.required" ng-readonly="!ngModel.writable || wfPreview()" ' +
										'ng-disabled="!ngModel.writable || wfPreview()" md-maxlength="150" rows="2" md-autogrow> ' +
									'</textarea> ' +
								'</md-input-container> ' +
								
								'<md-button class="md-icon-button formItem-info" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()"> ' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
								'</md-button> ' +

							'</section>';
						break;
					case 'enum':
						inputTmpl =
							'<section layout="row" class="md-block"> ' +
							
								'<md-input-container class="md-block" flex="50"> ' +
									'<label for="{{ngModel.id}}" style="white-space: nowrap;">{{ngModel.name}}</label> ' +
									'<md-select id="{{ngModel.id}}" ng-model="ngModel.value" style="font-size:15px" ng-required="ngModel.required" ' +
										'ng-readonly="!ngModel.writable || wfPreview()" ng-disabled="!ngModel.writable || wfPreview()">	' +
										'<md-option ng-repeat="(key, value) in ngModel.formValues" value="{{key}}">{{value}}</md-option> ' +
									'</md-select> ' +
								'</md-input-container> ' +
								
								'<md-button class="md-icon-button formItem-info" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()"> ' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
								'</md-button> ' +

							'</section>';
						break;
						case 'url':
							inputTmpl =
								'<section layout="row" layout-align="start center" class="md-block"> ' +

									'<span>{{ ngModel.name | translate }}:</span> ' +
									
									'<md-button ng-href="{{ngModel.value}}" class="md-primary" target="_blank">{{ngModel.value}}</md-button>' +

									'<md-button class="md-icon-button formItem-info" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()"> ' +
										'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
									'</md-button> ' +

								'</section>';
						break;
					case 'date':
						inputTmpl =
							'<section layout="row" class="md-block">' +

								'<md-input-container style="padding-left:0px;" class="md-block">' +
									'<label class="wf-label">{{ ngModel.name }}</label> ' +
									'<input mdc-datetime-picker date="true" time="ngModel.time" type="text" id="{{ngModel.id}}" short-time="true" min-date="minDate" ' +
										'ng-disabled="!ngModel.writable || wfPreview()" ng-required="ngModel.required" ' +
										'placeholder="{{ngModel.name}}" format="{{ngModel.datePattern}}" ng-model="ngModel.value"/> ' +
								'</md-input-container> ' +
								
								'<md-button class="md-icon-button" style="top: 16px;padding:0px;margin:0px" ' + 
									'aria-label="Clear" ng-click="ngModel.value = null" ng-disabled="!ngModel.writable || wfPreview()">' +
								'<md-icon md-svg-icon="img/icons/clear.svg" class="md-primary"></md-icon> </md-button>' +
								
								'<md-button class="md-icon-button formItem-info" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()">' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
								'</md-button>' +
									
							'</section>';
						break;
					case 'string':
					default:
						inputTmpl =
							'<section layout="row" class="md-block"> ' +
							
								'<md-input-container class="md-block" flex="50"> ' +
									'<label for="{{ngModel.id}}">{{ngModel.name}}</label> ' +
									'<input id="{{ngModel.id}}" type="text" ng-model="ngModel.value" ng-required="ngModel.required" ' +
										'ng-readonly="!ngModel.writable || wfPreview()" ng-disabled="!ngModel.writable || wfPreview()"/> ' +
								'</md-input-container> ' +
								
								'<md-button class="md-icon-button formItem-info" ng-if="ngModel.description" aria-label="Info" ng-click="showElementInfo()"> ' +
									'<md-icon md-svg-icon="img/icons/elementInfo.svg" class="md-primary"></md-icon> ' +
								'</md-button> ' +

							'</section>';
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
					wfIdPrefix: '@wfIdPrefix',
					ngModel: '=',
					wfDocumentSaveCallback: '='
				},
				link: function (scope, iElement, iAttributes, ngModelCtrl) {
					
					scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

					scope.showElementInfo = function () {
						$mdToast.show(
							$mdToast.simple()
								.textContent(scope.ngModel.description)
								.hideDelay(4000)
								.position("right")
								.parent(iElement[0])
						);
					};

					if (scope.ngModel.type == "string" || scope.ngModel.type == "textarea") {
						if (scope.ngModel.value == "null" || scope.ngModel.value == null) {
							scope.ngModel.value = "";
						}
					}
					
					if (scope.ngModel.type == "documentList") {
						scope.ngModel.value = angular.fromJson(scope.ngModel.value);
					}
					
					scope.goToDocument = function (documentId) {
						var path = scope.documentPath + documentId;
						$window.open(path, "_blank");
					};
					
					scope.copyLink = function (documentId) {
						var path = scope.documentPath + documentId;

						var body = angular.element($window.document.body);
						var textarea = angular.element('<textarea/>');
						textarea.val(path);
						body.append(textarea);
						textarea[0].select();

						try {
							var successful = document.execCommand('copy');

							if (!successful) throw successful
							if (successful) scope.showToast(($filter('translate')('copiedToClipboard')));

						} catch (err) {
							console.log(err);
							scope.showToast("Failed to copy link: " + path);
						}

						textarea.remove();
					}
					
					scope.showToast = function (message) {
						$mdToast.show(
							$mdToast.simple()
								.textContent(message)
								.position("top")
								.hideDelay(3500)
						);
					};

					scope.getDateString = function (time) {
						var date = new Date(time);
						return date.toLocaleString();
					};

					switch (scope.ngModel.type) {
						case "boolean":
							scope.ngModel.value = scope.ngModel.value === "true";
							break;

						case "date":
							// check whether the date is datetime or date

							if(scope.ngModel != null) {
								if(scope.ngModel.value != null)
									scope.ngModel.value = scope.ngModel.value.replace("EEST ", "");
							}

							if (scope.ngModel.format != null && scope.ngModel.format.indexOf('T') >= 1) {
								scope.ngModel["time"] = true;
								scope.ngModel["datePattern"] = "DD/MM/YYYY HH:mm";
								scope.ngModel.value ? scope.ngModel.value + moment().format('Z') : moment();

							} else if (scope.ngModel.format != null && scope.ngModel.format == 'MM/dd/yyyy HH:mm') {
								scope.ngModel["time"] = true;
								scope.ngModel["datePattern"] = "DD/MM/YYYY HH:mm";
								scope.ngModel.value ? scope.ngModel.value + moment().format('Z') : moment();

							} else {
								scope.ngModel["time"] = false;
								scope.ngModel["datePattern"] = "DD/MM/YYYY";
								scope.ngModel.value ? scope.ngModel.value + "T00:00:00.000" + moment().format('Z') : moment();
							}

							scope.ngModel.value = scope.ngModel.value ? moment(scope.ngModel.value) : moment();

							break;
					}

					iElement.html(itemTemplate(scope.ngModel.type));
					$compile(iElement.contents())(scope);
				}
			}
		}

		angular.module('wfWorkspaceDirectives').directive('nlkProcessFormItem', ['$compile', '$mdToast', '$window', '$filter', 'CONFIG', processFormItem]);
	}
);