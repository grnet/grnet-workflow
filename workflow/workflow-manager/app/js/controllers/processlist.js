define(['angular', 'services/processservice'],

	function (angular) {

		'use strict';

		function processListCtrl($scope, authProvider, $location, $mdDialog, processService, config) {

			// system constants
			$scope.imagePath = config.AVATARS_PATH;

			$scope.status = { allSelected: true };

			$scope.showProgressBar = true;

			$scope.options = [];
			$scope.orderByOption = null;

			$scope.sortOptions = { title: 'processTitle', id: 'name' };
			$scope.options.push($scope.sortOptions);
			$scope.sortOptions = { title: 'owner', id: 'owner' };
			$scope.options.push($scope.sortOptions);

			//checking if user has role admin in order to show all groups/owners
			if (authProvider.getRoles().indexOf('ROLE_Admin') >= 0 || authProvider.getRoles().indexOf('ROLE_Manager') >= 0) {
				processService.getGroups().then(
					// success callback
					function (response) {
						$scope.groups = response.data;
						$scope.groups = $scope.groups.map(function (elm) { return { group: elm, selected: true }; });
						$scope.showProcessByOwners();
					},
					function (response) {
						exceptionModal(response);
					}
				);

			} else {
				processService.getUserGroups().then(
					function (response) {
						$scope.groups = response.data;
						$scope.groups = $scope.groups.map(function (elm) { return { group: elm, selected: true }; });
						$scope.showProcessByOwners();
					}
				);
			}

			/**
			 * Show a dialog for uploading a new BPMN file to create a new workflow definition
			 * 
			 */
			$scope.addProcess = function (event) {
				$mdDialog.show({
					controller: 'AddDefinitionController',
					templateUrl: 'templates/adddefinition.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: false,
					locals: { 'process': null }
				});
			};

			/**
			 * Toggle all/none all owners
			 */
            $scope.updateOwnerSelection = function () {
                $scope.groups.forEach(function (elm) {
                    if($scope.status.allSelected)
                        elm.selected = $scope.status.allSelected;
                    return;
                });
                $scope.showProcessByOwners();
            };

            /**
             * @memberof ProcessListCtrl
             * @desc Clears any selection
             *
             */
            $scope.clearAllSelections = function () {
                $scope.groups.forEach(function (elm) {
                    elm.selected = false;
                });
                $scope.status.allSelected = false;
                $scope.showProcessByOwners();
            };

            /**
			 * Return processes definitions by selected owners
			 */
			$scope.showProcessByOwners = function () {
				$scope.showProgressBar = true;

                if($scope.status.allSelected)
                    var selectedOwners = "";
                else {
                    var selectedOwners = $scope.groups.filter(function (element) {
                        return element.selected === true;
                    }).map(function (element) {
						return element.group.ownerId;
					});
                }

				processService.getProcessesByOwners(selectedOwners).then(
					// success callback
					function (response) {
						// set default icon
						$scope.workflowDefinitions = response.data.map(
							function (def) {
								def.icon = def.icon || config.DEFAULT_AVATAR;
								return def;
							}
						);
					},
					function (response) {
						exceptionModal(response);
					}

				).finally(function () {
					$scope.showProgressBar = false;
				});
			};

			/**
			 * Returns true if the selected version is active
			 * @param {WorkflowDefinition} process
			 */
			$scope.isActive = function (process) {
				return processService.isProcessActive(process);
			};

			/**
			 * Sorting function
			 */
			$scope.sortBy = function (optionId) {
				$scope.orderByOption = optionId;
			};

			$scope.filteringOptions = function (event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog) {

						$scope.cancel = function () {
							$mdDialog.hide();
						};
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/filterProcessList.tmpl.html',
					parent: angular.element(document.body),
					targetEvent: event
				});
			};

			/**
			 * Exception modal
			 */
			function exceptionModal(response, event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog, error) {

						$scope.error = error;

						$scope.cancel = function () {
							$mdDialog.cancel();
						};
					},
					templateUrl: 'templates/exception.tmpl.html',
					parent: angular.element(document.body),
					targetEvent: event,
					clickOutsideToClose: false,
					locals: {
						'error': response.data
					}
				});
			}

		}

		angular.module('wfManagerControllers').controller('ProcessListCtrl', ['$scope', 'auth', '$location', '$mdDialog', 'processService', 'CONFIG', processListCtrl]);
	}
);
