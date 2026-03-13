package poker.player;

import static poker.eval.CardUtil.*;
import poker.eval.*;
import poker.strat.*;

/**
 * User: jim
 * Date: Aug 22, 2007
 * Time: 3:31:53 AM
 */

public class Player extends Seat implements Comparable<Player> {

    public TurnAct act;
    int lastPlayed;
    public float stack;
    public int wins;
    public Strategy strategy;
    public boolean out;

    public int[] pocket = EMPTYCARDS;
    private int[] shot;


    public Player() {
        super();
        init();
    }

    public Player(String... names) {
        super(names);
        init();
    }

    protected void init() {
        stack = 500;
        strategy = new DefaultStrategy();
        cards = EMPTYCARDS;
        pocket = EMPTYCARDS;
        this.shot = EMPTYCARDS;
    }

    public Player(int[]... hands) {
        init();
        if (hands.length == 1) cards = hands[0];
        else
            cards = mergeSortHands(hands);
    }


    public void bet(Dealer dealer, double amount) {
        final double v = Math.min(amount, this.stack);
        dealer.pot += v;
        stack -= v;
    }

    public TurnAct act(Dealer dealer) {
        final TurnAct turnAct = this.strategy.advise(dealer, this);
        turnAct.act(dealer, Player.this);
        return turnAct;
    }

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append('[');
        builder.append("out=");
        builder.append(out);
        builder.append(", wins=");
        builder.append(wins);
        builder.append(", stack=");
        builder.append(stack);
        builder.append(", act=");
        builder.append(act);
        builder.append(", pocket=");
        builder.append(toChar(pocket));
        builder.append(", shot=");
        builder.append(toChar(shot));
        builder.append(']');
        return builder.toString();
    }

    public Player refresh() {
        cards = EMPTYCARDS;
        super.init();
        out = false;
        return this;
    }


    public int compareTo(final Player other) {
        int eq = getPlay().ordinal() - other.getPlay().ordinal();
        if (eq == 0)
            eq = compareHighCard(shot, other.shot);
        if (eq == 0)
            eq = compareHighCard(mergeSortHands(pocket), mergeSortHands(other.pocket));
        return eq;
    }

    public Play getPlay() {

        if (play == null || cards.length != lastCount) {
            Pair<Play, int[]> pair = Play.assess(cards, this);
            play = pair.getFirst();
            this.shot = pair.getSecond();
            lastCount = cards.length;
        }
        return play;
    }

}
