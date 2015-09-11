package demo.scattergather;

import reactor.io.buffer.Buffer;

import java.util.List;

public class DogeResponse {

	private final String toy;
	private final String food;

	public DogeResponse(String toy, List<String> food) {
		this.toy = toy;
		this.food = food.toString();
	}

	public Buffer toJSON() {
		return Buffer.wrap(
		  "{ \"food\" : \"" + food + "\"," +
		  " \"toy\" : \"" + toy + "\" }"
		);
	}
}
