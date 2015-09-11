package demo.headfirst;

import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

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
		/* Create a new HTTP server on 8080. */
		HttpServer.newServer(port)
	     /* Starts the server with a request handler. */
		  .start((req, resp) ->
              /* Write the reply with an increment every 1 second, flush every 3 items */
			  resp.writeString(
			    Observable.interval(1, TimeUnit.SECONDS).map(Object::toString),
			    item -> Long.parseLong(item) % 3 == 0

			    //Flush only on Complete (never)
			    //Observable.interval(1, TimeUnit.SECONDS).map(Object::toString)

			    //Flush only on Complete (after 10)
			    //Observable.interval(1, TimeUnit.SECONDS).take(10).map(Object::toString)
			  )
		  );
	}

	public static void client(int port) {
		/* Prepare a new HTTP client */
		HttpClient.newClient("127.0.0.1", port)
		  /* Prepare an HTTP GET as an Observable which will connect on subscribe */
		  .createGet("/")
		  /* Start the client and consume the Response 'container' */
		  .subscribe(resp ->
			  /* Response body can be consumed separately */
			  resp.getContent().subscribe(
			    /* Consume each chunk */
			    buffer -> logger.info(buffer.toString(Charset.defaultCharset()))
			  )
		  );
	}
}
