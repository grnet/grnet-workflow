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
                    exceptionModal(response);
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

            /**
             * @memberof DocumentsCtrl
             * @desc Displays a modal panel, showing the exception message
             *
             * @param {any} response
             * @param {event} event
             */
            function exceptionModal(response, event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {
                        $scope.error = response.data;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                    },

                    templateUrl: 'templates/exception.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: event,
                    clickOutsideToClose: false
                })
            };
        }]
    );
})(angular);