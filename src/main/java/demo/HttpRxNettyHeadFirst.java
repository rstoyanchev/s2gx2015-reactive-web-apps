package demo;

import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.nio.charset.Charset;

public class HttpRxNettyHeadFirst {

	static final Logger logger = LoggerFactory.getLogger(HttpRxNettyHeadFirst.class);

	public static void main(String... args) throws Exception {
		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			server(8080);
		} else {
			client(8080);
		}
		System.in.read();
	}

	public static void server(int port) {
		/*Starts a new HTTP server on 8080.*/
		HttpServer.newServer(port)
	     /*Starts the server with a request handler.*/
		  .start((req, resp) ->
              /*Write a single content chunk as string "Hello World!"*/
			  resp.writeString(Observable.just("Hello World!"))
		  );
	}

	public static void client(int port) {
		/* */
		HttpClient.newClient("127.0.0.1", port)
		  /* */
		  .createGet("/")
		  /* */
		  .doOnNext(resp -> logger.info(resp.toString()))
		  /* */
		  .subscribe(resp ->
			  /* */
			  resp.getContent().forEach(
			    /* */
			    buffer -> logger.info(buffer.toString(Charset.defaultCharset())),
			    /* */
			    Throwable::printStackTrace
			  )
		  );
	}
}
