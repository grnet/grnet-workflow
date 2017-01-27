(function (angular) {

    'use strict';

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // system constants
    var WORKFLOW_SERVICE_ENTRY = 'http://localhost:8080/engine/api';
    var HOME_URL = 'http://localhost:80/workflow-manager/index.html';
    var WORKFLOW_DOCUMENTS_URL = 'http://localhost:8080/grnet-workflow-engine/document/';
    var AVATARS_PATH = 'img/avatars/';
    var DEFAULT_AVATAR = 'like.svg';
    var MAP_CENTER_LAT = 38.037496;
    var MAP_CENTER_LNG = 23.836321;

    // create controllers module
    angular.module('wfmanagerControllers', ['angularTrix', 'ngSanitize']);

    // create directives module
    angular.module('wfDirectives', []);

    // create services module
    angular.module('wfmanagerServices', []);

    // create the App module
    var wfmanagerApp = angular.module('wfmanagerApp', [
        'ngRoute',
        'ngMaterial',
        'wfmanagerControllers',
        'wfmanagerServices',
        'wfDirectives',
        'pascalprecht.translate',
        'ngMaterialDatePicker'
    ]);

    var auth = {};

    /**
     * @memberOf config
     * @desc Configure the SSO
     */
    angular.element(document).ready(function () {

        var keycloakAuth = new Keycloak('keycloak.json');
        auth.loggedIn = false;

        keycloakAuth.init({ onLoad: 'login-required' })
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
                if (response.status === 400 || response.status === 500) {
                    return $q.reject(response);
                }
                return authProvider.authErrorHandler($q, response);
            }
        };
    }]);

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
                    when('/history', {
                        templateUrl: 'views/history.html',
                        controller: 'HistoryCtrl',
                        reloadOnSearch: false
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
                        controller: 'PendingCtrl',
                        reloadOnSearch: false
                    }).
                    when('/inprogress', {
                        templateUrl: 'views/in-progress.html',
                        controller: 'InProgressCtrl',
                        reloadOnSearch: false
                    }).
                    when('/activity', {
                        templateUrl: 'views/activity.html',
                        controller: 'ActivityCtrl',
                        reloadOnSearch: false
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
                    otherwise({
                        redirectTo: '/process'
                    });

                // configure $http to use the new Promise interface
                $httpProvider.useLegacyPromiseExtensions(false);
                $httpProvider.defaults.useXDomain = true;
                $httpProvider.defaults.cache = false;

                // configure authentication interceptors
                $httpProvider.interceptors.push('errorInterceptor');
                $httpProvider.interceptors.push('authInterceptor');

                authProvider.auth = auth;
                authProvider.addIgnorePath(/img\//);
            }]
        );
})(angular);
