package demo.headfirst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.http.ResponseChunks;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import reactor.rx.Streams;

import static ratpack.websocket.WebSockets.websocketBroadcast;

public class HttpRatpackHeadFirst {

	static final Logger logger = LoggerFactory.getLogger(HttpRatpackHeadFirst.class);

	public static void main(String... args) throws Exception {
		//A Ratpack http server
		RatpackServer.of(spec -> spec
			/* */
			.serverConfig(conf ->
				conf.port(8080).baseDir(BaseDir.find())
			)
			/* */
			.handlers(chain -> chain
				/* */
				.prefix("rxjs", c ->
					/* */
					c.files(f ->
						f.dir("rxjs").indexFiles("rxjs_sample.html")
					)
				)
				/* */
				.get("stream", c ->
					websocketBroadcast(c, Streams.
						period(1)
						.map(Object::toString)
						.map(data -> System.currentTimeMillis()+","+data)
					    .map(data -> "{ \"data\" : \"" + data + "\" }")
					)
				)
				/* */
				.all(ctx ->
					// and now render the HTTP response
					ctx.render(ResponseChunks.stringChunks(Streams.just("Hello World")))
				)
			)
		)
		  /* */
		  .start();


		System.in.read();
	}

}
