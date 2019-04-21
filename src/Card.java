import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Card {
    private static final DataBase dataBase = DataBase.getInstance();
    private String id;
    private String name;
    private int price;
    private int mana;

    public String getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public int getMana() {
        return mana;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Account getOwner() {
        Pattern pattern = Pattern.compile(Constants.ID_PATTERN);
        Matcher matcher = pattern.matcher(id);
        return dataBase.getAccountWithUsername(matcher.group(1));
    }
}
