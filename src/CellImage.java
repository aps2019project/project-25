import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class CellImage {
    private static ClientDB clientDB = ClientDB.getInstance();
    private AnchorPane root;
    private final int CELL_WIDTH = 63;
    private final int CELL_HEIGHT = 50;
    private int row = 0;
    private int column = 0;
    private Label cellLabel = new Label();
    private List<BuffImage> buffImageList = new ArrayList<>();

    public CellImage(int row, int column, AnchorPane root) {
        this.root = root;
        this.row = row;
        this.column = column;
        cellLabel.relocate(ControllerBattleCommands.getOurInstance().getCellLayoutX(column)
                , ControllerBattleCommands.getOurInstance().getCellLayoutY(row));
        setLabelStyle();
        cellLabel.setOnMouseClicked(event -> {
            if (!ControllerBattleCommands.getOurInstance().canPlayerTouchScreen())
                return;
            ControllerBattleCommands.getOurInstance().handleCellClicked
                    (row, column, ClientDB.getInstance().getCurrentBattle());
        });
        addToRoot();
    }

    public Label getCellLabel(){
        return cellLabel;
    }

    private void addToRoot() {
        root.getChildren().add(cellLabel);
    }

    private void setLabelStyle() {
        cellLabel.setMinWidth(CELL_WIDTH);
        cellLabel.setMinHeight(CELL_HEIGHT);
        cellLabel.setStyle("-fx-background-radius: 10;-fx-background-color: #ebdad5;-fx-opacity: .2");
        cellLabel.setOnMouseEntered(e -> {
            cellLabel.setStyle("-fx-background-radius: 10;-fx-background-color: #ebdad5;-fx-opacity: .4");
        });
        cellLabel.setOnMouseExited(e -> {
            cellLabel.setStyle("-fx-background-radius: 10;-fx-background-color: #ebdad5;-fx-opacity: .2");
        });
    }

    public void clearBuffImageList() {
        for (BuffImage buffImage : buffImageList) {
            root.getChildren().remove(buffImage);
        }
        buffImageList.clear();
    }

    public void addBuffImage(BuffType buffType) {
        for (BuffImage buffImage : buffImageList) {
            if (buffImage.getBuffType().equals(buffType))
                return;
        }
        BuffImage buffImage = new BuffImage(buffType, root);
        buffImageList.add(buffImage);
        buffImage.relocate(cellLabel.getLayoutX() + CELL_WIDTH / 2 - BuffImage.BUFF_VIEW_SIZE / 2
                , cellLabel.getLayoutY() + CELL_HEIGHT / 2 - BuffImage.BUFF_VIEW_SIZE / 2);
    }
}