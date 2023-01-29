package iot.unipi.it;

import java.net.SocketException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.nio.charset.StandardCharsets;


public class RegistrationServer extends CoapServer {

    private final static CoapNetworkHandler coapHandler = CoapNetworkHandler.getInstance();

    public RegistrationServer() throws SocketException {
        // add registration resource to the server
        this.add(new RegistrationResource());
    }

    public float checkTemperature() {
        return coapHandler.checkTemperature();
    }

    class RegistrationResource extends CoapResource {

        public RegistrationResource() {
            // '/registration' is set as endpoint of Coap registration requests
            super("registration");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {

            String deviceName = exchange.getRequestText();
            String ipAddress = exchange.getSourceAddress().getHostAddress();

            if (deviceName.equals("temperature_sensor")) {
                coapHandler.addTemperatureSensor(ipAddress);
            } else {

            }
            exchange.respond(ResponseCode.CREATED, "Device registration completed!".getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void handleDELETE(CoapExchange exchange) {
            // TODO: write code to handle delete request
        }
    }
}
