/**
 * @author nlyk
 */
(function (angular) {

    'use strict';
    
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // system constants
    var WORKFLOW_SERVICE_ENTRY = 'http://10.0.0.156:8085/workflow-engine/api';
    var HOME_URL = 'http://10.0.0.156/workflow-workspace/index.html';
    var WORKFLOW_DOCUMENTS_URL = 'http://10.0.0.156:8085/workflow-engine/document/';
    var AVATARS_PATH = 'img/avatars/';
    var DEFAULT_AVATAR = 'like.svg';
    var MAP_CENTER_LAT = 38.037496;
    var MAP_CENTER_LNG = 23.836321;
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    
    /* create controllers module */
    angular.module('wfworkspaceControllers', []);

    /* create directives module */
    angular.module('nlkDirectives', []);
    
    /* create directives module */
    angular.module('wfDirectives', []);

    /* create services module */
    angular.module('wfworkspaceServices', []);
    
    /* workflow workspace App Module */
    var wfworkspaceApp = angular.module('wfworkspaceApp', [
        'ngRoute',
        'ngMaterial',
        'wfworkspaceControllers',
        'wfworkspaceServices',
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

                angular.bootstrap(document, ["wfworkspaceApp"]);
            })
            .error(function () {
                alert("Failed to Authenticate!");
                window.location.reload();
            });
    });
    
    /*
     * Authentication interceptor
     */
    wfworkspaceApp.factory('authInterceptor', ['$q', 'auth', function ($q, authProvider) {
        return {
            request: function (config) {
                return authProvider.authInterceptor($q, config);
            }
        };
    }]);
    
    /*
     * request interceptor - error handling
     */
    wfworkspaceApp.factory('errorInterceptor', ['$q', 'auth', function ($q, authProvider) {
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
    wfworkspaceApp
		    .constant('CONFIG', {
		        'WORKFLOW_SERVICE_ENTRY': WORKFLOW_SERVICE_ENTRY,
		        'WORKFLOW_DOCUMENTS_URL': WORKFLOW_DOCUMENTS_URL,
		        'AVATARS_PATH': AVATARS_PATH,
		        'DEFAULT_AVATAR': DEFAULT_AVATAR,
		        'MAP_CENTER_LAT': MAP_CENTER_LAT,
		        'MAP_CENTER_LNG': MAP_CENTER_LNG
		    })
		    
		    .config(['$routeProvider', '$httpProvider', '$mdThemingProvider','authProvider', '$translateProvider',
		        function ($routeProvider, $httpProvider, $mdThemingProvider,authProvider, $translateProvider) {
		    	
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
		                when('/activity', {
		                    templateUrl: 'views/task-activity.html',
		                    controller: 'TaskActivityListCtrl'
		                }).
		                when('/startform/:mode/:instanceId', {
		                    templateUrl: 'views/print-start-form.html',
		                    controller: 'PrintStartFormCtrl'
		                }).
		                otherwise({
		                    redirectTo: '/task'
		                });
		            
	                // configure $http to use the new Promise interface
	                $httpProvider.useLegacyPromiseExtensions(false);
	                $httpProvider.defaults.useXDomain=true;

	                // configure authentication interceptors
	                $httpProvider.interceptors.push('errorInterceptor');
	                $httpProvider.interceptors.push('authInterceptor');

	                authProvider.auth = auth;
	                authProvider.addIgnorePath(/img\//);
		
		            // configure theming
		            $mdThemingProvider.theme('default')
		                .primaryPalette('purple')
		                .accentPalette('amber');
		
		        }]);

})(angular);
