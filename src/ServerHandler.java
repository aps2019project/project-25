import com.gilecode.yagson.YaGson;
import com.gilecode.yagson.YaGsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ServerHandler extends Thread {
    private static ServerHandler instance;
    private ClientDB clientDB = ClientDB.getInstance();

    private String address;
    private int port;

    public ServerHandler(String address, int port) {
        this.address = address;
        this.port = port;
        instance = this;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(address, port);
            clientDB.setSocket(socket);
            JsonStreamParser parser = ClientDB.getInstance().getParser();
            YaGson yaGson = new YaGsonBuilder().setPrettyPrinting().create();
            while (true) {
                JsonObject obj = parser.next().getAsJsonObject();
                Response response = yaGson.fromJson(obj.toString(), Response.class);
                switch (response.getResponseType()) {
                    case matchFound:
                        caseMatchFound(response);
                        break;
                    case unitMoved:
                        caseUnitMoved(response);
                        break;
                    case cardInserted:
                        caseCardInserted(response);
                        break;
                    case specialPowerUsed:
                        caseSpecialPowerUsed(response);
                        break;
                    case collectableUsed:
                        break;
                    case PlayerForfeited:
                        casePlayerForfeited(response);
                        break;
                    case unitSelected:
                        caseUnitSelected(response);
                        break;
                    case unitAttacked:
                        caseUnitAttack(response);
                        break;
                    case unitAndEnemyAttacked:
                        caseUnitAndEnemyAttacked(response);
                        break;
                    case player1Won:
                        casePlayer1Won(response);
                        break;
                    case player2Won:
                        casePlayer2Won(response);
                        break;
                    case turnChanged:
                        caseTurnChanged(response);
                        break;
                    case sendMessage:
                        caseSendMessage(response);
                        break;
                    case close:
                        //empty
                        break;
                    case signUp:
                        caseSignUp(response);
                        break;
                    case login:
                        caseLogin(response);
                        break;
                    case logout:
                        caseLogout(response);
                        break;
                    case leaderBoard:
                        caseLeaderBoard(response);
                        break;
                    case updateLeaderBoard:
                        caseLeaderBoard(response);
                        break;
                    case shop:
                        caseShop(response);
                        break;
                    case buy:
                        caseBuy(response);
                        break;
                    case sell:
                        caseSell(response);
                        break;
                    case customCardAdded:
                        caseCustomCardAdded(response);
                        break;
                    case enterBuyAuction:
                        caseEnterBuyAuction(response);
                        break;
                    case auctionBuyUpdate:
                        caseAuctionBuyUpdate(response);
                        break;
                    case auctionSellUpdate:
                        caseAuctionSellUpdate(response);
                        break;
                    case auctionSellExit:
                        caseAuctionSellExit(response);
                        break;
                }
                logResponse(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void caseTurnChanged(Response response) {
        ControllerBattleCommands controllerBattleCommands = ControllerBattleCommands.getOurInstance();
        Battle battle = (Battle) response.getObjectList().get(0);
        clientDB.setCurrentBattle(battle);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controllerBattleCommands.updatePane();
            }
        });
    }

    private void casePlayer1Won(Response response) {
        ControllerBattleCommands controllerBattleCommands = ControllerBattleCommands.getOurInstance();
        Battle battle = (Battle) response.getObjectList().get(0);
        clientDB.setCurrentBattle(battle);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Parent root = null;
                try {
                    if (clientDB.getLoggedInPlayer().equals(battle.getPlayer1())) {
                        root = FXMLLoader.load(getClass().getResource("EndGameVictory.fxml"));
                        EndGameVictory.getInstance().setPrizeLabel(battle.getPrize());
                    } else
                        root = FXMLLoader.load(getClass().getResource("EndGameDefeat.fxml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Main.window.setScene(new Scene(root));
                Main.setCursor(Main.window);
                controllerBattleCommands.updatePane();
                stopRecording();
            }
        });
    }

    private void casePlayer2Won(Response response) {
        ControllerBattleCommands controllerBattleCommands = ControllerBattleCommands.getOurInstance();
        Battle battle = (Battle) response.getObjectList().get(0);
        clientDB.setCurrentBattle(battle);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Parent root = null;
                try {
                    if (clientDB.getLoggedInPlayer().equals(battle.getPlayer2())) {
                        root = FXMLLoader.load(getClass().getResource("EndGameVictory.fxml"));
                        EndGameVictory.getInstance().setPrizeLabel(battle.getPrize());
                    } else root = FXMLLoader.load(getClass().getResource("EndGameDefeat.fxml"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Main.window.setScene(new Scene(root));
                Main.setCursor(Main.window);
                controllerBattleCommands.updatePane();
                stopRecording();
            }
        });
    }

    private void stopRecording(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        ControllerBattleCommands.getOurInstance().getCurrentVideoCapture().stop();
    }
    private void caseAuctionSellExit(Response response) {
        Account account = (Account) response.getObjectList().get(0);
        clientDB.setLoggedInAccount(account);
        Platform.runLater(()->{
            ControllerMainMenu.auctionSell.close();
            ControllerCollectionEditMenu.getOurInstance().showCardsInDeck();
            ControllerCollectionEditMenu.getOurInstance().showCardsInCollection();

        });

    }

    private void caseAuctionSellUpdate(Response response) {
        System.out.println("caseAuctionSellUpdate");
        Auction auction = (Auction) response.getObjectList().get(0);
        List<String> strings = new ArrayList<>();
        for(Account account:auction.getBidders()){
            strings.add(account.getUsername());
        }
        strings.forEach(System.out::println);
        Platform.runLater(() -> {
            ControllerAuctionSell.getInstance().updateBiddersList(strings,auction.getBids());
        });
    }

    private void caseAuctionBuyUpdate(Response response) {
        List<String> strings = new ArrayList<>();
        for (Object o : response.getObjectList()) {
            strings.add((String) o);
        }
        Platform.runLater(() -> {
            ControllerAuctionBuy.getInstance().upadateAuctionList(strings);
        });
    }

    private void caseEnterBuyAuction(Response response) {
        List<String> strings = new ArrayList<>();
        for (Object object : response.getObjectList()) {
            String string = (String) object;
            strings.add(string);
        }
        Platform.runLater(() -> {
            try {

                AnchorPane root = FXMLLoader.load(getClass().getResource("ControllerAuctionBuy.fxml"));
                ControllerAuctionBuy.getInstance().upadateAuctionList(strings);
                Stage stage = new Stage();
                ControllerMainMenu.auctionBuy = stage;
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();
            }catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void caseCustomCardAdded(Response response) {
        Card card = (Card) response.getObjectList().get(0);
        ClientDB.getInstance().getCardList().add(card);
        Platform.runLater(() -> {
            try {
                ControllerShop.getOurInstance().showCards(clientDB.getCardList(), clientDB.getUsableList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void caseUnitAndEnemyAttacked(Response response) {
        ControllerBattleCommands controllerBattleCommands = ControllerBattleCommands.getOurInstance();
        String attackerId = (String) response.getObjectList().get(0);
        String targetedId = (String) response.getObjectList().get(1);
        Battle battle = (Battle) response.getObjectList().get(2);
        clientDB.setCurrentBattle(battle);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                UnitImage attackerImage = controllerBattleCommands.getUnitImageWithId(attackerId);
                UnitImage targetedImage = controllerBattleCommands.getUnitImageWithId(targetedId);
                attackerImage.showAttack(targetedImage.getColumn());
                targetedImage.showAttack(attackerImage.getColumn());
            }
        });
    }

    private void caseUnitAttack(Response response) {
        ControllerBattleCommands controllerBattleCommands = ControllerBattleCommands.getOurInstance();
        String attackerId = (String) response.getObjectList().get(0);
        String targetedId = (String) response.getObjectList().get(1);
        Battle battle = (Battle) response.getObjectList().get(2);
        clientDB.setCurrentBattle(battle);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                UnitImage attackerImage = controllerBattleCommands.getUnitImageWithId(attackerId);
                UnitImage targetedImage = controllerBattleCommands.getUnitImageWithId(targetedId);
                attackerImage.showAttack(targetedImage.getColumn());
            }
        });
    }

    private void caseCardInserted(Response response) {
        ControllerBattleCommands controllerBattleCommands = ControllerBattleCommands.getOurInstance();
        String id = response.getMessage();
        Integer row = (Integer) response.getObjectList().get(0);
        Integer column = (Integer) response.getObjectList().get(1);
        Card card = (Card) response.getObjectList().get(2);
        Battle battle = (Battle) response.getObjectList().get(3);
        clientDB.setCurrentBattle(battle);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                controllerBattleCommands.showCardInsertion(row, column, card);
                controllerBattleCommands.updatePane();
            }
        });
    }

    private void casePlayerForfeited(Response response) {
    }

    private void caseSell(Response response) {
        clientDB.setLoggedInAccount((Account) response.getObjectList().get(0));
        Platform.runLater(() -> {
            ControllerCollectionEditMenu controllerCollectionEditMenu = ControllerCollectionEditMenu.getOurInstance();
            Label messageLabel = controllerCollectionEditMenu.getMessageLabel();
            messageLabel.setText(response.getMessage());
            deleteMessageAfterSomeTime(messageLabel, 1);
            controllerCollectionEditMenu.showCardsInDeck();
            controllerCollectionEditMenu.showCardsInCollection();
        });
    }

    private void caseBuy(Response response) {
        clientDB.setLoggedInAccount((Account) response.getObjectList().get(0));
        System.out.println("here");
        System.out.println(clientDB.getLoggedInAccount().getMoney());
        Platform.runLater(() -> {
            ControllerShop.getOurInstance().getMoneyLabel().setText(Integer.toString(clientDB.getLoggedInAccount().getMoney()));
            ControllerShop.getOurInstance().getBuyMessage().setText(response.getMessage());
            deleteMessageAfterSomeTime(ControllerShop.getOurInstance().getBuyMessage(), 1);
        });
    }

    private void caseShop(Response response) {
        List<Card> cardList = new ArrayList<>();
        List<Usable> usableList = new ArrayList<>();
        separateCardsUsables(response, cardList, usableList);
        System.out.println("here");
//        cardList.forEach(e -> System.out.println(e.getName()));
        Platform.runLater(() -> {
            try {
                ControllerShop.getOurInstance().showCards(cardList, usableList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void caseSignUp(Response response) {
        if (response.getMessage().equals(OutputMessageType.CREATED_ACCOUNT_SUCCESSFULLY.getMessage())) {
            Account account = (Account) response.getObjectList().get(0);
            ClientDB.getInstance().setLoggedInAccount(account);
            Main.openMainMenu();
        } else if (response.getMessage().equals(OutputMessageType.USERNAME_ALREADY_EXISTS.getMessage())) {
            Label label = findInvalidUserName(Main.window, "loginPane", "invalidUsername");
            if (label != null) {
                Platform.runLater(() -> label.setText(OutputMessageType.USERNAME_ALREADY_EXISTS.getMessage()));
            }
        }
    }

    private void caseSendMessage(Response response) {
        ChatMessage chatMessage = (ChatMessage) response.getObjectList().get(0);
        Platform.runLater(() -> {
            AnchorPane chatBox = null;
            try {
                chatBox = FXMLLoader.load(getClass().getResource("ChatStyle.fxml"));
                ControllerGlobalChat.setChatBox(chatMessage, chatBox, "#1919f7");
                HBox hBox = new HBox();
                hBox.setPrefWidth(ControllerGlobalChat.getInstance().getChatVBox().getPrefWidth() - 30);
                hBox.setAlignment(Pos.BASELINE_LEFT);
                hBox.getChildren().add(chatBox);
                ControllerGlobalChat.getInstance().getChatVBox().getChildren().add(hBox);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    private void caseLogout(Response response) {
        if (response.getMessage().equals(OutputMessageType.LOGGED_OUT_SUCCESSFULLY.getMessage())) {
            ClientDB.getInstance().setLoggedInAccount(null);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Main.playWhenButtonClicked();
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("ControllerAccount.fxml"));
                        Main.window.setScene(new Scene(root));
                        Main.dragAbilityForScenes(Main.window, root);
                        Main.setCursor(Main.window);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void caseLogin(Response response) {
        if (response.getMessage().equals(OutputMessageType.LOGGED_IN_SUCCESSFULLY.getMessage())) {
            Account account = (Account) response.getObjectList().get(0);
            ClientDB.getInstance().setLoggedInAccount(account);
            Main.openMainMenu();
        } else if (response.getMessage().equals(OutputMessageType.INVALID_PASSWORD.getMessage())) {
            Label label = findInvalidUserName(Main.window, "loginPane", "invalidPassword");
            if (label != null) {
                Platform.runLater(() -> label.setText(OutputMessageType.INVALID_PASSWORD.getMessage()));
            }
        } else if (response.getMessage().equals(OutputMessageType.ACCOUNT_DOESNT_EXIST.getMessage())) {
            Label label = findInvalidUserName(Main.window, "loginPane", "invalidUsername");
            if (label != null) {
                Platform.runLater(() -> label.setText(OutputMessageType.ACCOUNT_DOESNT_EXIST.getMessage()));
            }
        } else if (response.getMessage().equals(OutputMessageType.ALREADY_LOGGED_IN.getMessage())) {
            Label label = findInvalidUserName(Main.window, "loginPane", "invalidUsername");
            if (label != null) {
                Platform.runLater(() -> label.setText(OutputMessageType.ALREADY_LOGGED_IN.getMessage()));
            }
        }
    }

    private void deleteMessageAfterSomeTime(Label messageLabel, double time) {
        PauseTransition visiblePause = new PauseTransition(
                Duration.seconds(time)
        );
        visiblePause.setOnFinished(
                event1 -> messageLabel.setText("")
        );
        visiblePause.play();
    }

    private void separateCardsUsables(Response response, List<Card> cardList, List<Usable> usableList) {
        for (int i = 0; i < response.getIntegers().get(0); i++) {
            cardList.add((Card) response.getObjectList().get(i));
        }
        for (int i = 0; i < response.getIntegers().get(1); i++) {
            usableList.add((Usable) response.getObjectList().get(i + response.getIntegers().get(0)));
        }
        clientDB.getCardList().clear();
        clientDB.getUsableList().clear();
        clientDB.getCardList().addAll(cardList);
        clientDB.getUsableList().addAll(usableList);
    }

    private void caseLeaderBoard(Response response) {
        List<Account> accountList = new ArrayList(response.getObjectList());
        List<Integer> integerList = new ArrayList<>(response.getIntegers());
        Map<Account, Integer> accountIntegerMap = new HashMap<>();
        for (int i = 0; i < accountList.size(); i++) {
            accountIntegerMap.put(accountList.get(i), integerList.get(i));
        }
        Collections.sort(accountList);
        Platform.runLater(new Runnable() {
            FXMLLoader fxmlLoader;

            @Override
            public void run() {
                VBox vBox = findVBoxInLeaderBoard(ControllerMainMenu.stage);
                vBox.getChildren().clear();
                for (int i = 0; i < accountList.size(); i++) {
                    fxmlLoader = new FXMLLoader();
                    fxmlLoader.setLocation(getClass().getResource("ControllerLeaderBoardAccount.fxml"));
                    try {
                        Node node = fxmlLoader.load();
                        ControllerLeaderBoardAccount controllerLeaderBoardAccount = fxmlLoader.getController();
                        controllerLeaderBoardAccount.setAccountLabels(accountList.get(i), accountIntegerMap.get(accountList.get(i)));
                        vBox.getChildren().add(node);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private VBox findVBoxInLeaderBoard(Stage stage) {
        Parent root = stage.getScene().getRoot();
        ScrollPane scrollPane = null;
        for (int i = 0; i < root.getChildrenUnmodifiable().size(); i++) {
            if (root.getChildrenUnmodifiable().get(i) instanceof ScrollPane) {
                scrollPane = (ScrollPane) root.getChildrenUnmodifiable().get(i);
            }
        }
        return (VBox) scrollPane.getContent();
    }

    private Label findInvalidUserName(Stage stage, String paneID, String labelID) {
        AnchorPane anchorPane = null;
        for (Object object : stage.getScene().getRoot().getChildrenUnmodifiable()) {
            if (object instanceof AnchorPane) {
                anchorPane = (AnchorPane) object;
                if (anchorPane.getId().equals(paneID)) {
                    break;
                }
            }
        }
        if (anchorPane != null) {
            for (Object object : anchorPane.getChildren()) {
                if (object instanceof Label) {
                    Label label = (Label) object;
                    if (label.getId().equals(labelID)) {
                        return label;
                    }
                }
            }
        }
        return null;
    }

    public static ServerHandler getInstance() {
        return instance;
    }

    private void caseSpecialPowerUsed(Response response) {
        Integer row = (Integer) response.getObjectList().get(0);
        Integer column = (Integer) response.getObjectList().get(1);
        clientDB.setCurrentBattle((Battle) response.getObjectList().get(2));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ControllerBattleCommands.getOurInstance().showSpecialPowerUse(row, column);
                ControllerBattleCommands.getOurInstance().updatePane();
            }
        });
    }

    private void caseUnitSelected(Response response) {
        UnitImage selectedUnit = ControllerBattleCommands.getOurInstance().getUnitImageWithId(response.getMessage());
        clientDB.setCurrentBattle((Battle) response.getObjectList().get(0));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                selectedUnit.showSelect();
                ControllerBattleCommands.getOurInstance().setClickedImageView(selectedUnit.getUnitView());
                ControllerBattleCommands.getOurInstance().updatePane();
            }
        });
    }

    private void caseUnitMoved(Response response) {
        clientDB.setCurrentBattle((Battle) response.getObjectList().get(2));
        String id = response.getMessage();
        Integer row = (Integer) response.getObjectList().get(0);
        Integer column = (Integer) response.getObjectList().get(1);
        UnitImage movedUnit = ControllerBattleCommands.getOurInstance().getUnitImageWithId(id);
        Platform.runLater(() -> {
            movedUnit.showRun(row, column);
            ControllerBattleCommands.getOurInstance().updatePane();
        });
    }

    private void caseMatchFound(Response response) {
        if (response.getResponseType().equals(ResponseType.matchFound)) {
            clientDB.getLoggedInAccount().setTurnDuration(response.getMessage());
            clientDB.setCurrentBattle((Battle) response.getObjectList().get(0));
            Player player1 = clientDB.getCurrentBattle().getPlayer1();
            if (clientDB.getLoggedInAccount().getUsername().equals(player1.getPlayerInfo().getPlayerName()))
                clientDB.setLoggedInPlayer(player1);
            else clientDB.setLoggedInPlayer(clientDB.getCurrentBattle().getPlayer2());
            Platform.runLater(() -> {
                Main.playWhenButtonClicked();
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("ControllerBattleCommandsFXML.fxml"));
                    ControllerMainMenu.multiPlayerStage.close();
                    ControllerMultiPlayerMenu.getInstance().getBackgroundMusic().stop();
                    Main.window.setScene(new Scene(root));
                    Main.setCursor(Main.window);
                    ControllerBattleCommands controller = ControllerBattleCommands.getOurInstance();
                    String player1Name = clientDB.getCurrentBattle().getPlayer1().getPlayerInfo().getPlayerName();
                    String player2Name = clientDB.getCurrentBattle().getPlayer2().getPlayerInfo().getPlayerName();
//                    if (clientDB.getLoggedInAccount().getPlayerInfo().getPlayerName().equals(player1Name))
//                        controller.recordVideo(clientDB.generateNameForVideoRecord(player2Name + "-"));
//                    else
//                        controller.recordVideo(clientDB.generateNameForVideoRecord(player1Name + "-"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void logResponse(Response response) {
        System.out.println("ServerHandler-->>" + response.getResponseType());
    }
}