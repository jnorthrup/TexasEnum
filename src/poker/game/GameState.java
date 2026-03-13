package poker.game;


import poker.eval.*;
import static poker.eval.CardUtil.*;
import poker.player.*;
import poker.strat.*;

import java.util.*;
import java.util.logging.*;

public enum GameState {

    shuffle {
        public void update(Dealer dealer) {
            dealer.deck.clear();
            if (!test)
                dealer.deck.shuffle();
            dealer.bettingRound = 0;
            dealer.blind = dealer.bet = 5;

            if (Dealer.test) {
                final char[] chars = CardUtil.toChar(dealer.deck.getCards().mark());
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
    deal {
        public void update(Dealer dealer) {
            for (Player player : dealer.players)
                if (!player.out) {
                    IntArrayCircularBuffer pocket = IntArrayCircularBuffer.allocate(2);
                    pocket.put(dealer.deck.deal());
                    pocket.put(dealer.deck.deal());
                    player.pocket = pocket.mark();
                    player.cards = mergeSortHands(player.pocket).rewind().mark();
                }

            dump(dealer);
        }},
    rake {
        public void update(Dealer dealer) {
            for (Player player : dealer.players)
                if (player != null)
                    player.stack -= dealer.ante;
        }},
    small_blind {
        public void update(Dealer dealer) {
            dealer.leadin.next().bet(dealer, (float) (dealer.lowerLimit / 2.0));
        }},
    big_blind {
        public void update(Dealer dealer) {
            dealer.leadin.next().bet(dealer, dealer.lowerLimit);
        }
    },
    preflop {
        public void update(Dealer dealer) {
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},
    round1 {
        public void update(Dealer dealer) {
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},
    burn1 {
        public void update(Dealer dealer) {
            burn(dealer);
        }},
    flop {
        public void update(Dealer dealer) {

            IntArrayCircularBuffer flop = dealer.cards = IntArrayCircularBuffer.allocate(5);
            flop.put(dealer.deck.deal());
            flop.put(dealer.deck.deal());
            flop.put(dealer.deck.deal());
            flop.put(-1);
            flop.put(-1);
            flop.flip().mark();
            flop.limit(3);
            for (Player player : dealer.players) {
                if (!player.out) {
                    flop.reset();
                    player.cards = mergeSortHands(player.cards, flop);
                }
            }
            dump(dealer);
        }},
round2 {
    public void update(Dealer dealer) {
        bettingRound(dealer);
        dealer.leadin = dealer.players.iterator();
    }},
    burn2 {
        public void update(Dealer dealer) {
            burn(dealer);
        }},
    turn {

        public void update(Dealer dealer) {
            int turn = dealer.deck.deal();

            dealer.cards.limit(4);
            dealer.cards.putAt(3, turn);

            for (Player player : dealer.players)
                if (!player.out)
                    player.cards = addSorted(turn, player.cards);
            dump(dealer);
        }},
    bet_doubles {

        public void update(Dealer dealer) {
            dealer.bet *= 2.0;
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},

    round3 {
        public void update(Dealer dealer) {
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},
    burn3 {
        public void update(Dealer dealer) {
            burn(dealer);
        }},
    river {
        public void update(Dealer dealer) {
            int river = dealer.deck.deal();

            dealer.cards.limit(5);
            dealer.cards.putAt(4, river);

            for (Player player : dealer.players)
                if (!player.out)
                    player.cards = addSorted(river, player.cards);
            dump(dealer);
        }},

    final_bets {
        public void update(Dealer dealer) {
            bettingRound(dealer);
            dealer.leadin = dealer.players.iterator();
        }},
    showdown {
        public void update(Dealer dealer) {
            dealer.marker = dealer.lastCalled;
            bettingRound(dealer);
        }
    },
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
