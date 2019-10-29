package poker.eval;

import java.util.*;

/**
 * Created by James Northrup
 * User: jim
 * Date: Aug 11, 2007
 * Time: 2:38:48 AM
 */
public enum Face {

    ACE('A'),
    KING('K'),
    QUEEN('Q'),
    JACK('J'),
    TEN('T'),
    NINE('9'),
    EIGHT('8'),
    SEVEN('7'),
    SIX('6'),
    FIVE('5'),
    FOUR('4'),
    THREE('3'),
    TWO('2');
    final char desc;

    private Face(final char c) {
        desc = c;
    }

    public static void listSuits() {
        System.out.println(Arrays.toString(values()));
    }
}
