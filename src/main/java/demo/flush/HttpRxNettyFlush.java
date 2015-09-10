package demo.flush;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.Processors;
import reactor.io.IO;
import reactor.rx.Stream;
import reactor.rx.Streams;
import rx.Observable;
import rx.RxReactiveStreams;
import rx.Subscriber;
import rx.subjects.AsyncSubject;

import java.nio.Buffer;
import java.nio.charset.Charset;

public class HttpRxNettyFlush {

	static final Logger logger = LoggerFactory.getLogger(HttpRxNettyFlush.class);

	public static void main(String... args) throws Exception {
		if (args.length == 0 || args[0].equalsIgnoreCase("server")) {
			server(8080);
		} else {
			client(8080);
		}
		System.in.read();
	}

	public static void server(int port) {


		Stream<ByteBuf> stream = Streams.wrap(
		  /**/
		  IO.readFile(HttpReactorFlush.musicFilepath, 1000)
		)
		  /**/
		  .process(Processors.async("radio", 256, false))
		  .map(reactor.io.buffer.Buffer::duplicate)
		  .map(buffer -> Unpooled.wrappedBuffer(buffer.byteBuffer()));
		  ;

		/*Starts a new HTTP server on 8080.*/
		HttpServer.newServer(port)

		 /*Starts the server with a request handler.*/

		  .start((req, resp) -> {

              /*Write N content chunk as string "Hello World!"*/
			    resp.addHeader("Content-Type", "audio/mp3");
			    if (req.containsHeader("Range")) {
				    return resp.write(RxReactiveStreams.toObservable(stream)
				      , chunk -> true
				    );
			    }else{
				    return Observable.empty();
			    }
		    }
		  );
	}

	public static void client(int port) {
		/* Prepare a new HTTP client */
		HttpClient.newClient("127.0.0.1", port)
		  /* Prepare an HTTP GET as an Observable which will connect on subscribe */
		  .createGet("/")
		  /* Start the client and consume the Response 'container' */
		  .subscribe(resp ->
			  /* Response body can be consumed separately */
			  resp.getContent().subscribe(
			    new Subscriber<ByteBuf>() {
				    @Override
				    public void onStart() {
					    request(1);
				    }

				    @Override
				    public void onCompleted() {
					    logger.info("completed");
				    }

				    @Override
				    public void onError(Throwable e) {
					    logger.error("", e);
				    }

				    @Override
				    public void onNext(ByteBuf buffer) {
					    simulateLatency();
					    logger.info(buffer.toString(Charset.defaultCharset()));
					    request(1);
				    }
			    }
			  )
		  );
	}

	private static void simulateLatency() {
		simulateLatency(null);
	}

	private static void simulateLatency(Object data) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			//
		}
	}
}
