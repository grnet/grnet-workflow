(function (angular) {

    'use strict';

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // system constants
    var WORKFLOW_SERVICE_ENTRY = 'http://localhost:12080/workflow-engine/api';
    var HOME_URL = 'http://localhost:63342/workflow-manager/app/index.html';
    var AVATARS_PATH = 'img/avatars/';
    var DEFAULT_AVATAR = 'like.svg';
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /* create controllers module */
    angular.module('wfmanagerControllers', []);

    /* create directives module */
    angular.module('nlkDirectives', []);

    /* create services module */
    angular.module('wfmanagerServices', []);

    /* workflow manager App Module */
    var wfmanagerApp = angular.module('wfmanagerApp', [
        'ngRoute',
        'ngMaterial',
        'wfmanagerControllers',
        'wfmanagerServices',
        'nlkDirectives'
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
            'AVATARS_PATH': AVATARS_PATH,
            'DEFAULT_AVATAR': DEFAULT_AVATAR
        })
        .config(['$routeProvider', '$httpProvider', 'authProvider',
            function ($routeProvider, $httpProvider, authProvider) {
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
                    otherwise({
                        redirectTo: '/process'
                    });

                // configure $http to use the new Promise interface
                $httpProvider.useLegacyPromiseExtensions(false);

                // configure authentication interceptors
                $httpProvider.interceptors.push('errorInterceptor');
                $httpProvider.interceptors.push('authInterceptor');

                authProvider.auth = auth;
                authProvider.addIgnorePath(/img\//);
            }]
    );
})(angular);
