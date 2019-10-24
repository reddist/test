/*
        import Story.*;
        import java.io.*;
        import java.net.*;
        import java.nio.channels.ClosedByInterruptException;
        import java.nio.channels.ServerSocketChannel;
        import java.nio.channels.SocketChannel;
        import java.time.LocalDateTime;
        import java.util.*;
        import java.util.Date;
        import java.util.concurrent.ConcurrentSkipListMap;
        import java.sql.*;


public class Useless implements Runnable {
    static  Connection connection;
    static Statement statement;

    static Date initDate;
    static ConcurrentSkipListMap<String, Human> tree;
    private ServerSocketChannel serverSocket;
    static List<Handler> clients = new LinkedList<>();
    private int i = 2608;

    Server(File file, int i) {
        Collection();
        this.i = i;
    }

    Server(int i) {
        this.i = i;
        Collection();
    }

    Server(File file) {
        Collection();
    }

    Server() {
        Collection();
    }

    public void run() {
        connection = ConnectionWithDataBase();
        CreateTableAndInsertObjectsIfExists();

        SocketAddress socketAddress = new InetSocketAddress(i);
        System.out.println("Попытка запустить сервер...");
        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(socketAddress);
            System.out.println("Порт: " + serverSocket.socket().getLocalPort());
        } catch (IOException e) {
            System.out.println("Что-то пошло не так");
            System.exit(-1);
        }
        while (serverSocket.isOpen()) {
            SocketChannel clientChannel = waitConnection();
            if (clientChannel == null) break;
            Handler handler = new Handler(clientChannel);
            handler.go();
        }
    }

    private void Collection() {
        initDate = new Date();
        tree = new ConcurrentSkipListMap<>();
    }

    static int toLoad(String text, String token) {
        try {
            if (text != null) {
                text = text.replaceAll("<Objects>", "").trim();

                String exprs[] = text.split("</Human>");
                int j = 0;
                for (int i = 0; i + 1 < exprs.length; i++) {
                    exprs[i] = exprs[i].replaceAll(" ", "");
                    exprs[i] = exprs[i].replaceAll("Humantype", "Human type");
                    exprs[i] += "</Human>";
                    exprs[i] = exprs[i].trim();
                    Human human = fromXmlToObject(exprs[i]);
                    if (human != null) {
                        if (!tree.containsKey(human.getName())){
                            Handler.InsertObject(human.getName(),Handler.getLogByToken(token),human.getName(),
                                    human.getType(),human.getForm(),human.getHeight(),human.getWallet(),human.getLocation(),human.date );
                            tree.put(human.getName(), human);}
                    } else j++;
                }
                System.out.println("Коллекция успешно заполнена всеми инициализированными элементами. Не инициализировано " + j + " элементов.");
                return j;
            }

            return -1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }


    private static Human fromXmlToObject(String xml) {
        try {
            Human h;
            String type;
            String name;
            Form form;
            Height height;
            Wallet wallet;
            Location location;
            name = xml.replaceAll(" ", "").split("<name>")[1].split("</name>")[0].trim();
            switch (xml.replaceAll(" ", "").split("<form>")[1].split("</form>")[0].trim()) {
                case "fat":
                    form = Form.fat;
                    break;
                case "sports":
                    form = Form.sports;
                    break;
                case "thin":
                    form = Form.thin;
                    break;
                default:
                    throw new NullPointerException("Некорректное значение формы");
            }

            switch (xml.replaceAll(" ", "").split("<height>")[1].split("</height>")[0].trim()) {
                case "small":
                    height = Height.small;
                    break;
                case "medium":
                    height = Height.medium;
                    break;
                case "tall":
                    height = Height.tall;
                    break;
                default:
                    throw new NullPointerException("Некорректное значение роста");
            }


            wallet = new Wallet(Integer.parseInt(xml.replaceAll(" ", "").split("<crons>")[1].split("</crons>")[0].trim()));
            location = new Location(Integer.parseInt(xml.replaceAll(" ", "").split("<x>")[1].split("</x>")[0].trim()),
                    Integer.parseInt(xml.replaceAll(" ", "").split("<y>")[1].split("</y>")[0].trim()),
                    Integer.parseInt(xml.replaceAll(" ", "").split("<z>")[1].split("</z>")[0].trim()));
            if (name.equals("")) throw new NullPointerException("имя не задано или задано некорректно");
            if (xml.replaceAll(" ", "").contains("type=\"Carlson\"")) {
                type = "Carlson";
                h = new Carlson(name, height, form, wallet, type, location);

                return h;
            } else if (xml.replaceAll(" ", "").contains("type=\"Baby\"")) {
                type = "Baby";
                h = new Baby(name, height, form, wallet, type, location);
                return h;
            } else if ((xml.replaceAll(" ", "").contains("type=\"Friend\""))) {
                type = "Friend";
                h = new Friend(name, height, form, wallet, type, location);
                return h;
            } else
                throw new NullPointerException("тип элемента не задан или задан неверно");

        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (NumberFormatException e) {
            System.out.println("Значение crons задано неверно в одном из объектов");
            return null;
        }
    }

    static String CollectionToXML() {
        String xml = "<Objects>\n";
        ArrayList<Human> list = new ArrayList<>(tree.values());
        Collections.sort(list);
        for (int i = 0; i < list.size(); i++) {
            Human human = list.get(i);
            String xmlObj = "";
            if (human.getType().equals("Carlson")) xmlObj += "<Human type=\"Carlson\">\n";
            if (human.getType().equals("Baby")) xmlObj += "<Human type=\"Baby\">\n";
            if (human.getType().equals("Friend")) xmlObj += "<Human type=\"Friend\">\n";
            xmlObj += "<name>" + human.getName() + "</name>\n";
            xmlObj += "<height>" + human.getHeight() + "</height>\n";
            xmlObj += "<form>" + human.getForm() + "</form>\n";
            xmlObj += "<wallet>\n" + "<crons>" + human.getWallet().crons + "</crons>\n" + "</wallet>\n";
            xmlObj += "<location>\n" +
                    "<x>" + human.getLocation().x + "</x>\n" +
                    "<y>" + human.getLocation().y + "</y>\n" +
                    "<z>" + human.getLocation().z + "</z>\n" +
                    "</location>\n";
            xmlObj += "</Human>\n";
            xml += xmlObj;
        }
        xml += "</Objects>";
        return xml;
    }

    private SocketChannel waitConnection() {
        try {
            SocketChannel client;
            System.out.println("Ждём подключения");
            client = serverSocket.accept();
            System.out.println("Произошло подключение.");
            return client;
        } catch (ClosedByInterruptException e) {
            System.out.println("Сервер выключается");
            try {
                serverSocket.close();
                clients.forEach(c -> c.thread.interrupt());
            } catch (IOException ex) {
                System.out.println("Не удалось закрыть сетевой канал.");
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Что-то не так");
            return null;
        }
    }

    private Connection ConnectionWithDataBase() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/postgres";
            Connection connection = DriverManager.getConnection(url, "postgres", "postgres");
            statement = connection.createStatement();
            return connection;
        } catch (ClassNotFoundException e) {

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private void CreateTableAndInsertObjectsIfExists() {

        String createTableSQL = "CREATE TABLE IF NOT EXISTS USERS( "+
                "USER_ID SERIAL PRIMARY KEY," +
                " USER_LOGIN text unique , " +
                " USER_MAIL text unique ," +
                "USER_PASSWORD text," +
                "TOKEN text)";

        String createTableOfObjects=
                "CREATE TABLE IF NOT EXISTS OBJECTS( "+
                        "OBJECT_KEY text unique PRIMARY KEY," +
                        " OWNER  text," +
                        "OBJECT_NAME text, OBJECT_TYPE text, OBJECT_FORM FORM, OBJECT_HEIGHT HEIGHT," +
                        " OBJECT_WALLET int, OBJECT_DATE TIMESTAMP, " +
                        "FOREIGN KEY (OWNER) REFERENCES USERS (USER_LOGIN))";



        try {
            statement.execute(createTableSQL);
            statement.execute(createTableOfObjects);
            statement.execute("CREATE TABLE IF NOT EXISTS LOCATION(" +
                    "X integer,Y integer,Z integer, HUMAN text," +
                    "FOREIGN key (HUMAN) REFERENCES OBJECTS(OBJECT_KEY))");

            ResultSet resultSet=statement.executeQuery("SELECT * FROM OBJECTS,LOCATION");

            while (resultSet.next()) {
                String name=resultSet.getString("OBJECT_NAME");
                String type=resultSet.getString("OBJECT_TYPE");
                Form form=Form.valueOf(resultSet.getString("OBJECT_FORM"));
                Height height=Height.valueOf((resultSet.getString("OBJECT_HEIGHT")));

                int x=resultSet.getInt("X");
                int y=resultSet.getInt("Y");
                int z=resultSet.getInt("Z");
                int crons=resultSet.getInt("OBJECT_WALLET");
                LocalDateTime localDateTime=resultSet.getObject("OBJECT_DATE",LocalDateTime.class);
                Human human;
                switch (type){
                    case "Friend": human=new Friend(name,height,form,new Wallet(crons),type,new Location(x,y,z));break;
                    case "Carlson":human=new Carlson(name,height,form,new Wallet(crons),type,new Location(x,y,z));break;
                    case "Baby":human=new Baby(name,height,form,new Wallet(crons),type,new Location(x,y,z));break;
                    default: human=new Friend(name,height,form,new Wallet(crons),type,new Location(x,y,z));break;
                }
                human.date=localDateTime;
                tree.put(human.getName(),human);

            }

            System.out.println("База данных готова");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }catch (NullPointerException e){
            System.out.println("Какой-то объект не проинициализирован");
        }
    }

}
*/