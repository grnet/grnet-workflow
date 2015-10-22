'use strict';

/* https://github.com/angular/protractor/blob/master/docs/toc.md */

describe('workflow::manager', function () {

    var hasClass = function (element, cls) {
        return element.getAttribute('class').then(function (classes) {
            return classes.split(' ').indexOf(cls) !== -1;
        });
    };

    describe(
        'process definition list',
        function () {

            beforeEach(function () {
                browser.get('app/index.html');
            });

            it('should open nav panel on menu button click', function() {
                var menuButton = element(by.id('menu-button'));
                menuButton.click();
                expect(hasClass(element(by.id('navbar')), 'md-closed')).toBe(false);
            });

        }
    );


    //it('should automatically redirect to /view1 when location hash/fragment is empty', function () {
    //    browser.get('index.html');
    //    expect(browser.getLocationAbsUrl()).toMatch("/view1");
    //});
    //
    //
    //describe('view1', function () {
    //
    //    beforeEach(function () {
    //        browser.get('index.html#/view1');
    //    });
    //
    //
    //    it('should render view1 when user navigates to /view1', function () {
    //        expect(element.all(by.css('[ng-view] p')).first().getText()).
    //            toMatch(/partial for view 1/);
    //    });
    //
    //});
    //
    //
    //describe('view2', function () {
    //
    //    beforeEach(function () {
    //        browser.get('index.html#/view2');
    //    });
    //
    //
    //    it('should render view2 when user navigates to /view2', function () {
    //        expect(element.all(by.css('[ng-view] p')).first().getText()).
    //            toMatch(/partial for view 2/);
    //    });
    //
    //});
});
