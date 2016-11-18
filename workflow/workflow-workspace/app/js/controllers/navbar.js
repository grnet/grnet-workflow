(function (angular) {

	'use strict';

    angular.module('wfworkspaceControllers').controller('NavBarCtrl', ['$scope', '$mdSidenav', '$location', '$window', 'auth',
		/**
		 * @name NavBarCtrl
		 * @ngDoc controllers
		 * @memberof wfworkspaceControllers
		 *
		 */
		function ($scope, $mdSidenav, $location, $window, authProvider) {

			$scope.isPrint = window.location.href.indexOf('/print/') >= 0;
			$scope.pages = [];
			$scope.page = { title: null, path: null, icon: null, color: null, disabled: null };

			$scope.inputFile = null;

			/**
			 * @memberof NavBarCtrl
			 * @desc Toggles the side navigation bar
			 */
			$scope.toggle = function () {
				$mdSidenav('navbar').toggle();
			};

			/**
			 * @memberof NavBarCtrl
			 * @desc Event listener on swipe right to open the navigation side bar
			 */
			$scope.onSwipeRight = function () {
				$mdSidenav('navbar').open();
			};

			/**
			 * @memberof NavBarCtrl
			 * @desc Event listener on swipe left to close the navigation side bar
			 */
			$scope.onSwipeLeft = function () {
				$mdSidenav('navbar').close();
			};

			/**
			 * @memberof NavBarCtrl
			 * @desc check if the given path is currently selected
			 * 
			 * @param {String} path
			 * @returns {Boolean} - Wheter the given path is selected
			 */
			$scope.isSelected = function (path) {
				return path === $location.path();
			};

			/**
			 * @memberof NavBarCtrl
			 * @desc Redirects to given path
			 * 
			 * @param {String} path
			 */
			$scope.goTo = function (path) {
				$mdSidenav('navbar').close().then(function () {

					if (path.indexOf('print') >= 0) {
						$window.open("#" + path, "_blank");
						return;
					}

					$location.path(path);
				});
			};

			/**
			 * @memberof NavBarCtrl
			 * @desc Logouts the user
			 */
			$scope.logout = function () {
				authProvider.logout();
			};

			/**
			 * @memberof NavBarCtrl
			 * @desc Initialize all available pages. Also checks for user rights in order to enable or disable any page
			 */
			$scope.initializePages = function () {

				var isSupervisor = authProvider.getRoles().indexOf("ROLE_Supervisor") >= 0 ? true : false;
				var isProcessAdmin = authProvider.getRoles().indexOf("ROLE_ProcessAdmin") >= 0 ? true : false;
				var isAdmin = authProvider.getRoles().indexOf("ROLE_Admin") >= 0 ? true : false;
				var isUser = authProvider.getRoles().indexOf("ROLE_User") >= 0 ? true : false;

				//always shown. no need to check
				$scope.page = { title: 'myTasks', path: '/task', icon: 'myTasks.svg', color: 'red', disabled: false };
				$scope.pages.push($scope.page);

				//handle admin
				if (isAdmin) {

					$scope.page = { title: 'assignTasks', path: '/assign', icon: 'assignTasks.svg', color: 'red', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'completedTasks', path: '/completed', icon: 'completedTasks.svg', color: 'green', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'startNewProcess', path: '/process', icon: 'startProccess.svg', color: 'blue', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'executionsInProgress', path: '/inprogress', icon: 'history.svg', color: 'purple', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'myActivity', path: '/activity', icon: 'myActivity.svg', color: 'green', disabled: false };
					$scope.pages.push($scope.page);

					//handle process admin
				} else if (isProcessAdmin) {

					$scope.page = { title: 'assignTasks', path: '/assign', icon: 'assignTasks.svg', color: 'red', disabled: true };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'completedTasks', path: '/completed', icon: 'completedTasks.svg', color: 'green', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'startNewProcess', path: '/process', icon: 'startProccess.svg', color: 'blue', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'executionsInProgress', path: '/inprogress', icon: 'history.svg', color: 'purple', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'myActivity', path: '/activity', icon: 'myActivity.svg', color: 'green', disabled: false };
					$scope.pages.push($scope.page);

					//handle supervisor
				} else if (isSupervisor) {
					$scope.page = { title: 'assignTasks', path: '/assign', icon: 'assignTasks.svg', color: 'red', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'completedTasks', path: '/completed', icon: 'completedTasks.svg', color: 'green', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'startNewProcess', path: '/process', icon: 'startProccess.svg', color: 'blue', disabled: true };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'executionsInProgress', path: '/inprogress', icon: 'history.svg', color: 'purple', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'myActivity', path: '/activity', icon: 'myActivity.svg', color: 'green', disabled: false };
					$scope.pages.push($scope.page);

					//handle user
				} else if (isUser) {
					$scope.page = { title: 'assignTasks', path: '/assign', icon: 'assignTasks.svg', color: 'red', disabled: true };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'completedTasks', path: '/completed', icon: 'completedTasks.svg', color: 'green', disabled: true };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'myActivity', path: '/activity', icon: 'myActivity.svg', color: 'green', disabled: false };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'startNewProcess', path: '/process', icon: 'startProccess.svg', color: 'blue', disabled: true };
					$scope.pages.push($scope.page);

					$scope.page = { title: 'executionsInProgress', path: '/inprogress', icon: 'history.svg', color: 'purple', disabled: false };
					$scope.pages.push($scope.page);

				}
			};

		}]);
})(angular);
