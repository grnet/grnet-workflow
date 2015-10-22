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
        }
    });

    // grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-open');
    grunt.loadNpmTasks('grunt-contrib-connect');
    grunt.loadNpmTasks('grunt-bower-concat');

    grunt.registerTask('libPrepare', ['bower_concat:all']);


    // grunt.registerTask('doc', ['clean:doc', 'jsdoc', 'connect']);
};