package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import org.json.JSONObject;

public class CoapNetworkHandler {
    
    private CoapClient clientTempSensor;
    private CoapClient clientCoolerActuator;
    private CoapObserveRelation observeSensor;
    
    private static CoapNetworkHandler instance = null;
    
    public static CoapNetworkHandler getInstance() {
        if (instance == null){
            instance = new CoapNetworkHandler();
        }
        return instance;
    }

    public void addTemperatureSensor(String ipAddress){

        clientTempSensor = new CoapClient("coap://[" + ipAddress + "]/temperature_sensor");
        System.out.println("Temperature sensor with ip-address [" + ipAddress + "] is now registered to the Coap net\n");

        observeSensor = clientTempSensor.observe(
                new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse coapResponse) {
                        handleTemperatureResponse(coapResponse);
                    }

                    @Override
                    public void onError() {
                        System.err.println("Temperature observing failed");
                    }
                });
    }

    public void addCoolerActuator(String ipAddress){
        
        clientCoolerActuator = new CoapClient("coap://[" + ipAddress + "]/cooler_actuator");
        System.out.println("Cooler actuator with ip-address [" + ipAddress + "] is now registered to the Coap net\n");
    }

    public void checkTemperature() {
        if (clientTempSensor != null){
            CoapResponse res = clientTempSensor.get();
            handleTemperatureResponse(res);
        } else{
            // temperature sensor has not been initialized yet
            System.out.println("\nTemperature sensor not registered!\n");
        }
    }

    private void handleTemperatureResponse(CoapResponse res) {
        try {

            /* handle the response of the get request which has a JSON payload
             * { "temp": float}
             */


            String responseString = res.getResponseText();

            JSONObject responseJson = new JSONObject(responseString);
            float temp_value = (float) responseJson.getDouble("temp");

            if (res.getOptions().hasObserve()) {
                /* notification received */
                //System.out.format("\nNotification: temperature %.2f C need to be stored to DB\n", temp_value);
                // TODO : save to DB the temperature notified

            } else{
                /* response to the get request received */
                System.out.format("\nCurrent machine temperature is %.2f C\n", temp_value);
            }

        } catch (Exception e) {
            System.err.println("Response received was not valid: " + e.getMessage() + "\n");;
        }
        
    }

    private void enableCoolerActuator(String respString){
        // TODO
    }

    public boolean deleteTemperatureSensor() {
        if (clientTempSensor == null) {
            return false;
        }
        clientTempSensor = null;
        return true;
    }
}
