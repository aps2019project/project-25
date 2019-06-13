public class ControllerBattleMenu {
    private static ControllerBattleMenu ourInstance;
    private final Request request = Request.getInstance();
    private final View view = View.getInstance();
    private static final DataBase dataBase = DataBase.getInstance();

    public static ControllerBattleMenu getInstance() {
        return ourInstance;
    }

    public ControllerBattleMenu() {
        ourInstance = this;
    }

    public void main() throws GoToMainMenuException {
        boolean didExit = false;
        while (!didExit) {
            request.getNewCommand();
            switch (request.getType()) {
                case EXIT:
                    didExit = true;
                    break;
                case ENTER:
                    enter();
                    break;
                case HELP:
                    help();
                    break;
                case SHOW_USERS:
                    view.showUsers(dataBase.getAccounts(), dataBase.getLoggedInAccount().getUsername());
                    break;
                default:
                    view.printOutputMessage(OutputMessageType.WRONG_COMMAND);
            }
        }
    }

    private void help() {
        view.printHelp(HelpType.CONTROLLER_BATTLE_MENU_HELP);
    }

    private void enter() throws GoToMainMenuException {
        if (request.getCommand().equals("enter singleplayer")) {
            ControllerSinglePlayerMenu.getInstance().main();
        } else if (request.getCommand().equals("enter multiplayer")) {
            ControllerMultiPlayerMenu.getInstance().main();
        } else {
            request.setOutputMessageType(OutputMessageType.WRONG_COMMAND);
            view.printOutputMessage(request.getOutputMessageType());
        }
    }
}
