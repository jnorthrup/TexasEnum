package poker.strat;

import poker.player.*;

/**
 * User: jim
 * Date: Sep 9, 2007
 * Time: 12:15:44 AM
 */
public enum TurnAct {

    fold {
        public boolean act2(Dealer dealer, Player player) {
            player.out = true;
            return true; //always succeeds
        }},
    check {
        public boolean act2(Dealer dealer, Player player) {

            player.bet(dealer, Math.min(dealer.blind, player.stack));

            dealer.isCalled = true;
            dealer.lastCalled = dealer.marker;
            return true;

        }},
    raise {
        public boolean act2(Dealer dealer, Player player) {
            if (!(player.stack <= 0)) return false;
            dealer.isCalled = false;
            player.bet(dealer, dealer.blind + player.strategy.getRaiseAmount(dealer, player));
            return true;
        }
    };

    public abstract boolean act2(Dealer dealer, Player player);

    public boolean act(Dealer dealer, Player player) {
        player.act = this;
//        if (Dealer.test) Logger.getAnonymousLogger().info("attempting " + this + " for " + player);
        return act2(dealer, player);

    }
}
