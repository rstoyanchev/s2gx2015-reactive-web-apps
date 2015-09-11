package demo.scattergather;

import reactor.Timers;
import reactor.io.codec.StandardCodecs;
import reactor.io.net.http.HttpServer;
import reactor.rx.Stream;
import reactor.rx.Streams;

import java.util.concurrent.TimeUnit;

import static reactor.io.net.NetStreams.httpServer;

public class FoodStoreService {


	public static void start(int port){


		HttpServer<String, String> server =
		  httpServer(spec -> spec.codec(StandardCodecs.STRING_CODEC).listen(port));

		server
		  .get("/", channel -> channel.writeWith(getFood()) )
		  .start();
	}


	public static Stream<String> getFood(){
		return Streams.just("Bone", "Treat", "Ham");
	}

}
