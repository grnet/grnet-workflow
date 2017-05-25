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

                });

            $scope.back = function () {
                $location.path('/instance/' + instanceId);
            };

            $scope.getDateString = function (time) {
                var date = new Date(time);
                return date.toLocaleString();
            };

        }

        angular.module('wfWorkspaceControllers').controller('InstanceDocumentsCtrl', ['$scope', '$routeParams', '$location', 'processService', 'CONFIG', instanceDocumentsCtrl]);
    }
);
