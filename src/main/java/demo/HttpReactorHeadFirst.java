package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.io.codec.StandardCodecs;
import reactor.io.net.http.HttpClient;
import reactor.io.net.http.HttpServer;
import reactor.rx.Streams;

import static reactor.io.net.NetStreams.*;

public class HttpReactorHeadFirst {

	static final Logger logger = LoggerFactory.getLogger(HttpReactorHeadFirst.class);

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

		/* Add a HTTP get Handler on root */
		server
		  .get("/", channel ->
			/* Write and flush the reply with a single-element stream*/
			  channel.writeWith(Streams.just("Hello World!"))
		  )
		  /* Start listening */
		  .start();
	}

	public static void client(int port) {
		/* Create HTTP client and Assign a Byte <=> String codec, the address and the port */
		HttpClient<String, String> client =
		  httpClient(spec -> spec.codec(StandardCodecs.STRING_CODEC).connect("127.0.0.1", port));

		/*  */
		client
		  /* */
		  .get("/")
		  /* Read channel headers */
		  .onSuccess(channel -> logger.info("headers: "+channel.responseHeaders().entries()) )
		  /* Consume response */
		  .onSuccess(channel ->
			  /* */
			  channel.consume(logger::info)
		  )
		  /* */
		  .onError(Throwable::printStackTrace);
	}
}
