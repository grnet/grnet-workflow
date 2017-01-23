(function (angular) {

    angular.module('wfmanagerControllers').controller('addDefinitionController', ['$scope', '$mdDialog', '$location', 'processService', 'process',
        /**
         * @name addDefinitionController
         * @ngDoc controllers
         * @memberof wfmanagerControllers
         * @desc Submit a new BPMN file. If a process is passed as argument, then we'll attempt to create a new version. Otherwise a new process definition will be created.
         * 
         */
        function ($scope, $mdDialog, $location, processService, process) {

            $scope.process = process;
            $scope.inProgress = false;
            $scope.exception = false;

            /**
             * @memberOf addDefinitionController
             * @desc Hides the opened modal panel
             * 
             */
            $scope.hide = function () {
                $mdDialog.hide();
            };

            /**
             * @memberOf addDefinitionController
             * @desc Cancel the opened modal panel
             */
            $scope.cancel = function () {
                $mdDialog.cancel();
            };

            /**
             * @memberOf addDefinitionController
             * @desc A function that been called after user uploads the BPMN files the confirms its action
             */
            $scope.answer = function () {
                $scope.inProgress = true;

                // Create new Process Definition
                if (process === null) {
                    processService.createProcess($scope.bpmnFile).then(
                        // success callback
                        function (response) {
                            $mdDialog.hide(response);
                            $location.path('/process/' + response.data.id);
                        },
                        // error callback
                        function (response) {
                            $scope.exception = true;
                            $scope.exceptionMessage = response.data;
                        }

                    ).finally(function () {
                        $scope.inProgress = false;
                    });

                    // Add Version to a Process Definition    
                } else {
                    processService.createProcessVersion(process.id, $scope.bpmnFile, $scope.justification).then(
                        // success callback
                        function (response) {
                            $mdDialog.hide(response);
                            var newVersion = response.data;
                            // add the new version to the open process definition
                            $scope.process.processVersions.unshift(newVersion);
                            $scope.process.activeDeploymentId = newVersion.deploymentId;
                        },
                        // error callback
                        function (response) {
                            $scope.exception = true;
                            $scope.exceptionMessage = response.data;
                        }

                    ).finally(function () {
                        $scope.inProgress = false;
                    });
                }
            };

        }]);
})(angular);