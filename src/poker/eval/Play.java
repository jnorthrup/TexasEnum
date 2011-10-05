package poker.eval;

import static poker.eval.CardUtil.*;
import static poker.eval.Face.*;
import static poker.eval.BuffUtil.*;
import poker.player.*;

import java.nio.*;
import static java.lang.Math.min;

/**
 * Created by James Northrup
 * User: jim
 * Date: Aug 11, 2007
 * Time: 2:51:38 AM
 */
@SuppressWarnings({"ConstantConditions"})

public enum Play {
    ROYALSTRAIGHTFLUSH(5) {
        IntBuffer recognize2(IntBuffer hand, CardMemory memory) {
            if (memory.ranks < 5 && !memory.ace1st) return null;
            final IntBuffer res = STRAIGHTFLUSH.recognize2(hand, memory);
            if (res != null && suit(res.get(0)) == ACE.ordinal())
                return res;
            return null;
        }
    },

    STRAIGHTFLUSH(5) {


        IntBuffer recognize2(IntBuffer hand, CardMemory memory) {

            if (memory.straightflush != BuffUtil.EMPTY_SET) {
                if (memory.straightflush != null) return memory.straightflush;
            } else {
                return null;
            }
            if (memory.ranks < 5) return null;
            IntBuffer res = null;
            IntBuffer fbuf = memory.flush[memory.flushidx];
            final IntBuffer sbuf = memory.straight;
            if (sbuf.limit() < 5 || fbuf.limit() < 5) return null;

            {
                final int card = sbuf.get(sbuf.limit() - 1);
                final int i = face(card);
                if (memory.ace1st && i == ACE.ordinal()) {

                    IntBuffer t = BuffUtil.allocate(fbuf.limit());
                    fbuf.reset();
                    final int first = fbuf.get();
                    t.put((fbuf));
                    t.put(first);
                    fbuf = (IntBuffer) t.reset();
                }
            }

            fbuf.reset();
            sbuf.reset();
            int hit =
                    0;

            while (fbuf.hasRemaining() && sbuf.hasRemaining()) {
                int f = fbuf.get();
                int s = sbuf.get();
                final int ff = face(f);
                final int sf = face(s);

                if (ff == sf) {
                    hit++;
                    if (hit == 5) {
                        res = (IntBuffer) ((IntBuffer) fbuf.reset()).slice().mark().limit(5);
                        break;
                    }
                } else {
                    hit = 0;
                    if (fbuf.remaining() < 5 || sbuf.remaining() < 5)
                        break;
                    if (ff > sf)
                        s = sbuf.get();
                    else
                        f = ((IntBuffer) fbuf.mark()).get();
                }
            }
            memory.straightflush = res == null ? BuffUtil.EMPTY_SET : res;
            return res;
        }
    },

    FOUROFAKIND(4) {
        public int compareHand
                (IntBuffer
                        cards, IntBuffer
                        cards1) {
            return compareDefault(cards, cards1);
        }

        IntBuffer recognize2
                (IntBuffer
                        hand, CardMemory
                        memory) {
            final int len = hand.limit();
            int alen = len - 4;
            if (memory.ranks <= 1 + alen) {
                for (IntBuffer run : memory.runs) {
                    if (run.limit() == 4) return run;
                }
            }
            return null;
        }
    },

    FULLHOUSE(5) {
        public int compareHand(IntBuffer cards, IntBuffer cards1) {
            return HoldemRules.compareBoat(cards, cards1);
        }

        IntBuffer recognize2
                (IntBuffer
                        hand, CardMemory
                        memory) {
            boolean b2 = false, b3 = false;
            IntBuffer swap = allocate(minimum);
            final int len = hand.limit();
            final int alen = len - 5;
            if (memory.ranks <= alen + 2) {
                for (IntBuffer run : memory.runs) {
                    if ((!b3) && run.limit() == 3) {
                        swap.position(0);
                        swap.put(run);
                        b3 = true;
                    } else if ((!b2) && run.limit() != 4) {
                        swap.position(3);
                        swap.put((IntBuffer) run.limit(2));
                        b2 = true;
                    }
                    if (b2 & b3)
                        return (IntBuffer) swap.reset();
                }
            }
            return null;
        }
    },
    /**
     * flush parsing returns as many flush cards as have been found in order to facilitiate better straightflush recogniztion from the outcomes of flush
     */
    FLUSH(5) {
        IntBuffer recognize2(IntBuffer hand, CardMemory memory) {
            final IntBuffer flush = memory.flush[memory.flushidx];
            return (IntBuffer) (flush.limit() > 4 ? flush.reset() : null);
        }
    },


    STRAIGHT(5) {
        IntBuffer recognize2(IntBuffer hand, CardMemory memory) {
            final IntBuffer buffer = memory.straight;
            final int i = buffer.limit();
            return (IntBuffer) (i > 4 ? buffer.reset() : null);
        }
    },

    THREEOFAKIND(3) {
        IntBuffer recognize2
                (IntBuffer
                        hand, CardMemory
                        memory) {
            int alen = hand.limit() - minimum;
            if (memory.ranks > 1 + alen) return null;
            for (IntBuffer run : memory.runs)
                if (run.limit() == 3) return run;
            return null;
        }
    },

