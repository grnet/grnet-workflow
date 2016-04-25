(function (angular) {

    'use strict';

    angular.module('wfworkspaceControllers')
        .controller('DocumentsCtrl', ['$scope', '$http', '$routeParams', '$location', 'processService', 'CONFIG',

            /**
             * Controller for the documents view
             *
             * @param $scope
             * @param $http
             * @param $routeParams
             * @param {ProcessService} processService
             * @param config
             */
                function ($scope, $http, $routeParams, $location, processService, config) {

        		var pageId = $routeParams['pageId'];
                var taskId = $routeParams['taskId'];
                
                $scope.documents = null;
                                
                $scope.documentPath = config.WORKFLOW_DOCUMENTS_URL;
                
                // get the process instance docuemnts
                processService.getProcessInstanceDocuments(taskId)
                    .then(
                    // success callback
                    function (response) {
                        
                    	$scope.documents = response.data;
                    },
                    // error callback
                    function (response) {
                    }
                );
                
                $scope.back = function() {
                	
                	$location.path('/' + pageId + '/' + taskId);
                };
                
                $scope.getDateString = function (time) {
                    
                	var date = new Date(time);
                	
                	return date.toLocaleString();
                };
            }]
    );

})(angular);
