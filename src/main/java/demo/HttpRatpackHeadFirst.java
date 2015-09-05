package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.http.ResponseChunks;
import ratpack.server.BaseDir;
import ratpack.server.RatpackServer;
import reactor.rx.Streams;

import java.nio.file.Path;

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
				.get("/", ctx ->
					// and now render the HTTP response
					ctx.render(ResponseChunks.stringChunks(Streams.just("Hello World")))
				)
				.prefix("rsvp", c ->
					c.files(f ->
						f.dir("rsvp").indexFiles("rsvp_sample.html")
					)
				)
				.prefix("rxjs", c ->
					c.files(f ->
						f.dir("rxjs").indexFiles("rsvp_sample.html")
					)
				)
			)
		)
		  /* */
		  .start();


		System.in.read();
	}

}
