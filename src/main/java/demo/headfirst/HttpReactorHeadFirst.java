package demo.headfirst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.io.buffer.Buffer;
import reactor.rx.Streams;

import static reactor.io.net.NetStreams.httpClient;
import static reactor.io.net.NetStreams.httpServer;

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
		httpServer(port)
		  /* Add a HTTP get Handler on root */
		  .get("/", channel ->
			/* Write the reply with an increment every 1 second, flush every 3 items */
			  channel.writeWith(
				Streams.period(1).map(n -> Buffer.wrap("[" + n + "]")).capacity(3)

			    //Flush only on Complete (never)
				//Streams.period(1).map(n -> Buffer.wrap("[" + n + "]"))

			    //Flush only on Complete (after 10)
				//Streams.period(1).take(10).map(n -> Buffer.wrap("[" + n + "]"))
			  )
		  )
		  /* Start listening */
		  .start()
		;
	}

	public static void client(int port) {
		httpClient()
		  /* Start a request to Connect on '/' */
		  .get("http://localhost:" + port)
		  /* Consume channels */
		  .consume(
			  /* Consume data chunks */
			channel -> channel.map(Buffer::asString).consume(logger::info)
		  );
	}
}
