import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Класс, предназначенный для выполнения команд, содержит реализации всех доступных в интерактивном режиме команд программы.
 */
public class CommandExecutor {
    /**
     * Коллекция, с которой производятся действия.
     */
    private ConcurrentHashMap<Creature, Creature> objects;

    private LocalDateTime initDate;

    public boolean isShort = true;

    private String login;

    /**
     * Конструктор, принимающий ссылку на Коллекцию.
     * @param objects Коллекция типа HashSet&lt;Creature&gt;
     */
    public CommandExecutor(ConcurrentHashMap<Creature, Creature> objects, LocalDateTime initDate){
        this.objects = objects;
        this.initDate = initDate;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Добавляет элемент в коллекцию.
     * @param creature объект для добавления.
     * @return Сообщение для клиента.
     */
    public String add(Creature creature) throws SQLException {
        if(objects.containsKey(creature)){
            return (">>> " + creature.getName() + " (" + creature.getClassName() + ")" + " is already in the Collection.\n");
        } else {
            objects.putIfAbsent(creature, creature);
            Connection connection = Server.connection;
            if(creature.getClassName().equals("Hatifnutt")) {
                PreparedStatement addingIntoDataBase = connection.prepareStatement("INSERT INTO hatifnutts (hatifnutt_key, hatifnutt_name, hatifnutt_height, hatifnutt_x, hatifnutt_y, hatifnutt_date, owner) values (?, ?, ?, ?, ?, ?, ?)");
                try {
                    addingIntoDataBase.setString(1, CryptoHash.getHash( creature.hashCode() + creature.getName() + creature.getOwner()));
                } catch(NoSuchAlgorithmException e){
                    e.printStackTrace();
                }
                addingIntoDataBase.setString(2, creature.getName());
                addingIntoDataBase.setInt(3, creature.getHeight());
                addingIntoDataBase.setInt(4, creature.getX());
                addingIntoDataBase.setInt(5, creature.getY());
                addingIntoDataBase.setDate(6, Date.valueOf(creature.getInitializationDate().toLocalDate()));
                addingIntoDataBase.setString(7, creature.getOwner());
                addingIntoDataBase.execute();
            }
            if(creature.getClassName().equals("Hemul")) {
                PreparedStatement addingIntoDataBase = connection.prepareStatement("INSERT INTO hatifnutts (hatifnutt_key, hatifnutt_name, hatifnutt_height, hatifnutt_x, hatifnutt_y, hatifnutt_date, owner) values (?, ?, ?, ?, ?, ?, ?)");
                try {
                    addingIntoDataBase.setString(1, CryptoHash.getHash(creature.getName()  + creature.hashCode() + creature.getOwner()));
                } catch(NoSuchAlgorithmException e){
                    e.printStackTrace();
                }
                addingIntoDataBase.setString(2, creature.getName());
                addingIntoDataBase.setInt(3, creature.getHeight());
                addingIntoDataBase.setInt(4, creature.getX());
                addingIntoDataBase.setInt(5, creature.getY());
                addingIntoDataBase.setDate(6, Date.valueOf(creature.getInitializationDate().toLocalDate()));
                addingIntoDataBase.setString(7, creature.getOwner());
                addingIntoDataBase.execute();
            }
            if(creature.getClassName().equals("Moomin")) {
                Moomin moomin = (Moomin) creature;
                PreparedStatement addingIntoDataBase = connection.prepareStatement("INSERT INTO Moomins (moomin_key, moomin_name, moomin_sex," +
                        " moomin_lastCall, moomin_lastEnd, moomin_energy, moomin_height, moomin_x, moomin_y, moomin_date, owner) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                try {
                    addingIntoDataBase.setString(1, CryptoHash.getHash(moomin.getName() + moomin.getOwner()) + moomin.hashCode());
                } catch(NoSuchAlgorithmException e){
                    e.printStackTrace();
                }
                addingIntoDataBase.setString(2, moomin.getName());
                addingIntoDataBase.setString(3, moomin.getSex().toString());
                addingIntoDataBase.setLong(4, moomin.getLastCall());
                addingIntoDataBase.setLong(5, moomin.getLastEnd());
                addingIntoDataBase.setDouble(6, moomin.getEnergy());
                addingIntoDataBase.setInt(7, moomin.getHeight());
                addingIntoDataBase.setInt(8, moomin.getX());
                addingIntoDataBase.setInt(9, moomin.getY());
                addingIntoDataBase.setDate(10, Date.valueOf(moomin.getInitializationDate().toLocalDate()));
                addingIntoDataBase.setString(11, moomin.getOwner());
                addingIntoDataBase.execute();
            }
            return (">>> " + creature.getName() + " (" + creature.getClassName() + ")" + " has been added to the Collection.\n");
        }
    }

    /**
     * Добавляет элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции.
     * @param creature объект для добавления.
     * @return Сообщение для клиента.
     */
    public String add_if_min(Creature creature) throws SQLException{
        long count = this.objects.keySet().stream()
                                            .filter(cr -> cr.getOwner().equals(login))
                                            .filter(cr -> ( cr.getName().length() <= creature.getName().length()))       // оставляет те элементы, длина имени которых <= длине имени переданного
                                            .count();
        if(count > 0)
            return (">>> " + creature.getName() + " (" + creature.getClassName() + ")" + " isn't lower than any element in your Collection.\n");
        else
            return this.add(creature);
    }

    /**
     * Удаляет элемент из коллекции.
     * @param creature объект.
     * @return Возвращает сообщение для посылки на клиент.
     */
    public String remove(Creature creature){
        String message = "";
        if(objects.containsKey(creature)) {
            if(objects.get(creature).getOwner().equals(login)) {
                objects.remove(creature);
                return (">>> " + creature.getName() + " (" + creature.getClassName() + ")" + " has been removed from the Collection.\n");
            } else {
                return (">>> You ain't the owner of this object.");
            }
        } else {
            return (">>> There is no such element in Collection.\n");
        }
    }

    /**
     * Удаляет из коллекции все элементы, превышающие заданный.
     * @param creature объект для сравнения.
     * @return Возвращает строку с именами удалённых объектов или 'Nothing'.
     */
    public String remove_greater(Creature creature){
        String returned = "";
        ConcurrentHashMap<Creature, Creature> changeableCopyOfObjects = new ConcurrentHashMap<Creature, Creature>();
        int numberOfDeletedElemets = 0;
        StringBuffer returnBuilder = new StringBuffer();
        Set<Creature> savedCreatures = objects.keySet().stream()
                                            .filter(cr -> cr.getOwner().equals(login))
                                            .filter(cr -> cr.getName().length() <= creature.getName().length())     // оставляет элементы с длиной имени <= длины имени переданного
                                            .collect(Collectors.toSet());
        savedCreatures.stream()
                .filter(cr -> cr.getOwner().equals(login))
                .forEach(cr -> changeableCopyOfObjects.putIfAbsent(cr, cr));
        int ownedSize = new Long(objects.keySet().stream()
                                    .filter(cr -> cr.getOwner().equals(login))
                                    .count()).intValue();
        numberOfDeletedElemets = ownedSize - changeableCopyOfObjects.size();
        objects.keySet().stream()
                            .filter(cr -> cr.getOwner().equals(login))
                            .filter(cr -> cr.getName().length() > creature.getName().length())
                            .forEach(cr -> returnBuilder.append(cr.getName() + " (" + cr.getClassName() + ")" + "; "));
        Set<Creature> notOwned = objects.keySet().stream()
                .filter(cr -> !cr.getOwner().equals(login))     // элементы, не принадлежащие пользователю
                .collect(Collectors.toSet());
        if(numberOfDeletedElemets == 0) {
            return ">>> Deleting : Nothing\n";
        } else {
            objects.clear();
            notOwned.forEach(cr -> objects.putIfAbsent(cr, cr));
            changeableCopyOfObjects.keySet().stream()
                    .filter(cr -> cr.getOwner().equals(login))
                    .forEach(cr -> objects.putIfAbsent(cr, cr));
            returned = returnBuilder.toString();
            return ">>> Deleting : " + returned + "\n";
        }
    }

    /**
     * Удаляет из коллекции все элементы, меньшие, чем заданный.
     * @param creature объект для сравнения.
     * @return Возвращает строку с именами удалённых объектов или 'Nothing'.
     */
    public String remove_lower(Creature creature){
        String returned = "";
        ConcurrentHashMap<Creature, Creature> changeableCopyOfObjects = new ConcurrentHashMap<Creature, Creature>();
        int numberOfDeletedElemets = 0;
        StringBuffer returnBuilder = new StringBuffer();
        Set<Creature> notOwned = objects.keySet().stream()
                .filter(cr -> !cr.getOwner().equals(login))     // элементы, не принадлежащие пользователю
                .collect(Collectors.toSet());
        Set<Creature> savedCreatures = objects.keySet().stream()
                .filter(cr -> cr.getOwner().equals(login))
                .filter(cr -> cr.getName().length() >= creature.getName().length())     // оставляет элементы с длиной имени >= длины имени переданного
                .collect(Collectors.toSet());
        savedCreatures.stream().forEach(cr -> changeableCopyOfObjects.putIfAbsent(cr, cr));
        int ownedSize = objects.keySet().stream()
                .filter(cr -> cr.getOwner().equals(login))
                .collect(Collectors.toList()).size();
        numberOfDeletedElemets = ownedSize - changeableCopyOfObjects.size();
        objects.keySet().stream()
                .filter(cr -> cr.getOwner().equals(login))
                .filter(cr -> cr.getName().length() < creature.getName().length())
                .forEach(cr -> returnBuilder.append(cr.getName() + " (" + cr.getClassName() + ")" + "; "));
        if(numberOfDeletedElemets == 0) {
            return ">>> Deleting : Nothing\n";
        } else {
            objects.clear();
            notOwned.forEach(cr -> objects.putIfAbsent(cr, cr));
            changeableCopyOfObjects.keySet().forEach(cr -> objects.putIfAbsent(cr, cr));
            returned = returnBuilder.toString();
            return ">>> Deleting : " + returned + "\n";
        }
    }


    /**
     * Возвращает все элементы коллекции в строковом представлении (по одному), посылает их клиенту.
     * @param client Объект класса Client, который будет посылать сообщения клиенту.
     */
    public void show(Client client){
        objects.keySet().stream()
                .sorted()
                .forEach(creature -> {
                    if(isShort) {
                        client.sendMessage("\t" + creature.shortToString() + "\n");
                    } else {
                        client.sendMessage("\t" + creature.toString() + ", hashCode = " + creature.hashCode() + "\n");
                    }
                });
    }


    /**
     * Возвращает информацию о коллекции (тип, количество элементов, дата инициализации).
     * @return Сообщение для клиента.
     */
    public String info(){
        int ownedSize = objects.keySet().stream()
            .filter(cr -> cr.getOwner().equals(login))
            .collect(Collectors.toList()).size();
        String info = "\tCollection's type - Creature\n" +
                        "\tCollection's size - " + objects.size() + " elements\n" +
                        "\tNumber of elements you own - " + ownedSize + " elements\n" +
                        "\tInitialisation date - " + initDate;
        return info;
    }

    /**
     * Возвращает информацию о всех доступных командах, их Формате и Формате json-объектов, используемых в командах.
     * @return Сообщение для клиента.
     */
    public String help(){
        return ("Допустимые команды: \n" +
            "\thelp - Выводит информацию о доступных командах и формате json-объектов, используемых в командах.\n" +
            "\tinfo - Выводит в стандартный поток вывода информацию о коллекции (тип, количество элементов, дата инициализации).\n" +
            "\tshow - Выводит в стандартный поток вывода все элементы коллекции в строковом представлении (в том числе hashCode каждого элемента).\n" +
            "\texit - Сохраняет данные и выполняет выход из программы.\n" +
            "\tsave - Сохраняет данные.\n" +
            "\tadd {element}: - Добавляет новый элемент в коллекцию.\n" +
            "\tremove {element} - Удаляет элемент из коллекции по его значению.\n" +
            "\tremove_greater {element} - Удаляет из коллекции все элементы, превышающие заданный.\n" +
            "\tremove_lower {element} - Удаляет из коллекции все элементы, меньшие, чем заданный.\n" +
            "\tadd_if_min {element} - Добавляет новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции.\n\n" +
        "Формат json-объектов {element}:\n" +
            "\t1) Объект обязан иметь поля \"Class\" и \"Name\". Поле \"Class\" может иметь значения \"Hatifnutt\", \n" +
                "\t\t\"Moomin\" или \"Hemul\". \u0418мя объекта (поле \"Name\") может состоять из букв и цифр. \n" +
                "\t\tТакже поля \"Class\" и \"Name\" - необходимые и достаточные для объекта класса \"Hatifnutt\".\n" +
            "\t2) Для объекта класса \"Hemul\", помимо \"Class\" и \"Name\", необходимы следующие поля: \n" +
                "\t\t\"Sex\" - пол объекта. Может принимать значения \"MALE\"; \"FEMALE\"; \"AGAMIC\" (бесполый).\n" +
                "\t\t\"Vocation\" - занятие объекта. Может принимать значения \"CollectThings\"; \"CollectRareFlowers\"; \"Ski\".\n" +
            "\t3) Для объекта класса \"Moomin\", помимо \"Class\" и \"Name\", необходимы следующие поля: \n" +
                "\t\t\"Sex\" - пол объекта. Может принимать значения \"MALE\"; \"FEMALE\"; \"AGAMIC\" (бесполый).\n" +
                "\t\t\"LastCall\" - Последнее время, когда объект начал что-то делать. Может принимать значения [ " + Long.MIN_VALUE + " ; " + Long.MAX_VALUE + " ] (тип int).\n" +
                "\t\t\"LastEnd\" - Последнее время, когда объект закончил что-то делать. Может принимать значения [ " + Long.MIN_VALUE  + " ; " + Long.MAX_VALUE + " ] (тип int).\n" +
                "\t\t\"Energy\" - текущая энергия объекта. Может принимать значения [ " + Double.MIN_VALUE + " ; " + Double.MAX_VALUE + " ] (тип double).\n");
    }

    /**
     * Метод производит сохранение Коллекции в файл, хранящий состояние Коллекции между запусками программы.
     * @param reader "считыватель".
     * @return Сообщение для клиента.
     * @see MyReader#write()
     */
    public String save(MyReader reader){
        String returned = ">>> Saving data into file...\n";
        try {
            returned += reader.writeWithOwner(login);
        } catch (IOException e){
            returned += ">>> Something wrong with file.\n";
        }
        return returned + ">>> Done.\n";

    }

    /**
     * Метод производит выход из программы, при этом состояние Коллекции сохраняется, с помощью CommandExecutor.save(MyReader).
     * @param reader "считыватель" из первоначального класса.
     * @see CommandExecutor#save(MyReader)
     */
    public void exit(MyReader reader){
        System.exit(0);
    }

    /**
     * Метод, который по переданной с клиента строке добавляет объекты.
     * @param in Строка с данными об объектах в формате csv
     */
    public String importString(String in, String owner) {
        String returned = "";
        try {
            returned += "+ Extraction from sent file...\n";
            List<String> lines = new ArrayList<>();
            List<Creature> forAdd = new ArrayList<>();
            lines = Arrays.asList(in.split("\\n"));
            StringBuffer builder = new StringBuffer();
            lines.stream()
                    .forEach(line -> {
                        CreatureFactory creator = new CreatureFactory(line, owner);
                        if (creator.isCheckFormat()) {
                            forAdd.add(creator.getValue());
                            builder.append("... adding: " + creator.getValue().toString() + "\n");
                        }
                    });
            returned += builder.toString();
            forAdd.stream().forEach(cr -> objects.putIfAbsent(cr, cr));
            returned += "+ Extraction is complete.\n-----------------------------------------------------\n\n";
        } catch (Exception e){
            e.printStackTrace();
        }
        return returned;
    }

    public void load(String address, String owner){
        MyReader reader = new MyReader(address, objects);
        try {                           // Считывание из какого-то файла с сервера.
            reader.read();
        } catch (IOException e){
            System.out.println(">>> С импортом из файла с сервера что-то не так.");
        }
    }

}