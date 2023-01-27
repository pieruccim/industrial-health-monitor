package iot.unipi.it;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HealthDataCollector {

    private static void printCommandList(){
        System.out.println("\n Industrial Health monitor command list:");
        System.out.println("\t!help: return a list of possible commands");
        System.out.println("\t!checkTemp: get current machine temperature");
        System.out.println("\t!exit: close the program");
        System.out.println("\n");
    }

    public static void main(String[] args){

        System.out.println("Health Data Collector is running...");

        RegistrationServer rs = new RegistrationServer();
        rs.start();

        // buffer to receive input from command line
        BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));

        // variables to receive and process input command
        String command = "";
        String[] tokens;

        printCommandList();

        while (true){
            try {
                command = buf.readLine();
                tokens = command.split(" ");

                if (tokens[0].equals("!help")){
                    printCommandList();
                }
                else if (tokens[0].equals("!checkTemp")){
                    float temp = rs.checkTemperature();
                    if (temp == -1){
                        System.out.println("Temperature sensor not registered!");
                    } else{
                        System.out.format("Current machine temperature is %f °C\n", temp);
                    }
                }
                else if (tokens[0].equals("!exit")){
                    System.exit(1);
                } else {
                    throw new IOException();
                }

            } catch (IOException e){
                System.out.println("Error: command not found, please use help command for a list of available commands!");
            }
        }
    }
}
