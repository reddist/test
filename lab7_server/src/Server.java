import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Главный класс программы, содержащий исполняемый метод.
 * @see Server#run()
 */
public class Server implements Runnable{

    static Connection connection;

    static Statement statement;

    /**
     * Константа, хранит название системной переменной, в которой лежит адрес файла с состоянием коллекции между вызовами программы.
     */
    private final String SYSTEM_VARIABLE = "lab7";
    /**
     * Переменная хранит дату инициализации Коллекции, или дату очередного запуска программы.
     * @see Server#getInitializationDate() "геттер"
     */
    private LocalDateTime initializationDate;

    /**
     * Переменная хранит адрес файла с данными.
     */
    private String path;

    private ConcurrentHashMap<Creature, Creature> objects;

    /**
     * Сетевой канал для связи с клиентами.
     */
    private ServerSocketChannel serverSocket;

    /**
     * Лист с клиентами.
     */
    private ArrayList<Client> clients = new ArrayList<>();

    /**
     * Номер порта.
     */
    private int i = 1488;

    private MyReader reader;
    private CommandExecutor executor;

    public Server(){
        path = System.getenv(SYSTEM_VARIABLE);
        objects = new ConcurrentHashMap<>();
        initializationDate = LocalDateTime.now();
        reader = new MyReader(path, objects);

        executor = new CommandExecutor(objects, initializationDate);
        /*try {                                       // Первичное считывание из файла.
            String result = reader.read();
            if(result.charAt(result.length() - 2) == 'F') {
                System.out.println("Файла не существует.");
                System.exit(0);
            }
            for(Creature cr : objects.keySet()){

            }
        } catch (IOException e){
            System.out.println(">>> С первичным импортом из файла что-то не так.");
            executor.exit(reader);
        }*/
        /*
        try {                                       // Первичное считывание из базы данных.
            reader.readFromDataBase();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
         */
    }

    /**
     * &#x0418;сполняемый метод программы.
     */
    public void run(){
        connection = ConnectionWithDataBase();
        createTables();
        SocketAddress socketAddress = new InetSocketAddress(i);
        System.out.println("Попытка запустить сервер...");
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(socketAddress);
            System.out.println("Порт: " + serverSocket.socket().getLocalPort());
        } catch (IOException e) {
            System.out.println("Что-то пошло не так.");
            e.printStackTrace();
            System.exit(-1);
        }
        while (serverSocket.isOpen()) {
            SocketChannel clientChannel = waitConnection();
            if (clientChannel == null) break;
            Client client = new Client(clientChannel, this, this.reader);
            clients.add(client);
            client.go();
        }
    }

    /**
     * Метод-"геттер" для поля initializationDate. Метод необходим классу CommandExecutor для выполнения команды info.
     * @see Server#initializationDate
     * @see CommandExecutor#info()
     * @return Возвращает дату инициализации.
     */
    public LocalDateTime getInitializationDate() {
        return initializationDate;
    }

    private Connection ConnectionWithDataBase() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5555/postgres";
            Connection connection = DriverManager.getConnection(url, "postgres", "UbtyfVj301");
            statement = connection.createStatement();
            //statement.executeQuery("INSERT INTO users (user_id, user_login, user_mail, user_password) VALUES (2, 'reddist', 'erddist@gmail.com', 'qwerty');");
            return connection;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void createTables(){
        String createUserTable = "CREATE TABLE IF NOT EXISTS users( "+
                    "user_id SERIAL PRIMARY KEY, " +
                    "user_login text unique, " +
                    "user_password text, " +
                    "user_mail text);";

        String createTableOfHatifnutts = "CREATE TABLE IF NOT EXISTS Hatifnutts( "+
                        "hatifnutt_key text unique PRIMARY KEY," +
                        "hatifnutt_name text, hatifnutt_height int, " +
                        "hatifnutt_x int, hatifnutt_y int, " +
                        "hatifnutt_date TIMESTAMP, " +
                        "owner text, " +
                        "FOREIGN KEY (owner) REFERENCES users (user_login))";

        String createTableOfHemuls = "CREATE TABLE IF NOT EXISTS Hemuls( "+
                "hemul_key text unique PRIMARY KEY," +
                "hemul_name text, hemul_sex text, hemul_vocation text, " +
                "hemul_height int, hemul_x int, hemul_y int, " +
                "hemul_date TIMESTAMP, " +
                "owner text, " +
                "FOREIGN KEY (owner) REFERENCES users (user_login))";

        String createTableOfMoomins = "CREATE TABLE IF NOT EXISTS Moomins( "+
                "moomin_key text unique PRIMARY KEY," +
                "moomin_name text, moomin_sex text, moomin_lastCall bigint, moomin_lastEnd bigint, moomin_energy float, " +
                "moomin_height int, moomin_x int, moomin_y int, " +
                "moomin_date TIMESTAMP, " +
                "owner text, " +
                "FOREIGN KEY (owner) REFERENCES users (user_login))";
        try {
            statement.execute(createUserTable);
            statement.execute(createTableOfHatifnutts);
            statement.execute(createTableOfHemuls);
            statement.execute(createTableOfMoomins);
            System.out.println("База данных готова");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }catch (NullPointerException e){
            System.out.println("Какой-то объект не проинициализирован");
        }
    }

    private SocketChannel waitConnection() {
        try {
            SocketChannel client;
            System.out.println("Ждём подключения");
            client = serverSocket.accept();
            System.out.println("Произошло подключение.");
            return client;
        } catch (ClosedByInterruptException e) {
            System.out.println("Сервер выключается.");
            try {
                serverSocket.close();
                clients.stream().forEach(c -> c.thread.interrupt());
            } catch (IOException ex) {
                System.out.println("Не удалось закрыть сетевой канал.");
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Что-то не так.");
            return null;
        }
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public ConcurrentHashMap<Creature, Creature> getObjects() {
        return objects;
    }

    public String getPath() {
        return path;
    }
}
