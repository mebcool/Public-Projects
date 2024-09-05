public class FlipCoinGame extends Game{

    private int upperBound;
    private int lowerBound;
    private int range;

    public FlipCoinGame(){
        upperBound = 2;
        lowerBound = 1;
        range = (upperBound - lowerBound) + 1;
    }
    @Override
    public String playGame() {
        String gameOuctome;
        int sideLandedOn = (int)(Math.random() * range) + lowerBound;

        if(sideLandedOn == 1){
            gameOuctome = "Heads";
        } else {
            gameOuctome = "Tails";
        }
        return gameOuctome;
    }

    @Override
    public int payout(String result, int bet) {
        int payout = 0;
        if(result.equalsIgnoreCase("Win")){
            payout = bet * 2;
        }else {
            payout = bet * -1;
        }
        return payout;
    }
}