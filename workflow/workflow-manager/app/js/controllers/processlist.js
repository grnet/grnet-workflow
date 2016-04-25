/**
 * @author nlyk
 */
(function () {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('ProcessListCtrl', ['$scope', 'auth', '$location', '$mdDialog', 'processService', 'CONFIG', 'auth',

            function ($scope, authProvider, $location, $mdDialog, processService, config, auth) {
        	
        	//comment to commit

                $scope.workflowDefinitions = null;
                $scope.imagePath = config.AVATARS_PATH;
                $scope.status = { allSelected: true };
                $scope.groups = null;
                $scope.selectedOnwers = [];
                
                $scope.options = [];
                $scope.orderByOption = null;
                
                $scope.sortOptions = {title: 'processTitle', id: 'name'};
				$scope.options.push($scope.sortOptions);
				$scope.sortOptions = {title: 'owner', id: 'owner'};
				$scope.options.push($scope.sortOptions);

                /**
                 * Get all process definitions
                 */
                processService.getProcesses().then(
                    // success callback
                    function (response) {
                        // set default icon
                        $scope.workflowDefinitions = response.data.map(
                            function (def) {
                                def.icon = def.icon || config.DEFAULT_AVATAR;
                                return def;
                            },
                            
                            function(response){
                            	alert(response);
                            }
                            
                        );
                    	}
                );
                

                //checking if user has role admin in order to show all groups/owners
                if(authProvider.getRoles().indexOf("ROLE_Admin") >= 0){
                    processService.getGroups().then(
        	                // success callback
        	                function (response) {
        	                	$scope.groups = response.data;
        	                	$scope.groups = $scope.groups.map(function(elm) { return { group: elm, selected: true }; }); 
        	                }
                        );
                }else{
                	processService.getUserGroups().then(
                			 function (response) {
         	                	$scope.groups = response.data;
         	                	$scope.groups = $scope.groups.map(function(elm) { return { group: elm, selected: true }; }); 
         	                }
                	);
                }

                /**
                 * Show a dialog for uploading a new BPMN file to create a new workflow definition
                 */
                $scope.addProcess = function (event) {
                    $mdDialog.show({
                        controller: 'addDefinitionController',
                        templateUrl: 'templates/adddefinition.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {'process': null}
                    });
                };
                
                /**
                 * Toggle all/none all owners
                 */
				$scope.updateOwnerSelection = function () {
					$scope.groups.forEach(function(elm) { elm.selected =  $scope.status.allSelected; return; });
					$scope.showProcessByOwners();
				};
				
				/**
				 * Return processes definitions by selected owners
				 */
				$scope.showProcessByOwners = function (){
					
					var selectedOwners = $scope.groups
					.filter(function(element){ return element.selected === true; })
					.map( function(element, index, that) { return element.group; } );
					
				   processService.getProcessesByOwners(selectedOwners).then(
		                    // success callback
		                    function (response) {
		                        // set default icon
		                        $scope.workflowDefinitions = response.data.map(
		                            function (def) {
		                                def.icon = def.icon || config.DEFAULT_AVATAR;
		                                return def;
		                            });
		                    }
		                );
				};

                /**
                 * Returns true if the selected version is active
                 * @param {WorkflowDefinition} process
                 */
                $scope.isActive = function (process) {
                    return processService.isProcessActive(process);
                };

                /**
                 * Supports navigation
                 * @param {string} path
                 */
                $scope.goTo = function (path) {
                    $location.path(path);
                };
                
                /**
				 * Sorting function
				 */
				$scope.sortBy = function (optionId){
					$scope.orderByOption = optionId;
				};
            }]
    );

})(angular);
