package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.io.codec.StandardCodecs;
import reactor.io.net.http.HttpClient;
import reactor.io.net.http.HttpServer;
import reactor.rx.Streams;

import static reactor.io.net.NetStreams.httpClient;
import static reactor.io.net.NetStreams.httpServer;

public class HttpRatpackHeadFirst {

	static final Logger logger = LoggerFactory.getLogger(HttpRatpackHeadFirst.class);

	public static void main(String... args) throws Exception {
		System.in.read();
	}

	public static void client(int port) {
		/* Create HTTP client and Assign a Byte <=> String codec, the address and the port */
		HttpClient<String, String> client =
		  httpClient(spec -> spec.codec(StandardCodecs.STRING_CODEC).connect("127.0.0.1", port));

		client
		  /* Start a request to Connect on '/' */
		  .get("/")
		  /* Read channel headers */
		  .onSuccess(channel -> logger.info("headers: " + channel.responseHeaders().entries()) )
		  /* Consume channel container */
		  .onSuccess(channel ->
			  /* Consume decoded chunks */
			  channel.consume(logger::info)
		  )
		  /* If connection failed, consume error */
		  .onError(Throwable::printStackTrace);
	}

}
