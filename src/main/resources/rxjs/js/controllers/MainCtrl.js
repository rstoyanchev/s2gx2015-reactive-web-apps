angular.module("controllers").controller('MainCtrl', ['$scope', '$http', '$location', '$route', '$timeout', 'StreamService', function ($scope, $http, $location, $route, $timeout, StreamService) {

    $scope.activities = [];
    $scope.count = 0;

    var csvToJson = function (csvrow) {
        var line = csvrow.split(",");
        return {
            time: new Date(line[0]),
            text: line[1]
        };
    };

    var discardEmpty = function (csvrow) {
        return csvrow.trim()
    };

    var unpackCsv = function (ev) {
        return Rx.Observable
            .from(ev.data ? ev.data.split("\n") : [])
            .filter(discardEmpty)
            .map(csvToJson)
    };

    var logComplete = function () {
        console.log("terminated");
    };

    var logError = function (error) {
        console.log("error:", error);
    };

    var updateUI = function (data) {
        $scope.count++;
        $scope.activities.push(data);
        if($scope.activities.length > 10){
            $scope.activities.splice(0, 1);
        }
        console.log(data);
        $scope.$digest();
    };

    $scope.initialize = function(){
        StreamService
            .connect("stream")
            .flatMap(unpackCsv)
            .subscribe(updateUI, logError, logComplete);
    };

    $scope.initialize();
}]);
