package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.Processors;
import reactor.io.IO;
import reactor.io.buffer.Buffer;
import reactor.io.codec.StandardCodecs;
import reactor.io.net.http.HttpClient;
import reactor.io.net.http.HttpServer;
import reactor.rx.Stream;
import reactor.rx.Streams;


import static reactor.io.net.NetStreams.httpClient;
import static reactor.io.net.NetStreams.httpServer;

public class HttpReactorFlush {

	static final Logger logger = LoggerFactory.getLogger(HttpReactorFlush.class);

	static final String musicFilepath = "/Users/smaldini/Desktop/music.mp3";

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
		HttpServer<Buffer, Buffer> server = httpServer(port);

		Stream<Buffer> stream = Streams.wrap(
		  /**/
		  IO.readFile(musicFilepath)
		)
		  /**/
		  .process(Processors.async())
		  /**/
		  .keepAlive()
		  /**/
		  .capacity(32);

		server
		  /* Add a HTTP get Handler on root */
		  .get("/", channel ->
			  channel
			    /* */
			    .responseHeader("Content-Type", "audio/mp3")
				/* Write and flush the reply with a single-element stream*/
				.writeWith(stream)
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
			  channel.consume(data -> logger.info(tab(index) + data), error -> logger.error("error ",error) ),
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
