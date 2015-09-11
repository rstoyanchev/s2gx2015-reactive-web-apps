package demo.backpressure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.io.buffer.Buffer;

import static reactor.io.net.NetStreams.httpClient;
import static reactor.io.net.NetStreams.httpServer;

public class HttpReactorBackpressure {


	static final Logger logger = LoggerFactory.getLogger(HttpReactorBackpressure.class);

	static DummyDataRepository repository = new DummyDataRepository();

	public static void main(String... args) throws Exception {
		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			server(8080);
		} else {
			client(8080);
		}
		System.in.read();
	}

	public static void server(int port) {
		httpServer(port)
		  /* Add a HTTP GET Handler on "/" */
		  .get("/", channel ->
			/* Write and flush the reply with a single-element stream*/
			  channel.writeWith(
				repository.findAll()
				  .map(Buffer::wrap)
				  .log("server")
				  .capacity(1)
			  )
		  )
		  /* Start listening */
		  .start();
	}

	public static void client(int port) {

		httpClient()
		  /* Start a request to Connect on '/' */
		  .get("http://localhost:" + port)
		  /* Consume response */
		  .consume(channel ->
			  /* */
			  channel
			    /* */
				.capacity(1)
				/* */
				.log("client")
				/* */
				.consume(data -> {
					//simulateLatency();
					logger.info("size:" + data.capacity());
				})

		  );

	}

	private static void simulateLatency() {
		simulateLatency(null);
	}

	private static void simulateLatency(Object data) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			//
		}
	}

	/*
				.options(new NettyServerSocketOptions().pipelineConfigurer(pipeline -> {
					ChannelConfig config = pipeline.channel().config();
					config.setOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 2);
					config.setOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 4);
				}))*/
}
