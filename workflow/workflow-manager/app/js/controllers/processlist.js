(function () {

	'use strict';

	angular.module('wfmanagerControllers').controller('ProcessListCtrl', ['$scope', 'auth', '$mdDialog', 'processService', 'CONFIG',
        /**
         * @name ProcessListCtrl
         * @ngDoc controllers
         * @memberof wfmanagerControllers
         * 
         * @desc Controller used in Process list view
         */
		function ($scope, authProvider, $mdDialog, processService, config) {

			$scope.imagePath = config.AVATARS_PATH;
			$scope.status = { allSelected: true };

			$scope.options = [];
			$scope.orderByOption = null;

			$scope.sortOptions = { title: 'processTitle', id: 'name' };
			$scope.options.push($scope.sortOptions);
			$scope.sortOptions = { title: 'owner', id: 'owner' };
			$scope.options.push($scope.sortOptions);

			$scope.showProgressBar = true;

			//checking if user has role admin in order to show all groups/owners
			if (authProvider.getRoles().indexOf("ROLE_Admin") >= 0) {
				processService.getGroups().then(
					// success callback
					function (response) {
						$scope.groups = response.data;
						$scope.groups = $scope.groups.map(function (elm) { return { group: elm, selected: true }; });
						$scope.showProcessByOwners();
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
			 * @memberOf ProcessListCtrl
			 * @desc Show a dialog for uploading a new BPMN file to create a new workflow definition
			 * 
			 * @param {event} event
			 */
			$scope.addProcess = function (event) {
				$mdDialog.show({
					controller: 'addDefinitionController',
					templateUrl: 'templates/adddefinition.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: false,
					locals: { 'process': null }
				});
			};

			/**
			 * @memberOf ProcessListCtrl
			 * @desc Check the selected owners and displays the definitions owned by the selected owners
			 */
			$scope.updateOwnerSelection = function () {
				$scope.groups.forEach(function (elm) { elm.selected = $scope.status.allSelected; return; });
				$scope.showProcessByOwners();
			};

			/**
			 * @memberOf ProcessListCtrl
			 * @desc Return processes definitions by selected owners
			 */
			$scope.showProcessByOwners = function () {

				$scope.showProgressBar = true;

				var selectedOwners = $scope.groups.filter(
					function (element) {
						return element.selected === true;

					}).map(function (element, index, that) {
						return element.group;
					});

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
					// error callback
					, function (response) {
						console.log(response);
						exceptionModal(response);
					}

				).finally(function () {
					$scope.showProgressBar = false;
				});
			};

			/**
			 * @memberOf ProcessListCtrl
			 * @desc Returns true if the selected version is active
			 * 
			 * @param {WorkflowDefinition} process
			 * @returns {Boolean} Whether the definition is active or not
			 */
			$scope.isActive = function (process) {
				return processService.isProcessActive(process);
			};

			/**
			 * @memberOf ProcessListCtrl
			 * @desc Sorts the process definitions by given option
			 * 
			 * @param {String} optionId
			 */
			$scope.sortBy = function (optionId) {
				$scope.orderByOption = optionId;
			};

			/**
			 * @memberOf ProcessListCtrl
			 * @desc Displays a modal panel showing the exception message
			 * 
			 * @param {any} response
			 * @param {event} $event
			 */
			function exceptionModal(response, $event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog, error) {

						$scope.error = error;

						$scope.cancel = function () {
							$mdDialog.cancel();
						};
					},
					templateUrl: 'templates/exception.tmpl.html',
					parent: angular.element(document.body),
					targetEvent: $event,
					clickOutsideToClose: false,
					locals: {
						'error': response.data
					}
				});
			};

		}]);
})(angular);