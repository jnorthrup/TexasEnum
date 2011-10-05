package poker.game;


import poker.eval.*;
import static poker.eval.CardUtil.*;
import poker.player.*;
import poker.strat.*;

import java.nio.*;
import java.util.*;
import java.util.logging.*;

/**
 * User: jim
 * Date: Sep 7, 2007
 * Time: 6:53:33 PM
 */
public enum GameState {

    /**
     * The dealer shuffles up a standard xcards of 52 playing cards.
     */
    shuffle {
        public void update(Dealer dealer) {
            dealer.deck.clear();
            if (!test)
                dealer.deck.shuffle();
            dealer.bettingRound = 0;
            dealer.blind = dealer.bet = 5;

            if (Dealer.test) {
                final char[] chars = CardUtil.toChar((IntBuffer) dealer.deck.getCards().mark());
                StringBuilder foo = new StringBuilder();
                foo.append(chars);
                if (Seat.test)
                    Logger.getAnonymousLogger().info(this + " " + foo.toString());
            }
            for (Player player : dealer.players)
                player.refresh().stack -= Math.min(player.stack, dealer.till);

            dealer.button = dealer.players.getFirst();
            dealer.leadin = dealer.players.iterator();
        }},
    /**
     * Each player is dealt two cards face down. These are called
     * your hole or pocket cards.
     */
    deal {
        public void update(Dealer dealer) {
            for (Player player : dealer.players)
                if (!player.out) {
                    player.pocket =
                            (IntBuffer) IntBuffer.wrap(
                                    new int[]{
                                            (dealer.deck.deal()),
                                            (dealer.deck.deal()),
                                    }).mark();
                    player.cards = (IntBuffer) mergeSortHands(player.pocket).rewind().mark();
                }

            dump(dealer);
        }},
    /**
     * seated players are billed a ante.
     */rake {
        public void update(Dealer dealer) {
            for (Player player : dealer.players)
                if (player != null)
                    player.stack -= dealer.ante;
        }},
    /**
     * Small Blind starts the button moving, half price?
     */
    small_blind {
        public void update(Dealer dealer) {
            dealer.leadin.next().bet(dealer, (float) (dealer.lowerLimit / 2.0));
        }},
    /**
     * big blind bets full price
     */
    big_blind {
        public void update(Dealer dealer) {
            dealer.leadin.next().bet(dealer, dealer.lowerLimit);
        }
    },
    /**
     * Then there is a round of betting starting with the guy to the
     * left of the two who posted the blinds. This round is usually
     * referred to by the term pre-flop.
     */
    preflop {
        public void update(Dealer dealer) {
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},
    /**
     * Much like most games of poker, players can call, raise, or fold.
     */
    round1 {
        public void update(Dealer dealer) {
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},
    /**
     * After the betting round ends, the dealer discards the top card
     * of the xcards. This is called a burn card. This is done to
     * prevent cheating.
     */
    burn1 {
        public void update(Dealer dealer) {
            burn(dealer);
        }},
    /**
     * The dealer then flips the next three cards face up on the table.
     * These cards are called the pocket. These are communal cards that
     * anyone can use in combination with their two pocket cards to
     * form a poker hand.
     */
    flop {
        public void update(Dealer dealer) {

            final IntBuffer flop = dealer.cards = (IntBuffer) IntBuffer.wrap(
                    new int[]{
                            (dealer.deck.deal()),
                            (dealer.deck.deal()),
                            (dealer.deck.deal()),
                            -1, -1
                    }).mark().limit(3);
            for (Player player : dealer.players) {
                if (!player.out) {
                    flop.reset();
                    player.cards = mergeSortHands(player.cards, flop);
                }
            }
            dump(dealer);
        }}, /**
 * There is another round of betting starting with the player to the left of the dealer.
 */
round2 {
    public void update(Dealer dealer) {
        bettingRound(dealer);
        dealer.leadin = dealer.players.iterator();
    }},
    /**
     * After the betting concludes, the dealer burns another card
     */
    burn2 {
        public void update(Dealer dealer) {
            burn(dealer);
        }},
    /**
     * flips one more onto the table. This is called the turn card. Players can use this sixth card now to form a five
     * card poker hand.
     */
    turn {

        public void update(Dealer dealer) {
            int turn = dealer.deck.deal();

            dealer.cards.limit(4);
            dealer.cards.put(3, turn);

            for (Player player : dealer.players)
                if (!player.out)
                    player.cards = addSorted(turn, player.cards);
            dump(dealer);
        }},
    /**
     * The player to the left of the dealer begins another round of betting. In many types of games, this is where the
     * bet size doubles.
     */
    bet_doubles {

        public void update(Dealer dealer) {
            dealer.bet *= 2.0;
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},

    /**
     * The player to the left of the dealer begins another round of betting. In many types of games, this is where the
     * bet size doubles.
     */
    round3 {
        public void update(Dealer dealer) {
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},
    /**
     * Finally, the dealer burns a card
     */
    burn3 {
        public void update(Dealer dealer) {
            burn(dealer);
        }},
    /**
     * dealer places a final card face up on the table. This is called the river. Players can now use any of the five cards on the table or the two cards in their pocket to form a five card poker hand.
     */
    river {
        public void update(Dealer dealer) {
            int river = dealer.deck.deal();

            dealer.cards.limit(5);
            dealer.cards.put(4, river);

            for (Player player : dealer.players)
                if (!player.out)
                    player.cards = addSorted(river, player.cards);
            dump(dealer);
        }},

    /**
     * There is one final round of betting starting with the player to the left of the dealer.
     */
    final_bets {
        public void update(Dealer dealer) {
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},
    /**
     * After that, all of the players remaining in the game begin to reveal their hands. This begins with the player to
     * the left of the last player to call. It's called the showdown. Players use a combination of their pocket cards
     * and the community cards to form a poker hand. For more about that, go to our forming a five card hand page.
     */
    showdown {
        public void update(Dealer dealer) {
            dealer.marker = dealer.lastCalled;
            bettingRound(dealer);
        }
    },
    /**
     * The player who shows the best hand wins! There are cases where players with equal hands share the winnings.
     * stack is updated from the pot.
     */
    collect {

        public void update(Dealer dealer) {

            ArrayList<Player> top = new ArrayList<Player>();

            for (Player player : dealer.players) {
                if (player.out) continue;
                int eq;
                if (top.isEmpty()) {
                    top.add(player);
                    continue;
                } else {
                    Player player1 = top.get(0);
                    eq = player.compareTo(player1);
                }
                if (eq == 0) {
                    top.add(player);
                } else {
                    if (eq < 0)
                        top = new ArrayList<Player>(Collections.singletonList(player));
                }
            }
            final int split = top.size();
            final double reward = dealer.pot / split;
            for (Player player : top) {
                player.stack += reward;
                player.wins++;
                if (test) Logger.getAnonymousLogger().info(this + " winner: " + player);
            }


        }},
    moveButton {
        public void update(Dealer dealer) {
            final Player player = dealer.players.removeLast();
            dealer.players.addFirst(player);
        }};


    public static boolean test = true;


    private static void burn(Dealer dealer) {
        dealer.deck.burn();
    }

    protected void bettingRound(Dealer dealer) {

        Iterator<Player> leadin = dealer.leadin;
        while (leadin.hasNext()) {
            Player player = leadin.next();
            if (!player.out) {
                TurnAct act = player.strategy.advise(dealer, player);
                boolean b2 = act.act(dealer, player);
                while (!b2) {
                    act = TurnAct.values()[act.ordinal() - 1];
                    b2 = act.act(dealer, player);
                }
            } else if (test)
                Logger.getAnonymousLogger().info(this + " out: " + player);

        }
    }


    public abstract void update(Dealer dealer);

    public static void start(Dealer dealer) {
        for (GameState gs : values())
            gs.update(dealer);
    }

    void dump(Dealer dealer) {
        if (test)
            Logger.getAnonymousLogger().info(this + " **** " + dealer.toString());
    }
}