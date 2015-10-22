/**
 * @author nlyk
 */
(function () {

    'use strict';

    angular.module('wfmanagerControllers')
        .controller('ProcessListCtrl', ['$scope', '$http', '$location', '$mdDialog', 'processService', 'CONFIG',

            function ($scope, $http, $location, $mdDialog, processService, config) {

                $scope.workflowDefinitions = null;
                $scope.imagePath = config.AVATARS_PATH;

                /**
                 * Get all process definitions
                 */
                processService.getProcesses().then(
                    // success callback
                    function (response) {
                        // set default icon
                        $scope.workflowDefinitions = response.data.map(
                            function (def) {
                                def.icon = def.icon || config.DEFAULT_AVATAR;
                                return def;
                            });
                    }
                );

                /**
                 * Show a dialog for uploading a new BPMN file to create a new workflow definition
                 */
                $scope.addProcess = function () {
                    $mdDialog.show({
                        controller: 'addDefinitionController',
                        templateUrl: 'templates/adddefinition.tmpl.html',
                        parent: document.body,
                        targetEvent: event,
                        clickOutsideToClose: true,
                        locals: {'process': null}
                    });
                };

                /**
                 * Returns true if the selected version is active
                 * @param {WorkflowDefinition} process
                 */
                $scope.isActive = function (process) {
                    return processService.isProcessActive(process);
                };

                /**
                 * Supports navigation
                 * @param {string} path
                 */
                $scope.goTo = function (path) {
                    $location.path(path);
                };
            }]
    );

})(angular);
