package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import org.json.JSONObject;

public class CoapNetworkHandler {
    
    private CoapClient clientTempSensor;
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

    public float checkTemperature() {
        if (clientTempSensor != null){
            CoapResponse res = clientTempSensor.get();
            return handleTemperatureResponse(res);
        }
        // temperature sensor has not been initialized yet
        return -1;
    }

    private float handleTemperatureResponse(CoapResponse res) {
        try {

            // handle the response of the get request which has a JSON payload
            String responseString = res.getResponseText();

            JSONObject responseJson = new JSONObject(responseString);
            float temp_value = (float) responseJson.getDouble("temp");

            return temp_value;

        } catch (Exception e) {
            System.err.println("Response received was not valid: " + e.getMessage() + "\n");
            return 0;
        }
        
    }
}
