
public abstract class Game {

    public abstract String playGame();

    public String result(String gameOutcome, String sideBettedOn){
        String result;
        if(gameOutcome.equalsIgnoreCase(sideBettedOn)){
            result = "Win";
        } else {
            result = "Loss";
        }
        return result;
    }

    public abstract int payout(String result, int bet);

}
