module.exports = function (grunt) {

	grunt.initConfig({
		pkg: grunt.file.readJSON('package.json'),

		requirejs: {
			compile: {
				options: {
					optimize: "uglify2",
					uglify2: {
						mangle: false
					},
					baseUrl: 'app/js',
					paths: {
						app: 'app',
						angular: 'lib',
						controllers: "controllers",
						directives: "directives"
					},
					name: "almond",
					wrap: {
						startFile: 'app/js/start.frag',
						endFile: 'app/js/end.frag'
					},
					shim: {
						'angular': {
							exports: 'angular',
							init: function () {
								angular.module('wfWorkspaceServices', []);
								angular.module('wfWorkspaceControllers', []);
								angular.module('wfWorkspaceDirectives', []);
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
					},
					include: ['app', 'angular-translate-loader-static-files', 'angular-material-datetimepicker.min'],
					out: 'app/workflow-workspace-min.js'
				}
			}
		},

		// bower modules concatenation
		bower_concat: {
			all: {
				dest: {
					'js': 'app/js/lib.js',
					'css': 'app/css/lib.css'
				},
				callback: function (mainFiles, component) {
					return mainFiles.map(function (filepath) {
						// Use minified files if available
						var min = filepath.replace(/\.js$/, '.min.js');
						var cssMin = filepath.replace(/\.css$/, '.min.css');

						var returnFile;

						if (filepath.indexOf(".js") > 0) {
							returnFile = grunt.file.exists(min) ? min : filepath;

						} else if (filepath.indexOf(".css") > 0) {
							returnFile = grunt.file.exists(cssMin) ? cssMin : filepath;
						}

						return returnFile;
					});
				}
			}
		}
	});

	grunt.loadNpmTasks('grunt-contrib-requirejs');
	grunt.loadNpmTasks('grunt-bower-concat');

	grunt.registerTask('libPrepare', ['bower_concat:all']);
	grunt.registerTask('default', ['libPrepare', 'requirejs']);
};