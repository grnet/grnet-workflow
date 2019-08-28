define(['angular', 'services/process-service'],

    function (angular) {

        'use strict';

        function instanceDocumentsCtrl($scope, $routeParams, $location, $mdDialog, $window, $mdToast, $filter, processService, config) {

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

            $scope.goToDocument = function (documentId) {

                var path = $scope.documentPath + documentId;
                $window.open(path, "_blank");
            };

            $scope.copyLink = function (documentId) {

                var path = $scope.documentPath + documentId;

                var body = angular.element($window.document.body);
                var textarea = angular.element('<textarea/>');
                textarea.val(path);
                body.append(textarea);
                textarea[0].select();

                try {
                    var successful = document.execCommand('copy');

                    if (!successful) throw successful
                    if (successful) $scope.showToast(($filter('translate')('copiedToClipboard')));

                } catch (err) {
                    console.log(err);
                    $scope.showToast("Failed to copy link: " + path);
                }

                textarea.remove();
            }

            $scope.checkFileType = function (filename, type) {

                if (type === "any") {
                    return filename.indexOf("pdf") == -1 && filename.indexOf("xls") == -1 && filename.indexOf("xlsx") == -1;
                } else {
                    return filename.indexOf(type) > 0;
                }
            };

            $scope.toggleVersions = function (document) {
                document.isExpanded = !document.isExpanded;
            };

            $scope.showToast = function (message) {

                $mdToast.show(
                    $mdToast.simple()
                        .textContent(message)
                        .position("top")
                        .hideDelay(3500)
                );
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
                });
            }
        }

        angular.module('wfWorkspaceControllers').controller('InstanceDocumentsCtrl', ['$scope', '$routeParams', '$location', '$mdDialog', '$window', '$mdToast', '$filter', 'processService', 'CONFIG', instanceDocumentsCtrl]);
    }
);
