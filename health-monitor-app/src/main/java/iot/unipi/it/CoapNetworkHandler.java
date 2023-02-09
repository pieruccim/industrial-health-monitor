package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONObject;

public class CoapNetworkHandler {

    private float temp_threshold = 90;
    private boolean cooler_enabled = false;
    
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

        if(clientTempSensor != null){
            System.out.println("Temperature sensor already registered!\n");
            return;
        }

        clientTempSensor = new CoapClient("coap://[" + ipAddress + "]/temperature_sensor");
        System.out.print("Temperature sensor with ip-address [" + ipAddress + "] is now registered to the Coap net\n" + "> ");

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
        
        if(clientCoolerActuator != null){
            System.out.println("Cooler actuator already registered!\n");
            return;
        }

        clientCoolerActuator = new CoapClient("coap://[" + ipAddress + "]/cooler_actuator");
        System.out.print("Cooler actuator with ip-address [" + ipAddress + "] is now registered to the Coap net\n" + "> ");
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

                /* activate or deactivate the cooler if sensed temperature is above or below temperature threshold */
                if (temp_value > temp_threshold && !cooler_enabled) {
                    
                    System.out.print("Temperature above threshold: activating cooler...\n" + "> ");

                    triggerCoolerActuator("activate");
                    notifyTempSensor("activate");
                    cooler_enabled = true;

                } else if(temp_value <= temp_threshold && cooler_enabled){

                    System.out.print("Temperature below threshold: deactivating cooler...\n" + "> ");

                    triggerCoolerActuator("deactivate");
                    notifyTempSensor("deactivate");
                    cooler_enabled = false;

                }

                /* save temperature data to DB (only when notification data arrives) */
                sensorDB.insertTemperatureRecord(temp_value);

            } else{
                /* response to the get request received */
                System.out.format("\nCurrent machine temperature is %.2f C\n\n", temp_value);
            }

        } catch (Exception e) {
            System.err.println("Response received was not valid: " + e.getMessage() + "\n");;
        }
        
    }

    private void triggerCoolerActuator(String payload){
        
        if(clientCoolerActuator != null){

            clientCoolerActuator.put(new CoapHandler() {
                
                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if(!response.isSuccess())
                            System.out.println("Error with cooler actuator!\n");
                    }
                }

                public void onError() {
                    System.err.println("Error with cooler PUT request!\n");
                }

            }, payload, MediaTypeRegistry.TEXT_PLAIN);

        } else{
            System.out.println("Error: no cooler actuator registered!");
        }
    }

    private void notifyTempSensor(String payload){

        if(clientTempSensor != null){

            clientTempSensor.put(new CoapHandler() {
                
                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if(!response.isSuccess())
                            System.out.println("Error with temperature sensor!\n");
                    }
                }

                public void onError() {
                    System.err.println("Error with temperature PUT request!\n");
                }

            }, payload, MediaTypeRegistry.TEXT_PLAIN);

        } else{
            System.out.println("Error: no temperature sensor registered!");
        }
    }

    public void showTempThreshold() {
        System.out.format("\nCritical temperature threshold is set to %.2f C\n\n", temp_threshold);
    }

    public void editTempThreshold(float new_th) {
        temp_threshold = new_th;
    }

    public void printTempSensor() {
        if(clientTempSensor != null){
            System.out.println("\t- [temperature]\t" + clientTempSensor.getURI());
        } else{
            System.out.println("\t- [temperature]\t" + "Offline");
        }
    }

    public void printCoolerActuator() {
        if(clientCoolerActuator != null){
            System.out.println("\t- [cooler]\t" + clientCoolerActuator.getURI());
        } else{
            System.out.println("\t- [cooler]\t" + "Offline");
        }
    }

    public void cutAllConnection() {
        if (observeSensor != null) {
            observeSensor.proactiveCancel();
        }
    }

    public boolean deleteTemperatureSensor() {
        if (clientTempSensor == null) {
            return false;
        }
        clientTempSensor = null;
        return true;
    }

    public boolean deleteCoolerActuator(){
        if (clientCoolerActuator == null) {
            return false;
        }
        clientCoolerActuator = null;
        return true;
    }
}
