define(['angular', 'services/processservice', 'util/core'],

	function (angular) {

		'use strict';

		function externalFormsCtrl($scope, $filter, $mdDialog, processService, config) {

			$scope.wrappedGroups = null;
			$scope.groupNames = [];
			$scope.supervisors = null;
			$scope.imagePath = config.AVATARS_PATH;

			$scope.showProgressBar = true;
			
			processService.getProcesses().then(
				function (response) {
					$scope.processes = response.data;
					//error callback	
				}
				, function (response) {
					exceptionModal(response);
			});

			function getGroupsFormsWrapped() {
				processService.getGroupsFormsWrapped().then(
					function (response) {
						$scope.wrappedGroups = ArrayUtil.mapByProperty(response.data, 'groupName');

						var groups = {};
						response.data.forEach(function (o) {
							var group = (o['groupName'] || '!empty!').toString();
							if (!groups.hasOwnProperty(group)) {
								groups[group] = o['groupId'];
							}
						});

						if (!groups.hasOwnProperty('!empty!')) {
							groups['!empty!'] = null;
						}
						$scope.groups = Object.keys(groups).sort().map(function (group) {
							return { name: group, groupId: groups[group] };
						});

						$scope.groupNames = Object.keys($scope.wrappedGroups).sort();
						//error callback
					}, function (response) {
					exceptionModal(response);

				}).finally(function () {
						$scope.showProgressBar = false;
					});
			}

			getGroupsFormsWrapped();

			/**
			 * Opens a modal to create a new group
			 */
			$scope.addNewGroup = function ($event) {

				$mdDialog.show({
					controller: function ($scope, $mdDialog, group, processService) {

						$scope.isNew = true;
						$scope.group = group;

						$scope.cancel = function () {
							$mdDialog.cancel();
						};

						$scope.save = function () {
							processService.createExternalGroup($scope.group).then(
								function (response) {
									$mdDialog.hide();

									//error callback	
								}, function (response) {
								$mdDialog.cancel();
							});
						};
					},
					templateUrl: 'templates/externalGroup.tmpl.html',
					parent: document.body,
					targetEvent: $event,
					clickOutsideToClose: true,
					locals: {
						'group': $scope.group,
						'processService': processService,
						'isNew': $scope.isNew
					}

					//handle the close events for dialog    
				}).then(
					//when hide
					function () {
						getGroupsFormsWrapped();

						//when canceled	
					}, function () {
					});
			};

			/**
			 * Edit a group
			 */
			$scope.editGroup = function ($event, group) {

				$mdDialog.show({
					controller: function ($scope, $mdDialog, group, processService) {

						$scope.isNew = false;
						$scope.group = group;

						$scope.cancel = function () {
							$mdDialog.cancel();
						};

						$scope.save = function () {
							processService.editPublicGroup($scope.group).then(
								//success callback
								function (response) {
									$mdDialog.hide();

									//error callback	
								}, function (response) {
								$mdDialog.cancel();
							});

						};
					},
					templateUrl: 'templates/externalGroup.tmpl.html',
					parent: document.body,
					targetEvent: $event,
					clickOutsideToClose: true,
					locals: {
						'group': group,
						'isNew': $scope.isNew,
						'processService': processService
					}
					//handle then close events for dialog    
				}).then(
					//when hide
					function () {
						getGroupsFormsWrapped();
						//when canceled	
					}, function () {
						getGroupsFormsWrapped();
					});
			};

			/**
			 * Delete a group
			 */
			$scope.deleteGroup = function ($event, groupId) {

				var confirmDialog = $mdDialog.confirm()
					.title($filter('translate')('deleteGroup'))
					.content($filter('translate')('deleteGroupConfirm'))
					.ariaLabel($filter('translate')('deleteGroup'))
					.targetEvent($event)
					.ok($filter('translate')('confirm'))
					.cancel($filter('translate')('cancel'));

				$mdDialog.show(confirmDialog).then(
					// agree
					function () {
						processService.deletePublicGroup(groupId).then(
							//success callback
							function (response) {
								getGroupsFormsWrapped();
								$mdDialog.hide();

								//error callback	
							}, function (response) {
							$mdDialog.cancel();
							exceptionModal(response);
						});
					});
			};

			/**
			 * Create a new external form
			 */
			$scope.createExtForm = function ($event, group) {

				$mdDialog.show({
					controller: function ($scope, $mdDialog, extForm, supervisors, groups, processes, processService, group, createFromGroup) {

						//to show or hide the delete button
						$scope.isNew = true;

						$scope.extForm = extForm;
						$scope.supervisors = supervisors;
						$scope.groups = groups;
						$scope.processes = processes;

						$scope.group = group;
						$scope.createFromGroup = false;

						if ($scope.group != null) {
							$scope.createFromGroup = true;
						} else {
							$scope.createFromGroup = false;
						}

						$scope.getSupervisorsByProcess = function () {
							processService.getSupervisorsByProcessId($scope.extForm.workflowDefinitionId).then(
								function (response) {
									$scope.supervisors = response.data;
								});
						};

						$scope.cancel = function () {
							$mdDialog.cancel();
						};

						$scope.save = function () {
							processService.saveExternalForm($scope.extForm).then(
								function (response) {
									$mdDialog.hide();

									//error callback	
								}, function (response) {
								$mdDialog.cancel();
									//exceptionModal(response);
							});
						};
					},
					templateUrl: 'templates/externalForm.tmpl.html',
					parent: document.body,
					targetEvent: $event,
					clickOutsideToClose: true,
					locals: {
						'extForm': $scope.extForm,
						'supervisors': $scope.supervisors,
						'groups': $scope.groups,
						'processes': $scope.processes,
						'isNew': $scope.isNew,
						'processService': processService,
						'group': group,
						'createFromGroup': $scope.createFromGroup
					}

					//handle the close events for dialog    
				}).then(
					//executes when hide
					function () {
						getGroupsFormsWrapped();

						//when canceled	
					}, function () {

					});
			};

			/**
			 * Edit an existing external form
			 */
			$scope.editForm = function ($event, form) {

				$mdDialog.show({
					controller: function ($scope, $mdDialog, extForm, supervisors, groups, processes, processService, createFromGroup) {

						//to show or hide the delete button
						$scope.isNew = false;

						$scope.extForm = extForm;
						$scope.groups = groups;
						$scope.processes = processes;

						$scope.createFromGroup = false;

						$scope.getSupervisorsByProcess = function () {
							processService.getSupervisorsByProcessId($scope.extForm.workflowDefinitionId).then(
								function (response) {
									$scope.supervisors = response.data;
								});
						};

						//get the supervisors on open since we know the workflow definition
						$scope.getSupervisorsByProcess($scope.extForm.workflowDefinitionId);

						$scope.cancel = function () {
							$mdDialog.cancel();
						};

						$scope.save = function () {
							processService.updateExternalForm(extForm).then(
								function (response) {
									$mdDialog.hide();

									//error callback	
								}, function (response) {
								$mdDialog.cancel();

									//exceptionModal(response);
							});
						};

						$scope.deleteExtForm = function () {
							processService.deleteExternalForm($scope.extForm.formId).then(
								//success callback
								function (response) {
									$mdDialog.hide();

									//error callback	
								}, function (response) {
								$mdDialog.cancel();
							});
						};

					},
					templateUrl: 'templates/externalForm.tmpl.html',
					parent: document.body,
					targetEvent: $event,
					clickOutsideToClose: true,
					locals: {
						'extForm': form,
						'supervisors': $scope.supervisors,
						'groups': $scope.groups,
						'processes': $scope.processes,
						'isNew': $scope.isNew,
						'processService': processService,
						'createFromGroup': $scope.createFromGroup
					}

					//handle the close events for dialog    
				}).then(
					function () {
						getGroupsFormsWrapped();
					});
			};


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

		angular.module('wfManagerControllers').controller('ExternalFormsCtrl', ['$scope', '$filter', '$mdDialog', 'processService', 'CONFIG', externalFormsCtrl]);
	}
);
