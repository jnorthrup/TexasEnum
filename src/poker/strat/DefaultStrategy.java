package poker.strat;

import poker.player.*;

/**
 * Passive strategy that always checks. This is the baseline strategy
 * used by the engine when no custom strategy is provided.
 */
public class DefaultStrategy implements Strategy {

    @Override
    public TurnAct advise(Dealer dealer, Player player) {
        return TurnAct.check;
    }

    @Override
    public double getRaiseAmount(Dealer dealer, Player player) {
        return 0;
    }
}
