define(['angular', 'services/processservice', 'services/authprovider'],

	function (angular) {

		'use strict';

		function settingsCtrl($scope, $http, $window, $mdDialog, processService, auth, $filter) {

			$scope.settings = null;
			$scope.fbpages = [];
			$scope.registries = null;
			$scope.isPageNumbering = false;
			$scope.isSocialMedia = false;
			$scope.isOwner = false;
			$scope.isRoles = false;
			$scope.isNew = false;
			$scope.twitterAccounts = [];
			$scope.nextPattern = "[0-9]+";

			processService.getSettings().then(
				function (response) {
					$scope.settings = response.data;
					$scope.checkTokens();
				},
				//error callback
				function (response) {
					alertGenericResponseError(response);
				}
			);

			processService.getRegistries().then(
				function (response) {
					$scope.registries = response.data;
				}
			);

			processService.getTwitterAccounts().then(
				function (response) {
					$scope.twitterAccounts = response.data;
				},
				function (response) {
				}
			);

			$scope.pushFacebookPage = function (page) {
				$scope.fbpages.push({ name: page, valid: true, profilePicUrl: null, coverPicUrl: "img/facebook.png" });
			};

			$scope.checkTokens = function () {
				processService.checkTokens($scope.settings.pages).then(
					function (response) {
						$scope.fbpages = response.data || [];
						$scope.fbpages.forEach(function (item) {
							if (item.coverPicUrl == null) {
								item.coverPicUrl = "img/facebook.png";
							}
						});
					},
					//error callback
					function (response) {
						alertGenericMessage($filter('translate')('tokenError'), "error");
					}
				);
			};

			$scope.selectPage = function (fbp) {
				$scope.fbp = fbp;
				$mdDialog.show({
					controller: function ($scope, $mdDialog, $http, fbpages, processService) {
						$scope.fbpage = fbp;
						$scope.pages = fbpages;

						$scope.remove = function (fbp) {
							processService.removeFacebookPageAccess(fbp).then(
								function (response) {
									var index = $scope.pages.indexOf(fbp);
									$scope.pages.splice(index, 1);
									$mdDialog.cancel();
								},
								function (response) {
									alertGenericResponseError(res);
								}
							);
						};

						$scope.cancel = function () {
							$mdDialog.cancel();
						};

					},
					templateUrl: 'templates/facebookpage.tmpl.html',
					parent: document.body,
					locals: {
						'fbpages': $scope.fbpages,
						'processService': processService,
					},
					clickOutsideToClose: true

				})
			}

			$scope.selectAccount = function (account) {
				$scope.account = account;
				$mdDialog.show({
					controller: function ($scope, $mdDialog, $http, twitterAccounts, processService) {
						$scope.twitterAccount = account;
						$scope.accounts = twitterAccounts;

						$scope.remove = function (account) {
							processService.removeTwitterAccountAccess(account).then(
								function (response) {
									var index = $scope.accounts.indexOf(account);
									$scope.accounts.splice(index, 1);
									$mdDialog.cancel();
								},
								function (response) {
									alertGenericResponseError(res);
								}
							);
						};

						$scope.cancel = function () {
							$mdDialog.cancel();
						};

					},
					templateUrl: 'templates/twitteraccount.tmpl.html',
					parent: document.body,
					locals: {
						'twitterAccounts': $scope.twitterAccounts,
						'processService': processService,
					},
					clickOutsideToClose: true

				})
			}

			$scope.newFacebookPage = function () {
				$mdDialog.show({
					controller: function ($scope, $mdDialog, $http, fbpages, processService, pushPage) {

						if (fbpages == null || fbpages == "") fbpages = [];

						$scope.pages = fbpages;
						$scope.facebookPage;

						$scope.acquireToken = function () {
							var cancel = false;

							if ($scope.facebookPage == null || $scope.facebookPage == "") {
								alertGenericMessage($filter('translate')('undefinedFacebookPage'), "error");
								cancel = true;
							}

							if (!cancel && $scope.pages != null && $scope.pages != "") {
								$scope.pages.forEach(function (item) {
									if ($scope.facebookPage == item) {
										alertGenericMessage($filter('translate')('pageRegistered'), "info");
										cancel = true;
									}
								});
							}

							if (cancel) return;

							FB.init({
								appId: '973785599381892',
								xfbml: true,
								version: 'v2.5'
							});

							var fb;

							FB.getLoginStatus(function (response) {

								if (response.status == "connected") {
									fb = {
										userID: response.authResponse.userID,
										accessToken: response.authResponse.accessToken,
										page: $scope.facebookPage
									};

									postFBAccessToken(fb);

								}


								else {
									FB.login(function (response) {
										fb = {
											userID: response.authResponse.userID,
											accessToken: response.authResponse.accessToken,
											page: $scope.facebookPage
										};

										postFBAccessToken(fb);

									}, { scope: 'publish_pages,manage_pages' });
								}

							});

						}

						$scope.cancel = function () {
							$mdDialog.cancel();
						};

						function postFBAccessToken(fb) {
							processService.postAccessToken(fb).then(
								function (res) {
									if (res.data) {
										pushPage(fb.page);
										alertGenericMessage($filter('translate')('tokenClaimed'), "success");
									}
									else alertGenericMessage($filter('translate')('tokenNotClaimed'), "error");
								},
								function (res) {
									alertGenericResponseError(res);
								}
							);
						}


						// Load the SDK asynchronously
						(function (d, s, id) {
							var js, fjs = d.getElementsByTagName(s)[0];
							if (d.getElementById(id)) return;
							js = d.createElement(s); js.id = id;
							js.src = "http://connect.facebook.net/en_US/all.js";
							fjs.parentNode.insertBefore(js, fjs);
						}(document, 'script', 'facebook-jssdk'));

					},
					templateUrl: 'templates/facebooktokens.tmpl.html',
					parent: document.body,
					locals: {
						'fbpages': $scope.fbpages,
						'processService': processService,
						'pushPage': $scope.pushFacebookPage
					},
					clickOutsideToClose: true
				})

			}


			$scope.authenticateTwitter = function () {
				var twitterauth;
				processService.authenticateTwitter().then(
					function (response) {
						twitterauth = response.data;
						window.location.href = twitterauth.url;
					},
					function (response) {
					}
				);

			}

			$scope.settingsChanged = function () {
				processService.updateSettings($scope.settings).then(
					//update settings success callback
					function (response) {
						$scope.settings = response.data;
						$scope.settingsForm.$setPristine();
					},
					//update settings error callback
					function (response) {
						if (response.status == "403") {
							$mdDialog.show($mdDialog.alert()
								.parent(document.body)
								.clickOutsideToClose(true)
								.title($filter('translate')('error'))
								.content("No authorized user")
								.ok($filter('translate')('confirm'))
							);
						} else {
							$mdDialog.show({
								controller: function ($scope, $mdDialog, error) {
									$scope.error = error;

									$scope.cancel = function () {
										$mdDialog.hide();
									};
								},
								scope: $scope,
								preserveScope: true,
								templateUrl: 'templates/exception.tmpl.html',
								parent: angular.element(document.body),
								targetEvent: event,
								locals: {
									'error': response.data
								}
							})
						}
					}
				);
			};

			/**
			 * Display modal to edit registry
			 */
			$scope.editRegistry = function (registry, event) {
				$scope.isNew = false;
				$scope.registry = registry;

				$mdDialog.show({
					controller: function ($mdDialog) {

						$scope.saveRegistry = function () {
							processService.updateRegistry($scope.registry).then(
								function (response) {

									$mdDialog.hide();
								});
						};

						$scope.cancel = function () {
							$mdDialog.hide();
						};

						$scope.deleteRegistry = function (event) {
							processService.deleteRegistry($scope.registry.id).then(
								//delete registry success callback
								function (response) {
									processService.getRegistries().then(
										function (response) {
											$scope.registries = response.data;
										});
								},
								//delete registry error callback
								function (response) {
									if (response.status == "403") {
										$mdDialog.show($mdDialog.alert()
											.parent(document.body)
											.clickOutsideToClose(true)
											.title($filter('translate')('error'))
											.content("No authorized user")
											.ok($filter('translate')('confirm'))
										);
									} else {
										$mdDialog.show({
											controller: function ($scope, $mdDialog, error) {
												$scope.error = error;

												$scope.cancel = function () {
													$mdDialog.hide();
												};
											},
											scope: $scope,
											preserveScope: true,
											templateUrl: 'templates/exception.tmpl.html',
											parent: angular.element(document.body),
											targetEvent: event,
											locals: {
												'error': response.data
											}
										})
									}
								});

							$mdDialog.hide();
						};
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/registry.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'registry': $scope.registry
					}
				})
			};

			$scope.back = function () {
				$window.history.back();
			};

			$scope.hasCoverPic = function (f) {
				if (f.coverPicUrl == null) return false;
				return true;
			}


			/**
			 * Tab change listener
			 */
			$scope.onTabSelected = function (tab) {
				$scope.isPageNumbering = false;
				$scope.isSocialMedia = false;
				$scope.isOwner = false;
				$scope.isRoles = false;

				if (tab == 'numbering') {
					$scope.isPageNumbering = true;
				}

				if (tab == 'socialMedia') {
					$scope.isSocialMedia = true;
				}
			};

			/**
			 * Display modal to add new registry
			 */
			$scope.addNewRegistry = function (event) {
				$scope.registry = null;

				$scope.codePattern = "[0-9A-Za-z]+";

				$scope.isNew = true;

				$mdDialog.show({
					controller: function ($scope, $mdDialog) {

						$scope.cancel = function () {
							$mdDialog.hide();
						};

						$scope.saveRegistry = function () {
							processService.createRegistry($scope.registry).then(
								function (response) {
									processService.getRegistries().then(
										function (response) {
											$scope.registries = response.data;
										}
									);
									$mdDialog.hide();
								},
								// error callback
								function (response) {
									if (response.status == "403") {
										$mdDialog.show($mdDialog.alert()
											.parent(document.body)
											.clickOutsideToClose(true)
											.title($filter('translate')('error'))
											.content("No authorized user")
											.ok($filter('translate')('confirm'))
										);
									} else {
										$mdDialog.show({
											controller: function ($scope, $mdDialog, error) {
												$scope.error = error;

												$scope.cancel = function () {
													$mdDialog.hide();
												};
											},
											scope: $scope,
											preserveScope: true,
											templateUrl: 'templates/exception.tmpl.html',
											parent: angular.element(document.body),
											targetEvent: event,
											locals: {
												'error': response.data
											}
										})
									}
								});
						};
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/registry.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'registry': $scope.registry,
						'codePattern': $scope.codePattern,
						'nextPattern': $scope.nexyPattern,
						'isNew': $scope.isNew
					}
				})
			};

			/** ******************** Owners tab ******************** **/

			$scope.synchronizeOwners = function () {

				processService.synchOwners().then(
					function (response) {
						$scope.selectOwnersToSync(response.data);
					}
				);
			};

			$scope.selectOwnersToSync = function (owners, event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog) {

						$scope.ownersToSync = owners;
						$scope.selected = [];
						$scope.showProgress = false;

						$scope.cancel = function () {
							$mdDialog.hide();
						};
						
						$scope.isIndeterminate = function() {
							return ($scope.selected.length !== 0 && $scope.selected.length !== $scope.ownersToSync.length);
						};

						$scope.toggle = function (item, list) {
							var idx = list.indexOf(item);
							if (idx > -1) {
								list.splice(idx, 1);
							}
							else {
								list.push(item);
							}
						};

						$scope.isChecked = function () {
							return $scope.selected.length === $scope.ownersToSync.length;
						};

						$scope.toggleAll = function () {
							if ($scope.selected.length === $scope.ownersToSync.length) {
								$scope.selected = [];
							} else if ($scope.selected.length === 0 || $scope.selected.length > 0) {
								$scope.selected = $scope.ownersToSync.slice(0);
							}
						};

						$scope.exists = function (item, list) {
							return list.indexOf(item) > -1;
						};

						$scope.confirm = function () {
							$scope.showProgress = true;

							processService.importOwners($scope.selected).then(
								function () {
									$scope.showProgress = false;
									$mdDialog.hide();

									processService.getGroups().then(
										function (response) {
											$scope.owners = response.data;
										}
									);
								}
							);
						};
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/selectOwners.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true
				});
			};

			$scope.ownersSelected = function () {
				$scope.isPageNumbering = false;
				$scope.isSocialMedia = false;
				$scope.isOwner = true;
				$scope.isRoles = false;

				$scope.rolesProgress = true;

				processService.getGroups().then(
					function (response) {
						$scope.owners = response.data;
					}

				).finally(function () {
					$scope.rolesProgress = false;
				});
			};

			$scope.editOwner = function (event, owner) {

				$mdDialog.show({
					controller: function ($scope, $mdDialog, owner) {
						$scope.owner = owner
						$scope.isNew = false;
						$scope.ownerAlreadyDeleted = false;

						$scope.cancel = function () {
							getGroups();
							$mdDialog.hide();
						};

						$scope.deleteOwner = function () {
							processService.deleteOwner($scope.owner.ownerId).then(
								// success callback
								function () {
									getGroups();
									$mdDialog.hide();
								},
								//error callback
								function (response) { }
							);
						};

						$scope.saveOwner = function () {
							processService.saveOwner($scope.owner).then(
								// success callback
								function (response) {
									getGroups();
									$mdDialog.hide();

								}
								// error callback
								, function (response) {
									$scope.ownerAlreadyDeleted = true;
									getGroups();
								}
							);
						};

						function getGroups() {
							processService.getGroups().then(
								function (response) {
									$scope.owners = response.data;
								}
							);
						}
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/owner.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'owner': owner
					}
				});
			};

			$scope.createOwner = function (event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog) {
						$scope.isNew = true;
						$scope.owner = null;
						$scope.ownerAlreadyDeleted = false;

						$scope.cancel = function () {
							$mdDialog.hide();
						};

						$scope.saveOwner = function () {
							processService.saveOwner($scope.owner).then(
								// success callback
								function (response) {
									getGroups();
									$mdDialog.hide();
								}
							);
						};

						function getGroups() {
							processService.getGroups().then(
								function (response) {
									$scope.owners = response.data;
								}
							);
						}
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/owner.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true
				});
			};

			/** ******************** Roles tab ******************** **/

			$scope.synchronizeRoles = function () {

				processService.synchRoles().then(
					function (response) {
						$scope.selectRolesToSync(response.data);
					}
				);
			};

			$scope.selectRolesToSync = function (roles, event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog) {

						$scope.rolesToSync = roles;
						$scope.selected = [];
						$scope.showProgress = false;

						$scope.cancel = function () {
							$mdDialog.hide();
						};

						$scope.isIndeterminate = function() {
							return ($scope.selected.length !== 0 && $scope.selected.length !== $scope.rolesToSync.length);
						};

						$scope.toggle = function (item, list) {
							var idx = list.indexOf(item);
							if (idx > -1) {
								list.splice(idx, 1);
							}
							else {
								list.push(item);
							}
						};

						$scope.isChecked = function () {
							return $scope.selected.length === $scope.rolesToSync.length;
						};

						$scope.toggleAll = function () {
							if ($scope.selected.length === $scope.rolesToSync.length) {
								$scope.selected = [];
							} else if ($scope.selected.length === 0 || $scope.selected.length > 0) {
								$scope.selected = $scope.rolesToSync.slice(0);
							}
						};

						$scope.exists = function (item, list) {
							return list.indexOf(item) > -1;
						};

						$scope.confirm = function () {

							$scope.showProgress = true;

							processService.importRoles($scope.selected).then(
								function () {
									$scope.showProgress = false;
									$mdDialog.hide();

									processService.getRoles().then(
										function (response) {
											$scope.roles = response.data;
										}
									);
								}
							);
						};
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/selectRoles.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true
				});
			};

			$scope.rolesSelected = function () {
				$scope.isPageNumbering = false;
				$scope.isSocialMedia = false;
				$scope.isOwner = false;
				$scope.isRoles = true;

				$scope.ownersProgress = true;

				processService.getRoles().then(
					function (response) {
						$scope.roles = response.data;
					}

				).finally(function () {
					$scope.ownersProgress = false;
				});
			};

			$scope.crateRole = function (event) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog) {
						$scope.isNew = true;
						$scope.role = null;

						$scope.cancel = function () {
							getRoles();
							$mdDialog.hide();
						};

						$scope.saveRole = function () {
							processService.saveRole($scope.role).then(
								// success callback
								function () {
									getRoles();
									$mdDialog.hide();
								}
							);
						};

						function getRoles() {
							processService.getRoles().then(
								function (response) {
									$scope.roles = response.data;
								}
							);
						}
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/role.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true
				});
			};

			$scope.editRole = function (event, role) {
				$mdDialog.show({
					controller: function ($scope, $mdDialog, role) {
						$scope.role = role;
						$scope.isNew = false;
						$scope.ownerAlreadyDeleted = false;

						$scope.cancel = function () {
							getRoles();
							$mdDialog.hide();
						};

						$scope.saveRole = function () {
							processService.saveRole($scope.role).then(
								// success callback
								function () {
									getRoles();
									$mdDialog.hide();
								}
							);
						};

						$scope.deleteRole = function () {
							processService.deleteRole($scope.role.roleId).then(
								// success callback
								function () {
									getRoles();
									$mdDialog.hide();
								}
							);
						};

						function getRoles() {
							processService.getRoles().then(
								function (response) {
									$scope.roles = response.data;
								}
							);
						}
					},
					scope: $scope,
					preserveScope: true,
					templateUrl: 'templates/role.tmpl.html',
					parent: document.body,
					targetEvent: event,
					clickOutsideToClose: true,
					locals: {
						'role': role
					}
				});
			};

			/**
			 * Helper function for showing generic error alert
			 * @param response
			 */
			function alertGenericResponseError(response) {
				$mdDialog.show($mdDialog.alert()
					.parent(document.body)
					.clickOutsideToClose(true)
					.title($filter('translate')('error'))
					.content(response.data.message)
					.ok($filter('translate')('confirm'))
				);
			}


			/**
			 * Helper function for showing generic error alert
			 * @param response
			 */
			function alertGenericMessage(msg, outcome) {
				var title = "";
				if (outcome == "error") title = $filter('translate')('error');
				else if (outcome == "info") title = $filter('translate')('info');
				else title = $filter('translate')('success');
				$mdDialog.show($mdDialog.alert()
					.parent(document.body)
					.clickOutsideToClose(true)
					.title(title)
					.content(msg)
					.ok($filter('translate')('confirm'))
				);
			}

		}

		angular.module('wfManagerControllers').controller('SettingsCtrl', ['$scope', '$http', '$window', '$mdDialog', 'processService', 'auth', '$filter', settingsCtrl]);

	}
);
