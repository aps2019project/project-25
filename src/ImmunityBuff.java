public class ImmunityBuff extends Buff {
    private String immunity;

    ImmunityBuff(int durationTurn, boolean isDispellable
            , boolean isContinuous, String immunity) {
        super(durationTurn, isDispellable, isContinuous);
        setPositiveOrNegative(Constants.POSITIVE);
        this.immunity = immunity;
    }

    @Override
    public void doEffect() {
        //todo looks gonna be empty
    }

    @Override
    public void doEndingEffect() {
        //todo looks gonna be empty
    }

    @Override
    public ImmunityBuff clone() {
        return new ImmunityBuff(getDurationTurn(), isDispellable(), isContinuous(), immunity);
    }

    public String getImmunity() {
        return immunity;
    }
}
