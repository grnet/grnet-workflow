define(['angular',

	// Controllers
	'controllers/documents',
	'controllers/in-progress',
	'controllers/instance-details',
	'controllers/instance-documents',
	'controllers/navbar',
	'controllers/print-start-form',
	'controllers/process-detail',
	'controllers/process-start',
	'controllers/task-activity-list',
	'controllers/task-assign',
	'controllers/task-assign-list',
	'controllers/task-completed-details',
	'controllers/task-completed-list',
	'controllers/task-detail',
	'controllers/task-detail',
	'controllers/tasklist',

	// Services
	'services/authprovider',
	'services/process-service',
	'services/cacheService',

	// Directives
	'directives/nlk-checklist',
	'directives/nlk-process-form',
	'directives/nlk-process-form-item',
	'directives/print-form-item',
	'directives/wfConversation',
	'directives/wfDocumentInput',
	'directives/wfPositionInput',
	'directives/wfPositionPrint',

	// Third party
	'angular-translate.min',
	'angular-translate-loader-static-files',
	'angular-material-datetimepicker.min'
],

	function AngularConfig(angular) {

		'use strict';

		var initialize = function () {

			/* Localhost */
			var WORKFLOW_SERVICE_ENTRY = 'http://10.0.0.150:8080/workflow-engine/api';
			var HOME_URL = 'http://localhost/workflow-workspace';
			var WORKFLOW_DOCUMENTS_URL = 'http://10.0.0.150:8080/workflow-engine/document/';

			var AVATARS_PATH = 'img/avatars/';
			var DEFAULT_AVATAR = 'like.svg';
			var MAP_CENTER_LAT = 30.037496;
			var MAP_CENTER_LNG = 20.836321;
			var POSTCODE = 15230;

			var wfWorkspaceApp = angular.module('wfWorkspaceApp', [
				'ngRoute',
				'ngMaterial',
				'ngMessages',
				'ngAria',
				'ngAnimate',
				'ngSanitize',
				'pascalprecht.translate',
				'ngMaterialDatePicker',
				'wfWorkspaceControllers',
				'wfWorkspaceDirectives',
				'wfWorkspaceServices'
			]);

			/**
			 * Application Bootstrapping integrating with SSO
			 */
			var auth = {};

			angular.element(document).ready(function () {

				var keycloakAuth = new Keycloak('keycloak.json');
				auth.loggedIn = false;

				keycloakAuth.init({ onLoad: 'login-required' })
					.success(function () {
						auth.loggedIn = true;
						auth.authz = keycloakAuth;
						auth.logoutUrl = keycloakAuth.authServerUrl + "/realms/" + auth.authz.realm + "/protocol/openid-connect/logout?redirect_uri=" + HOME_URL;

						angular.bootstrap(document, ["wfWorkspaceApp"]);
					})
					.error(function () {
						alert("Failed to Authenticate!");
						window.location.reload();
					});
			});

			/*
			 * Authentication interceptor
			 */
			wfWorkspaceApp.factory('authInterceptor', ['$q', 'auth', function ($q, authProvider) {
				return {
					request: function (config) {
						return authProvider.authInterceptor($q, config);
					}
				};
			}]);

			/*
			 * request interceptor - error handling
			 */
			wfWorkspaceApp.factory('errorInterceptor', ['$q', 'auth', function ($q, authProvider) {
				return {
					response: function (response) {
						return response;
					},
					responseError: function (response) {
						if (response.status === 400 || response.status === 500) {
							return $q.reject(response);
						}
						return authProvider.authErrorHandler($q, response);
					}
				};
			}]);

			/*
			 * Application configuration
			 */
			wfWorkspaceApp
				.constant('CONFIG', {
					'WORKFLOW_SERVICE_ENTRY': WORKFLOW_SERVICE_ENTRY,
					'WORKFLOW_DOCUMENTS_URL': WORKFLOW_DOCUMENTS_URL,
					'AVATARS_PATH': AVATARS_PATH,
					'DEFAULT_AVATAR': DEFAULT_AVATAR,
					'MAP_CENTER_LAT': MAP_CENTER_LAT,
					'MAP_CENTER_LNG': MAP_CENTER_LNG,
					'POSTCODE': POSTCODE
				})
				.config(['$routeProvider', '$httpProvider', '$mdThemingProvider', 'authProvider', '$translateProvider', '$locationProvider',
					function ($routeProvider, $httpProvider, $mdThemingProvider, authProvider, $translateProvider, $locationProvider) {

						$translateProvider.preferredLanguage('el');
						$translateProvider.useSanitizeValueStrategy(null);

						$translateProvider.useStaticFilesLoader({
							prefix: 'lang/',
							suffix: '.json'
						});

						$routeProvider.
							when('/task', {
								templateUrl: 'views/task-list.html',
								controller: 'TaskListCtrl'
							}).
							when('/task/:taskId', {
								templateUrl: 'views/task-detail.html',
								controller: 'TaskDetailCtrl'
							}).
							when('/process', {
								templateUrl: 'views/process-detail.html',
								controller: 'ProcessDetailCtrl'
							}).
							when('/process/start/:processId', {
								templateUrl: 'views/process-start.html',
								controller: 'ProcessStartCtrl'
							}).
							when('/assign', {
								templateUrl: 'views/task-assign-list.html',
								controller: 'TaskAssignListCtrl'
							}).
							when('/assign/:taskId', {
								templateUrl: 'views/task-assign.html',
								controller: 'TaskAssignCtrl'
							}).
							when('/completed', {
								templateUrl: 'views/task-completed-list.html',
								controller: 'CompletedTasksCtrl'
							}).
							when('/completed/:taskId', {
								templateUrl: 'views/task-completed-details.html',
								controller: 'TaskCompletedDetailsCtrl'
							}).
							when('/documents/:pageId/:taskId', {
								templateUrl: 'views/documents.html',
								controller: 'DocumentsCtrl'
							}).
							when('/instance/:instanceId/documents', {
								templateUrl: 'views/documents.html',
								controller: 'InstanceDocumentsCtrl'
							}).
							when('/activity', {
								templateUrl: 'views/task-activity.html',
								controller: 'TaskActivityListCtrl'
							}).
							when('/startform/:mode/:instanceId', {
								templateUrl: 'views/print-start-form.html',
								controller: 'PrintStartFormCtrl'
							}).
							when('/print/instance/:instanceId/task/:taskId', {
								templateUrl: 'views/print-start-form.html',
								controller: 'PrintStartFormCtrl'
							}).
							when('/inprogress', {
								templateUrl: 'views/in-progress.html',
								controller: 'InProgressCtrl'
							}).
							when('/instance/:instanceId', {
								templateUrl: 'views/instance-details.html',
								controller: 'InstanceDetailsCtrl'
							}).
							otherwise({
								redirectTo: '/task'
							});

						// configure $http to use the new Promise interface
						//$httpProvider.useLegacyPromiseExtensions(false);
						$httpProvider.defaults.useXDomain = true;
						delete $httpProvider.defaults.headers.common['Access-Control-Allow-Headers'];

						// configure authentication interceptors
						$httpProvider.interceptors.push('errorInterceptor');
						$httpProvider.interceptors.push('authInterceptor');

						authProvider.auth = auth;
						authProvider.addIgnorePath(/img\//);
						authProvider.addIgnorePath(/maps\.googleapis\.com\//);

						$locationProvider.hashPrefix("");

						// configure theming
						$mdThemingProvider.theme('default')
							.primaryPalette('purple')
							.accentPalette('amber');
					}]);
		}

		return {
			initialize: initialize
		};

	}
);