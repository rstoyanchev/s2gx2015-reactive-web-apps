package demo.scattergather;

import demo.headfirst.HttpReactorHeadFirst;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class HttpRxNettyScatterGather {

	static final Logger logger = LoggerFactory.getLogger(HttpRxNettyScatterGather.class);

	public static void main(String... args) throws Exception {
		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			server(8080);
		} else {
			HttpReactorHeadFirst.client(8080);
		}
		System.in.read();
	}

	public static void server(int port) {
		/* Create a new HTTP server on 8080. */
		HttpServer.newServer(port)
	     /* Starts the server with a request handler. */
		  .start((req, resp) ->
              /* Write a single content chunk as string "Hello World!" */
			  resp.writeString(Observable.just("Hello World!"))
		  );
	}
}
