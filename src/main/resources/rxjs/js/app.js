
// Create controllers module application
angular.module('controllers', []);

// Create services module application
angular.module('services', []);


// Append external mobile to main application
angular.module('app', [
    'ngRoute', 'controllers', 'services'
]);

angular.module('app').config(['$routeProvider', function($routeProvider) {
    $routeProvider
        .when('/main', {templateUrl: 'template/main.html', reloadOnSearch:false, controller: 'MainCtrl'})
        .otherwise({redirectTo: '/main'});
}]);
