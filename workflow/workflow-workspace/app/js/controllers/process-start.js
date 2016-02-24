/**
 * @author nlyk
 * Created by nlyk on 6/10/2015.
 */
(function (angular) {

    angular.module('wfworkspaceControllers').controller('ProcessStartCtrl',
        ['$scope', '$routeParams', '$mdDialog', '$location', '$filter', 'processService', 'CONFIG',

            /**
             *
             * @param $scope
             * @param $routeParams
             * @param $mdDialog
             * @param $location
             * @param {ProcessService} processService
             */
                function ($scope, $routeParams, $mdDialog, $location, $filter, processService, config) {

                var processId = $routeParams['processId'];
                $scope.supervisors = null;
                $scope.showProgress = false;
                
                $scope.process = null;
                $scope.instance = {
                		title: "",
                		supervisor: null
                };
                
                $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;
                
                // get the process definition
                processService.getProcessMetadata(processId)
                    .then(
                    // success callback
                    function (response) {
                        $scope.process = response.data;
                    },
                    // error callback
                    function (response) {
                    }
                );
                
                processService.getSupervisors()
                   .then(
                    // success callback
                    function (response) {
                        $scope.supervisors = response.data;
                    },
                    // error callback
                    function (response) {}
                );

                /**
                 * get process form data and start a new process instance
                 */
                $scope.startProcess = function (event) {
                	$scope.showProgress = true;
                	$scope.instance.processForm = $scope.process.processForm;
                	
                	processService.startProcess(processId, $scope.instance)
                        .then(
                        // success callback
                        function () {
                        	$scope.showProgress = false;
                            $mdDialog.show($mdDialog.alert()
                                    .parent(document.body)
                                    .clickOutsideToClose(true)
                                    .title($filter('translate')('executionStarted'))
                                    .content($filter('translate')('executionStarted'))
                                    .ok($filter('translate')('confirm'))
                            ).then(
                                function () {
                                    $location.path('/assign');
                                }
                            );
                        },
                        // error callback
                        function (response) {
                        	$mdDialog.show({
                        		controller: function ($scope, $mdDialog, error) {
                        			$scope.error = error;
                        			
                                    $scope.cancel = function () {
                                    	$mdDialog.hide();
                                    };
                                },
                                scope: $scope,
                                preserveScope: true,
                                templateUrl: 'templates/exception.tmpl.html',
                                parent: angular.element(document.body),
                                targetEvent: event,
                                locals: {
                                	'error': response.data
                                }
                        	})
                        }
                    );
                };
        }]);

})(angular);