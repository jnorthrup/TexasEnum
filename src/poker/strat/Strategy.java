package poker.strat;

import static poker.eval.CardUtil.*;
import poker.player.*;

/**
 * User: jim
 * Date: Sep 9, 2007
 * Time: 12:26:41 AM
 */
public class Strategy {
    private static final int[][] HOLE_POINTS = new int[][]{
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

    public TurnAct advise(Dealer dealer, Player player) {
        return TurnAct.check;
    }

    public double getRaiseAmount(Dealer dealer, Player player) {
        final int c1 = player.pocket.get();
        final int c2 = player.pocket.get();
        final double rate = ((c1 > c2 ? pocketRank(c1, c2) : pocketRank(c2, c1))) / 8F;
        final double v = fuse(player, handspan(dealer));
        final int s = dealer.players.size();
        return Math.min(dealer.bet, Math.max(s * v * rate * player.stack, player.stack));

    }

    /**
     * assign a group strategy to the pocket cards
     *
     * @param hiCard
     * @param loCard
     * @return group for play
     */
    static public int pocketRank(int hiCard, int loCard) {
        return suit(hiCard) == suit(loCard) ?
                HOLE_POINTS[face(hiCard)][face(loCard)] :
                HOLE_POINTS[face(loCard)][face(hiCard)];
    }

    //only needs calc once per round
    static public double handspan(Dealer dealer) {
        return (dealer.blind * 1.5 + (Math.max(1, dealer.players.size() - 2)) * dealer.ante);
    }

    //longer fuse=more aggressive
    static public double fuse(Player player, double handspan) {
        return player.stack / handspan;
    }

    /**
     * What is the probability that none of some number of random hands
     * will be better than yours?
     * <p/>
     * It is the chance that one random hand will not be better than yours
     * multiplied by itself N-1 times, which is the same as saying it's
     * that probability raised to the Nth power.
     * <p/>
     * For example, if there's a 40% chance that a random hand won't be
     * better (i.e., a 60% chance it will be better), then the chance that
     * none of three random hands will be better is 40% x 40% x 40%, or 0.4
     * to the 3rd power, which equals 0.064. Hence, the chance that at least
     * one of the three hands will be better is 1.0 - 0.064 or 0.936 or 94%.
     */
    void showdownEquity() {

    }

    void handEquity() {

    }

    private final static double[] logFac = new double[100];

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

    {


        double[][] se = new double[][]{{
                0.00000154,
                0.00001385,
                0.00024010,
                0.00144058,
                0.00196540,
                0.00392465,
                0.02112845,
                0.04753902,
                0.42256903,
                0.501177394,
        },
                {
                        0.000009,
                        0.000081,
                        0.000720,
                        0.008153,
                        0.010108,
                        0.017763,
                        0.035963,
                        0.124411,
                        0.477969,
                        0.324822,
                },
                {
                        0.00003232,
                        0.00027851,
                        0.00168067,
                        0.02596102,
                        0.03025494,
                        0.04619382,
                        0.04829870,
                        0.23495536,
                        0.43822546,
                        0.17411920,
                },
        };
    }
}
