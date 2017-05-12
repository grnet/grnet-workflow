(function (scope) {

	scope.ArrayUtil = {

		groupByProperty: function (array, property) {
			var groups = {};
			array.forEach(function (o) {
				var group = (o[property] || "!empty!").toString();
				groups[group] = groups[group] || [];
				groups[group].push(o);
			});
			return Object.keys(groups).map(function (group) {
				return groups[group];
			});
		},

		mapByProperty: function (array, property) {
			var groups = {};
			array.forEach(function (o) {
				var group = (o[property] || "!empty!").toString();
				groups[group] = groups[group] || [];
				groups[group].push(o);
			});
			return groups;
		},

		mapByProperty2Property: function (array, property, innerProperty) {
			var groups = {};
			array.forEach(function (o) {
				var key = (o[property] || "!empty!").toString();
				groups[key] = groups[key] || {};
				groups[key][innerProperty] = groups[key][innerProperty] || [];
				groups[key][innerProperty].push(o);
			});
			return groups;
		},

		mapByProperty2innerProperty: function (array, property, innerProperty,
			name) {
			var groups = {};
			array.forEach(function (o) {
				var key = (o[property][innerProperty] || "!empty!").toString();
				groups[key] = groups[key] || {};
				groups[key][name] = groups[key][name] || [];
				groups[key][name].push(o);
			});
			return groups;
		},
		/**
		 * 
		 */
		extendMapByProperty: function (array, map, property, newProperty) {
			var groups = map;
			array.forEach(function (o) {
				var key = (o[property] || "!empty!").toString();
				groups[key] = groups[key] || {};
				groups[key][newProperty] = groups[key][newProperty] || [];
				groups[key][newProperty].push(o);
			});
			return groups;
		}
	}

})(window);
