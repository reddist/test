import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Класс, в котором создаётся коллекция и запускается работа с ней.
 * @see Server
 */

public class Main {

    public static void main(String[] args) {
        //LocalDateTime defaulted = LocalDateTime.parse("2018-10-22T20:15:56");
        //System.out.println(defaulted.plusHours(10));
        //Hemul hemul = new Hemul(Sex.MALE, "Man", new CollectRareFlowers(), 50, -9, 10, LocalDateTime.parse("2015-07-18T06:05:04"));
        //Hemul hemulCopy = new Hemul(Sex.MALE, "Man", new CollectRareFlowers(), 50, -9, 10, LocalDateTime.parse("2015-07-18T06:05:04"));
        //System.out.println(hemul.equals(hemulCopy));
        //System.out.println(hemul.hashCode());
        //System.out.println(hemulCopy.hashCode());
        //add_if_min{ "Class" : "Hatifnutt", "Name" : "Mary", "Height" : 192, "X" : -89, "Y" : -77, "Date" : "2018-06-27T05:51:52"}
        /*Troll mummie = new Troll();
        Troll mummieCopy = new Troll();
        Hemul hemulCopy = new Hemul();
        Hatifnutt hatifCopy = new Hatifnutt();

        System.out.println(mummie.csvDescription());
        System.out.println(hemul.csvDescription());
        System.out.println(hatif.csvDescription());

        System.out.println(mummie.toString());
        System.out.println(hemul.toString());
        System.out.println(hatif.toString());

        System.out.println(mummie.equals(mummieCopy));
        System.out.println(hemul.equals(hemulCopy));
        System.out.println(hatif.equals(hatifCopy));
        //System.out.println("add{\"Class\":\"Troll\",\"Name\":\"Groll\",\"Sex\":\"MALE\",\"LastCall\":100,\"LastEnd\":-150,\"Energy\":156.98}".matches("[a-z_]+(\\{((\"[\\w]+\":)((\"[\\w]+\",)|(-?[0-9]+(\\.[0-9]+)?,))){5}((\"[\\w]+\":)((\"[\\w]+\")|(-?[0-9]+(\\.[0-9]+)?)))\\})"));
        //add{"Class" : "Troll", "Name" : "Жмылёв", "Sex" : "MALE", "LastCall" : -100, "LastEnd" : 150, "Energy" : -156.98, "Height" : 192, "X" : -89, "Y" : -77, "Date" : "2018-06-27T05:51:52"}
        */
        try {
            Server server = new Server();
            Thread serverThread = new Thread(server);
            serverThread.start();
            boolean exit = false;
            Scanner scanner = new Scanner(System.in);
            String s;
            try {
                while (!exit) {
                    s = scanner.next();
                    exit = s.equals("exit");
                    if (s.equals("show")) {
                        server.getClients().stream().forEach(c -> System.out.println(c.getId()));
                    }
                    if (s.equals("show tables")){
                        Connection connection = Server.connection;
                    }
                }
                serverThread.interrupt();
            } catch (NoSuchElementException e) {
                serverThread.interrupt();
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    serverThread.interrupt();
                    Server.statement.close();
                    Server.connection.close();
                    finish(server);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(">>> Данные не сохранены.");
                }
            }));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void finish(Server server) throws IOException{
        MyReader writer = new MyReader(server.getPath(), server.getObjects());
        System.out.println(writer.write());
    }
}
