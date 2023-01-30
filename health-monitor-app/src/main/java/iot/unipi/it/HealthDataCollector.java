package iot.unipi.it;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class HealthDataCollector {

    private static void printCommandList(){
        System.out.println("\nIndustrial Health monitor command list:");
        System.out.println("\t-!help\t return a list of possible commands");
        System.out.println("\t-!checkTemp\t get current machine temperature");
        System.out.println("\t-!exit\t close the program");
        System.out.println("\n");
    }

    public static void main(String[] args) throws SocketException{

        System.out.println("Health Data Collector is running...\n");

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
                System.out.print("> ");
                command = buf.readLine();
                tokens = command.split(" ");

                if (tokens[0].equals("!help")){
                    printCommandList();
                }
                else if (tokens[0].equals("!checkTemp")){
                    float temp = rs.checkTemperature();
                    if (temp == -1){
                        System.out.println("Temperature sensor not registered!\n");
                    } else{
                        System.out.format("Current machine temperature is %.2f C\n", temp);
                    }
                }
                else if (tokens[0].equals("!exit")){
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
