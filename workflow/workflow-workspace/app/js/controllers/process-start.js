/**
 * @author nlyk
 * Created by nlyk on 6/10/2015.
 */
(function (angular) {

    angular.module('wfworkspaceControllers').controller('ProcessStartCtrl',
        ['$scope', '$routeParams', '$mdDialog', '$location', 'processService',

            /**
             *
             * @param $scope
             * @param $routeParams
             * @param $mdDialog
             * @param $location
             * @param {ProcessService} processService
             */
                function ($scope, $routeParams, $mdDialog, $location, processService) {

                var processId = $routeParams['processId'];

                $scope.process = null;

                // get the process definition
                processService.getProcessMetadata(processId)
                    .then(
                    // success callback
                    function (response) {
                        $scope.process = response.data;
                    },
                    // error callback
                    function (response) {
                    }
                );

                /**
                 * get process form data and start a new process instance
                 */
                $scope.startProcess = function () {

                    processService.startProcess(processId, $scope.process.processForm)
                        .then(
                        // success callback
                        function () {
                            $mdDialog.show($mdDialog.alert()
                                    .parent(document.body)
                                    .clickOutsideToClose(true)
                                    .title('New process instance started')
                                    .content('The process started!')
                                    .ok('Ok')
                            ).then(
                                function () {
                                    $location.path('/assign');
                                }
                            );
                        },
                        // error callback
                        function (response) {
                            $mdDialog.show($mdDialog.alert()
                                    .parent(document.body)
                                    .clickOutsideToClose(true)
                                    .title('Start process failed')
                                    .content('Failed to start the process<br><div class="md-warn">' +
                                    response.data.message +
                                    '</div>')
                                    .ok('Ok')
                            );
                        }
                    );
                }

            }]);

})(angular);