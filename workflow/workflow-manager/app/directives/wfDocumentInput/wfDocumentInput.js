angular.module('wfDirectives')
	.directive('wfDocumentInput', ['$mdDialog', function($mdDialog) {
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
				
				if (typeof scope.ngModel !== 'object') {
					
					scope.ngModel = angular.fromJson(scope.ngModel);
				}
				
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
                
                scope.wfDocumentCallback = function (document, file) {
                    
                	console.log('Saving document! ' + document.title + ' - ' + document.refNo);
					
					if (file !== null) {
						
						if (scope.wfCallback) {
							scope.wfCallback(document, file).then(
									function successCallback(response) {
										
										console.log("Setting file Success!");
										scope.setDocument(response.data);
										
										$mdDialog.hide();
										
									}, function errorCallback(response) {
										
										if (response.data)
											console.log('Setting file Error! ' + response.data.errorMessage);
										else
											console.log('Setting file Error! ' + response.statusText);
									});
						} else {
							
							console.log("Setting file!");
							document.file = file;
							scope.setDocument(document);
														
							$mdDialog.hide();
						}
					} else {
						
						if (scope.wfCallback) {
							scope.wfCallback(scope.document).then(
									function successCallback(response) {
										
										console.log("Updating document Success! " + response.data);
										scope.setDocument(response.data);
										
										$mdDialog.hide();
										
									}, function errorCallback(response) {
										
										if (response.data)
											console.log('Updating document Error! ' + response.data.errorMessage);
										else
											console.log('Updating document Error! ' + response.statusText);
									});
						} else {
							
							console.log("Updating document!");
							scope.setDocument(document);
							
							$mdDialog.hide();
						}
					}
                };
                
                scope.setDocument = function(data) {
					
                	controller.$setViewValue(data);
				}
                
                scope.getDateString = function (time) {
                    
                	var date = new Date(time);
                	
                	return date.toLocaleString();
                };
                
                controller.$validators.wfDocumentInput = function(modelValue, viewValue) {
                	
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

angular.module('wfmanagerApp')
	.controller('addFileController', ['$scope', '$mdDialog', 'wfDocument', 'wfCallback',
		                                  
	
	    /**
	     * Submit a new file.
	     * @param $scope
	     * @param $mdDialog
	     * @param $location
	     * @param {ProcessService} processService   - Service for accessing the workflow-definition API
	     * @param {Document} wfDocument      		- the current document object
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
										
				$scope.setFile = function(element) {
					
			        $scope.$apply(function($scope) {
			        	
			        	$scope.file = element.files[0];
			        	$scope.document.title = $scope.file.name;
			        });
			        
			        console.log('Set file: ' + $scope.file.name);
			    };
		
				$scope.save = function () {
					
					wfCallback($scope.document, $scope.file);
				}
				
				$scope.hide = function () {
		            $mdDialog.hide();
		        };
		
		        $scope.cancel = function () {
		        	
		        	console.log('Cancel document upload!');
		        	$scope.file = {};
			        $mdDialog.cancel();
		        };
    		}
	]);