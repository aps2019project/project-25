import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class CardBackGroundController {
    private ControllerShop controllerShop = ControllerShop.getOurInstance();
    private int number = 0;
    @FXML
    private AnchorPane cardPane;

    @FXML
    private Label spellOrUnit;

    @FXML
    private ImageView card;

    @FXML
    private Label numberBoughtLabel;

    @FXML
    private Label manaLabel;

    @FXML
    private Label apLabel;

    @FXML
    private Label hpLabel;

    @FXML
    private Label cardName;

    @FXML
    private Label priceLable;

    @FXML
    private ImageView descBtn;

    @FXML
    void makeDescBtnOpaque(MouseEvent event) {
        descBtn.setStyle("-fx-opacity: 1");
    }

    @FXML
    void makeDescBtnTransparent(MouseEvent event) {
        descBtn.setStyle("-fx-opacity: 0.6");
    }

    @FXML
    void showDescription(MouseEvent event) throws IOException {
        FXMLLoader fxmlLoader;
        fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("DescriptionController.fxml"));
        DescriptionController descriptionController = fxmlLoader.getController();
        Parent root = fxmlLoader.load();
        Stage descStage = new Stage();
        descStage.setScene(new Scene(root));
        descStage.show();
    }

    @FXML
    void buyCard(MouseEvent event) {
        Main.playWhenButtonClicked();
        new ServerRequestSender(new Request
                (RequestType.buy, cardName.getText(), null, null)).start();
    }

    @FXML
    void makeCardOpaque(MouseEvent event) {
        Main.playWhenMouseEntered();
        card.setStyle("-fx-opacity: 1");
    }

    @FXML
    void makeCardTransparent(MouseEvent event) {
        card.setStyle("-fx-opacity: 0.8");
    }

    public void setLabelsOfCardOrItem(Object object) {
        numberBoughtLabel.setText(Integer.toString(number));
        if (object instanceof Card) {
            Card newCard = (Card) object;
            if (newCard instanceof Unit) {
                Unit unit = (Unit) newCard;
                priceLable.setText(Integer.toString(unit.getPrice()));
                apLabel.setText(Integer.toString(unit.getAp()));
                cardName.setText(unit.getName());
                hpLabel.setText(Integer.toString(unit.getHp()));
                spellOrUnit.setText(unit.getHeroOrMinion());
                manaLabel.setText(Integer.toString(unit.getMana()));
            } else if (newCard instanceof Spell) {
                Spell spell = (Spell) newCard;
                manaLabel.setText(Integer.toString(spell.getMana()));
                cardName.setText(spell.getName());
                spellOrUnit.setText("Spell");
                priceLable.setText(Integer.toString(spell.getPrice()));
            }
        }
        if (object instanceof Usable) {
            Usable usable = (Usable) object;
            cardName.setText(usable.getName());
            priceLable.setText(Integer.toString(usable.getPrice()));
            spellOrUnit.setText("Usable Item");
        }
    }

    public AnchorPane getCardPane() {
        return cardPane;
    }
}