/**
 * @memberOf workflow-workspace
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers').controller('DocumentsCtrl', ['$scope', '$routeParams', '$location', 'processService', 'CONFIG',
        /**
         * @name DocumentsCtrl
         * @ngDoc controllers
         * @memberof wfworkspaceControllers
         * @desc Controller for the documents view
         */
        function ($scope, $routeParams, $location, processService, config) {

            var pageId = $routeParams['pageId'];
            var taskId = $routeParams['taskId'];
            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

            $scope.documents = null;
            $scope.showProgress = true;

            // get the process instance docuemnts
            processService.getProcessInstanceDocuments(taskId).then(
                // success callback
                function (response) {
                    $scope.documents = response.data;
                },
                // error callback
                function (response) {
                }

            ).finally(function () {
                $scope.showProgress = false;
            });

            /**
             * @memberof DocumentsCtrl
             * 
             * @desc Returns back to task
             * 
             */
            $scope.back = function () {
                $location.path('/' + pageId + '/' + taskId);
            };

            /**
             * @memberof DocumentsCtrl
             * 
             * @param {Date} time
             * @returns The given date to {String} 
             */
            $scope.getDateString = function (time) {
                var date = new Date(time);
                return date.toLocaleString();
            };
        }]
    );
})(angular);