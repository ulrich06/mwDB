module.exports = function (grunt) {
    grunt.initConfig({
        // ----- Environment
        // read in some metadata from project descriptor
        project: grunt.file.readJSON('package.json'),
        // define some directories to be used during build
        dir: {
            // location where TypeScript source files are located
            "source_ts": "target/generated-sources/src/main",
            // location where TypeScript/Jasmine test files are located
            "source_test_ts": "target/generated-test-sources/src/main",
            // location where all build files shall be placed
            "target": "target",
            // location to place (compiled) javascript files
            "target_js": "target/classes",
            // location to place (compiles) javascript test files
            "target_test_js": "target/test-classes",
            // location to place documentation, etc.
            "target_report": "target/report"
        },
        // ----- TypeScript compilation
        //  See https://npmjs.org/package/grunt-typescript
        typescript: {
            // Compiles the code into a single file. Also generates a typescript declaration file
            compile: {
                src: ['<%= dir.source_ts %>/**/*.ts'],
                dest: '<%= dir.target_js %>/<%= project.name %>.js',
                options: {
                    basePath: '<%= dir.source_ts %>',
                    target: 'es5',
                    declaration: true,
                    comments: true
                }
            },
            // Compiles the tests.
            compile_test: {
                src: ['<%= dir.source_test_ts %>/**/*.ts'],
                dest: '<%= dir.target_test_js %>',
                options: {
                    base_path: '<%= dir.source_test_ts %>',
                    target: 'es5'
                }
            }
        },
        concatDev: {
            options: {
                separator: '\n'
            },
            dist: {
                src: [
                    '<%= dir.target_test_js %>/generated-sources/src/main/jre.js',
                    '<%= dir.target_test_js %>/generated-sources/src/main/api.js',
                    '<%= dir.target_test_js %>/generated-sources/src/main/core.js',
                    '<%= dir.target_test_js %>/generated-test-sources/src/main/junit.js',
                    '<%= dir.target_test_js %>/generated-test-sources/src/main/test.js',
                    '<%= dir.source_test_ts %>/testsRunnerDev.js'
                ],
                dest: '<%= dir.target_test_js %>/allDev.js'
            }
        },
        concat: {
            options: {
                separator: '\n'
            },
            dist: {
                src: [
                    '<%= dir.target_test_js %>/generated-sources/src/main/jre.js',
                    '<%= dir.target_test_js %>/generated-sources/src/main/api.js',
                    '<%= dir.target_test_js %>/generated-sources/src/main/core.js',
                    '<%= dir.target_test_js %>/generated-test-sources/src/main/junit.js',
                    '<%= dir.target_test_js %>/generated-test-sources/src/main/test.js',
                    '<%= dir.source_test_ts %>/testsRunner.js'
                ],
                dest: '<%= dir.target_test_js %>/all.js'
            },
            dev: {
                src: [
                    '<%= dir.target_test_js %>/generated-sources/src/main/jre.js',
                    '<%= dir.target_test_js %>/generated-sources/src/main/api.js',
                    '<%= dir.target_test_js %>/generated-sources/src/main/core.js',
                    '<%= dir.target_test_js %>/generated-test-sources/src/main/junit.js',
                    '<%= dir.target_test_js %>/generated-test-sources/src/main/test.js',
                    '<%= dir.source_test_ts %>/testsRunnerDev.js'
                ],
                dest: '<%= dir.target_test_js %>/allDev.js'
            }
        },
        jasmine_nodejs: {
            test: {
                // target specific options
                options: {
                    specNameSuffix: "all.js",
                    stopOnFailure: false,
                    reporters: {
                        console: {
                            colors: 2,        // (0|false)|(1|true)|2
                            cleanStack: 1,       // (0|false)|(1|true)|2|3
                            verbosity: 2,        // (0|false)|1|2|3|(4|true)
                            listStyle: "indent", // "flat"|"indent"
                            activity: true
                        }
                    }
                },
                // spec files
                specs: ['<%= dir.target_test_js %>/**'],
                helpers: []
            }
        }
    })
    ;
    // ----- Setup tasks

    grunt.loadNpmTasks('grunt-typescript');
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-jasmine-nodejs');
    grunt.registerTask('default', ['typescript:compile', 'typescript:compile_test', 'concat', 'jasmine_nodejs']);
};