(function (angular) {

    'use strict';

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // system constants
    var WORKFLOW_SERVICE_ENTRY = 'http://10.0.0.156:8085/workflow-engine/api';
    var HOME_URL = 'http://10.0.0.156/workflow-manager/index.html';
    var WORKFLOW_DOCUMENTS_URL = 'http://10.0.0.156:8085/workflow-engine/document/';
    var AVATARS_PATH = 'img/avatars/';
    var DEFAULT_AVATAR = 'like.svg';
    var MAP_CENTER_LAT = 38.037496;
    var MAP_CENTER_LNG = 23.836321;
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /* create controllers module */
    angular.module('wfmanagerControllers', []);

    /* create directives module */
    angular.module('nlkDirectives', []);
    
    /* create directives module */
    angular.module('wfDirectives', []);

    /* create services module */
    angular.module('wfmanagerServices', []);

    /* workflow manager App Module */
    var wfmanagerApp = angular.module('wfmanagerApp', [
        'ngRoute',
        'ngMaterial',
        'wfmanagerControllers',
        'wfmanagerServices',
        'nlkDirectives',
        'wfDirectives',
        'pascalprecht.translate',
        'ngMaterialDatePicker'
    ]);

    /**
     * Application Bootstrapping integrating with SSO
     */
    var auth = {};

    angular.element(document).ready(function () {

        var keycloakAuth = new Keycloak('keycloak.json');
        auth.loggedIn = false;

        keycloakAuth.init({onLoad: 'login-required'})
            .success(function () {
                auth.loggedIn = true;
                auth.authz = keycloakAuth;
                auth.logoutUrl = keycloakAuth.authServerUrl
                    + "/realms/workflow/tokens/logout?redirect_uri="
                    + HOME_URL;

                angular.bootstrap(document, ["wfmanagerApp"]);
            })
            .error(function () {
                alert("Failed to Authenticate!");
                window.location.reload();
            });
    });

    /*
     * Authentication interceptor
     */
    wfmanagerApp.factory('authInterceptor', ['$q', 'auth', function ($q, authProvider) {
        return {
            request: function (config) {
                return authProvider.authInterceptor($q, config);
            }
        };
    }]);

    /*
     * request interceptor - error handling
     */
    wfmanagerApp.factory('errorInterceptor', ['$q', 'auth', function ($q, authProvider) {
        return {
            response: function (response) {
                return response;
            },
            responseError: function (response) {
            	if(response.status === 400 || response.status === 500) {
            		return $q.reject(response);
            	}
                return authProvider.authErrorHandler($q, response);
            }
        };
    }]);
    
    /*
     * Application configuration
     */
    wfmanagerApp
        .constant('CONFIG', {
            'WORKFLOW_SERVICE_ENTRY': WORKFLOW_SERVICE_ENTRY,
            'WORKFLOW_DOCUMENTS_URL': WORKFLOW_DOCUMENTS_URL,
            'AVATARS_PATH': AVATARS_PATH,
            'DEFAULT_AVATAR': DEFAULT_AVATAR,
            'MAP_CENTER_LAT': MAP_CENTER_LAT,
	        'MAP_CENTER_LNG': MAP_CENTER_LNG
        })
        .config(['$routeProvider', '$httpProvider', 'authProvider', '$translateProvider',
            function ($routeProvider, $httpProvider, authProvider, $translateProvider) {
        	
	    		$translateProvider.preferredLanguage('el');
	    		$translateProvider.useSanitizeValueStrategy(null);
	    		
		    	$translateProvider.useStaticFilesLoader({
		    		  prefix: 'lang/',
		    		  suffix: '.json'
		    		});
	    	
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
                        controller: 'InstanceDetailCtrl'
                    }).
                    when('/task/details/:taskId', {
                        templateUrl: 'views/task-details.html',
                        controller: 'TaskDetailsCtrl'
                    }).
                    when('/pending', {
                        templateUrl: 'views/pending.html',
                        controller: 'PendingCtrl'
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
                    otherwise({
                        redirectTo: '/process'
                    });

                // configure $http to use the new Promise interface
                $httpProvider.useLegacyPromiseExtensions(false);
                $httpProvider.defaults.useXDomain=true;
                $httpProvider.defaults.cache = false;

                // configure authentication interceptors
                $httpProvider.interceptors.push('errorInterceptor');
                $httpProvider.interceptors.push('authInterceptor');

                authProvider.auth = auth;
                authProvider.addIgnorePath(/img\//);
            }]
    );
})(angular);
