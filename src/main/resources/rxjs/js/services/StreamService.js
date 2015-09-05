angular.module("services").service('StreamService', [ function() {

    var socketStream = null;

    return {
        connect: connect,
        stream: stream,
        isConnected: isConnected,
        handleError: handleError,
        handleOpen: handleOpen
    };

    function stream(){
        return socketStream;
    }

    function fromWebsocket(address, openObserver) {
        var ws = new WebSocket(address);

        var observer = Rx.Observer.create(function (data) {
            if (ws.readyState === WebSocket.OPEN) { ws.send(data); }
        });

        // Handle the data
        var observable = Rx.Observable.create (function (obs) {
            // Handle open
            if (openObserver) {
                ws.onopen = function (e) {
                    openObserver.onNext(e);
                    openObserver.onCompleted();
                };
            }

            // Handle messages
            ws.onmessage = function(msg){
                var data =  angular.fromJson(msg.data);
                if(data.cause){
                    obs.onError(data);
                }else{
                    obs.onNext(data);
                }
            };
            ws.onerror = function(e){
                var error = {cause: e.message};
                handleError(error);
                obs.onError(error);
            };

            ws.onclose = function(e){
                obs.onCompleted();
            };

            // Return way to unsubscribe
            return function(){
                console.log("closing connection");
                ws.close();
                socketStream = null;
            }
        });

        return Rx.Subject.create(observer, observable);
    }


    function handleError(err) {
        console.log('Handle Error:', err);
        socketStream = null;

    }

    function handleOpen() {
        if (socketStream)
            return;

        console.log("connected!");
    }

    function isConnected() {
        return socketStream != null;
    }

    function connect(streamId) {

        if (!streamId) {
            var error = new Error("Must define a stream type and a stream ID");
            handleError(error);
            return Rx.Observable.throw(error);
        }

        var hostname =  window.location.hostname;
        var port =  window.location.port;
        var isSecure =  window.location.protocol == "https:";

        // Using an observer for the open
        var observer = Rx.Observer.create(handleOpen);
        var link = hostname+":"+port+"/" + streamId;
        link = (isSecure ? "s" : "") + "://" + link;

        console.log("connecting to "+link);

        if (WebSocket) {
            socketStream = fromWebsocket("ws"+link, observer);
        } else {
            var browserError = new Error("Real-time streaming is not supported by your browser");
            handleError(browserError);
            return Rx.Observable.throw(browserError);
        }
        return socketStream;
    }
}]);