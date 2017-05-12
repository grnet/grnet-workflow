define(['angular',

	// Controllers
	'controllers/activity',
	'controllers/adddefinition',
	'controllers/completed-instance-details',
	'controllers/dashboard',
	'controllers/external-forms',
	'controllers/history',
	'controllers/in-progress',
	'controllers/inprogress-instance-details',
	'controllers/instance-documents',
	'controllers/navbar',
	'controllers/pending',
	'controllers/processdetail',
	'controllers/processlist',
	'controllers/settings',
	'controllers/task-details',

	// Services
	'services/authprovider',
	'services/dashboardService',
	'services/processService',
	'services/cacheService',

	// Directives
	'directives/nlk-process-form-item',
	'directives/nlk-process-form',
	'directives/nlkFileInput',
	'directives/wfConversation',
	'directives/wfDashlet',
	'directives/wfDocumentInput',
	'directives/wfList',
	'directives/wfMultiSeriesChart',
	'directives/wfMultiSeriesTimeChart',
	'directives/wfPositionInput',
	'directives/wfSingleSeriesChart',
	'directives/wfSingleTimeSeriesChart',
	'directives/wfTaskList',

	// Third party
	'angular-translate.min',
	'angular-translate-loader-static-files',
	'angular-chart.min',
	'angular-gridster.min',
	'angular-material-datetimepicker.min'
],

	function AngularConfig(angular) {

		'use strict';

		var initialize = function () {

			/* localhost */
			var WORKFLOW_SERVICE_ENTRY = 'http://10.0.0.150:8080/workflow-engine/api';
			var DASHBOARD_SERVICE_ENTRY = 'http://10.0.0.150:8080/workflow-engine/api/v2';
			var HOME_URL = 'http://localhost/workflow-manager/index.html';
			var WORKFLOW_DOCUMENTS_URL = 'http://10.0.0.150:8080/workflow-engine/document/';
			var WORKFLOW_WORKSPACE_URL = 'http://10.0.0.150/workflow-workspace';
			
			var AVATARS_PATH = 'img/avatars/';
			var DEFAULT_AVATAR = 'like.svg';
			var MAP_CENTER_LAT = 30.037496;
			var MAP_CENTER_LNG = 20.836321;

			var wfManagerApp = angular.module('wfManagerApp', [
				'ngRoute',
				'ngMaterial',
				'ngMessages',
				'ngAria',
				'ngAnimate',
				'ngSanitize',
				'wfManagerControllers',
				'wfManagerServices',
				'wfManagerDirectives',
				'pascalprecht.translate',
				'ngMaterialDatePicker',
				'gridster',
				'chart.js'
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
						auth.logoutUrl = keycloakAuth.authServerUrl + '/realms/' + auth.authz.realm + '/protocol/openid-connect/logout' + '?redirect_uri=' + HOME_URL;
						angular.bootstrap(document, ['wfManagerApp']);
					})
					.error(function () {
						alert('Failed to Authenticate!');
						window.location.reload();
					});
			});

			/*
			 * Authentication interceptor
			 */
			wfManagerApp.factory('authInterceptor', ['$q', 'auth', function ($q, authProvider) {
				return {
					request: function (config) {
						return authProvider.authInterceptor($q, config);
					}
				};
			}]);

			/*
			 * request interceptor - error handling
			 */
			wfManagerApp.factory('errorInterceptor', ['$q', 'auth', function ($q, authProvider) {
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
			wfManagerApp.constant('CONFIG', {
				'WORKFLOW_SERVICE_ENTRY': WORKFLOW_SERVICE_ENTRY,
				'WORKFLOW_DOCUMENTS_URL': WORKFLOW_DOCUMENTS_URL,
				'DASHBOARD_SERVICE_ENTRY': DASHBOARD_SERVICE_ENTRY,
				'WORKFLOW_WORKSPACE_URL': WORKFLOW_WORKSPACE_URL,
				'AVATARS_PATH': AVATARS_PATH,
				'DEFAULT_AVATAR': DEFAULT_AVATAR,
				'MAP_CENTER_LAT': MAP_CENTER_LAT,
				'MAP_CENTER_LNG': MAP_CENTER_LNG
			})
				.config(['$routeProvider', '$httpProvider', 'authProvider', '$translateProvider', 'ChartJsProvider', '$mdDateLocaleProvider', '$locationProvider',
					function ($routeProvider, $httpProvider, authProvider, $translateProvider, ChartJsProvider, $mdDateLocaleProvider, $locationProvider) {

						$translateProvider.preferredLanguage('el');
						$translateProvider.useSanitizeValueStrategy(null);

						$translateProvider.useStaticFilesLoader({
							prefix: 'lang/',
							suffix: '.json'
						});

						$locationProvider.hashPrefix("");

						$routeProvider.
							when('/process', {
								templateUrl: 'views/process-list.html',
								controller: 'ProcessListCtrl'
							}).
							when('/process/:processId', {
								templateUrl: 'views/process-detail.html',
								controller: 'ProcessDetailCtrl'
							}).
							when('/inprogress', {
								templateUrl: 'views/inprogress.html',
								controller: 'InProgressCtrl'
							}).
							when('/history', {
								templateUrl: 'views/history.html',
								controller: 'HistoryCtrl'
							}).
							when('/history/:instanceId', {
								templateUrl: 'views/instance-details.html',
								controller: 'CompletedInstanceDetailCtrl'
							}).
							when('/task/details/:taskId', {
								templateUrl: 'views/task-details.html',
								controller: 'TaskDetailsCtrl'
							}).
							when('/pending', {
								templateUrl: 'views/pending.html',
								controller: 'PendingCtrl'
							}).
							when('/inprogress', {
								templateUrl: 'views/in-progress.html',
								controller: 'InProgressCtrl'
							}).
							when('/activity', {
								templateUrl: 'views/activity.html',
								controller: 'ActivityCtrl'
							}).
							when('/settings', {
								templateUrl: 'views/settings.html',
								controller: 'SettingsCtrl'
							}).
							when('/externalForms', {
								templateUrl: 'views/external-forms.html',
								controller: 'ExternalFormsCtrl'
							}).
							when('/instance/:instanceId', {
								templateUrl: 'views/inprogress-instance-details.html',
								controller: 'InProgressInstanceDetailCtrl'
							}).
							when('/instance/:instanceId/documents', {
								templateUrl: 'views/documents.html',
								controller: 'InstanceDocumentsCtrl'
							}).
							when('/dashboard', {
								templateUrl: 'views/dashboard.html',
								controller: 'DashboardCtrl'
							}).
							otherwise({
								redirectTo: '/process'
							});

						ChartJsProvider.setOptions({
							maintainAspectRatio: false,
							responsive: true
						});

						$mdDateLocaleProvider.formatDate = function (date) {
							if (date != null && date != 'null') {
								return moment(date).format('DD-MM-YYYY');
							}
							return date;
						};

						/*
						// configure $http to use the new Promise interface
						$httpProvider.useLegacyPromiseExtensions(false);
						*/
						$httpProvider.defaults.useXDomain = true;
						$httpProvider.defaults.cache = false;

						// configure authentication interceptors
						$httpProvider.interceptors.push('errorInterceptor');
						$httpProvider.interceptors.push('authInterceptor');

						authProvider.auth = auth;
						authProvider.addIgnorePath(/img\//);
					}]
				);
		}

		return {
			initialize: initialize
		};

	});