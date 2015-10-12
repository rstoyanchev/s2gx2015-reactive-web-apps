package demo.backpressure;

import reactor.rx.Stream;
import reactor.rx.Streams;

import java.util.Random;

public class DummyDataRepository {


	static byte[] DUMMY_DATA;

	static {
		DUMMY_DATA = new byte[8192 * 10];
		Random random = new Random();
		random.nextBytes(DUMMY_DATA);
	}

	public Stream<byte[]> findAll(){
		return Streams.range(1, Integer.MAX_VALUE).map( n -> DUMMY_DATA);
	}

}
