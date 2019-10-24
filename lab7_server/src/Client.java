import java.io.*;
import java.math.BigDecimal;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Random;

public class Client {
    private SocketChannel socketChannel;
    public Thread thread;
    private Server server;
    private static int name = 0;
    private int id;
    private Socket clientSocket;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private CommandExecutor executor;
    private MyReader saver;
    private String login;
    private boolean isLogged;
    private CreatureFactory factory;

    public Client(SocketChannel socketChannel, Server server, MyReader saver) {
        this.id = ++name;
        this.server = server;
        this.socketChannel = socketChannel;
        this.clientSocket = socketChannel.socket();
        this.executor = new CommandExecutor(server.getObjects(), server.getInitializationDate());
        this.saver = saver;
        this.isLogged = false;
        try {

            InputStream inputClientStream = clientSocket.getInputStream();
            OutputStream outClientStream = clientSocket.getOutputStream();

            writer = new ObjectOutputStream(outClientStream);
            reader = new ObjectInputStream(inputClientStream);
            System.out.println("Клиент номер " + id + " подключился к серверу");
        } catch (IOException e) {
            System.out.println("Поток ввода не получен");
            System.exit(0);
        }
    }

    private void serveClient() {
        while (!clientSocket.isClosed()) {
            try {
                if(this.isLogged) {
                    sendMessage("> ");
                    String command = getMessage();

                    switch (command) {
                        case "import": {
                            String imported = getMessage();
                            if (imported.equals("")) break;
                            sendMessage(executor.importString(imported, login));
                            break;
                        }

                        case "add": {
                            Creature creature = (Creature) reader.readObject();
                            if (creature == null) break;
                            creature.setOwner(login);
                            sendMessage(executor.add(creature));
                            break;
                        }

                        case "add_if_min": {
                            Creature creature = (Creature) reader.readObject();
                            if (creature == null) break;
                            creature.setOwner(login);
                            sendMessage(executor.add_if_min(creature));
                            break;
                        }

                        case "remove": {
                            Creature creature = (Creature) reader.readObject();
                            if (creature == null) break;
                            creature.setOwner(login);
                            sendMessage(executor.remove(creature));
                            break;
                        }

                        case "remove_lower": {
                            Creature creature = (Creature) reader.readObject();
                            if (creature == null) break;
                            creature.setOwner(login);
                            sendMessage(executor.remove_lower(creature));
                            break;
                        }

                        case "remove_greater": {
                            Creature creature = (Creature) reader.readObject();
                            if (creature == null) break;
                            creature.setOwner(login);
                            sendMessage(executor.remove_greater(creature));
                            break;
                        }

                        case "show": {
                            executor.show(this);
                            break;
                        }

                        case "info": {
                            sendMessage(executor.info());
                            sendMessage("\n");
                            break;
                        }

                        case "save": {
                            sendMessage(executor.save(saver));
                            break;
                        }

                        case "load": {
                            String result = saver.load(login);
                            if (result.charAt(result.length() - 2) == 'F')
                                sendMessage(result.substring(0, result.length() - 2) + "\n");
                            else
                                sendMessage(result);
                            break;
                        }

                        case "exit": {
                            sendMessage("Goodbye!\n");
                            close();
                            break;
                        }

                        case "change_password":{
                            change_password();
                            break;
                        }
                        case "help": {
                            sendMessage(executor.help());
                         /*sendMessage("Доступные команды:\n" +
                                 "insert {String key} {element}: добавить новый элемент с заданным ключом\n" +
                                 "show: вывести в стандартный поток вывода все элементы коллекции в строковом представлении\n" +
                                 "info: вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)\n" +
                                 "remove_all {element}: удалить из коллекции все элементы, эквивалентные заданному\n" +
                                 "remove_lower {element}: удалить из коллекции все элементы, меньшие, чем заданный\n" +
                                 "remove {String key}: удалить элемент из коллекции по его ключу\n" +
                                 "load: перечитать коллекцию из файла\n" +
                                 "exit: выйти из приложения, сихранив данные в файл");*/
                            break;
                        }
                        case "short": {
                            if(executor.isShort) {
                                sendMessage("Description is already shown in a short form.\n");
                            } else {
                                sendMessage("Now description would be shown in a short form.\n");
                            }
                                executor.isShort = true;
                            break;
                        }
                        case "full": {
                            if(executor.isShort) {
                                sendMessage("Now description would be shown in a full form.\n");
                            } else {
                                sendMessage("Description is already shown in a full form.\n");
                            }
                            executor.isShort = false;
                            break;
                        }
                        case "wrong": {
                            sendMessage("Wrong command.\n");
                            break;
                        }
                        default: {
                            sendMessage(command + " - wrong command.\n");
                            break;
                        }
                        case "Ivan":{
                            sendMessage("Ivan");
                            sendMessage("Give me points in personal qualities, please!!!\n");
                            break;
                        }
                        case "Aleksandr":{
                            sendMessage("Aleksandr");
                            sendMessage("Give me points in personal qualities, please!!!\n");
                            break;
                        }
                    }
                } else {
                    boolean hasLogin = false;
                    while(!hasLogin && (!clientSocket.isClosed())) {
                        sendMessage("for login");
                        sendMessage("Do you have an account? [yes / no]\n> ");
                        boolean notLogged = true;
                        while (notLogged && (!clientSocket.isClosed())) {
                            String accountResponse = getMessage();
                            if (accountResponse == null) {
                                close();
                            }
                            switch (accountResponse) {
                                case "yes": {
                                    hasLogin = true;
                                    notLogged = false;
                                    tryLogin();
                                    break;
                                }
                                case "no": {
                                    sendMessage("for login");
                                    sendMessage("Do you want to register? [yes / no]\n> ");
                                    boolean wantRegister = false;
                                    while (!wantRegister && (!clientSocket.isClosed())) {
                                        String registerResponse = getMessage();
                                        if (registerResponse == null) {
                                            close();
                                        }
                                        switch (registerResponse) {
                                            case "yes": {
                                                wantRegister = true;
                                                register();
                                                notLogged = false;
                                                break;
                                            }
                                            case "no": {
                                                sendMessage("Goodbye!\n");
                                                close();
                                                break;
                                            }
                                            default: {
                                                sendMessage("for login");
                                                sendMessage("Please, type 'yes' or 'no'.\n> ");
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                }
                                default: {
                                    sendMessage("for login");
                                    sendMessage("Please, type 'yes' or 'no'.\n> ");
                                    break;
                                }
                            }
                        }
                    }

                }
            } catch (NullPointerException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
                close();
                break;
            } catch (SQLException e){
                e.printStackTrace();
                System.out.println(e.getMessage());
                close();
                break;
            }
        }
    }

    private void tryLogin() throws SQLException, NullPointerException {
        while (!this.isLogged && (!clientSocket.isClosed())) {
            sendMessage("for login");
            sendMessage("login: ");
            String gotLogin = getMessage();

            Connection connection = Server.connection;
            if (connection == null) {
                System.out.println("Bitch!!");
            }
            PreparedStatement findLogin = connection.prepareStatement("SELECT count(*) FROM users WHERE user_login=?");
            findLogin.setString(1, gotLogin);
            System.out.println("login attempt: \'" + gotLogin + "\'");
            ResultSet counted = findLogin.executeQuery();
            counted.next();
            BigDecimal count = counted.getBigDecimal("count");
            if (count.intValue() == 1) {
                System.out.println("valid.");
                sendMessage("for login");
                sendMessage("password: ");
                String gotPassword = getMessage();
                boolean truePassword = false;  // проверяем пароль в БД.
                connection = Server.connection;
                PreparedStatement findPassword = connection.prepareStatement("SELECT user_password FROM users WHERE user_login = ?");
                findPassword.setString(1, gotLogin);
                ResultSet passwordFromTable = findPassword.executeQuery();
                passwordFromTable.next();
                String password = passwordFromTable.getString("user_password");
                try {
                    gotPassword = CryptoHash.getHash(gotPassword);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                if (password.equals(gotPassword))
                    truePassword = true;
                if (truePassword) {
                    this.isLogged = true;
                    this.login = gotLogin;
                    this.executor.setLogin(gotLogin);
                    System.out.println("Correct password.\n");
                    return;
                } else {
                    sendMessage("for login");
                    sendMessage("Wrong password.\n");
                    System.out.println("Wrong password.\n");
                }
            } else {
                System.out.println("invalid.");
                sendMessage("for login");
                sendMessage("Wrong login.\n");
            }

            sendMessage("for login");
            sendMessage("Try again? [yes / no]\n> ");
            boolean wantContinue = false;
            while (!wantContinue && (!clientSocket.isClosed())) {
                String continueResponse = getMessage();
                if (continueResponse == null) {
                    close();
                }
                switch (continueResponse) {
                    case "yes": {
                        wantContinue = true;
                        break;
                    }
                    case "no": {
                        sendMessage("Goodbye!\n");
                        close();
                        break;
                    }
                    default: {
                        sendMessage("for login");
                        sendMessage("Please, type 'yes' or 'no'.\n> ");
                        break;
                    }
                }
            }
        }
    }

    private void register() throws SQLException {
        boolean allowedLogin = false;
        String sendedLogin = "";
        while (!allowedLogin && (!clientSocket.isClosed())) {
            sendMessage("for login");
            sendMessage("Please, type the login you prefer.\n> ");
            String preferedLogin = getMessage();
            if (preferedLogin == null) {
                close();
            }
            Connection connection = Server.connection;
            PreparedStatement findLogin = connection.prepareStatement("SELECT count(*) FROM users WHERE user_login=?");
            findLogin.setString(1, preferedLogin);
            ResultSet countLogin = findLogin.executeQuery();
            countLogin.next();
            BigDecimal count = countLogin.getBigDecimal("count");
            if (count.intValue() == 0) {
                sendedLogin = preferedLogin;
                allowedLogin = true;
            } else {
                sendMessage("for login");
                sendMessage("The login is already used. Try again? [yes / no]\n> ");
                boolean wantContinue = false;
                while (!wantContinue && (!clientSocket.isClosed())) {
                    String continueResponse = getMessage();
                    if (continueResponse == null) {
                        close();
                    }
                    switch (continueResponse) {
                        case "yes": {
                            wantContinue = true;
                            break;
                        }
                        case "no": {
                            sendMessage("Goodbye!\n");
                            close();
                            break;
                        }
                        default: {
                            sendMessage("for login");
                            sendMessage("Please, type 'yes' or 'no'.\n> ");
                            break;
                        }
                    }
                }
            }
        }
        sendMessage("for login");
        sendMessage("Please, type your email, we will send password to it.\n> ");
        String email = getMessage();
        if (email == null) {
            close();
        }
        final String sendLogin = sendedLogin;
        Thread sendingThread = new Thread(() -> {
            System.out.println("Sending email to \'" + email + "\'....");
            String sendedPassword = generateRandomString(5);
            System.out.println("Sended password: " + sendedPassword);
            JavaMail.registration(email, sendedPassword, sendLogin);
            System.out.println("Done sending email to \'" + email + "\'....");
            try {
                PreparedStatement findLogin = Server.connection.prepareStatement("SELECT count(*) FROM users");
                ResultSet countLogin = findLogin.executeQuery();
                countLogin.next();
                int count = countLogin.getBigDecimal("count").intValue();
                PreparedStatement insertUser = Server.connection.prepareStatement("INSERT INTO users (user_id, user_login, user_mail, user_password) VALUES (?, ?, ?, ?);");
                insertUser.setInt(1, count);
                insertUser.setString(2, sendLogin);
                insertUser.setString(3, email);
                try {
                    insertUser.setString(4, CryptoHash.getHash(sendedPassword));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                insertUser.execute();
            } catch(SQLException e) {
                System.out.println(e.getMessage());
            }
        });
        if(!clientSocket.isClosed())
            sendingThread.start();
    }

    private void change_password() throws SQLException{
        boolean truePassword = false;
        while (!truePassword && (!clientSocket.isClosed())) {
            sendMessage("for login");
            sendMessage("Please, type your current password.\n> ");
            String oldPassword = getMessage();
            if (oldPassword == null) {
                close();
            }
            Connection connection = Server.connection;
            PreparedStatement findPassword = connection.prepareStatement("SELECT user_password FROM users WHERE user_login = ?");
            findPassword.setString(1, login);
            ResultSet passwordFromTable = findPassword.executeQuery();
            passwordFromTable.next();
            String password = passwordFromTable.getString("user_password");
            try {
                oldPassword = CryptoHash.getHash(oldPassword);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            if (password.equals(oldPassword)) {
                truePassword = true;
                sendMessage("for login");
                sendMessage("Please, type new password.\n> ");
                String newPassword = getMessage();
                if (newPassword == null) {
                    close();
                }
                try {
                    PreparedStatement updateUser = Server.connection.prepareStatement("UPDATE users SET user_password = ? WHERE user_login = ?;");
                    updateUser.setString(2, login);
                    try {
                        updateUser.setString(1, CryptoHash.getHash(newPassword));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    updateUser.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                sendMessage("for login");
                sendMessage("Wrong password. Try again? [yes / no]\n> ");
                boolean wantContinue = false;
                while (!wantContinue && (!clientSocket.isClosed())) {
                    String continueResponse = getMessage();
                    if (continueResponse == null) {
                        close();
                    }
                    if(continueResponse.equals("yes")) {
                        wantContinue = true;
                    }else {
                        if(continueResponse.equals("no")) {
                            break;
                        }else {
                            sendMessage("for login");
                            sendMessage("Please, type 'yes' or 'no'.\n> ");
                            break;
                        }
                    }
                }
                if (wantContinue)
                        continue;
                    else
                        break;
            }
        }
    }

    private static final String characters = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";

    public static String generateRandomString(int length)
    {
        Random rng = new Random();
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    private void close() {
        try {
            clientSocket.close();
            writer.close();
            reader.close();
            thread.interrupt();
            socketChannel.close();
            server.getClients().remove(this);
            System.out.println("Обслуживание клиента " + id + " завершено.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("не удалось прервать обслуживание клиента.");
        }
    }

    public void sendMessage(String s) {
        try {
            writer.writeUTF(s);
            writer.flush();
        } catch (IOException e) {
            System.out.println("Отправка не удалась.");
        }
    }

    private String getMessage() {
        try {
            return reader.readUTF();
        } catch (Exception e) {
            System.out.println("Команда не была передана.");
            return null;
        }

    }

    void go() {
        thread = new Thread(this::serveClient);
        thread.start();
    }

    public int getId() {
        return id;
    }
}
