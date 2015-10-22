(function () {

    'use strict';

    /* workflow manager App Module */
    var wfmanagerApp = angular.module('wfmanagerApp', [
        'ngRoute',
        'ngMaterial',
        'wfmanagerControllers',
        'wfmanagerServices',
        'nlkDirectives'
    ]);

    wfmanagerApp.config(['$routeProvider', '$httpProvider',
        function ($routeProvider, $httpProvider) {
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

        }])
        .constant('CONFIG', {
            'WORKFLOW_SERVICE_ENTRY': 'http://localhost:8080/workflow-engine/api',
            'AVATARS_PATH': 'img/avatars/',
            'DEFAULT_AVATAR': 'like.svg'
        });

    /* create controllers module */
    angular.module('wfmanagerControllers', []);

    /* create directives module */
    angular.module('nlkDirectives', []);

    /* create services module */
    angular.module('wfmanagerServices', []);

})();
