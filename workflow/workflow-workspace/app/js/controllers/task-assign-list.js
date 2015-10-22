/**
 * @author nlyk
 */
(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('TaskAssignListCtrl', ['$scope', '$http', '$location', '$mdDialog', 'processService', 'CONFIG',

            /**
             * Controller for the tasks-to-assign view
             *
             * @param $scope
             * @param $http
             * @param $location
             * @param $mdDialog
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $http, $location, $mdDialog, processService, config) {

                $scope.processesSupervised = null;
                $scope.processesSelected = null;
                $scope.tasksToAssign = null;
                $scope.imagePath = config.AVATARS_PATH;

                // get supervised processes
                processService.getSupervisedProcesses()
                    .then(
                    // success callback
                    function (response) {
                        $scope.processesSupervised = response.data;
                    },
                    // error callback
                    function (response) {
                    }
                );

                $scope.$watch('processesSelected', function (newValue, oldValue) {
                    if (newValue !== oldValue) {

                        if (newValue == null || newValue.length == 0) {
                            $scope.tasksToAssign = null;
                            return;
                        }

                        // process selected changed
                        // get all tasks of unassigned tasks for these processes
                        processService.getUnassignedTasks($scope.processesSelected)
                            .then(
                            // success callback
                            function (response) {
                                console.log(response.data);

                                $scope.tasksToAssign = response.data;
                                for (var i = 0; i < $scope.tasksToAssign.length; i++) {
                                    $scope.tasksToAssign[i].process =
                                        getProcessById($scope.tasksToAssign[i].processId);
                                }
                            },
                            // error callback
                            function (response) {
                            }
                        );
                    }
                });

                /**
                 * Find the process using the Process Definition Id
                 * @param {string} defId
                 * @return {WfProcess}
                 */
                function getProcessByDefinitionId(defId) {
                    for (var i = 0; i < $scope.processesSupervised.length; i++) {
                        if (defId == $scope.processesSupervised[i].processDefinitionId) {
                            return $scope.processesSupervised[i];
                        }
                    }
                    return null;
                }

                /**
                 * Find the process by Id
                 * @param {number} id
                 * @return {WfProcess}
                 */
                function getProcessById(id) {
                    for (var i = 0; i < $scope.processesSupervised.length; i++) {
                        if (id == $scope.processesSupervised[i].id) {
                            return $scope.processesSupervised[i];
                        }
                    }
                    return null;
                }

            }]
    );

})(angular);
