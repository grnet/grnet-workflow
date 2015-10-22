/**
 * Created by nlyk on 5/10/2015.
 * @author nlyk
 */
(function (angular) {
    angular.module('nlkDirectives').directive('nlkChecklist',
        function () {
            return {
                require: 'ngModel',
                restrict: 'E',
                replace: true,
                scope: {
                    ngModel: '=',
                    allLabel: '@',
                    allSelected: '=allSelected',
                    checklistModel: '=checklistModel',
                    checklistSelected: '=checklistSelected',
                    checklistValueField: '@checklistValueField',
                    expanded: '@expanded'
                },
                templateUrl: 'templates/nlkchecklist.tmpl.html',
                controller: function ($scope, $element) {

                    $scope.checklistValueField = $scope.checklistValueField || 'value';
                    $scope.expanded = ($scope.expanded === undefined) ? true : $scope.expanded;

                    $scope.allSelected = $scope.allSelected
                        || ($scope.checklistSelected || []).length === ($scope.checklistModel || []).length;

                    $scope.toggleExpand = function () {
                        $scope.expanded = $scope.expanded ? false : true;
                    };

                    $scope.selectionStatus = ($scope.checklistModel || []).map(function (item) {
                        return $scope.checklistSelected.indexOf(item[$scope.checklistValueField]) >= 0;
                    });

                    //if ($scope.checklistSelected == null) {
                    //    selectAll();
                    //}

                    $scope.$watch('allSelected', function (newValue, oldValue) {
                        if (newValue !== oldValue) {
                            if (newValue === true) {
                                selectAll();
                            } else {
                                deselectAll();
                            }
                        }
                    });

                    $scope.$watch('checklistModel', function (newValue, oldValue) {
                        if (newValue !== oldValue) {
                            update();
                        }
                    });

                    $scope.$watch('selectionStatus', function (newValue, oldValue) {
                        var selectedCount = (newValue || []).length === 0 ?
                            0 :
                            newValue.reduce(
                                function (a, b) {
                                    return a + (b ? 1 : 0)
                                });

                        var checkboxAll = $element[0].querySelector('.select-all .md-icon');

                        if (selectedCount === ($scope.checklistModel || []).length) {
                            $scope.allSelected = true;
                            if (checkboxAll) checkboxAll.classList.remove('third-state');
                        }
                        else if (selectedCount === 0) {
                            $scope.allSelected = false;
                            if (checkboxAll) checkboxAll.classList.remove('third-state');
                        }
                        else {
                            if (checkboxAll && !checkboxAll.classList.contains('third-state')) {
                                checkboxAll.classList.add('third-state');
                            }
                        }
                        $scope.checklistSelected = [];
                        for (var i = 0; i < newValue.length; i++) {
                            if (newValue[i]) {
                                $scope.checklistSelected.push($scope.checklistModel[i][$scope.checklistValueField]);
                            }
                        }

                    }, true);

                    update();

                    function selectAll() {
                        $scope.allSelected = true;
                        $scope.selectionStatus = ($scope.checklistModel || []).map(function () {
                            return true;
                        });
                    }

                    function deselectAll() {
                        $scope.allSelected = false;
                        $scope.selectionStatus = $scope.checklistModel.map(function () {
                            return false;
                        });
                    }


                    function update() {
                        $scope.checklistSelected = [];
                        $scope.selectionStatus = [];

                        if ($scope.allSelected) {
                            selectAll();
                        }
                        else {
                            deselectAll();
                        }
                    }

                }
            }
        }
    );
})(angular);
