define(['angular', 'services/process-service'],

    function (angular) {

        'use strict';

        function instanceDocumentsCtrl($scope, $routeParams, $location, processService, config) {

            var instanceId = $routeParams['instanceId'];

            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

            // get the process instance docuemnts
            processService.getDocumentsByInstance(instanceId).then(
                // success callback
                function (response) {
                    $scope.documents = response.data;
                },
                // error callback
                function (response) {
                    exceptionModal(response);
                });

            $scope.back = function () {
                $location.path('/instance/' + instanceId);
            };

            $scope.getDateString = function (time) {
                var date = new Date(time);
                return date.toLocaleString();
            };

            /**
             * @memberof InstanceDocumentsCtrl
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
            }
        }

        angular.module('wfWorkspaceControllers').controller('InstanceDocumentsCtrl', ['$scope', '$routeParams', '$location', 'processService', 'CONFIG', instanceDocumentsCtrl]);
    }
);
