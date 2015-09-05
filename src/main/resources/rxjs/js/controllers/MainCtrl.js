angular.module("controllers").controller('MainCtrl', ['$scope', '$http', '$location', '$route', '$timeout', 'StreamService', function ($scope, $http, $location, $route, $timeout, StreamService) {

    $scope.activities = [];
    $scope.count = 0;

    $scope.initialize = function(){
        StreamService.connect("stream")
            .flatMap(function (ev) {
                return Rx.Observable.from(ev.data ? ev.data.split("\n") : [])
                    .filter(function (csvrow) {
                        return csvrow.trim()
                    })
                    .map(function (csvrow) {
                        var line = csvrow.split(",");
                        return {
                            time: new Date(line[0]),
                            text: line[1]
                        };
                    })
            })
            .subscribe(function (data) {
                    $scope.count++;
                    $scope.activities.push(data);
                    if($scope.activities.length > 10){
                        $scope.activities.splice(0, 1);
                    }
                    console.log(data);
                $scope.$digest();
            }, function (error) {
                console.log("error:", error);
            }, function () {
                console.log("terminated");
            });
    };

    $scope.initialize();
}]);
