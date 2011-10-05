package poker.eval;
/**
 * User: jim
 * Date: Oct 4, 2007
 * Time: 12:22:00 AM
 */

import junit.framework.*;
import static poker.eval.CardUtil.*;
import static poker.eval.Face.*;
import static poker.eval.Play.*;
import static poker.eval.Suit.*;
import poker.player.*;

import java.nio.*;

public class PlayTest extends TestCase {
    IntBuffer cards =  (IntBuffer) IntBuffer.wrap(
                    new int[]{
                            (card(KING, DIAMONDS)),
                            (card(KING, SPADES)),
                            (card(QUEEN, SPADES)),
                            (card(JACK, SPADES)),
                            (card(EIGHT, DIAMONDS))}).rewind().mark();

    public void testAssess() throws Exception {
        final Pair<Play, IntBuffer> intBufferPair = Play.assess((IntBuffer) cards.reset());
        final Play play = intBufferPair.getFirst();
        assertEquals(PAIR, play);
        final IntBuffer buffer = intBufferPair.getSecond();
        buffer.rewind().mark();
        final int i = buffer.get(0);
        assertEquals(KING, face((Integer) i));
        assertEquals(2, buffer.limit());
    }
 
}