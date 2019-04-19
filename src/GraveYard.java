import java.util.ArrayList;
import java.util.List;

class GraveYard {
    private List<Card> deadCards = new ArrayList<>();
    private static final ControllerGraveYard controllerGraveYard = ControllerGraveYard.getInstance();

    public List<Card> getDeadCards() {
        return deadCards;
    }

    public void addDeadCard(Card deadCard) {
        deadCards.add(deadCard);
    }

    public void showInfoOfDeadCards() {
        controllerGraveYard.showInfoOfCards(deadCards);
    }

//    public void showInfo(String cardID) {             find a use for this empty method or delete it
//
//    }

}
