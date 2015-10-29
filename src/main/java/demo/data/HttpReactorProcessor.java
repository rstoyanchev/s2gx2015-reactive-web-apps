package demo.data;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.Processors;
import reactor.io.net.http.ReactorHttpClient;
import reactor.io.net.http.ReactorHttpServer;
import reactor.io.net.preprocessor.CodecPreprocessor;
import reactor.rx.Stream;
import reactor.rx.Streams;

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
		ReactorHttpServer<String, String> server =
		  httpServer(spec -> spec.httpProcessor(CodecPreprocessor.string())
		                         .listen(port));

		Stream<String> stream = Streams
		  /**/
		  .period(300, TimeUnit.MILLISECONDS)
		  /**/
		  .log("server")
		  /**/
		  .map(Object::toString)
		  /**/
		  .process(Processors.queue())
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
		ReactorHttpClient<String, String> client =
		  httpClient(spec -> spec.httpProcessor(CodecPreprocessor.string())
		                         .connect("127.0.0.1", port));

		client
		  /* Start a request to Connect on '/' */
		  .get("/")
		  /* Consume channel container */
		  .consume(channel ->
			  /* Consume decoded chunks */
			  channel.consume(
			    data -> logger.info(tab(index) + data),
			    error -> logger.error("error ", error))
		  );
	}


	private static String tab(int index) {
		String tabs = "";
		for (int i = 0; i < index; i++) {
			tabs += "\t\t\t\t";
		}
		return tabs + "[" + index + "]";
	}

}
