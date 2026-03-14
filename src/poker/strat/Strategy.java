package poker.strat;

import poker.player.*;

/**
 * Pluggable strategy interface for poker bots.
 *
 * Implement this interface to create custom tradebot strategies.
 * The engine calls {@link #advise} each betting round to get the player's action,
 * and {@link #getRaiseAmount} when the action is a raise.
 *
 * User: jim
 * Date: Sep 9, 2007
 * Time: 12:26:41 AM
 */
public interface Strategy {

    /**
     * Decide what action to take given the current game state.
     *
     * @param dealer the dealer/table state (pot, blinds, community cards, player list)
     * @param player the player making the decision (pocket cards, stack, position)
     * @return the action to take: fold, check, or raise
     */
    TurnAct advise(Dealer dealer, Player player);

    /**
     * Determine the raise amount when advise() returns raise.
     *
     * @param dealer the dealer/table state
     * @param player the player raising
     * @return the amount to raise
     */
    double getRaiseAmount(Dealer dealer, Player player);
}
