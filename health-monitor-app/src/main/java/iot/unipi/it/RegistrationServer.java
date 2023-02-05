package iot.unipi.it;

import java.net.SocketException;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;


public class RegistrationServer extends CoapServer {

    private final static CoapNetworkHandler coapHandler = CoapNetworkHandler.getInstance();

    public RegistrationServer() throws SocketException {
        // add registration resource to the server
        this.add(new RegistrationResource());
    }

    public void checkTemperature() {
        coapHandler.checkTemperature();
    }

    public void showTempThreshold() {
        coapHandler.showTempThreshold();
    }

    public void editTempThreshold(float new_th) {
        coapHandler.editTempThreshold(new_th);
    }

    public void listOnlineDevices() {
        coapHandler.printTempSensor();
        coapHandler.printCoolerActuator();
        System.out.println();
    }

    class RegistrationResource extends CoapResource {

        public RegistrationResource() {
            // '/registration' is set as endpoint of Coap registration requests
            super("registration");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            /* handle sensor add requests in format
             * { "device": "temperature_sensor"}
             */
            //System.out.println(exchange.getRequestText());

            JSONObject responseJson = null;
            String deviceName = "";

            try {
                responseJson = new JSONObject(exchange.getRequestText());
                deviceName = responseJson.getString("device");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String ipAddress = exchange.getSourceAddress().getHostAddress();

            if (deviceName.equals("temperature_sensor")) {
                coapHandler.addTemperatureSensor(ipAddress);
            } else if(deviceName.equals("cooler_actuator")){
                coapHandler.addCoolerActuator(ipAddress);
            }

            exchange.respond(ResponseCode.CREATED, "reg_completed".getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void handleDELETE(CoapExchange exchange) {
            /* handle sensor delete requests in format
             * { "device": "temperature_sensor"}
            */

            JSONObject responseJson;
            String deviceName = "";
            boolean success = false;

            try {

                responseJson = new JSONObject(exchange.getRequestText());
                deviceName = responseJson.getString("device");

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (deviceName.equals("temperature_sensor")) {
                success = coapHandler.deleteTemperatureSensor();
            } else if(deviceName.equals("cooler_actuator")){
                success = coapHandler.deleteCoolerActuator();
            }
            
            if (success) {
                exchange.respond(ResponseCode.DELETED, "dev_deleted".getBytes(StandardCharsets.UTF_8));
            } else{
                exchange.respond(ResponseCode.DELETED, "dev_not_deleted".getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
