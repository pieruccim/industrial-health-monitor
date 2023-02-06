package iot.unipi.it;
import java.sql.*;

public class sensorDB {

    private static final String username = "root";
    private static final String password = "root";
    private static final String databaseName = "industrial_health_monitor";
    
    private static final String ipAddress = "localhost";
    private static final String port = "3306";

    private static Connection makeConnection(){

        Connection dbConnection = null;

        try {

            /* DriverManager object to handle DB connection */
            String url = "jdbc:mysql://" + ipAddress + ":" + port + "/" + databaseName; 
            dbConnection = DriverManager.getConnection(url, username, password);

            if (dbConnection == null) {
                System.err.println("Connection object dbConnection is Null");
            }

        } catch (Exception e) {
            System.err.println("makeConnection() function throws an exeption");
            e.printStackTrace();
        }

        return dbConnection;
    }

    public static ResultSet readData(String table, int count){

        ResultSet resSet = null;
        String readQuery = "SELECT * FROM " + table + " ORDER BY timestamp DESC LIMIT " + count;

        try (
            Connection conn = makeConnection();
            PreparedStatement statement = conn.prepareStatement(readQuery);
        ) {

            resSet = statement.executeQuery(readQuery);

            ResultSetMetaData rsmd = resSet.getMetaData();
            int columnCount = rsmd.getColumnCount();

            System.out.println("\tid\ttimestamp\t\t" + table + "\t\tunit");

            while (resSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print("\t" + resSet.getString(i));
                }
                System.out.println();
            }

            System.out.println();

        } catch (SQLException sqle) {

            sqle.printStackTrace();
        } 
        return resSet;
    }

    public static void insertTemperatureRecord(float temperature_value){

        String insertQuery = "INSERT INTO temperature(temp_value) values (?)";

        try (
            Connection conn = makeConnection();
            PreparedStatement statement = conn.prepareStatement(insertQuery);
        ) {

            /* insert value into query */
            statement.setFloat(1, temperature_value);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                //System.out.println("Temperature record correctly inserted to DB!");
            }
            
        } catch (SQLException sqle) {
            
            sqle.printStackTrace();
        }
    }

    public static void insertVibration(){
        // TODO: implement funtion to store vibration data to DB
    }

}
