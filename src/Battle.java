import java.util.ArrayList;
import java.util.List;

public class Battle {
    public DataBase database = DataBase.getInstance();
    private Player player1;
    private Player player2;
    private BattleGround battleGround = new BattleGround();
    private Player playerInTurn;
    private String mode;
    private int turnNumber = 1;
    private boolean isBattleFinished = false;
    private int numberOfFlags;

    public Battle(Account firstPlayerAccount, Account secondPlayerAccount, String mode, int numberOfFlags) {
        player1 = new Player(firstPlayerAccount.getPlayerInfo(), firstPlayerAccount.getMainDeck());
        player2 = new Player(secondPlayerAccount.getPlayerInfo(), secondPlayerAccount.getMainDeck());
        playerInTurn = player1;
        this.mode = mode;
        this.setNumberOfFlags(numberOfFlags);
        List<Flag> temp = new ArrayList<>();
        for (int i = 0; i < numberOfFlags; i++) {
            temp.add(new Flag());
        }
        this.battleGround.addFlagsToBattleGround(temp);
    }

    public OutputMessageType nextTurn() {
        if (isBattleFinished)
            return OutputMessageType.BATTLE_FINISHED;
        reviveContinuousBuffs();
        removeExpiredBuffs();
        resetUnitsMoveAndAttack();
        doBuffsEffects();
        checkForDeadUnits();
        //todo check turns of flag in hand??
        changeTurn();
        turnNumber++;
        setManaBasedOnTurnNumber();
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public BattleGround getBattleGround() {
        return battleGround;
    }

    public String getMode() {
        return mode;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void addTurnNumber() {
        this.turnNumber++;
    }

    public void changeTurn() {
        this.addTurnNumber();
        if (playerInTurn == player1)
            playerInTurn = player2;
        else playerInTurn = player1;
    }

    public boolean isBattleFinished() {
        return isBattleFinished;
    }

    public void setBattleFinished(boolean battleFinished) {
        isBattleFinished = battleFinished;
    }

    public Player getPlayerInTurn() {
        return playerInTurn;
    }

    public void killUnit(Unit unit) {
        if (unit.getId().contains(player1.getPlayerInfo().getPlayerName()))
            player1.getGraveYard().addDeadCard(unit);
        if (unit.getId().contains(player2.getPlayerInfo().getPlayerName()))
            player2.getGraveYard().addDeadCard(unit);
        for (Spell specialPower : unit.getSpecialPowers()) {
            if (specialPower.equals(SpellActivationType.ON_DEATH))
                specialPower.doSpell(battleGround.getCoordinationOfUnit(unit)[0]
                        , battleGround.getCoordinationOfUnit(unit)[1]);
        }
        this.getBattleGround().getCellOfUnit(unit).setUnit(null);
    }

    public void checkForDeadUnits() {
        for (int i = 0; i < Constants.BATTLE_GROUND_WIDTH; i++) {
            for (int j = 0; j < Constants.BATTLE_GROUND_LENGTH; j++) {
                Cell cell = battleGround.getCells()[i][j];
                if (cell.isEmptyOfUnit())
                    continue;
                if (cell.getUnit().getHp() <= 0)
                    killUnit(cell.getUnit());
            }
        }
    }

    public void setManaBasedOnTurnNumber() {
        if (turnNumber % 2 == 1) {
            if (turnNumber < 14) {
                player1.setMana((turnNumber + 1) / 2 + 1);
                player2.setMana(0);
            } else {
                player1.setMana(9);
                player2.setMana(0);
            }
        } else if (turnNumber % 2 == 0) {
            if (turnNumber < 15) {
                player1.setMana(0);
                player2.setMana((turnNumber / 2 + 2));
            } else {
                player1.setMana(0);
                player2.setMana(9);
            }
        }
    }

    public int getNumberOfFlags() {
        return numberOfFlags;
    }

    public void setNumberOfFlags(int numberOfFlags) {
        this.numberOfFlags = numberOfFlags;
    }

    public Player checkEndBattle() {
        if (mode.equals(Constants.CLASSIC)) {
            return checkEndBattleModeClassic();
        }
        if (mode.equals(Constants.ONE_FLAG)) {
            return checkEndBattleModeOneFlag();
        }
        if (mode.equals(Constants.FLAGS)) {
            return checkEndBattleModeFlags();
        }
    }

    public Player checkEndBattleModeClassic() {
        if (player1.getDeck().getHero().getHp() <= 0) {
            isBattleFinished = true;
            return player2;
        } else if (player2.getDeck().getHero().getHp() <= 0) {
            isBattleFinished = true;
            return player1;
        }
        return null;
    }

    public Player checkEndBattleModeOneFlag() {
        int numberOfFlagsPlayer1 = battleGround.getNumberOfFlagsForPlayer(player1);
        int numberOfFlagsPlayer2 = battleGround.getNumberOfFlagsForPlayer(player2);
        if (numberOfFlagsPlayer1 >= getNumberOfFlags() / 2 + 1) {
            isBattleFinished = true;
            return player1;
        } else if (numberOfFlagsPlayer2 >= getNumberOfFlags() / 2 + 1) {
            isBattleFinished = true;
            return player2;
        }
        return null;
    }

    public Player checkEndBattleModeFlags() {
        Cell cell = battleGround.getCellWithFlag();
        if (cell != null) {
            if (cell.getUnit().getFlags().get(0).getTurnsInUnitHand() >= 6) {
                isBattleFinished = true;
                if (cell.getUnit().getId().contains(player1.getPlayerInfo().getPlayerName()))
                    return player1;
                return player2;
            }
        }
        return null;
    }

    public Card getCardByCardID(String id) {
        Card card = this.getBattleGround().getCardByID(id);
        return null;
        //todo complete this method
    }

    public List<Player> getPlayersHavingBuff(Buff buff) {
        List<Player> players = new ArrayList<>();
        if (player1.getBuffs().contains(buff))
            players.add(player1);
        if (player2.getBuffs().contains(buff))
            players.add(player2);
        return players;
    }

    public void reviveContinuousBuffs() {
        int i;
        int j;
        for (i = 0; i < Constants.BATTLE_GROUND_WIDTH; i++) {
            for (j = 0; j < Constants.BATTLE_GROUND_LENGTH; j++) {
                Cell cell = database.getCurrentBattle().getBattleGround().getCells()[i][j];
                for (Buff buff : cell.getBuffs()) {
                    if (buff.isContinuous())
                        buff.revive();
                }
                for (Buff buff : cell.getUnit().getBuffs()) {
                    if (buff.isContinuous())
                        buff.revive();
                }
            }
        }
    }

    public void removeExpiredBuffs() {
        int i;
        int j;
        for (i = 0; i < Constants.BATTLE_GROUND_WIDTH; i++) {
            for (j = 0; j < Constants.BATTLE_GROUND_LENGTH; j++) {
                Cell cell = database.getCurrentBattle().getBattleGround().getCells()[i][j];
                for (Buff buff : cell.getBuffs()) {
                    if (buff.isExpired())
                        buff.remove();
                }
            }
        }
        for (Buff buff : player1.getBuffs()) {
            if (buff.isExpired())
                buff.remove();
        }
        for (Buff buff : player2.getBuffs()) {
            if (buff.isExpired())
                buff.remove();
        }
    }

    public void resetUnitsMoveAndAttack() {
        int i;
        int j;
        for (i = 0; i < Constants.BATTLE_GROUND_WIDTH; i++) {
            for (j = 0; j < Constants.BATTLE_GROUND_LENGTH; j++) {
                Unit unit = database.getCurrentBattle().getBattleGround()
                        .getCells()[i][j].getUnit();
                unit.setDidAttackThisTurn(false);
                unit.setDidMoveThisTurn(false);
            }
        }
    }

    public void doBuffsEffects() {
        int i;
        int j;
        for (i = 0; i < Constants.BATTLE_GROUND_WIDTH; i++) {
            for (j = 0; j < Constants.BATTLE_GROUND_LENGTH; j++) {
                Cell cell = database.getCurrentBattle().getBattleGround().getCells()[i][j];
                for (Buff buff : cell.getBuffs()) {
                    buff.doEffect();
                }
                for (Buff buff : cell.getUnit().getBuffs()) {
                    buff.doEffect();
                }
            }
        }
        for (Buff buff : player1.getBuffs())
            buff.doEffect();
        for (Buff buff : player2.getBuffs())
            buff.doEffect();
    }

    public OutputMessageType insert(Card card, int row, int column) {
        if (card == null)
            return OutputMessageType.NO_SUCH_CARD_IN_HAND;
        if (card instanceof Unit) {
            if (row >= Constants.BATTLE_GROUND_WIDTH || row < 0
                    || column >= Constants.BATTLE_GROUND_LENGTH || column < 0)
                return OutputMessageType.INVALID_NUMBER;
            if (database.getCurrentBattle().getBattleGround().getCells()[row][column].getUnit() == null) {
                database.getCurrentBattle().getBattleGround().getCells()[row][column].setUnit((Unit) card);
                database.getCurrentBattle().getPlayerInTurn().getHand().getCards().remove(card);
                database.getCurrentBattle().getPlayerInTurn().setNextCard(database.getCurrentBattle()
                        .getPlayerInTurn().getDeck());
                //todo is it complete
            } else return OutputMessageType.THIS_CELL_IS_FULL;
        } else if (card instanceof Spell) {
            ((Spell) card).doSpell(row, column);
            database.getCurrentBattle().getPlayerInTurn().getGraveYard().addDeadCard(card);
            database.getCurrentBattle().getPlayerInTurn().getHand().deleteCard(card);
        }
        return OutputMessageType.NO_ERROR;
    }

    public OutputMessageType useSpecialPower(Unit hero, Player player, int row, int column) {
        if (hero.getMainSpecialPower().getMana() <= player.getMana()
                && hero.getMainSpecialPower().getCoolDown() == 0
                && hero.getMainSpecialPower().getActivationType() == SpellActivationType.ON_CAST) {
            hero.getMainSpecialPower().doSpell(row, column);
        } else {
            return OutputMessageType.NO_HERO;
        }
        return OutputMessageType.SPECIAL_POWER_USED;
    }

    public List<String> getAvailableMoves() {
        List<String> output = new ArrayList<>();
        for (Unit unit : battleGround.getUnitsOfPlayer(playerInTurn)) {
            String temp = "";
            temp += unit.getId() + ":" +
                    "\n\tCan Attack: " + !unit.didAttackThisTurn()
                    + "\n\tCan Move: " + !unit.didMoveThisTurn() +
                    "\n\tAttack Options: ";
            Player player;
            if (player2 == playerInTurn) {
                player = player1;
            } else {
                player = player2;
            }
            if (!unit.didAttackThisTurn()) {
                for (Unit enemyUnit : battleGround.getUnitsOfPlayer(player)) {
                    if (unit.canAttackTarget(enemyUnit)) {
                        temp += "\n\t\t" + enemyUnit.getId();
                    }
                }
            }
            output.add(temp);
        }
        return output;
    }
}
