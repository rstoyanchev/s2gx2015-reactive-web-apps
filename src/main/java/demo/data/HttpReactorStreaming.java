package demo.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.Processors;
import reactor.io.IO;
import reactor.io.buffer.Buffer;
import reactor.io.net.http.ReactorHttpClient;
import reactor.io.net.http.ReactorHttpServer;
import reactor.io.net.preprocessor.CodecPreprocessor;
import reactor.rx.Stream;
import reactor.rx.Streams;

import static reactor.io.net.NetStreams.httpClient;
import static reactor.io.net.NetStreams.httpServer;

public class HttpReactorStreaming {

	static final Logger logger = LoggerFactory.getLogger(HttpReactorStreaming.class);

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
		ReactorHttpServer<Buffer, Buffer> server = httpServer(port);

		Stream<Buffer> stream = Streams.wrap(
		  /**/
		  IO.readFile(musicFilepath, 1000)
		)
		  /**/
		  .process(Processors.topic("radio", 256, false))
		  .map(Buffer::duplicate)
		  .log("broadcast")
		  .capacity(1)
		  ;

		server
		  /* Add a HTTP get Handler on root */
		  .get("/", channel ->
			{
				channel
				/* */
				  .responseHeader("Content-Type", "audio/mp3");

				if(channel.headers().get("Range") != null) {
					return channel.writeWith(stream);
				}else{
					return channel.writeWith(Streams.just(Buffer.wrap("")));
				}
			}
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
