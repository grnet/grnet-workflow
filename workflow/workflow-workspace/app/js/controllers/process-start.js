(function (angular) {
    angular.module('wfworkspaceControllers').controller('ProcessStartCtrl', ['$scope', '$routeParams', '$mdDialog', '$location', '$filter', 'processService', 'CONFIG',
        /**
         * @name ProcessStartCtrl
         * @ngDoc controllers
         * @memberof wfworkspaceControllers
         * 
         * @desc Controller used by Start Process view
         */
        function ($scope, $routeParams, $mdDialog, $location, $filter, processService, config) {

            var processId = $routeParams['processId'];
            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

            $scope.supervisors = null;
            $scope.showProgress = false;

            $scope.process = null;
            $scope.instance = {
                title: "",
                supervisor: null
            };

            // get the process definition
            processService.getProcessMetadata(processId).then(
                // success callback
                function (response) {
                    $scope.process = response.data;
                },
                // error callback
                function (response) {
                    exceptionModal(response);
                }
            );

            processService.getSupervisors().then(
                // success callback
                function (response) {
                    $scope.supervisors = response.data;
                }
            );

            /**
             * @memberof ProcessStartCtrl
             * @desc Starts a new instance using the user's input
             * 
             * @param {any} event
             */
            $scope.startProcess = function (event) {
                $scope.showProgress = true;
                $scope.instance.processForm = $scope.process.processForm;

                processService.startProcess(processId, $scope.instance).then(
                    // success callback
                    function () {
                        $mdDialog.show($mdDialog.alert()
                            .parent(document.body)
                            .clickOutsideToClose(true)
                            .title($filter('translate')('executionStarted'))
                            .content($filter('translate')('executionStarted'))
                            .ok($filter('translate')('confirm'))
                        ).then(
                            function () {
                                $location.path('/assign');
                            }
                            );
                    },
                    // error callback
                    function (response) {
                        exceptionModal(response);
                    }

                ).finally(function () {
                    $scope.showProgress = false;
                });
            };

            /**
             * @memberof ProcessStartCtrl
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
                });
            };

        }]);
})(angular);