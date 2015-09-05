package demo;

import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.nio.charset.Charset;

public class HttpRxNettyScatterGather {

	static final Logger logger = LoggerFactory.getLogger(HttpRxNettyScatterGather.class);

	public static void main(String... args) throws Exception {
		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			server(8080);
		} else {
			client(8080);
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

	public static void client(int port) {
		/* Prepare a new HTTP client */
		HttpClient.newClient("127.0.0.1", port)
		  /* Prepare an HTTP GET as an Observable which will connect on subscribe */
		  .createGet("/")
		  /* The Observable emits a Response object and complete */
		  .doOnNext(resp -> logger.info(resp.toString()))
		  /* Start the client and consume the Response 'container' */
		  .subscribe(resp ->
			  /* Response body can be consumed separately */
			  resp.getContent().subscribe(
			    /* Consume each chunk */
			    buffer -> logger.info(buffer.toString(Charset.defaultCharset())),
			    /* Implement an error handler */
			    Throwable::printStackTrace
			  )
		  );
	}
}
