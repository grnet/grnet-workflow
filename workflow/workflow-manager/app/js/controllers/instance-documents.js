(function (angular) {

    'use strict';

    angular.module('wfmanagerControllers').controller('InstanceDocumentsCtrl', ['$scope', '$routeParams', '$location', 'processService', 'CONFIG',
        /**
         * @name InstanceDocumentsCtrl
         * @ngDoc controllers
         * @memberof wfmanagerControllers
         * 
         * @desc Controller used in Instance's documents view
         */
        function ($scope, $routeParams, $location, processService, config) {

            var instanceId = $routeParams['instanceId'];
            $scope.showProgress = true;
            $scope.documents = null;
            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

            // get the process instance docuemnts
            processService.getDocumentsByInstance(instanceId).then(
                // success callback
                function (response) {
                    $scope.documents = response.data;
                },
                // error callback
                function (response) {

                }).finally(function () {
                    $scope.showProgress = false;
                });

            /**
             * @memberof InstanceDocumentsCtrl
             * @desc Redirects back to instance
             */
            $scope.back = function () {
                $location.path('/instance/' + instanceId);
            };

            /**
             * @memberof InstanceDocumentsCtrl
             * @desc Converts a given date to string
             * 
             * @param {Number} time
             * @returns {String} - The converted date
             */
            $scope.getDateString = function (time) {
                var date = new Date(time);
                return date.toLocaleString();
            };
            
        }]);
})(angular);
