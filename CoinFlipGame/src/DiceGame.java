
public class DiceGame extends Game{
    private int upperBound;
    private int lowerBound;
    private int range;

    public DiceGame(){
        upperBound = 6;
        lowerBound = 1;
        range = (upperBound - lowerBound) + 1;
    }
    @Override
    public String playGame() {
        String gameOutcome;
        int sideLandedOn = (int)(Math.random() * range) + lowerBound;

        gameOutcome = Integer.toString(sideLandedOn);

        return gameOutcome;
    }

    @Override
    public int payout(String result, int bet) {
        int payout = 0;
        if (result.equalsIgnoreCase("Win")){
            payout = bet * 6;
        } else {
            payout = bet * -1;
        }

        return payout;
    }
}
