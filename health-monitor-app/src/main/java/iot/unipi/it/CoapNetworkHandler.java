package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

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

        clientTempSensor = new CoapClient("coap://[" + ipAddress + "]/temperature-sensor");
        System.out.println("Temperature sensor with ip-address [" + ipAddress + "] is now registered to the Coap net");

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
        // TODO: handle the response of the get request depending on server sensor
        return 0;
    }
}
