package demo;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.IncomingConnection;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaTypes;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

public class HttpAkkaStreamHeadFirst {

	static final Logger logger = LoggerFactory.getLogger(HttpAkkaStreamHeadFirst.class);

	public static void main(String... args) throws Exception {
		ActorSystem system = ActorSystem.create();

		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			server(8080, system);
		} else {
			//client(8080, system);
		}
		System.in.read();
		system.shutdown();
	}

	public static void server(int port, ActorSystem system) {
		/* */
		final Materializer materializer = ActorMaterializer.create(system);

		Http
		  /* */
		  .get(system)
		  /* */
		  .bind("localhost", port, materializer)
		  /* */
		  .to(Sink
		    /* */
			.foreach( connection ->
			    /* */
				connection.handleWith(
					/* */
				    Flow.of(HttpRequest.class)
					/* */
					.map(request -> {
						/* */
						if (request.method() == HttpMethods.GET
						  && request.getUri().path().equals("/")) {
							/* */
							return HttpResponse.create()
							  .withEntity(MediaTypes.TEXT_HTML.toContentType(), "Hello world!");
						}
						/* */
						return HttpResponse.create()
						  .withStatus(404);

					}),
				  materializer)
			))
		  /* */
		  .run(materializer);
	}

	public static void client(int port, ActorSystem system) {
		final ActorMaterializer materializer = ActorMaterializer.create(system);

		//Todo display in logger
		Http.get(system)
		  .singleRequest(HttpRequest.create("localhost:"+ port), materializer);
	}


}
