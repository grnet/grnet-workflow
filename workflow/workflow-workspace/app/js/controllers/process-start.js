define(['angular', 'services/process-service'],

    function (angular) {

        'use strict';

        function processStartCtrl($scope, $routeParams, $mdDialog, $location, $filter, $mdToast, processService, authProvider, config) {

            var processId = $routeParams['processId'];
            $scope.supervisors = null;
            $scope.showProgress = true;

            $scope.process = null;
            $scope.instance = {
                title: "",
                supervisor: null
            };

            $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;

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

            ).finally(function () {
                $scope.showProgress = false;
            });

            processService.getSupervisors()
                .then(
                // success callback
                function (response) {
                    $scope.supervisors = response.data;
                },
                // error callback
                function (response) {
                    exceptionModal(response);
                }
                );

            /**
             * get process form data and start a new process instance
             */
            $scope.startProcess = function (event) {
                $scope.showProgress = true;

                $scope.instance.processForm = $scope.process.processForm;

                processService.startProcess(processId, $scope.instance)
                    .then(
                    // success callback
                    function () {
                        $scope.showProgress = false;
                        $mdDialog.show($mdDialog.alert()
                            .parent(document.body)
                            .clickOutsideToClose(true)
                            .title($filter('translate')('executionStarted'))
                            .content($filter('translate')('executionStarted'))
                            .ok($filter('translate')('confirm'))
                        ).then(
                            function () {
                                var isAdmin = authProvider.getRoles().indexOf("ROLE_Admin") >= 0 ? true : false;
                                var isSupervisor = authProvider.getRoles().indexOf("ROLE_Supervisor") >= 0 ? true : false;

                                if(isAdmin || isSupervisor)
                                    $location.path('/assign');
                                else
                                    $location.path('/task');
                            });
                    },
                    // error callback
                    function (response) {
                        exceptionModal(response);
                        $scope.showProgress = false;
                    }
                    );
            };

            $scope.$on('zoomException', function ($event, error) {
                $mdToast.show(
                    $mdToast.simple()
                        .textContent(error.message)
                        .hideDelay(3500)
                        .position("top, left")
                        .parent(document.getElementById("map"))
                );
            });


            $scope.$on('postCodeException', function ($event, error) {
                $mdToast.show(
                    $mdToast.simple()
                        .textContent(error.message)
                        .hideDelay(3500)
                        .position("top, left")
                        .parent(document.getElementById("map"))
                );
            });

            /**
             * Gets the address from map and set it to address property
             */
            $scope.$on('setAddress', function (event, value) {
                for (var i = 0; i < $scope.process.processForm.length; i++) {
                    if ($scope.process.processForm[i].id.indexOf("address") > -1) {
                        $scope.process.processForm[i].value = value.address;
                    }
                }
            });

            function exceptionModal(response, $event) {
                $mdDialog.show({
                    controller: function ($scope, $mdDialog) {
                        $scope.error = response.data;

                        $scope.cancel = function () {
                            $mdDialog.hide();
                        };
                    },

                    templateUrl: 'templates/exception.tmpl.html',
                    parent: angular.element(document.body),
                    targetEvent: $event,
                    clickOutsideToClose: false
                });
            }
        }

        angular.module('wfWorkspaceControllers').controller('ProcessStartCtrl', ['$scope', '$routeParams', '$mdDialog', '$location',
            '$filter', '$mdToast', 'processService', 'auth', 'CONFIG', processStartCtrl]);

    }
);