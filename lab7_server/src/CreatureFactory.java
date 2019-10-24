import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Класс, предназначенный для создания объектов Creature по их текстовому описанию.
 */
public class CreatureFactory {
    /**
     * Значения полей объекта.
     */
    private List<String> fields;
    /**
     * Переменная хранит результат создания объекта.
     * @see CreatureFactory#getValue() "геттер"
     */
    private Creature value = null;
    /**
     * Переменная хранит описание объекта.
     * @see CreatureFactory#getDescription() "геттер"
     */
    private String description;
    /**
     * Переменная-флаг, показывающая, правильного ли формата поступающее описание объекта.
     * @see CreatureFactory#isCheckFormat() "геттер"
     */
    private boolean checkFormat = true;
    /**
     * Переменная хранит название класса созданного объекта.
     * @see CreatureFactory#getCreatedClass() "геттер"
     */
    private String createdClass;

    /**
     * Конструктор, принимающий описание объекта в виде строки в формате csv или json (без границ объекта '{' и '}'). При выполнении создаёт соответствующий описанию объект в поле value.
     * @param description описание объекта
     * @param owner владелец объекта
     */

    public CreatureFactory(String description, String owner){
        this.description = description + ", " + owner;
        getFields();
        create();
    }

    public CreatureFactory(String description){
        this.description = description;
        getFields();
        create();
    }


    /**
     * Метод "достаёт" значения полей объекта путём сплита по запятым. Предварительно убирает знаки пробелов и табуляций из строки с помощью метода String.replaceAll(String, String).
     * @see String#split(String)
     * @see String#replaceAll(String, String)
     */
    private void getFields() {
        fields = Arrays.asList(description.replaceAll("\\s+","").split(","));
    }

    /**
     * Метод является "геттером" для поля value.
     * @see CreatureFactory#value
     * @return значение поля value.
     */
    public Creature getValue() {
        return value;
    }

    /**
     * Общий метод для создания объекта. Внутри вызываются более конкретизированные методы.
     * @see CreatureFactory#createHatifnutt()
     * @see CreatureFactory#createHemul()
     * @see CreatureFactory#createMoomin() ()
     */
    private void create() {
        if(fields.size() > 0){
            String type = fields.get(0);
            try {
                switch (type) {
                    case "Hemul":
                        createHemul();
                        break;
                    case "Moomin":
                        createMoomin();
                        break;
                    case "Hatifnutt":
                        createHatifnutt();
                        break;
                    default: {
                        checkFormat = false;
                        System.out.println("Неверные данные. (Неверный тип)");
                    }
                    break;
                }
            } catch(IndexOutOfBoundsException e){
                checkFormat = false;
                System.out.println("Неверные данные в файле. (Нехватка полей у объекта)");
            }
        } else {
            checkFormat = false;
            System.out.println("Неверные данные в файле. (Отсутствие полей у объекта)");
        }
    }

    /**
     * Метод для создания объектов типа Hatifnutt.
     */
    private void createHatifnutt() {
        createdClass = "Hatifnutt";
        try {
            if (checkFormat) {
                int height = Integer.parseInt(fields.get(2));
                int x = Integer.parseInt(fields.get(3));
                int y = Integer.parseInt(fields.get(4));
                LocalDateTime date = Creature.parseDate(fields.get(5));
                value = new Hatifnutt(fields.get(1), height, x, y, date);
                value.setOwner(fields.get(6));
            } else {
                System.out.println("Неверные данные в файле.");
            }
        } catch (NumberFormatException e) {
            checkFormat = false;
            System.out.println("Неверные данные в файле. (Неверное значение высоты или позиции)");
        } catch (DateTimeParseException e) {
            checkFormat = false;
            System.out.println("Неверные данные в файле. (Неверное значение даты)");
        }
        // public Hatifnutt(String name)
        // Hatifnutt, <name (Name of the object) >
        //     0               1                                   (номер в fields)
    }

