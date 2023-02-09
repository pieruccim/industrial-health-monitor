package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class HealthDataCollector {

    private static void printCommandList(){
        System.out.println("\nIndustrial Health monitor command list:\n");
        System.out.println("\t!help\t\t\t\t return a list of possible commands");
        System.out.println("\t!checkTemp\t\t\t get current machine temperature");
        System.out.println("\t!showCritTemp\t\t\t show critical temperature threshold");
        System.out.println("\t!editCritTemp <new_value>\t edit critical temperature threshold");
        System.out.println("\t!checkVibr\t\t\t get current machine vibration");
        System.out.println("\t!devList\t\t\t return a list of registered devices");
        System.out.println("\t!readData <sensor_type>\t\t display latest sensor data");
        System.out.println("\t!exit\t\t\t\t close the program");
        System.out.println("\n");
    }

    public static void main(String[] args) throws SocketException{

        System.out.println("Health Data Collector is running...\n");

        RegistrationServer rs = new RegistrationServer();
        rs.start();

        MqttClientManager cm = new MqttClientManager(); 

        // buffer to receive input from command line
        BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));

        // variables to receive and process input command
        String command = "";
        String[] tokens;

        printCommandList();

        while (true){
            try {
                System.out.print("> ");
                command = buf.readLine();
                tokens = command.split(" ");

                if (tokens[0].equals("!help")){

                    printCommandList();
                }
                else if (tokens[0].equals("!checkTemp")){

                    rs.checkTemperature();
                } else if (tokens[0].equals("!showCritTemp")) {
                    

                    rs.showTempThreshold();
                }
                else if (tokens[0].equals("!editCritTemp")) {
                    
                    if (tokens.length == 2) {

                        float new_th;
                        try {
                            new_th = Float.parseFloat(tokens[1]);
                        } catch (Exception e) {
                            System.out.println("\nNot parsable parameter error: new threshold value is not numerical!\n");
                            continue;
                        }
                        
                        rs.editTempThreshold(new_th);
                        System.out.println("\nCritical temperature threshold correctly updated!\n");

                    } else {
                        System.out.println("\nError: wrong command parameters!\n");
                    }
                    
                }
                else if (tokens[0].equals("!checkVibr")) {
                    cm.checkVibration();
                }
                else if(tokens[0].equals("!devList")){
                    rs.listOnlineDevices();
                }
                else if(tokens[0].equals("!readData")){

                    if (tokens.length == 2) {

                        String sensor_type = tokens[1];
                        
                        if (sensor_type.equals("temperature") || sensor_type.equals("vibration")) {

                            rs.printStoredData(sensor_type);
                        } else{

                            System.out.println("\nIncorrect parameter error: sensor type is not corect!\n");
                        }
                        

                    } else {
                        System.out.println("\nError: wrong command parameters!\n");
                    }
                
                }
                else if (tokens[0].equals("!exit")){
                    
                    rs.exit();
                    System.exit(1);
                    
                } else {
                    throw new IOException();
                }

            } catch (IOException e){
                System.out.println("Error: command not found, please use help command for a list of available commands!\n");
            }
        }
    }
}
