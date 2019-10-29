package poker.player;

import poker.eval.*;

import java.util.*;

/**
 * User: jim
 * Date: Sep 7, 2007
 * Time: 2:56:36 PM
 */
public class Dealer extends Seat {

    public final Deque<Player> players;
    public final Deck deck = new Deck();
    public int marker;
    public Player button;
    public double lowerLimit;
    public double pot;
    public final double ante;
    public int smallBlind, bigBlind;
    public boolean canCheck;
    public boolean canCall;
    public double blind;
    public boolean isCalled;
    public int bettingRound = 0;
    public int lastCalled;
    public Iterator<Player> leadin;
    public final double till = .5f;
    public double bet;

    public Dealer() {
        super();
        ante = 1;
        players = new LinkedList<Player>();
    }

    static PriorityQueue<Player> masterTablesHistories = new PriorityQueue<Player>(10);


    public Play getPlay() {
        return null;
    }

    public String toString() {
        String s = super.toString();
        s += String.valueOf(players);
        return s;
    }
}
