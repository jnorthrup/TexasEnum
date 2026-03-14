package poker.strat;

import static poker.eval.CardUtil.*;
import poker.player.*;

/**
 * Strategy based on Sklansky-style pocket card groupings.
 * Uses pre-computed hole card rankings to determine aggression level.
 *
 * The HOLE_POINTS matrix maps each pocket hand to a group (0-8),
 * where 0 is the strongest (AA, KK) and 8 is the weakest.
 * Suited hands use the upper triangle; offsuit uses the lower.
 *
 * This is the foundation for tradebot-style strategies: override
 * {@link #advise} to incorporate pot odds, position, and opponent modeling.
 */
public class PocketRankStrategy implements Strategy {

    /**
     * Sklansky-style pocket hand groupings.
     * Row/column indexed by Face ordinal (ACE=0, KING=1, ..., TWO=12).
     * Upper triangle (suited): HOLE_POINTS[hiCard][loCard]
     * Lower triangle (offsuit): HOLE_POINTS[loCard][hiCard]
     */
    protected static final int[][] HOLE_POINTS = {
            {0, 0, 1, 1, 2, 4, 4, 4, 4, 4, 4, 4, 4,}, /*A*/
            {1, 0, 1, 2, 3, 5, 6, 6, 6, 6, 6, 6, 6,}, /*K*/
            {2, 3, 0, 2, 3, 4, 6, 8, 8, 8, 8, 8, 8,}, /*Q ..etc*/
            {3, 4, 4, 0, 2, 3, 5, 7, 8, 8, 8, 8, 8,},
            {5, 5, 5, 4, 1, 3, 4, 6, 8, 8, 8, 8, 8,},
            {7, 7, 7, 6, 6, 2, 3, 4, 7, 8, 8, 8, 8,},
            {8, 8, 8, 7, 7, 6, 3, 4, 5, 7, 8, 8, 8,},
            {8, 8, 8, 8, 8, 8, 7, 4, 4, 5, 7, 8, 8,},
            {8, 8, 8, 8, 8, 8, 8, 7, 5, 4, 6, 8, 8,},
            {8, 8, 8, 8, 8, 8, 8, 8, 7, 5, 5, 6, 8,},
            {8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 6, 6, 7,},
            {8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 6, 7,},
            {8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 6,},
    };

    /** Threshold group: hands at or below this group will raise. Default: group 3. */
    protected int raiseThreshold = 3;

    /** Threshold group: hands above this will fold. Default: group 6. */
    protected int foldThreshold = 6;

    @Override
    public TurnAct advise(Dealer dealer, Player player) {
        if (player.pocket.limit() < 2) return TurnAct.check;

        final int c1 = player.pocket.get(0);
        final int c2 = player.pocket.get(1);
        final int rank = c1 > c2 ? pocketRank(c1, c2) : pocketRank(c2, c1);

        if (rank <= raiseThreshold) return TurnAct.raise;
        if (rank > foldThreshold) return TurnAct.fold;
        return TurnAct.check;
    }

    @Override
    public double getRaiseAmount(Dealer dealer, Player player) {
        final int c1 = player.pocket.get(0);
        final int c2 = player.pocket.get(1);
        final double rate = ((c1 > c2 ? pocketRank(c1, c2) : pocketRank(c2, c1))) / 8.0;
        final double v = fuse(player, handspan(dealer));
        final int s = dealer.players.size();
        return Math.min(dealer.bet, Math.max(s * v * rate * player.stack, player.stack));
    }

    /**
     * Assign a Sklansky group to the pocket cards.
     *
     * @param hiCard the higher-valued card
     * @param loCard the lower-valued card
     * @return group number (0=best, 8=worst)
     */
    public static int pocketRank(int hiCard, int loCard) {
        return suit(hiCard) == suit(loCard) ?
                HOLE_POINTS[face(hiCard)][face(loCard)] :
                HOLE_POINTS[face(loCard)][face(hiCard)];
    }

    /** Calculate the cost of a hand for pot-odds comparison. */
    public static double handspan(Dealer dealer) {
        return (dealer.blind * 1.5 + (Math.max(1, dealer.players.size() - 2)) * dealer.ante);
    }

    /** Stack-to-handspan ratio; higher = more aggressive play is justified. */
    public static double fuse(Player player, double handspan) {
        return player.stack / handspan;
    }

    // ---- Combinatorial utilities for equity calculations ----

    private static final double[] logFac = new double[100];

    static {
        double logRes = 0.0;
        for (int i = 1, stop = logFac.length; i < stop; i++)
            logFac[i] = logRes += Math.log(i);
    }

    public static double logBinom(int n, int k) {
        return logFac[n] - logFac[n - k] - logFac[k];
    }

    public static double binomCoeff(int n, int k) {
        return Math.exp(logBinom(n, k));
    }

    /**
     * Showdown equity table: probability distribution of hand rankings
     * for 2-player, 6-player, and 10-player tables.
     * Index 0=RSF, 1=SF, 2=4K, 3=FH, 4=FL, 5=ST, 6=3K, 7=2P, 8=P, 9=HC
     */
    protected static final double[][] SHOWDOWN_EQUITY = {
            {0.00000154, 0.00001385, 0.00024010, 0.00144058, 0.00196540,
             0.00392465, 0.02112845, 0.04753902, 0.42256903, 0.501177394},
            {0.000009,   0.000081,   0.000720,   0.008153,   0.010108,
             0.017763,   0.035963,   0.124411,   0.477969,   0.324822},
            {0.00003232, 0.00027851, 0.00168067, 0.02596102, 0.03025494,
             0.04619382, 0.04829870, 0.23495536, 0.43822546, 0.17411920},
    };
}
