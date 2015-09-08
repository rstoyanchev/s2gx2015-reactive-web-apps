package demo.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.Processors;
import reactor.io.codec.StandardCodecs;
import reactor.io.net.http.HttpClient;
import reactor.io.net.http.HttpServer;
import reactor.rx.Stream;
import reactor.rx.Streams;

import java.util.concurrent.TimeUnit;

import static reactor.io.net.NetStreams.httpClient;
import static reactor.io.net.NetStreams.httpServer;

public class HttpReactorProcessor {

	static final Logger logger = LoggerFactory.getLogger(HttpReactorProcessor.class);

	public static void main(String... args) throws Exception {
		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			server(8080);
		} else {
			for (int i = 0; i < 4; i++) {
				client(8080, i);
			}
		}
		System.in.read();
	}

	public static void server(int port) {
		/* Create HTTP server and Assign a Byte <=> String codec and the port */
		HttpServer<String, String> server =
		  httpServer(spec -> spec.codec(StandardCodecs.STRING_CODEC).listen(port));

		Stream<String> stream = Streams
		  /**/
		  .period(300, TimeUnit.MILLISECONDS)
		  /**/
		  .log("server")
		  /**/
		  .map(Object::toString)
		  /**/
		  .process(Processors.work())
		  /**/
		  .capacity(1);

		server
		  /* Add a HTTP get Handler on root */
		  .get("/", channel ->
			/* Write and flush the reply with a single-element stream*/
			  channel.writeWith(stream)
		  )
		  /* Start listening */
		  .start();
	}

	public static void client(int port, int index) {
		/* Create HTTP client and Assign a Byte <=> String codec, the address and the port */
		HttpClient<String, String> client =
		  httpClient(spec -> spec.codec(StandardCodecs.STRING_CODEC).connect("127.0.0.1", port));

		client
		  /* Start a request to Connect on '/' */
		  .get("/")
		  /* Read channel headers */
		  .observe(channel -> logger.info("headers: " + channel.responseHeaders().entries()))
		  /* Consume channel container */
		  .consume(channel ->
			  /* Consume decoded chunks */
			  channel.consume(data -> logger.info(tab(index) + data), error -> logger.error("error ", error)),
		    /* If connection failed, consume error */
			Throwable::printStackTrace
		  );
	}


	private static String tab(int index) {
		String tabs = "";
		for (int i = 0; i < index; i++) {
			tabs += "\t";
		}
		return tabs + "[" + index + "]";
	}

}
