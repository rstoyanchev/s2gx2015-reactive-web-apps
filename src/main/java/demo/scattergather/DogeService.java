package demo.scattergather;

import reactor.io.buffer.Buffer;
import reactor.rx.Stream;

import static reactor.io.net.NetStreams.httpClient;

public class DogeService {


	final int port;

	public DogeService(int port) {
		this.port = port;
	}

	public Stream<String> send(){
/*
		try {
			Thread.sleep(50000);
		}catch (InterruptedException ie){

		}*/

		return httpClient()
		  .get("http://localhost:" + port + "/")
		  .flatMap(resp -> resp.map(Buffer::asString));
	}

}
