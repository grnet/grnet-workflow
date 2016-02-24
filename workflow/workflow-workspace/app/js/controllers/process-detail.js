/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')

        .controller('ProcessDetailCtrl',
        ['$scope', '$location', '$mdDialog', '$filter', 'processService', 'CONFIG',

            /**
             * Controller for the Process Details view
             * @param {object} $scope
             * @param {$location} $location
             * @param {$mdDialog} $mdDialog
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $location, $mdDialog, $filter, processService, config) {

                /** @type {WorkflowDefinition[]} */
        		$scope.workflowDefinitions = null;
                $scope.groupProcesses = null;
                $scope.process = null;
                $scope.selectedIndex = null;
                $scope.activeVersion = null;
                
                
                // get the processes
                processService.getProcesses()
                    .then(
                    // success callback
                    function (response) {
                        $scope.groupProcesses  = ArrayUtil.mapByProperty(response.data, "owner");

                        if(response.data.length == 0){
                        	$mdDialog.show($mdDialog.alert()
         		                   .parent(document.body)
         		                   .title($filter('translate')('noAvailableProcess'))
         		                   .content($filter('translate')('noAvailableProcess'))
         		                   .ok($filter('translate')('confirm')))
                        	$location.path('/task');
                        }
                        
                        if (response.data.length > 0)
                        	$scope.processSelectionChanged(response.data[0].id);
                    },
                    // error callback
                    function (response) {}
                );                
                
                $scope.processSelectionChanged = function (processId) {
                	$scope.selectedIndex = processId;
                    processService.getProcess(processId)
                    .then(
                    // success callback
                    function (response) {
                    	$scope.process = response.data;
                    	
                    	for (var processVersion in $scope.process.processVersions) {
                    		  if ($scope.process.processVersions.hasOwnProperty(processVersion)) {
                    			  if($scope.process.processVersions[processVersion].status == "active"){
                    				  $scope.activeVersion = $scope.process.processVersions[processVersion].version;
                    			  }
                    			  
                			  }
                    		  
                    	}
                    },
                    // error callback
                    function (response) {}
                    );
                };

                /**
                 * Shows a dialog with the process diagram
                 * @param event
                 */
                $scope.showDiagram = function (event) {
                    $mdDialog.show({
                        controller: function ($scope, $mdDialog, process, service) {
                            $scope.process = process;
                            $scope.service = service;
                            $scope.cancel = function () {
                                $mdDialog.cancel();
                            };
                        },
                        templateUrl: 'templates/diagram.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {
                            'service': config.WORKFLOW_SERVICE_ENTRY,
                            'process': $scope.process
                        }
                    })
                };
                
                /**
                 * Returns true if the selected version is active
                 * @param {WorkflowDefinition} process
                 */
                $scope.isActive = function (process) {
                    return processService.isProcessActive(process);
                };
                
                /**
                 * Shows the UI to start a new instance of the selected process
                 */
                $scope.startProcess = function () {
                    $location.path('/process/start/' + $scope.process.id);
                };

            }]
    );

})(angular);
