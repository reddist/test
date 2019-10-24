import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс, предназначенный для чтения из файла, хранящего состояние Коллекции между вызовами программы, и дозаписи в него.
 */
public class MyReader {
    /**
     * Переменная с адресом файла, хранящего состояние Коллекции.
     */
    private String address;
    /**
     * Переменная с ссылкой на Коллекцию.
     */
    private ConcurrentHashMap<Creature, Creature> objects;
    /**
     * Переменная-флаг, показывающая, существует ли файл, указанный в системной переменной. False - существует, True - не существует.
     * @see MyReader#isFileNotFound()
     */
    private boolean fileNotFound;

    /**
     * Конструктор, принимающий адрес файла, хранящего состояние Коллекции, и ссылку на Коллекцию.
     * @param address адрес файла, хранящего состояние Коллекции между вызовами программы.
     * @param objects ссылка на Коллекцию.
     */
    public MyReader(String address, ConcurrentHashMap<Creature, Creature> objects){
        this.address = address;
        if(address == null)
            fileNotFound = true;
        else
            fileNotFound = false;
        this.objects = objects;
    }

    public void readFromDataBase() throws SQLException {
        ResultSet resultSet = Server.statement.executeQuery("SELECT * FROM hatifnutts, hemuls, trolls");
        /*
        while (resultSet.next()) {
            String name = resultSet.getString("OBJECT_NAME");
            String type = resultSet.getString("OBJECT_TYPE");
            Form form = Form.valueOf(resultSet.getString("OBJECT_FORM"));
            Height height = Height.valueOf((resultSet.getString("OBJECT_HEIGHT")));

            int x = resultSet.getInt("X");
            int y = resultSet.getInt("Y");
            int z = resultSet.getInt("Z");
            int crons = resultSet.getInt("OBJECT_WALLET");
            LocalDateTime localDateTime = resultSet.getObject("OBJECT_DATE", LocalDateTime.class);
            Human human;
            switch (type) {
                case "Friend":
                    human = new Friend(name, height, form, new Wallet(crons), type, new Location(x, y, z));
                    break;
                case "Carlson":
                    human = new Carlson(name, height, form, new Wallet(crons), type, new Location(x, y, z));
                    break;
                case "Baby":
                    human = new Baby(name, height, form, new Wallet(crons), type, new Location(x, y, z));
                    break;
                default:
                    human = new Friend(name, height, form, new Wallet(crons), type, new Location(x, y, z));
                    break;
            }
            human.date = localDateTime;
            tree.put(human.getName(), human);

        }*/
    }

    /**
     * Метод, производящий распаковку объектов из файла.
     * @throws IOException Если с файлом проблемы.
     * @return Сообщение о вводе.
     */
    public String read() throws IOException {
        String returned = "";
        ConcurrentHashMap<Creature, Creature> newObjects = new ConcurrentHashMap<>();
        if(!fileNotFound) {
            try {
                InputStreamReader in = new InputStreamReader(new FileInputStream(address), "UTF-8");
                try {      // блок, создающий коллекцию из файла
                    returned = "+ Extraction from " + address + "\n";
                    int bufferSize = 130000;
                    char[] buffer = new char[bufferSize];
                    CreatureFactory creator;
                    int readSize = in.read(buffer, 0, buffer.length);
                    if (readSize < bufferSize) {
                        buffer = Arrays.copyOf(buffer, readSize);    // копирование в buffer, если количество считанных символов
                    }                                                 // меньше 1024, т.к. иначе лишние переносы строк
                    for (String st : new String(buffer).split("\n")) {   // деление на строки и создание объектов
                        creator = new CreatureFactory(st);
                        if (creator.isCheckFormat()) {
                            newObjects.putIfAbsent(creator.getValue(), creator.getValue());
                            returned += "... adding: " + creator.getValue().toString() + "\n";
                        }
                    }
                    returned += "+ Extraction is complete.\n-----------------------------------------------------\n\n";
                    objects.clear();
                    newObjects.keySet().stream().forEach(creature -> objects.putIfAbsent(creature, creature));
                    copyObjects();
                } catch (UnsupportedEncodingException e) {
                    returned += "Charset is not supported.\n";
                } catch (NegativeArraySizeException e) {
                    returned += "No data in File.\n" +
                                    "+ Extraction is complete.\n-----------------------------------------------------\n\n";
                } catch (SecurityException e) {
                    returned += "File is prohibited for reading.\n";
                }
            } catch (FileNotFoundException e) {
                returned += "File is not found.\n";
            }
        }
        else{
            returned += "File doesn't exist.";
            return returned + "F\n";
        }
        return returned;
    }

