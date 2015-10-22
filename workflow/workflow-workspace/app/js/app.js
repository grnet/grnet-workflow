/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    /* workflow workspace App Module */
    var wfworkspaceApp = angular.module('wfworkspaceApp', [
        'ngRoute',
        'ngMaterial',
        'wfworkspaceControllers',
        'wfworkspaceServices',
        'nlkDirectives'
    ]);

    wfworkspaceApp.config(['$routeProvider', '$httpProvider', '$mdThemingProvider',
        function ($routeProvider, $httpProvider, $mdThemingProvider) {
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
                otherwise({
                    redirectTo: '/task'
                });

            // configure $http to use the new Promise interface
            $httpProvider.useLegacyPromiseExtensions(false);

            // configure theming
            $mdThemingProvider.theme('default')
                .primaryPalette('purple')
                .accentPalette('amber');

        }])
        .constant('CONFIG', {
            'WORKFLOW_SERVICE_ENTRY': 'http://localhost:8080/workflow-engine/api',
            'AVATARS_PATH': 'img/avatars/',
            'DEFAULT_AVATAR': 'like.svg'
        });

    /* create controllers module */
    angular.module('wfworkspaceControllers', []);

    /* create directives module */
    angular.module('nlkDirectives', []);

    /* create services module */
    angular.module('wfworkspaceServices', []);

})(angular);
