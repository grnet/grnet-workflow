define(['angular'],

    function (angular) {

        function authenticationService() {

            var that = this;

            // the singleton Auth object with authentication information
            // handled by keycloak
            this.auth = null;
            this.ignorePath = [];

            this.addIgnorePath = function (path) {
                that.ignorePath.push(path);
            };

            this.$get = function () {
                /**
                 * @class AuthProvider
                 */
                return {
                    /**
                     * @name AuthProvider#getRoles
                     */
                    getRoles: function () {
                        return that.auth.authz.realmAccess.roles;
                    },

                    /**
                     * @name AuthProvider#logout
                     */
                    logout: function () {
                        that.auth.loggedIn = false;
                        that.auth.authz = null;
                        window.location = that.auth.logoutUrl;
                    },

                    /**
                     * Authentication request interceptor
                     * @param $q
                     * @param config
                     * @return {*}
                     *
                     * @name AuthProvider#authInterceptor
                     */
                    authInterceptor: function ($q, config) {

                        for (var i = 0; i < that.ignorePath.length; i++) {
                            if (config.url.match(that.ignorePath[i])) {
                                return config;
                            }
                        }

                        var deferred = $q.defer();
                        if (that.auth.authz.token) {
                            that.auth.authz.updateToken(5).success(function () {
                                config.headers = config.headers || {};
                                config.headers.Authorization = 'Bearer ' + that.auth.authz.token;

                                deferred.resolve(config);
                            }).error(function () {
                                deferred.reject('Failed to refresh token');
                            });
                        }
                        return deferred.promise;
                    },

                    /**
                     * Authentication error handler
                     * @param $q
                     * @param response
                     * @return {Promise}
                     *
                     * @name AuthProvider#authErrorHandler
                     */
                    authErrorHandler: function ($q, response) {
                        if (response.status == 401) {
                            console.log('session timeout?');
                            // auth.logout();
                            that.auth.loggedIn = false;
                            that.auth.authz = null;
                            window.location = that.auth.logoutUrl;
                        } else if (response.status == 403) {
                            alert("Forbidden");
                        } else if (response.status == 404) {
                            alert("Not found");

                        } else if (response.status) {
                            if (response.data && response.data.errorMessage) {
                                alert(response.data.errorMessage);
                            } else {
                                alert("An unexpected server error has occurred " + response.status);
                                console.log(response);
                            }
                        }

                        return $q.reject(response);
                    }
                };
            };
        }

        angular.module('wfManagerServices').provider('auth', authenticationService);

    }
);
