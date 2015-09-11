package demo.scattergather;

import demo.backpressure.HttpReactorBackpressure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.io.buffer.Buffer;
import reactor.io.codec.StandardCodecs;
import reactor.io.codec.json.JsonCodec;
import reactor.io.net.NetStreams;
import reactor.io.net.http.HttpServer;
import reactor.rx.Streams;

import java.util.concurrent.TimeUnit;

import static reactor.io.net.NetStreams.httpServer;
import static reactor.rx.Streams.zip;

public class HttpReactorScatterGather {

	static final Logger logger = LoggerFactory.getLogger(HttpReactorScatterGather.class);

	public static void main(String... args) throws Exception {
		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			FoodStoreService.start(8081);
			ToyService.start(8082);
			gateway(8080, 8081, 8082);
		} else {
			HttpReactorBackpressure.client(8080);
		}
		System.in.read();
	}

	public static void gateway(int port, int backendPort1, int backendPort2) {

		DogeService foodStore = new DogeService(backendPort1);
		DogeService toyService = new DogeService(backendPort2);


		httpServer(port)
		  .get("/", channel ->
			  channel
			    .responseHeader("Content-Type", "application/json")
			    .writeWith(
				  zip(toyService.send(), foodStore.send().buffer(),
				    tuple -> new DogeResponse(tuple.getT1(), tuple.getT2())
				  )
				    .map(DogeResponse::toJSON)
			    )
		  )
		  /* Start listening */
		  .start();
	}

}
