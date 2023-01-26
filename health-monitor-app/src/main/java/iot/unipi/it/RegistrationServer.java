package iot.unipi.it;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

class CoAPResourceExample extends CoapResource {
	public CoAPResourceExample(String name) {
		super(name);
		setObservable(true);
	}
	public void handleGET(CoapExchange exchange) {
		exchange.respond("hello world");
	}
	public void handlePOST(CoapExchange exchange) {
		/* your stuff */
		exchange.respond(ResponseCode.CREATED);
	}
}

public class RegistrationServer extends CoapServer {

	public static void main(String[] args) {
		System.out.print("Running it!");

		RegistrationServer server = new RegistrationServer();
		server.add(new CoAPResourceExample("hello")); // the argument string is the path of this server
		server.start();

	}

}
