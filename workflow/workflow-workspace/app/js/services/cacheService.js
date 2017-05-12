define(['angular'],

	function (angular) {

		'use strict';

		var savedCriteria = {};

		function CacheService() {

			this.saveCriteria = function (pageTitle, criteriaObject) {

				savedCriteria[pageTitle] = criteriaObject;
			};

			this.getCriteria = function (pageTitle) {

				if (savedCriteria.hasOwnProperty(pageTitle))
					return savedCriteria[pageTitle];

				return null;
			};

		}
		angular.module('wfWorkspaceServices').service('cacheService', [CacheService]);
	}
);