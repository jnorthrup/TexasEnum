package poker.player;

import static poker.eval.CardUtil.*;
import poker.eval.*;

import java.util.*;
import java.util.logging.*;

public abstract class Seat extends CardMemory {

    public IntArrayCircularBuffer cards = EMPTYCARDS;
    public static boolean test = false;

    static {

        if (test) Logger.getAnonymousLogger().info("Seat.TEST is TRUE, this is probably not what you want!");

    }

    private String name;
    private final UUID uuid;
    protected Play play = null;

    public Seat(String... names) {
        super();
        uuid = UUID.randomUUID();
        if (names.length > 0)
            name = (names[0]);

    }

    public Seat(UUID uuid) {
        this.uuid = uuid;

    }

    public String getName() {

        if (name == null || name.isEmpty())
            return uuid.toString();

        return name;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getPlay());
        builder.append(':');
        builder.append(toChar(cards.rewind().mark()));
        builder.append("[");
        builder.append("name='");
        builder.append(getName());
        builder.append(']');
        return builder.toString();
    }

    public void addCard(int card) {
        cards = poker.eval.CardUtil.addSorted(card, cards);
        play = null;
    }

    abstract public Play getPlay();

}
