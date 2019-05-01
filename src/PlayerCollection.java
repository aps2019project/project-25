import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerCollection {
    private static final DataBase dataBase = DataBase.getInstance();
    private static final Account loggedInAccount = dataBase.getLoggedInAccount();
    private List<Deck> decks = new ArrayList<>();
    private List<Card> cards = new ArrayList<>();
    private List<Usable> items = new ArrayList<>();

    public List<Card> getCards() {
        return cards;
    }

    public List<Usable> getItems() {
        return items;
    }

    public void addCard(Card newCard) {
        cards.add(newCard);
    }
    public OutputMessageType addCard(String id,String toDeck){
        Deck destinationDeck=getDeckByName(toDeck);
        Card card=getCardWithID(id);
        Item item=getItemWithID(id);
        if(card==null && item == null){
            return OutputMessageType.NOT_IN_COLLECTION;
        }else if(destinationDeck.getItem()==item ||destinationDeck.hasCard(card)){
            return OutputMessageType.CARD_ALREADY_IN_BATTLE;
        }else if(destinationDeck.getCards().size()==20){
            return OutputMessageType.DECK_IS_FULL;
        }else if(destinationDeck.getHero()!=null){
            return OutputMessageType.DECK_HAS_HERO;
        }else {
            if(card!=null){
                if(card instanceof Unit){
                    if(((Unit)card).getHeroOrMinion().equals(Constants.HERO)){
                        destinationDeck.setHero((Unit)card);
                    }else{
                        destinationDeck.getCards().add(card);
                    }
                }
            }else if(item!=null){
                destinationDeck.setItem(item);
            }
            return OutputMessageType.NO_ERROR;
        }
    }

    public OutputMessageType removeCard(String id,String fromDeck){
        Deck deck= getDeckByName(fromDeck);
        if(deck==null){
            return OutputMessageType.DECK_DOESNT_EXIST;
        }else {
            Card temp=deck.getCardByCardId(id);
            if(temp!=null){
                if(temp instanceof Unit && ((Unit)temp).getHeroOrMinion().equals(Constants.HERO)){
                    deck.setHero(null);
                }else deck.getCards().remove(temp);
            }else{
                if(deck.getItem().getId().equals(id)){
                    deck.setItem(null);
                }else {
                    return OutputMessageType.NO_SUCH_CARD_IN_DECK;
                }
            }
        }

    }

    public void addItem(Usable newItem) {
        items.add(newItem);
    }

    public void deleteCard(Card card) {
        cards.remove(card);
    }

    public void deleteItem(Usable item) {
        items.remove(item);
    }

    public List<Deck> getDecks() {
        return decks;
    }

    public Card getCardWithID(String id) {
        for (Card card : cards) {
            if (card.getId().equals(id))
                return card;
        }
        return null;
    }

    public boolean doesHaveCard(String id) {
        return getCardWithID(id) != null;
    }

    public Item getItemWithID(String id) {
        for (Item item : items) {
            if (item.getId().equals(id))
                return item;
        }
        return null;
    }

    public boolean doesHaveItem(String id) {
        return getCardWithID(id) != null;
    }

    //public boolean doesHave

    public void addNewDeck() {
        //todo maybe it isn't needed
    }

    public OutputMessageType deleteDeck(String deckName) {
        if (!doesHaveDeck(deckName))
            return OutputMessageType.DECK_DOESNT_EXIST;
        Deck deck = getDeckByName(deckName);
        decks.remove(deck);
        if (dataBase.getLoggedInAccount().getMainDeck() == deck)
            dataBase.getLoggedInAccount().setMainDeck(null);
        return OutputMessageType.DECK_DELETED;
    }

    public Deck getDeckByName(String deckName) {
        for (Deck deck : decks) {
            if (deck.getName().equals(deckName))
                return deck;
        }
        return null;
    }

    public boolean doesHaveDeck(Deck deck) {
        return doesHaveDeck(deck.getName());
    }

    public boolean doesHaveDeck(String deckName) {
        for (Deck deck : decks) {
            if (deck.getName().equals(deckName))
                return true;
        }
        return false;
    }

    public OutputMessageType createDeck(String deckName) {
        if (doesHaveDeck(deckName))
            return OutputMessageType.DECK_ALREADY_EXISTS;
        Deck newDeck = new Deck(deckName);
        decks.add(newDeck);
        return OutputMessageType.DECK_CREATED;
    }

    public OutputMessageType buy(String name) {
        if (dataBase.doesCardExist(name)) {
            Card card = dataBase.getCardWithName(name);
            if (loggedInAccount.getMoney() < card.getPrice())
                return OutputMessageType.INSUFFICIENT_MONEY;
            if (items.size() == 3)
                return OutputMessageType.CANT_HAVE_MORE_ITEMS;
            else {
                //todo
                return OutputMessageType.BOUGHT_SUCCESSFULLY;
            }
        }
        if (dataBase.doesUsableExist(name)) {
            Usable usable = dataBase.getUsableWithName(name);
            if (loggedInAccount.getMoney() < usable.getPrice())
                return OutputMessageType.INSUFFICIENT_MONEY;
            if (items.size() == 3)
                return OutputMessageType.CANT_HAVE_MORE_ITEMS;
            else {
                //todo
                return OutputMessageType.BOUGHT_SUCCESSFULLY;
            }
        }
        return OutputMessageType.NOT_IN_SHOP;
    }


    public OutputMessageType sell(String id) {
        if(doesHaveCard(id)){
            //todo
        }
        //todo
        return OutputMessageType.NOT_IN_COLLECTION;
    }

    public OutputMessageType selectDeckAsMain(String deckName) {
        if (!doesHaveDeck(deckName))
            return OutputMessageType.DECK_DOESNT_EXIST;
        dataBase.getLoggedInAccount().setMainDeck(getDeckByName(deckName));
        return OutputMessageType.DECK_SELECTED;
    }

    public OutputMessageType validateDeck(String deckName) {
        if (!doesHaveDeck(deckName))
            return OutputMessageType.DECK_DOESNT_EXIST;
        Deck deck = getDeckByName(deckName);
        if (deck.isValid())
            return OutputMessageType.DECK_VALID;
        return OutputMessageType.DECK_NOT_VALID;
    }
}
