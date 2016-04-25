(function (angular) {
	'use strict';
	
	angular.module('wfworkspaceControllers').controller('PrintStartFormCtrl', 
			['$scope', '$routeParams', '$mdDialog', 'processService', 'CONFIG',
			 
			 function ($scope, $routeParams, $mdDialog, processService, config) {

                var instanceId = $routeParams['instanceId'];
                $scope.taskForm = {};
                $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;
                
                /**
                 * Get the start from
                 */
                processService.getStartEventForm(instanceId).then(
                		//success callback
                		function (response) {
                			$scope.taskForm = response.data;
                			
                		//error callback	
                		},function (response) {
                			exceptionModal(response);
                		});
                
                /**
                 * Exception modal
                 */
                function exceptionModal(response,event){
                	$mdDialog.show({
                		controller: function ($scope, $mdDialog) {
                			$scope.error = response.data;
                			
                            $scope.cancel = function () {
                            	 $mdDialog.hide();
                            };
                        },
                        
                        templateUrl: 'templates/exception.tmpl.html',
                        parent: angular.element(document.body),
                        targetEvent: event,
                        clickOutsideToClose: false
                	})
                }
        }]);

})(angular);