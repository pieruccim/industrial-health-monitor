package iot.unipi.it;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.json.JSONObject;

public class MqttClientManager implements MqttCallback{

    private final String broker = "tcp://127.0.0.1:1883";
    private final String clientID = "MqttManager";
    private MqttClient client = null;

    private final String vibrationSubTopic = "vibration";

    private float current_vibration = 0;

    public MqttClientManager() {

        do {
            
            try {

                this.client = new MqttClient(this.broker, this.clientID);
                System.out.println("MQTT Manager connecting to broker -> " + broker);

                /* Set callback listener for asynchronous events */
                this.client.setCallback(this);
                this.client.connect();

                this.client.subscribe(vibrationSubTopic);
                System.out.println("Subscribed to topic: "+ this.vibrationSubTopic);

            } catch (MqttException e) {

                System.out.println("MQTT can not connect!");
                e.printStackTrace();
            }
            
        } while (!this.client.isConnected());

    }

    @Override
    public void connectionLost(Throwable cause) {
        
        System.out.println("Connection lost, attempting to reconnect...");
        int reconnWindow = 3000;

        while(!client.isConnected()){
            try{
                System.out.println("Reconnecting in " + reconnWindow/1000 + " seconds");
                Thread.sleep(reconnWindow);

                System.out.println("Reconnecting . . .");
                client.connect();
                reconnWindow *= 2;

                //Now we can subscribe to the topics
                client.subscribe(vibrationSubTopic);
                System.out.printf("Resubscribed to %s topic!\n", vibrationSubTopic);

            }catch(MqttException | InterruptedException mqtte){
                System.out.println("Error during the reconnection " + throwable.getCause().getMessage());
            }
        }
        
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        
        byte[] payload = message.getPayload();
        
        JSONObject jsonMessage = new JSONObject(new String(payload));

        if (topic.equals(this.vibrationSubTopic)) {
            
            if (jsonMessage.has("vibration")) {
                
                /* parsing vibration data from JSON */
                this.current_vibration = (float) jsonMessage.getDouble("vibration");
                String unit = jsonMessage.getString("unit");

                /* storing vibration data to DB */
                sensorDB.insertVibrationRecord(this.current_vibration, unit);
            }

        } else{
            System.out.print("JSON data received is not valid!");
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        
        System.out.println("Delivery completed!");
        
    }

    public void checkVibration() {

        if (client.isConnected()) {

            System.out.format("\nCurrent machine vibration is %.1f Hz\n\n", current_vibration);
        } else{

            System.out.println("\nVibration sensor is not connected!\n");
        }
       
    }
    
}
