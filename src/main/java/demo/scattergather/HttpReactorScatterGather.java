package demo.scattergather;

import demo.backpressure.HttpReactorBackpressure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.io.buffer.Buffer;
import reactor.io.codec.StandardCodecs;
import reactor.io.net.http.HttpServer;
import reactor.rx.Streams;

import java.util.concurrent.TimeUnit;

import static reactor.io.net.NetStreams.httpClient;
import static reactor.io.net.NetStreams.httpServer;
import static reactor.rx.Streams.merge;

public class HttpReactorScatterGather {

	static final Logger logger = LoggerFactory.getLogger(HttpReactorScatterGather.class);

	public static void main(String... args) throws Exception {
		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			backend(8081);
			backend(8082);
			frontend(8080, 8081, 8082);
		} else {
			HttpReactorBackpressure.client(8080);
		}
		System.in.read();
	}

	public static void frontend(int port, int backendPort1, int backendPort2) {
		/* Create HTTP server and Assign a Byte <=> String codec and the port */
		HttpServer<Buffer, Buffer> server = httpServer(port);

		server
		  /* Add a HTTP get Handler on root */
		  .get("/", channel ->
			/* Write and flush the reply with a single-element stream*/
			  channel.writeWith(
			    /**/
			    merge(
			      httpClient().get("http://localhost:" + backendPort1 + "/"),
			      httpClient().get("http://localhost:" + backendPort2 + "/")
			    )
				  /**/
				  .flatMap(resp -> resp)
				  /**/
				  .log("frontend")
				  /* flush every 2 packets */
				  .capacity(2)
			  )
		  )
		  /* Start listening */
		  .start();
	}

	public static void backend(int port) {
		/* Create HTTP server and Assign a Byte <=> String codec and the port */
		HttpServer<String, String> server =
		  httpServer(spec -> spec.codec(StandardCodecs.STRING_CODEC).listen(port));

		server
		  /* Add a HTTP get Handler on root */
		  .get("/", channel ->
			/* Write and flush the reply with a single-element stream*/
			  channel.writeWith(Streams
				  .period(randomTime(port), TimeUnit.SECONDS)
			  /**/
				  .map(data -> data + " - " + port)
		      /**/
				  .capacity(1L)
		      /**/
				  .log("backend-" + port)
			  )
		  )

		  /* Start listening */
		  .start()
		  /**/
		  .poll();
	}

	private static long randomTime(int port) {
		return port == 8081 ? 1L : 3L;
	}

}
