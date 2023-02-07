package iot.unipi.it;

import org.eclipse.paho.client.mqttv3.*;

public class MqttClientManager {

    private final String broker = "tcp://127.0.0.1:1884";
    private final String clientID = "MqttManager";
    private MqttClient client = null;

    private final String vibrationSubTopic = "vibration";

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
    
}
