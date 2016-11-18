module.exports = function (grunt) {

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        // clean: {
        //     doc: ['doc']
        // },
        open: {
            doc: {
                path: 'http://127.0.0.1:8888/doc',
                app: 'Google Chrome'
            }
        },
        connect: {
            server: {
                options: {
                    port: '8888',
                    keepalive: true,
                    base: '.',
                    open: 'http://127.0.0.1:8888/doc'
                }
            }
        },

        // bower modules concatenation
        bower_concat: {
            all: {
                options: {separator: ';\n'},
                dest: "app/js/lib.js",
                cssDest: "app/css/lib.css",
                mainFiles: {
                    bootstrap: [ 'dist/css/bootstrap.min.css', 'dist/js/bootstrap.min.js' ]
                },
                callback: function (mainFiles, component) {
                    return mainFiles.map(function(currentFile, fileIndex, filesArray){
                        var min = currentFile.replace(/\.js$/, '.min.js');
                        var newFile = grunt.file.exists(min) ? min : currentFile;

                        console.log("adding file: " + newFile);
                        return newFile;
                    });
                }
            }
        },
		jsdoc: {
			dist: {
			  src: [ 'app/js/controllers', 'app/js/services', 'app/js/directives', 'app/directives/*', 'app/js/types.js' ],
			  options: {
				destination: 'dist/docs',
				configure: 'node_modules/angular-jsdoc/common/conf.json',
				template: 'node_modules/angular-jsdoc/angular-template',
				tutorial: 'tutorials',
				readme: './README.md'
			  }
			}
		}
    });

    // grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-open');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-bower-concat');
	grunt.loadNpmTasks('grunt-jsdoc');

    grunt.registerTask('libPrepare', ['bower_concat:all']);
	
	grunt.registerTask('doc', 'jsdoc');


    // grunt.registerTask('doc', ['clean:doc', 'jsdoc', 'connect']);
};