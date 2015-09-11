package demo.scattergather;

import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;


public class ToyService {

	public static void start(int port) {
		HttpServer.newServer(port)
		  .start((req, resp) -> resp.writeString(ask()));
	}

	public static Observable<String> ask() {
		return Observable.just("Yellow Ball");
	}

}
