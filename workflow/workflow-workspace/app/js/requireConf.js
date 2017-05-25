var shimDef = {
	'angular': {
		exports: 'angular',
		init: function () {

			/* create services module */
			angular.module('wfWorkspaceServices', []);

			/* create controllers module */
			angular.module('wfWorkspaceControllers', []);

			/* create directives module */
			angular.module('wfWorkspaceDirectives', []);

			angular.module("ngSanitize", ["ngSanitize"]);
		}
	},
	'angular-translate-loader-static-files': {
		deps: ['angular', 'angular-translate.min']
	},
	'angular-translate.min': {
		deps: ['angular']
	},
	'angular-material-datetimepicker.min': {
		deps: ['angular']
	}
};

var conf = {
	baseUrl: 'js/',
	paths: {
		app: 'app',
		angular: 'lib',
		controllers: "controllers",
		directives: "directives"
	},
	shim: shimDef
};

requirejs.config(conf);

// Start loading the main app file. Put all of your application logic in there.
//requirejs(['app']);

require(["app"], function (app) {
	app.initialize();
});

