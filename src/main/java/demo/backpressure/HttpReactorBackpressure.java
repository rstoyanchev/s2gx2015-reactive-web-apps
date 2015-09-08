package demo.backpressure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.io.codec.StandardCodecs;
import reactor.io.net.http.HttpClient;
import reactor.io.net.http.HttpServer;
import reactor.rx.Streams;

import static reactor.io.net.NetStreams.httpClient;
import static reactor.io.net.NetStreams.httpServer;

public class HttpReactorBackpressure {

	static final Logger logger = LoggerFactory.getLogger(HttpReactorBackpressure.class);

	public static void main(String... args) throws Exception {
		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			server(8080);
		} else {
			client(8080);
		}
		System.in.read();
	}

	public static void server(int port) {
		/* Create HTTP server and Assign a Byte <=> String codec and the port */
		HttpServer<String, String> server =
		  httpServer(spec -> spec.codec(StandardCodecs.STRING_CODEC).listen(port));

		server
		  /* Add a HTTP GET Handler on "/" */
		  .get("/", channel ->
			/* Write and flush the reply with a single-element stream*/
			  channel.writeWith(
				Streams
				  .range(1, 1000)
				  .map(Object::toString)
				  .log("server")
				  .observe(HttpReactorBackpressure::simulateLatency)
				  .capacity(1)
			  )
		  )
		  /* Start listening */
		  .start();
	}

	public static void client(int port) {
		/* Create HTTP client and Assign a Byte <=> String codec, the address and the port */
		HttpClient<String, String> client =
		  httpClient(spec -> spec.codec(StandardCodecs.STRING_CODEC).connect("127.0.0.1", port));

		client
		  /* */
		  .get("/")
		  /* Consume response */
		  .consume(channel ->
			  /* */
			  channel
			    /* */
				.capacity(1)
				/* */
				.log("client")
			    /* */
				.consume(data -> {
					simulateLatency();
					logger.info(data);
				})
			,
		    /* */
			Throwable::printStackTrace,
		    v -> logger.info("onComplete")
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