    public String load(String owner) throws IOException {
        StringBuffer returned = new StringBuffer();
        Set<Creature> loadedObjects = new HashSet<>();
        if(!fileNotFound) {
            try {
                InputStreamReader in = new InputStreamReader(new FileInputStream(address), "UTF-8");
                try {      // блок, создающий коллекцию из файла
                    returned.append("+ Extraction from " + address + "\n");
                    int bufferSize = 130000;
                    char[] buffer = new char[bufferSize];
                    CreatureFactory creator;
                    int readSize = in.read(buffer, 0, buffer.length);
                    if (readSize < bufferSize) {
                        buffer = Arrays.copyOf(buffer, readSize);    // копирование в buffer, если количество считанных символов
                    }                                                 // меньше 1024, т.к. иначе лишние переносы строк
                    for (String st : new String(buffer).split("\n")) {   // деление на строки и создание объектов
                        creator = new CreatureFactory(st);
                        if (creator.isCheckFormat()) {
                            loadedObjects.add(creator.getValue());
                        }
                    }
                    objects.keySet().removeIf(creature -> creature.getOwner().equals(owner));
                    loadedObjects.removeIf(creature -> !creature.getOwner().equals(owner));
                    loadedObjects.forEach(creature -> {
                        objects.putIfAbsent(creature, creature);
                        returned.append("... adding: " + creature.toString() + "\n");
                    });
                    returned.append("+ Extraction is complete.\n-----------------------------------------------------\n\n");
                } catch (UnsupportedEncodingException e) {
                    returned.append("Charset is not supported.\n");
                } catch (NegativeArraySizeException e) {
                    returned.append("No data in File.\n" +
                            "+ Extraction is complete.\n-----------------------------------------------------\n\n");
                } catch (SecurityException e) {
                    returned.append("File is prohibited for reading.\n");
                }
            } catch (FileNotFoundException e) {
                returned.append("File is not found.\n");
            }
        }
        else{
            returned.append("File doesn't exist.F\n");
            return returned.toString();
        }
        return returned.toString();
    }

    /**
     * Метод выводит Коллекцию (HashSet&lt;Creature&gt;) в файл.
     * @throws IOException Если с файлом проблемы.
     * @return Сообщение о выводе.
     */
    public String write() throws IOException{
        return this.write(this.address);
    }

    public String writeWithOwner(String owner) throws IOException{
        synchronized (MyReader.class) {
            StringBuffer returned = new StringBuffer("");
            try {
                File f = new File(address);
                InputStreamReader fromFile = new InputStreamReader(new FileInputStream(f));
                char[] buffer = new char[100000];
                int size = fromFile.read(buffer);
                f.delete();
                f.createNewFile();
                if (size != -1) {
                    buffer = Arrays.copyOf(buffer, size);
                    String readed = new String(buffer);
                    String[] lines = readed.split("\n");
                    Arrays.stream(lines)
                            .filter(line -> {
                                String[] fields = line.replaceAll("\\s+", "").split(",");
                                if (fields[fields.length - 1].equals(owner))
                                    return false;
                                return true;
                            })
                            .forEach(line -> {
                                try (PrintWriter writer = new PrintWriter(f, "UTF-8")) {
                                    writer.println(line);
                                } catch (IOException e) {
                                    returned.append("File is not found.\n");
                                    return;
                                }
                            });
                }
                System.out.println(11);
                try (PrintWriter writer = new PrintWriter(f, "UTF-8")) {
                    objects.keySet().stream()
                            .filter(creature -> creature.getOwner().equals(owner))
                            .forEach(creature -> writer.println(creature.csvDescription()));
                } catch (FileNotFoundException e) {
                    returned.append("File is not found.\n");
                }
            } catch (SecurityException e) {
                returned.append("File is prohibited for writing.\n");
            }
            return returned.toString();
        }
    }

    /**
     * Метод выводит Коллекцию (HashSet&lt;Creature&gt;) в файл по указанному адресу.
     * @param address адрес файла для вывода.
     * @throws IOException Если с файлом по указанному адресу проблемы.
     * @return Сообщение о выводе.
     */
    private String write(String address) throws IOException{
        String returned = "";
        try {
            File f = new File(address);
            f.delete();
            f.createNewFile();
            try (PrintWriter writer = new PrintWriter(f, "UTF-8")) {
                for (Creature creature : objects.keySet()) {
                    writer.println(creature.csvDescription());
                }
            } catch (FileNotFoundException e) {
                returned += "File is not found.\n";
            }
        } catch (SecurityException e){
            returned += "File is prohibited for writing.\n";
        }
        return returned;
    }

    /**
     * Метод делает "backup" файла с данными о состоянии Коллекции. "Backup"-файл имеет постфикс "_last_copy" и лежит в той же директории, что и сам файл с данными о Коллекции.
     * @throws IOException Если с файлом проблемы.
     * @return Сообщение о выводе.
     */
    private String copyObjects() throws IOException {
        return this.write(address.substring(0, address.length() - 4) + "_last_copy.csv");
    }

    /**
     * Метод-"геттер" для поля fileNotFound.
     * @see MyReader#fileNotFound
     * @return Возвращает значение поля fileNotFound.
     */
    public boolean isFileNotFound() {
        return fileNotFound;
    }
}
