package demo.backpressure;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.nio.charset.Charset;

public class HttpRxNettyBackpressure {

	static final Logger logger = LoggerFactory.getLogger(HttpRxNettyBackpressure.class);

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
              /*Write N content chunk as string "Hello World!"*/
			  resp.writeString(Observable
				  .range(1, 100000)
				  .doOnNext(n -> logger.info("" + n))
				 // .doOnNext(HttpRxNettyBackpressure::simulateLatency)
				  .map(Object::toString)
			    , chunk -> true
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
			    new Subscriber<ByteBuf>() {
				    @Override
				    public void onStart() {
					    request(1);
				    }

				    @Override
				    public void onCompleted() {
					    logger.info("completed");
				    }

				    @Override
				    public void onError(Throwable e) {
					    logger.error("", e);
				    }

				    @Override
				    public void onNext(ByteBuf buffer) {
					    simulateLatency();
					    logger.info(buffer.toString(Charset.defaultCharset()));
					    request(1);
				    }
			    }
			  )
		  );
	}

	private static void simulateLatency() {
		simulateLatency(null);
	}

	private static void simulateLatency(Object data) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			//
		}
	}
}