    TWOPAIR(4) {
        IntBuffer recognize2
                (IntBuffer
                        hand, CardMemory
                        memory) {
            boolean b1 = false;
            IntBuffer swap = allocate(4);


            if (memory.runs.size() > 1) {
                swap.put(memory.runs.get(0));
                swap.put(memory.runs.get(1));
                return swap;
            }
            return null;
        }
    },
    PAIR(2) {
        IntBuffer recognize2
                (IntBuffer
                        hand, CardMemory
                        memory) {

            if (memory.runs.isEmpty()) return null;
            return memory.runs.get(0);
        }
    },
    HIGH(1) {
        IntBuffer recognize2
                (IntBuffer
                        hand, CardMemory
                        cardMemory) {
            return HoldemRules.doHighCard(hand);
        }
    },
    OUT(0) {
        public int compareHand
                (IntBuffer
                        ignorethis, IntBuffer
                        ignorethis2) {
            return 0;
        }
        IntBuffer recognize2
                (IntBuffer
                        hand, CardMemory
                        cardMemory) {
            return EMPTYCARDS;
        }
    };

    public final int minimum;
    private static final int PLAY_LEN = Play.values().length;

    Play(int minimum) {
        this.minimum = minimum;
    }

    abstract IntBuffer recognize2(IntBuffer hand, CardMemory memory);

    /**
     * marshal the memory cache
     *
     * @param hand
     * @param memory
     * @return
     */
    public IntBuffer recognize(IntBuffer hand, CardMemory memory) {
        IntBuffer res = null;
        if (hand.rewind().mark().limit() >= minimum) {
//            IntBuffer cache = memory.mpos(this);
//
//            int card = cache.get();
//            cache.reset();
//
//            final int hlim = hand.limit();
//
//            final int nixPlay = 0x80 | hlim;
//            if (nixPlay != card) {  //nixplay indicates a previous failed test
//                if (-1 == card) { //-1 indicates virgin
            res = this.recognize2(hand, memory); //blast off
        }
        return res;
    }

    public static Pair<Play, IntBuffer> assess(IntBuffer cards) {
        return assess(cards, new CardMemory());
    }

    public static Pair<Play, IntBuffer> assess(IntBuffer cards, CardMemory memory) {

        Pair<Play, IntBuffer> res = null;
        final IntBuffer hand = (IntBuffer) cards.asReadOnlyBuffer().mark();

        if (memory.lastCount != cards.limit()) {
            memory.wipe();
            handRanks(hand, memory);
        }

        memory.lastCount = cards.limit();

        for (final Play play : Play.values()) {
            final IntBuffer swap = play.recognize(hand, memory);
            if (swap != null) {
                swap.rewind().mark();
                res = new Pair<Play, IntBuffer>() {
                    public Play getFirst() {
                        return play;
                    }

                    public IntBuffer getSecond() {
                        return (IntBuffer) swap.reset();
                    }
                };
                break;
            }
        }
        return res;
    }

    /**
     * counts up number of unique faces
     *
     * @return
     */
    static void handRanks(IntBuffer hand, CardMemory memory) {
        memory.ranks = 0;
        hand.reset();
        memory.runs.clear();

        memory.flushidx = 0;
        int hlen;
        hlen = hand.limit();

        memory.straight = EMPTY_SET;
        memory.straightflush = null;

        IntBuffer straight = allocate(hlen);

        for (int i = 0; i < memory.flush.length; i++)
            memory.flush[i] = BuffUtil.allocate(hlen);

        int run = 0; //find pairs, trips, etc.

        int card = -1;
        int prev = card;
        memory.ace1st = ACE.ordinal() == face(hand.get(0));

        for (int curs = 0; curs < hlen; curs++, prev = card) {
            card = hand.get(curs);
            final int face = face(card);
            final int pface = face(prev);
            final boolean same = face == pface;
            final int suit = suit(card);
            memory.flush[suit].put(card);

            final boolean ending = (curs == (hlen - 1));

            if (same) {
                run++;
                if (!ending)
                    continue;
            } else
                memory.ranks++;

            if (!same || ending)
                if (run != 0) {
                    memory.runs.add((IntBuffer) ((IntBuffer) hand.position(curs - run)).slice().mark().limit(run + 1));
                    run = 0;
                }


            final boolean consecutive = curs == 0 || face - pface == 1;

            boolean wheel = false;
            if (consecutive) {
                straight.put(card);
                wheel = memory.ace1st &&
                        face == TWO.ordinal() &&
                        face(straight.get(0)) == FIVE.ordinal();
                if (wheel) {
                    IntBuffer tmp = (IntBuffer) allocate(hlen).mark();
                    tmp.put((IntBuffer) straight.flip());
                    tmp.put(hand.get(0));
                    straight = tmp;
                }
                if (!ending && !wheel) continue; //wheel ends straights
            }


            if (ending || wheel || straight.position() >= memory.straight.limit())  //guarantee that equal length straights favor the first
            {
                memory.straight = (IntBuffer) straight.flip().mark();
                if (ending)
                    break;
                straight = allocate(hlen - curs);
            } else
                straight.reset();
            straight.put(card);
        }

        int max = 0;
        for (int i = 0; i < 4; i++) {
            final IntBuffer flush = memory.flush[i];
            final int slen = flush.flip().mark().limit();
            if (slen > max) {
                memory.flushidx = i;
                max = slen;
            }
        }
    }

    public int compareHand(IntBuffer hand1, IntBuffer hand2) {
        return compareDefault(hand1, hand2);
    }
}


