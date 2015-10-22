'use strict';

/* jasmine specs for controllers go here */

describe('Unit: Home Controller', function () {

    // Load the module with MainController
    beforeEach(module('hello'));

    var ctrl, scope;
    var $http;
    var createController;

    // inject the $controller and $rootScope services
    // in the beforeEach block
    beforeEach(inject(function ($controller, $rootScope, $injector) {

        $http = $injector.get('$httpBackend');
        $http.when('GET', 'http://localhost:8080/workflow-engine/api/process')
            .respond([{id: 'process 1'}, {'id': 'process 2'}]);

        // Create a new scope that's a child of the $rootScope
        scope = $rootScope.$new();
        // Create the controller
        ctrl = $controller('home', {
            $scope: scope
        });

        createController = function() {
            return $controller('home', {'$scope' : scope });
        };
    }));

    it("return process definitions", function () {
        $http.expectGET('http://localhost:8080/workflow-engine/api/process').respond(200);
        var newCtrl = createController();
        $http.flush();
        expect(scope.workflowDefinitions.length).toBe(2);
    });

});
