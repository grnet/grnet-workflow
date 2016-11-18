angular.module('wfDirectives').directive('wfDocumentInput', ['$mdDialog',
	/**
	 * @name wfDocumentInput
	 * @ngDoc directives
	 * @memberof wfDirectives
	 * @desc Directive used to render the Document's element
	 */
	function ($mdDialog) {
		return {
			require: 'ngModel',
			restrict: 'E',
			scope: {
				wfDocumentLabel: '@wfDocumentLabel',
				wfDocumentPath: '@wfDocumentPath',
				ngRequired: '=',
				ngReadonly: '=',
				ngModel: '=',
				wfCallback: '='
			},
			templateUrl: 'directives/wfDocumentInput/wfDocumentInput.tmpl.html',
			link: function (scope, element, attributes, controller) {

				scope.ngModel = scope.ngModel || {};

				if (typeof scope.ngModel !== 'object')
					scope.ngModel = angular.fromJson(scope.ngModel);

				/**
				 * @memberof wfDocumentInput
				 * @desc Displays a modal panel in order to upload a document
				 * 
				 * @param {event} event
				 */
				scope.showAddFile = function (event) {
                    $mdDialog.show({
                        controller: 'addFileController',
                        templateUrl: 'directives/wfDocumentInput/addFile.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                            'wfDocument': scope.ngModel,
                            'wfCallback': scope.wfDocumentCallback,
                        }
                    });
                };

                /**
                 * @memberof wfDocumentInput
				 * 
                 * @param {Document} document
                 * @param {File} file
                 */
                scope.wfDocumentCallback = function (document, file) {

					if (file !== null) {

						if (scope.wfCallback) {
							scope.wfCallback(document, file).then(
								function successCallback(response) {
									scope.setDocument(response.data);
									$mdDialog.hide();

								}, function errorCallback(response) {

									if (response.data)
										console.log('Setting file Error! ' + response.data.errorMessage);
									else
										console.log('Setting file Error! ' + response.statusText);
								});
						} else {
							document.file = file;
							scope.setDocument(document);

							$mdDialog.hide();
						}
					} else {

						if (scope.wfCallback) {
							scope.wfCallback(scope.document).then(
								function successCallback(response) {
									scope.setDocument(response.data);
									$mdDialog.hide();

								}, function errorCallback(response) {

									if (response.data)
										console.log('Updating document Error! ' + response.data.errorMessage);
									else
										console.log('Updating document Error! ' + response.statusText);
								});
						} else {
							scope.setDocument(document);
							$mdDialog.hide();
						}
					}
                };

                /**
                 * @memberof wfDocumentInput
                 * 
                 * @param {any} data
                 */
                scope.setDocument = function (data) {
					controller.$setViewValue(data);
				};

                /**
                 * @memberof wfDocumentInput
                 * @desc Converts a given date to string
                 * @param {Number} time
				 * 
                 * @returns {String} - The given date as string
                 */
                scope.getDateString = function (time) {
					var date = new Date(time);
					return date.toLocaleString();
                };

                /**
                 * @memberof wfDocumentInput
				 * @desc Directive's validators
                 * 
                 * @param {any} modelValue
                 * @param {any} viewValue
                 * @returns {Boolean} - Whether the directive is valid or not
                 */
                controller.$validators.wfDocumentInput = function (modelValue, viewValue) {

					if (scope.ngRequired) {
						if (modelValue.file || modelValue.documentId) {
							return true;
						} else {
							return false;
						}
					} else {

						return true;
					}
                };
			}
		};
	}]);

angular.module('wfmanagerApp').controller('addFileController', ['$scope', '$mdDialog', 'wfDocument', 'wfCallback',
	/**
	 * @name addFileController
	 * @ngDoc controllers
	 * @memberof wfmanagerControllers
	 * @desc Document input directive controller
	 */
	function ($scope, $mdDialog, wfDocument, wfCallback) {

		$scope.file = null;
		$scope.document = {};

		$scope.document.file = wfDocument.file;

		$scope.document.title = wfDocument.title;
		$scope.document.refNo = wfDocument.refNo;

		$scope.document.documentId = wfDocument.documentId;
		$scope.document.version = wfDocument.version;
		$scope.document.author = wfDocument.author;
		$scope.document.authorId = wfDocument.authorId;
		$scope.document.submittedDate = wfDocument.submittedDate;

		/**
		 * @memberof addFileController
		 * @desc Sets file to variable after uploading 
		 * 
		 * @param {any} element
		 */
		$scope.setFile = function (element) {

			$scope.$apply(function ($scope) {
				$scope.file = element.files[0];
				$scope.document.title = $scope.file.name;
			});
		};

		/**
		 * @memberof addFileController
		 */
		$scope.save = function () {
			wfCallback($scope.document, $scope.file);
		};

		/**
		 * @memberof addFileController
		 */
		$scope.hide = function () {
			$mdDialog.hide();
		};

		/**
		 * @memberof addFileController
		 */
		$scope.cancel = function () {
			$scope.file = {};
			$mdDialog.cancel();
		};
	}
]);