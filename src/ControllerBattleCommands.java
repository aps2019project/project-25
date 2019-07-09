//import com.teamdev.jxcapture.Codec;
//import com.teamdev.jxcapture.EncodingParameters;
//import com.teamdev.jxcapture.VideoCapture;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerBattleCommands implements Initializable {
    private static ClientDB clientDB = ClientDB.getInstance();
    private static ControllerBattleCommands ourInstance;
    private List<ImageView> handRings = new ArrayList<>();
    private List<UnitImage> unitImageList = new ArrayList<>();
    private List<HandImage> handImageList = new ArrayList<>();
    private List<SpellImage> spellImageList = new ArrayList<>();
    private List<FlagImage> flagImages = new ArrayList<>();
    private CellImage[][] cellsImages = new CellImage[5][9];
    private CollectableImage collectableImage;
    private NextCardImage nextCardImage;
    private ImageView clickedImageView = new ImageView();
    private Timeline timeline = new Timeline();
    //todo next card has bug

    public void setClickedImageView(ImageView clickedImageView) {
        this.clickedImageView = clickedImageView;
    }

    public ImageView getClickedImageView() {
        return clickedImageView;
    }

    @FXML
    private ImageView endTurnMineBtn;

    @FXML
    private ImageView endTurnEnemyBtn;

    @FXML
    private AnchorPane battleGroundPane;

    @FXML
    private ImageView graveYardBtn;

    @FXML
    private Label player1Label;

    @FXML
    private Label player2Label;

    @FXML
    private ImageView specialPowerView;

    @FXML
    private ImageView collectableView;

    @FXML
    private ImageView usableView;

    @FXML
    private ImageView forfeitBtn;

    @FXML
    void forfeitGame(MouseEvent event) {
        forfeitGame();
        returnToMainMenu();
    }

    @FXML
    void makeForfeitBtnOpaque(MouseEvent event) {
        Main.playWhenMouseEntered();
        forfeitBtn.setStyle("-fx-opacity: 1");
    }

    @FXML
    void makeForfeitBtnTransparent(MouseEvent event) {
        forfeitBtn.setStyle("-fx-opacity: 0.6");
    }

    //todo grave yard doesn't get any resource from server and does the work
    // with the current resources : clientDB.getCurrentBattle....
    // maybe it's better to send currentBattle to client every time we enter grave yard
    @FXML
    void enterGraveYard(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ControllerGraveYard.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UNDECORATED);
        File file = new File("src/pics/cursors/main_cursor.png");
        Image image = new Image(file.toURI().toString());
        scene.setCursor(new ImageCursor(image));
        stage.setScene(scene);
        ControllerGraveYard.stage = stage;
        stage.showAndWait();
    }

    @FXML
    void makeGraveYardBtnOpaque(MouseEvent event) {
        Main.playWhenMouseEntered();
        graveYardBtn.setStyle("-fx-opacity: 1");
    }

    @FXML
    void makeGraveYardBtnTransparent(MouseEvent event) {
        graveYardBtn.setStyle("-fx-opacity: 0.6");
    }

    @FXML
    void makeEndTurnMineOpaque(MouseEvent event) {
        Main.playWhenMouseEntered();
        endTurnMineBtn.setStyle("-fx-opacity: 1");
    }

    @FXML
    void makeEndTurnMineTransparent(MouseEvent event) {
        endTurnMineBtn.setStyle("-fx-opacity: 0.6");
    }

    @FXML
    private ImageView handRing1;

    @FXML
    private ImageView handRing2;

    @FXML
    private ImageView handRing3;

    @FXML
    private ImageView handRing4;

    @FXML
    private ImageView handRing5;

    @FXML
    private ImageView nextCardRing;

    @FXML
    private Label player1ManaLabel;

    @FXML
    private Label player2ManaLabel;

    @FXML
    private ProgressBar timeBar;

    @FXML
    void endTurn(MouseEvent event) {
        if (!clientDB.getLoggedInPlayer().equals(clientDB.getCurrentBattle().getPlayerInTurn()))
            return;
        endTurnWhenClicked();
    }

    private void endTurnWhenClicked() {
        Main.playMedia("src/music/end_turn.m4a"
                , Duration.INDEFINITE, 1, false, 100);
        clickedImageView = null;
        if (clientDB.getCurrentBattle().getSingleOrMulti().equals(Constants.MULTI)) {
            new ServerRequestSender(new Request
                    (RequestType.endTurn, null, null, null)).start();
        }
        if (clientDB.getCurrentBattle().getSingleOrMulti().equals(Constants.SINGLE)) {
            Battle battle = clientDB.getCurrentBattle();
            if (endTurn(battle)) {
                return;
            }
            if (battle.getSingleOrMulti().equals(Constants.SINGLE) && battle.getPlayerInTurn().equals(battle.getPlayer2())) {
                AI.getInstance().doNextMove(battleGroundPane, battle);
                if (endTurn(battle)) {
                    return;
                }
            }
        }
        setTimeBar();
        updatePane();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setTimeBar();
        Main.getGlobalMediaPlayer().stop();
        Main.playMedia("src/music/battle.m4a"
                , Duration.INDEFINITE, Integer.MAX_VALUE, true, 100);
        setupPlayersInfoViews();
        setupBattleGroundCells();
        setupHandRings();
        setupHeroesImages();
        setupCursor();
        setupHeroSpecialPowerView();
        setupItemView();
        setupNextCardImage();
        updatePane();
    }

    private void setupNextCardImage() {
        nextCardImage = new NextCardImage(battleGroundPane);
    }

    private void setTimeBar() {
        String turnDuration = clientDB.getLoggedInAccount().getTurnDuration();
        if (turnDuration != null && !turnDuration.equals(Constants.NO_LIMIT)) {
            timeline.stop();
            timeline.getKeyFrames().clear();
            timeBar.setProgress(0);
            timeBar.setVisible(true);
            double seconds = Integer.parseInt(turnDuration);
            KeyValue keyValue = new KeyValue(timeBar.progressProperty(), 1);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(seconds), keyValue);
            timeline.getKeyFrames().add(keyFrame);
            timeline.play();
            timeline.setOnFinished(e -> {
                timeBar.setProgress(0);
                endTurnWhenClicked();
            });
        }
    }

    private void setupPlayersInfoViews() {
        player1Label.setText(clientDB.getCurrentBattle().getPlayer1().getPlayerInfo().getPlayerName());
        player2Label.setText(clientDB.getCurrentBattle().getPlayer2().getPlayerInfo().getPlayerName());
    }

    private void setupItemView() {
        Usable usable = (Usable) clientDB.getLoggedInAccount().getMainDeck().getItem();
        try {
            if (usable != null) {
                usableView.setImage(new Image(new FileInputStream("/src/ApProjectResources/units/" + usable.getName() + "/usable")));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupHeroSpecialPowerView() {
        final String MOUSE_ENTERED_STYLE = "-fx-effect: dropshadow(three-pass-box, rgba(255,255,255,1), 10, 0, 0, 0);";
        final String MOUSE_EXITED_STYLE = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0), 10, 0, 0, 0);";
        final String SELECTED_STYLE = "-fx-effect: dropshadow(three-pass-box, rgb(255,255,0), 10, 0, 0, 0);";
        Unit hero = clientDB.getLoggedInAccount().getMainDeck().getHero();
        if (hero.getMainSpecialPower() == null) {
            return;
        }
        try {
            specialPowerView.setImage(new Image(new FileInputStream
                    ("src/ApProjectResources/units/" + hero.getName() + "/special_power.png")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        specialPowerView.setOnMouseClicked(event -> {
            if (!clientDB.getLoggedInPlayer().equals(clientDB.getCurrentBattle().getPlayerInTurn()))
                return;
            if (clickedImageView != null && clickedImageView == specialPowerView) {
                clickedImageView = null;
                specialPowerView.setStyle(null);
            } else {
                clickedImageView = specialPowerView;
                specialPowerView.setStyle(SELECTED_STYLE);
            }
            updatePane();
        });
        specialPowerView.setOnMouseEntered(event -> {
            if (!specialPowerView.getStyle().equals(SELECTED_STYLE)) {
                specialPowerView.setStyle(MOUSE_ENTERED_STYLE);
            }
        });
        specialPowerView.setOnMouseExited(event -> {
            if (!specialPowerView.getStyle().equals(SELECTED_STYLE)) {
                specialPowerView.setStyle(MOUSE_EXITED_STYLE);
            }
        });
    }

    private void setupCursor() {
        try {
            ImageCursor cursor = new ImageCursor(new Image
                    (new FileInputStream("src/pics/mouse_icon")));
            battleGroundPane.setCursor(cursor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ControllerBattleCommands getOurInstance() {
        return ourInstance;
    }

    public ControllerBattleCommands() {
        ourInstance = this;
    }

    private void setupHeroesImages() {
        Unit playerHero = clientDB.getCurrentBattle().getPlayer1().getDeck().getHero();
        Unit opponentHero = clientDB.getCurrentBattle().getPlayer2().getDeck().getHero();
        UnitImage playerHeroImage = new UnitImage(playerHero.getId(), battleGroundPane);
        UnitImage opponentHeroImage = new UnitImage(opponentHero.getId(), battleGroundPane);
        unitImageList.add(opponentHeroImage);
        unitImageList.add(playerHeroImage);
        playerHeroImage.setInCell(2, 0);
        opponentHeroImage.setInCell(2, 8);
        opponentHeroImage.getUnitView().setScaleX(-1);
    }

    private void setupBattleGroundCells() {
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 9; column++) {
                cellsImages[row][column] = new CellImage(row, column, battleGroundPane);
            }
        }
    }

    public void handleCellClicked(Integer row, Integer column, Battle battle) {
        if (clientDB.getCurrentBattle().getSingleOrMulti().equals(Constants.MULTI))
            handleCellClickedForMulti(row, column);
        if (clientDB.getCurrentBattle().getSingleOrMulti().equals(Constants.SINGLE))
            handleCellClickedForSingle(row, column, battle);
    }

    private void handleCellClickedForSingle(Integer row, Integer column, Battle battle) {
        //todo complete it for other purposes too
        if (isClickedImageViewInHand()) {
            if (handleCardInsertion(row, column, battle)) {
                updatePane();
                return;
            }
        }
        if (clickedImageView.equals(specialPowerView)) {
            if (handleSpecialPowerInsertion(row, column, battle)) {
                updatePane();
                return;
            }
        }
        if (clickedImageView.equals(collectableView)) {
            //todo
        }
        if (isClickedImageViewUnit()) {
            if (handleUnitMove(row, column)) {
                updatePane();
                return;
            }
        }
        updatePane();
    }

    private void handleCellClickedForMulti(Integer row, Integer column) {
        List<Object> objects = new ArrayList<>();
        objects.add(row);
        objects.add(column);
        if (isClickedImageViewInHand()) {
            new ServerRequestSender(
                    new Request(RequestType.insertCard
                            , getHandImageWithCardView(clickedImageView).getId()
                            , null, objects)).start();
        }
        if (clickedImageView.equals(specialPowerView)) {
            new ServerRequestSender(
                    new Request(RequestType.useSpecialPower, null
                            , null, objects)).start();
        }
        if (clickedImageView.equals(collectableView)) {
            new ServerRequestSender(
                    new Request(RequestType.useCollectable, null,
                            null, objects)).start();
        }
        if (isClickedImageViewUnit()) {
            new ServerRequestSender(
                    new Request(RequestType.moveUnit
                            , getUnitImageWithUnitView(clickedImageView).getId()
                            , null, objects)).start();
        }
    }

    private boolean isClickedImageViewUnit() {
        for (UnitImage unitImage : unitImageList) {
            if (unitImage.getUnitView().equals(clickedImageView))
                return true;
        }
        return false;
    }

    private UnitImage getUnitImageWithUnitView(ImageView unitView) {
        for (UnitImage unitImage : unitImageList) {
            if (unitImage.getUnitView().equals(unitView))
                return unitImage;
        }
        return null;
    }

    private boolean handleSpecialPowerInsertion(int row, int column, Battle battle) {
        switch (clientDB.getCurrentBattle().useSpecialPower(clientDB.getLoggedInPlayer(), row, column, battle)) {
            case NO_HERO:
                //empty
                break;
            case HERO_HAS_NO_SPELL:
                //empty
                break;
            case SPECIAL_POWER_IN_COOLDOWN:
                //empty
                break;
            case NOT_ENOUGH_MANA:
                //empty
                break;
            case SPECIAL_POWER_USED:
                showSpecialPowerUse(row, column);
                break;
            default:
                System.out.println("unhandled case!!!!!");
        }
        //todo
        return false;
    }

    public void showSpecialPowerUse(int row, int column) {
        Unit hero = clientDB.getCurrentBattle().getBattleGround().getHeroOfPlayer(clientDB.getLoggedInPlayer());
        UnitImage heroImage = getUnitImageWithId(hero.getId());
        heroImage.showSpell();
        SpellImage specialPowerImage = new SpellImage
                (hero.getId(), row, column, battleGroundPane, SpellType.specialPower);
        spellImageList.add(specialPowerImage);
        clickedImageView = null;
    }

    private boolean handleUnitMove(int row, int column) {
        switch (clientDB.getCurrentBattle().getBattleGround()
                .moveUnit(row, column, clientDB.getCurrentBattle())) {
            case UNIT_NOT_SELECTED:
                //empty
                break;
            case OUT_OF_BOUNDARIES:
                //empty
                break;
            case CELL_IS_FULL:
                //empty
                break;
            case CELL_OUT_OF_RANGE:
                //empty
                break;
            case UNIT_ALREADY_MOVED:
                //empty
                break;
            case UNIT_MOVED:
                Player currentPlayer = clientDB.getCurrentBattle().getPlayerInTurn();
                UnitImage movedUnitImage = getUnitImageWithId(currentPlayer.getSelectedUnit().getId());
                movedUnitImage.showRun(row, column);
                return true;
            default:
        }
        return false;
    }

    private boolean handleCardInsertion(int row, int column, Battle battle) {
        HandImage handImage = getHandImageWithCardView(clickedImageView);
        Card card = clientDB.getLoggedInPlayer().getHand().getCardById(handImage.getId());
        switch (clientDB.getCurrentBattle().insert(card, row, column, battle)) {
            case NO_SUCH_CARD_IN_HAND:
                //empty
                break;
            case NOT_ENOUGH_MANA:
                //empty
                break;
            case INVALID_NUMBER:
                //empty
                break;
            case NOT_NEARBY_FRIENDLY_UNITS:
                //empty
                break;
            case THIS_CELL_IS_FULL:
                //empty
                break;
            case CARD_INSERTED:
                showCardInsertion(row, column, card.getId());
                return true;
            default:
        }
        return false;
    }

    public void showCardInsertion(int row, int column, String id) {
        Card card = clientDB.getLoggedInPlayer().getHand().getCardById(id);
        if (card == null) {
            if (clientDB.getCurrentBattle().getPlayer1().getPlayerInfo().getPlayerName().equals(
                    clientDB.getLoggedInAccount().getUsername()
            )) {
                card = clientDB.getCurrentBattle().getPlayer2().getHand().getCardById(id);
            } else {
                card = clientDB.getCurrentBattle().getPlayer1().getHand().getCardById(id);
            }
        }
        HandImage handImage = getHandImageWithId(id);
        if (card instanceof Unit)
            insertUnitView(row, column, card);
        if (card instanceof Spell)
            insertSpellView(row, column, card);
        if (handImage != null) {
            handImage.clearHandImage();
        }
    }

    public List<SpellImage> getSpellImageList() {
        return spellImageList;
    }

    private void insertSpellView(int row, int column, Card card) {
        SpellImage insertedSpellImage = new SpellImage
                (card.getId(), row, column, battleGroundPane, SpellType.spell);
        spellImageList.add(insertedSpellImage);
        clickedImageView = null;
    }

    public void insertUnitView(int row, int column, Card card) {
        UnitImage insertedUnitImage = new UnitImage(card.getId(), battleGroundPane);
        unitImageList.add(insertedUnitImage);
        insertedUnitImage.setInCell(row, column);
        insertedUnitImage.showSpawn();
        clickedImageView = null;
    }

    public HandImage getHandImageWithCardView(ImageView cardView) {
        for (HandImage handImage : handImageList) {
            if (handImage.getCardView().equals(cardView))
                return handImage;
        }
        return null;
    }

    public HandImage getHandImageWithId(String id) {
        for (HandImage handImage : handImageList) {
            if (handImage.getId().equals(id))
                return handImage;
        }
        return null;
    }

    public boolean isClickedImageViewInHand() {
        for (HandImage handImage : handImageList) {
            if (handImage.getCardView().equals(clickedImageView))
                return true;
        }
        return false;
    }

    public void handleUnitClicked(String id) {
        if (clientDB.getCurrentBattle().getSingleOrMulti().equals(Constants.MULTI))
            handleUnitClickedForMulti(id);
        if (clientDB.getCurrentBattle().getSingleOrMulti().equals(Constants.SINGLE))
            handleUnitClickedForSingle(id);
    }

    private void handleUnitClickedForSingle(String id) {
        Player currentPlayer = clientDB.getCurrentBattle().getPlayerInTurn();
        if (handleUnitSelection(id)) {
            updatePane();
            return;
        }
        if (currentPlayer.getSelectedUnit() != null) {
            if (handleUnitAttack(id)) {
                updatePane();
                return;
            }
        }
        updatePane();
    }

    private void handleUnitClickedForMulti(String id) {
        //todo other cases select and attack
        Battle battle = clientDB.getCurrentBattle();
        UnitImage clickedUnit = getUnitImageWithId(id);
        if (battle.getBattleGround().isUnitFriendlyOrEnemy(clickedUnit.getId(), battle)
                .equals(Constants.FRIEND)) {
            new ServerRequestSender(new Request(RequestType.selectUnit
                    , id, null, null)).start();
        }
        if (battle.getBattleGround().isUnitFriendlyOrEnemy(clickedUnit.getId(), battle)
                .equals(Constants.ENEMY)) {
            new ServerRequestSender(new Request(RequestType.attackUnit
                    , id, null, null)).start();
        }
    }

    private boolean handleUnitAttack(String id) {
        Player currentPlayer = clientDB.getCurrentBattle().getPlayerInTurn();
        UnitImage selectedUnitImage = getUnitImageWithId(currentPlayer.getSelectedUnit().getId());
        UnitImage targetedUnitImage = getUnitImageWithId(id);
        //todo add this feature to unselect a unit with clicking on it if needed
        switch (currentPlayer.getSelectedUnit().attack(id, clientDB.getCurrentBattle())) {
            case UNIT_ATTACKED:
                selectedUnitImage.showAttack(targetedUnitImage.getColumn());
                return true;
            case UNIT_AND_ENEMY_ATTACKED:
                selectedUnitImage.showAttack(targetedUnitImage.getColumn());
                targetedUnitImage.showAttack(selectedUnitImage.getColumn());
                return true;
            case ATTACKED_FRIENDLY_UNIT:
                //empty
                break;
            case ALREADY_ATTACKED:
                //empty
                break;
            case INVALID_CARD:
                //empty
                break;
            case TARGET_NOT_IN_RANGE:
                //empty
                break;
            default:
                System.out.println("unhandled case");
        }
        return false;
    }

    private boolean handleUnitSelection(String id) {
        Player currentPlayer = clientDB.getCurrentBattle().getPlayerInTurn();
        UnitImage unitImage = getUnitImageWithId(id);
        switch (currentPlayer.selectUnit(id, clientDB.getCurrentBattle())) {
            case SELECTED:
                unitImage.setUnitStyleAsSelected();
                clickedImageView = unitImage.getUnitView();
                return true;
            case ENEMY_UNIT_SELECTED:
                //empty
                break;
            case INVALID_COLLECTABLE_CARD:
                //empty
                break;
            case UNIT_IS_STUNNED:
                //empty
                break;
            default:
                System.out.println("unhandled case !!!!!!!!");
        }
        return false;
    }

    public List<ImageView> getHandRings() {
        return handRings;
    }

    private void setupHandRings() {
        handRings.add(handRing1);
        handRings.add(handRing2);
        handRings.add(handRing3);
        handRings.add(handRing4);
        handRings.add(handRing5);
        handImageList.add(new HandImage(0, getBattleGroundPane()));
        handImageList.add(new HandImage(1, getBattleGroundPane()));
        handImageList.add(new HandImage(2, getBattleGroundPane()));
        handImageList.add(new HandImage(3, getBattleGroundPane()));
        handImageList.add(new HandImage(4, getBattleGroundPane()));
    }

    public UnitImage getUnitImageWithId(String id) {
        for (UnitImage unitImage : unitImageList) {
            if (unitImage.getId().equals(id))
                return unitImage;
        }
        return null;
    }

    public void updatePane() {
        updateUnitImages();
        updateSpecialPowerImage();
        updateCellImages();
//        updateNextCardImage();
//        updateCollectableIcon();//todo
        updateFlags();
//        updateCollectable();
        updateHandImages();
        updatePlayersInfo();
        updateHand();
        updateEndTurnButton();
    }

    private void updateEndTurnButton() {
        System.out.println(clientDB.getCurrentBattle().getPlayerInTurn().getPlayerInfo().getPlayerName());
        System.out.println("** " + clientDB.getLoggedInPlayer().getPlayerInfo().getPlayerName());
        if (clientDB.getLoggedInPlayer().equals(clientDB.getCurrentBattle().getPlayerInTurn())) {
            endTurnMineBtn.setVisible(true);
            endTurnEnemyBtn.setVisible(false);
        } else {
            endTurnMineBtn.setVisible(false);
            endTurnEnemyBtn.setVisible(true);
        }
    }

    private void updateCollectableIcon() {
        /*Collectable collectable = clientDB.getCurrentBattle().getCollectable();
        Player player1 = clientDB.getCurrentBattle().getPlayer1();
        if (!player1.getCollectables().isEmpty()) {
            if (clientDB.getLoggedInAccount().getPlayerInfo().getPlayerName().equals(player1.getPlayerInfo().getPlayerName()) &&
                    player1.getCollectables().get(0).equals(collectable)) {
                try {
                    collectableView.setImage(new Image(new FileInputStream("src/ApProjectResources/collectables/" +
                            collectable.getName() + "/collectable")));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }

    private void updateNextCardImage() {
        Card card = clientDB.getLoggedInPlayer().getNextCard();
        nextCardImage.setCardImage(card.getId());
    }

    private void updateCellImages() {
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 9; column++) {
                Cell cell = clientDB.getCurrentBattle().getBattleGround().getCells()[row][column];
                CellImage cellImage = cellsImages[row][column];
                cellImage.clearBuffImageList();
                for (Buff buff : cell.getBuffs()) {
                    cellImage.addBuffImage(buff.getType());
                }
            }
        }
    }

    private void updateUnitImages() {
        for (UnitImage unitImage : unitImageList) {
            if (unitImage == null)
                continue;
            BattleGround battleGround = clientDB.getCurrentBattle().getBattleGround();
            if (!battleGround.doesHaveUnit(unitImage.getId())) {
                unitImage.showDeath();
                continue;
            }
            Unit unit = battleGround.getUnitWithID(unitImage.getId());
            if (unit == null)
                continue;
            unitImage.setApNumber(unit.getAp());
            unitImage.setHpNumber(unit.getHp());
            if (unitImage.getUnitView().equals(clickedImageView))
                unitImage.setUnitStyleAsSelected();
            else unitImage.setStyleAsNotSelected();
            unitImage.clearBuffImageList();
            for (Buff buff : unit.getBuffs()) {
                unitImage.addBuffImage(buff.getType());
            }
        }
    }

    private void updateSpecialPowerImage() {
        if (clickedImageView != specialPowerView)
            specialPowerView.setStyle(null);
    }

    private void updateFlags() {
        BattleGround battleGround = clientDB.getCurrentBattle().getBattleGround();
        for (FlagImage flagImage : flagImages)
            flagImage.removeFromRoot();
        flagImages.clear();
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 9; column++) {
                if (battleGround.getCells()[row][column].getFlags().size() > 0) {
                    FlagImage flagImage = new FlagImage(battleGroundPane);
                    flagImage.setInCell(row, column);
                    flagImages.add(flagImage);
                }
            }
        }
    }

    private void updateCollectable() {
        BattleGround battleGround = clientDB.getCurrentBattle().getBattleGround();
        collectableImage.removeFromRoot();
        collectableImage = null;
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 9; column++) {
                if (battleGround.getCells()[row][column].getCollectable() != null) {
                    collectableImage = new CollectableImage(battleGroundPane);
                    collectableImage.setInCell(row, column);

                }
            }
        }
    }

    private void updateHandImages() {
        for (HandImage handImage : handImageList) {
            if (handImage.getCardView().equals(clickedImageView))
                handImage.setStyleAsSelected();
            else handImage.setStyleAsNotSelected();
        }
    }

    private void updatePlayersInfo() {
        player1ManaLabel.setText(Integer.toString(clientDB.getCurrentBattle().getPlayer1().getMana()));
        player2ManaLabel.setText(Integer.toString(clientDB.getCurrentBattle().getPlayer2().getMana()));
    }

    private void updateHand() {
        List<Card> handCards = clientDB.getLoggedInPlayer().getHand().getCards();
        for (int i = 0; i < handCards.size(); i++) {
            Card card = handCards.get(i);
            handImageList.get(i).setCardImage(card.getId());
        }
    }

    private void showNextCard() {
        /*Card card = clientDB.getCurrentBattle().getPlayerInTurn().getNextCard();
        if (card instanceof Spell) {
            view.showCardInfoSpell((Spell) card);
        } else if (card instanceof Unit) {
            view.showCardInfoMinion((Unit) card);
        }*/
    }

    private void attackCombo() {
      /*  String[] orderPieces = request.getCommand().split(" ");
        String[] attackers = new String[orderPieces.length - 3];
        if (orderPieces.length - 3 >= 0)
            System.arraycopy(orderPieces, 3, attackers, 0
                    , orderPieces.length - 3);
        view.printOutputMessage(Unit.attackCombo(orderPieces[2], attackers));
    */
    }

    public void useCollectable() {
        /*int row = Integer.parseInt(request.getCommand().split("[ (),]")[2]);
        int column = Integer.parseInt(request.getCommand().split("[ (),]")[3]);
        Collectable collectable = clientDB.getCurrentBattle().getPlayerInTurn().getSelectedCollectable();
        view.printOutputMessage(clientDB.getCurrentBattle().useCollectable(collectable, row, column));
    */
    }

    private void forfeitGame() {
        //todo this method have to implemented in model
        // to be used by both server and client
        /*Main.getGlobalMediaPlayer().play();
        Account account = clientDB.getAccountWithUsername(clientDB.getCurrentBattle().getPlayerInTurn().getPlayerInfo().getPlayerName());
        Account player1 = clientDB.getAccountWithUsername(clientDB.getCurrentBattle().getPlayer1().getPlayerInfo().getPlayerName());
        Account player2 = clientDB.getAccountWithUsername(clientDB.getCurrentBattle().getPlayer2().getPlayerInfo().getPlayerName());
        MatchInfo matchInfo1 = player1.getMatchList().get(player1.getMatchList().size() - 1);
        MatchInfo matchInfo2 = player2.getMatchList().get(player2.getMatchList().size() - 1);
        if (player1 == account) {
            matchInfo1.setWinner(player2.getUsername());
            matchInfo2.setWinner(player2.getUsername());
        } else {
            matchInfo1.setWinner(player1.getUsername());
            matchInfo2.setWinner(player1.getUsername());
        }*/
    }

    private boolean endGame() {
        timeBar.setDisable(true);
        clientDB.setCurrentBattle(null);
        //todo setGame finished
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "game has finished please press ok to exit to main menu");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
        returnToMainMenu();
        return true;
    }

    private void returnToMainMenu() {
        endTurnMineBtn.setDisable(true);
        graveYardBtn.setDisable(true);
        KeyValue keyValue = new KeyValue(Main.window.opacityProperty(), 0);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(2000), keyValue);
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(keyFrame);
        timeline.setOnFinished(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("ControllerMainMenu.fxml"));
                Main.window.setScene(new Scene(root));
                Main.setCursor(Main.window);
            } catch (IOException ignored) {

            }
            KeyValue keyValueFinished = new KeyValue(Main.window.opacityProperty(), 1);
            KeyFrame keyFrameFinished = new KeyFrame(Duration.millis(2000), keyValueFinished);
            Timeline timelineFinished = new Timeline();
            timelineFinished.getKeyFrames().add(keyFrameFinished);
            timelineFinished.play();
        });
        timeline.play();
    }

    private boolean endTurn(Battle battle) {
        OutputMessageType outputMessageType = clientDB.getCurrentBattle().nextTurn();
        if (outputMessageType == OutputMessageType.WINNER_PLAYER1
                || outputMessageType == OutputMessageType.WINNER_PLAYER2) {
            return endGame();
            //todo add winner name to endgame
        }
        return false;
    }

    public double getCellLayoutX(int column) {
        return GraphicConstants.BATTLE_GROUND_START_X + GraphicConstants.CELL_WIDTH * column;
    }

    public double getCellLayoutY(int row) {
        return GraphicConstants.BATTLE_GROUND_START_Y + GraphicConstants.CELL_HEIGHT * row;
    }

    public AnchorPane getBattleGroundPane() {
        return battleGroundPane;
    }

    public ImageView getNextCardRing() {
        return nextCardRing;
    }

    public List<UnitImage> getUnitImageList() {
        return unitImageList;
    }

    public ImageView getEndTurnMineBtn() {
        return endTurnMineBtn;
    }

    public ImageView getEndTurnEnemyBtn() {
        return endTurnEnemyBtn;
    }

//    public void recordVideo() {
//        final VideoCapture videoCapture = VideoCapture.create();
//        videoCapture.setCaptureArea(new Rectangle(100, 100, 1486, 819));
//
//        java.util.List<Codec> videoCodecs = videoCapture.getVideoCodecs();
//        Codec videoCodec = videoCodecs.get(1);
//
//        EncodingParameters encodingParameters = new EncodingParameters(new File("Rectangle." + videoCapture.getVideoFormat().getId()));
//        encodingParameters.setSize(new Dimension(640, 480));
//        encodingParameters.setBitrate(500000);
//        encodingParameters.setFramerate(30);
//        encodingParameters.setCodec(videoCodec);
//
//        videoCapture.setEncodingParameters(encodingParameters);
//        videoCapture.start();

    //        videoCapture.stop();
//        System.out.println("Done.");
//    }
}