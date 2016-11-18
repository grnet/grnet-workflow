(function (angular) {
    angular.module('wfworkspaceServices').provider('auth',
        /**
         * @name auth
         * @ngDoc services
         * @memberof wfworkspaceServices
         * @desc Authentication provider service. 
         */
        function () {

            var that = this;

            this.auth = null;
            this.ignorePath = [];

            /**
             * @memberof auth
             * @desc Configures ignore path
             * 
             * @param {String} path - Path to be ignored (such as /img)
             */
            this.addIgnorePath = function (path) {
                that.ignorePath.push(path);
            };

            this.$get = function () {

                return {

                    /**
                     * @memberof auth
                     * @desc Returns all available roles
                     * 
                     */
                    getRoles: function () {
                        return that.auth.authz.realmAccess.roles;
                    },

                    /**
                     * @memberof auth
                     * @desc Logouts the user
                     *  
                     */
                    logout: function () {
                        that.auth.loggedIn = false;
                        that.auth.authz = null;
                        window.location = that.auth.logoutUrl;
                    },

                    /**
                     * @memberof auth
                     * 
                     * @desc Authentication interceptor
                     * 
                     * @param {any} $q
                     * @param {any} config
                     * @returns {HttpPromise}
                     */
                    authInterceptor: function ($q, config) {

                        for (var i = 0; i < that.ignorePath.length; i++) {

                            if (config.url.match(that.ignorePath[i]))
                                return config;
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
                     * @memberof auth
                     * @desc Configures the Authentication Error Interceptor
                     * 
                     * @param {any} $q
                     * @param {any} response
                     * @returns {HttpPromise}
                     */
                    authErrorHandler: function ($q, response) {

                        if (response.status == 401) {
                            console.log('session timeout?');
                            auth.logout();

                        } else if (response.status == 403) {
                            alert("Forbidden");

                        } else if (response.status == 404) {
                            alert("Not found");

                        } else if (response.status) {

                            if (response.data && response.data.errorMessage)
                                alert(response.data.errorMessage);
                            else
                                alert("An unexpected server error has occurred");
                        }
                        return $q.reject(response);
                    }
                };
            };
        }
    );
})(angular);