    /**
     * Метод для создания объектов типа Hemul.
     */
    private void createHemul() {
        createdClass = "Hemul";
        Sex sex = null;              // проверка Пола, т.к. все элементы fields - строки, а нужно как-то получить Sex
        switch (fields.get(1)) {
            case "MALE":
                sex = Sex.MALE;
                break;
            case "FEMALE":
                sex = Sex.FEMALE;
                break;
            case "AGAMIC":
                sex = Sex.AGAMIC;
                break;
            default: {
                checkFormat = false;
                System.out.println("Неверные данные. (Неверное значение пола)");
                return;
            }
        }
        Vocation vocation = null;       // проверка vocation, т.к. все элементы fields - строки, а нужно как-то получить Vocation
        switch (fields.get(3)) {
            case "CollectThings":
                vocation = new CollectThings();
                break;
            case "CollectRareFlowers":
                vocation = new CollectRareFlowers();
                break;
            case "Ski":
                vocation = new Ski();
                break;
            default: {
                checkFormat = false;
                System.out.println("Неверные данные. (Неверное значение призвания)");
                return;
            }
        }
        try {
            if (checkFormat) {
                int height = Integer.parseInt(fields.get(4));
                int x = Integer.parseInt(fields.get(5));
                int y = Integer.parseInt(fields.get(6));
                LocalDateTime date = Creature.parseDate(fields.get(7));
                value = new Hemul(sex, fields.get(2), vocation, height, x, y, date);
                value.setOwner(fields.get(8));
            }
        } catch (NumberFormatException e) {
            checkFormat = false;
            System.out.println("Неверные данные. (Неверное значение времени вызова, или энергии, или высоты, или позиции)");
        } catch (DateTimeParseException e) {
            checkFormat = false;
            System.out.println("Неверные данные. (Неверное значение даты)");

        }
        // private Hemul(Sex sex, String name, Vocation v)
        // Hemul, <sex (Sex of the object) >, <name (Name of the object) >, <vocation>
        //   0             1                                2                   3
    }

    /**
     * Метод для создания объектов типа Moomin.
     */
    private void createMoomin() {
        createdClass = "Moomin";
        Sex sex = null;              // проверка Пола, т.к. все элементы fields - строки, а нужно как-то получить Sex
        switch (fields.get(2)) {
            case "FEMALE":
                sex = Sex.FEMALE;
                break;
            case "MALE":
                sex = Sex.MALE;
                break;
            case "AGAMIC":
                sex = Sex.AGAMIC;
                break;
            default: {
                checkFormat = false;
                System.out.println("Неверные данные в файле. (Неверное значение пола)");
                return;
            }
        }
        try {
            if (checkFormat) {
                long lastCall = Long.parseLong(fields.get(3));
                long lastEnd = Long.parseLong(fields.get(4));
                double energy = Double.parseDouble(fields.get(5));
                int height = Integer.parseInt(fields.get(6));
                int x = Integer.parseInt(fields.get(7));
                int y = Integer.parseInt(fields.get(8));
                LocalDateTime date = Creature.parseDate(fields.get(9));
                value = new Moomin(sex, fields.get(1), lastCall, lastEnd, energy, height, x, y, date);
                value.setOwner(fields.get(10));
            }
        } catch (NumberFormatException e) {
            checkFormat = false;
            System.out.println("Неверные данные в файле. (Неверное значение времени вызова, или энергии, или высоты, или позиции)");
        } catch (DateTimeParseException e) {
            checkFormat = false;
            System.out.println("Неверные данные в файле. (Неверное значение даты)");
        }
        // public Moomin(Sex sex, String name, long lastCall, long lastEnd, double energy)
        // Moomin, <name (Name of the object) >, <sex (Sex of the object) >, <lastCall>, <lastEnd>, <energy>
        //   0             1                                2                   3           4          5
    }

    /**
     * Метод является "геттером" для поля checkFormat.
     * @see CreatureFactory#checkFormat
     * @return значение поля checkFormat.
     */
    public boolean isCheckFormat() {
        return checkFormat;
    }

    /**
     * Метод является "геттером" для поля description.
     * @see CreatureFactory#description
     * @return значение поля description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Метод является "геттером" для поля createdClass.
     * @see CreatureFactory#createdClass
     * @return значение поля createdClass.
     */
    public String getCreatedClass() {
        return createdClass;
    }
}
