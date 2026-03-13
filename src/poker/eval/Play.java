package poker.eval;

import static poker.eval.CardUtil.*;
import static poker.eval.Face.*;
import static poker.eval.BuffUtil.*;
import poker.player.*;

import java.util.*;
import static java.lang.Math.min;

/**
 * Created by James Northrup
 * User: jim
 * Date: Aug 11, 2007
 * Time: 2:51:38 AM
 * 
 * Replaced IntBuffer with int[] for memory efficiency.
 */
@SuppressWarnings({"ConstantConditions"})

public enum Play {
    ROYALSTRAIGHTFLUSH(5) {
        int[] recognize2(int[] hand, CardMemory memory) {
            if (memory.ranks < 5 && !memory.ace1st) return null;
            final int[] res = STRAIGHTFLUSH.recognize2(hand, memory);
            if (res != null && face(res[0]) == ACE.ordinal())
                return res;
            return null;
        }
    },

    STRAIGHTFLUSH(5) {


        int[] recognize2(int[] hand, CardMemory memory) {

            if (memory.straightflush != null && memory.straightflush.length > 0) {
                if (memory.straightflush != null) return memory.straightflush;
            } else {
                return null;
            }
            if (memory.ranks < 5) return null;
            int[] res = null;
            int[] fbuf = memory.flush[memory.flushidx];
            final int[] sbuf = memory.straight;
            if (sbuf.length < 5 || fbuf.length < 5) return null;

            {
                final int card = sbuf[sbuf.length - 1];
                final int i = face(card);
                if (memory.ace1st && i == ACE.ordinal()) {

                    int[] t = BuffUtil.allocate(fbuf.length);
                    final int first = fbuf[0];
                    System.arraycopy(fbuf, 0, t, 0, fbuf.length);
                    t[fbuf.length] = first;
                    fbuf = t;
                }
            }

            int fidx = 0;
            int sidx = 0;
            int hit = 0;

            while (fidx < fbuf.length && sidx < sbuf.length) {
                int f = fbuf[fidx++];
                int s = sbuf[sidx++];
                final int ff = face(f);
                final int sf = face(s);

                if (ff == sf) {
                    hit++;
                    if (hit == 5) {
                        res = Arrays.copyOfRange(fbuf, fidx - 5, fidx);
                        break;
                    }
                } else {
                    hit = 0;
                    if (fbuf.length - fidx < 5 || sbuf.length - sidx < 5)
                        break;
                    if (ff > sf)
                        s = sbuf[sidx++];
                    else
                        f = fbuf[fidx - 1];
                }
            }
            memory.straightflush = res == null ? new int[0] : res;
            return res;
        }
    },

    FOUROFAKIND(4) {
        public int compareHand
                (int[]
                        cards, int[]
                        cards1) {
            return compareDefault(cards, cards1);
        }

        int[] recognize2
                (int[]
                        hand, CardMemory
                        memory) {
            final int len = hand.length;
            int alen = len - 4;
            if (memory.ranks <= 1 + alen) {
                for (int[] run : memory.runs) {
                    if (run.length == 4) return run;
                }
            }
            return null;
        }
    },

    FULLHOUSE(5) {
        public int compareHand(int[] cards, int[] cards1) {
            return HoldemRules.compareBoat(cards, cards1);
        }

        int[] recognize2
                (int[]
                        hand, CardMemory
                        memory) {
            boolean b2 = false, b3 = false;
            int[] swap = allocate(minimum);
            final int len = hand.length;
            final int alen = len - 5;
            if (memory.ranks <= alen + 2) {
                for (int[] run : memory.runs) {
                    if ((!b3) && run.length == 3) {
                        System.arraycopy(run, 0, swap, 0, 3);
                        b3 = true;
                    } else if ((!b2) && run.length != 4) {
                        System.arraycopy(run, 0, swap, 3, 2);
                        b2 = true;
                    }
                    if (b2 & b3)
                        return swap;
                }
            }
            return null;
        }
    },
    /**
     * flush parsing returns as many flush cards as have been found in order to facilitiate better straightflush recogniztion from the outcomes of flush
     */
    FLUSH(5) {
        int[] recognize2(int[] hand, CardMemory memory) {
            final int[] flush = memory.flush[memory.flushidx];
            return flush.length > 4 ? flush : null;
        }
    },


    STRAIGHT(5) {
        int[] recognize2(int[] hand, CardMemory memory) {
            final int[] buffer = memory.straight;
            final int i = buffer.length;
            return i > 4 ? buffer : null;
        }
    },

    THREEOFAKIND(3) {
        int[] recognize2
                (int[]
                        hand, CardMemory
                        memory) {
            int alen = hand.length - minimum;
            if (memory.ranks > 1 + alen) return null;
            for (int[] run : memory.runs)
                if (run.length == 3) return run;
            return null;
        }
    },

    TWOPAIR(4) {
        int[] recognize2
                (int[]
                        hand, CardMemory
                        memory) {
            int[] swap = allocate(4);


            if (memory.runs.size() > 1) {
                System.arraycopy(memory.runs.get(0), 0, swap, 0, 2);
                System.arraycopy(memory.runs.get(1), 0, swap, 2, 2);
                return swap;
            }
            return null;
        }
    },
    PAIR(2) {
        int[] recognize2
                (int[]
                        hand, CardMemory
                        memory) {

            if (memory.runs.isEmpty()) return null;
            return memory.runs.get(0);
        }
    },
    HIGH(1) {
        int[] recognize2
                (int[]
                        hand, CardMemory
                        cardMemory) {
            return HoldemRules.doHighCard(hand);
        }
    },
    OUT(0) {
        public int compareHand
                (int[]
                        ignorethis, int[]
                        ignorethis2) {
            return 0;
        }
        int[] recognize2
                (int[]
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

    abstract int[] recognize2(int[] hand, CardMemory memory);

    /**
     * marshal the memory cache
     *
     * @param hand
     * @param memory
     * @return
     */
    public int[] recognize(int[] hand, CardMemory memory) {
        int[] res = null;
        if (hand.length >= minimum) {

            res = this.recognize2(hand, memory);

        }
        return res;
    }

    public static Pair<Play, int[]> assess(int[] cards) {
        return assess(cards, new CardMemory());
    }

    public static Pair<Play, int[]> assess(int[] cards, CardMemory memory) {

        Pair<Play, int[]> res = null;
        final int[] hand = cards.clone();

        if (memory.lastCount != cards.length) {
            memory.wipe();
            handRanks(hand, memory);
        }

        memory.lastCount = cards.length;

        for (final Play play : Play.values()) {
            final int[] swap = play.recognize(hand, memory);
            if (swap != null) {
                res = new Pair<Play, int[]>() {
                    public Play getFirst() {
                        return play;
                    }

                    public int[] getSecond() {
                        return swap;
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
    static void handRanks(int[] hand, CardMemory memory) {
        memory.ranks = 0;
        memory.runs.clear();

        memory.flushidx = 0;
        int hlen = hand.length;

        memory.straight = new int[0];
        memory.straightflush = null;

        int[] straight = allocate(hlen);

        for (int i = 0; i < memory.flush.length; i++)
            memory.flush[i] = BuffUtil.allocate(hlen);

        int run = 0; //find pairs, trips, etc.

        int card = -1;
        int prev = card;
        memory.ace1st = ACE.ordinal() == face(hand[0]);

        for (int curs = 0; curs < hlen; curs++, prev = card) {
            card = hand[curs];
            final int face = face(card);
            final int pface = face(prev);
            final boolean same = face == pface;
            final int suit = suit(card);
            memory.flush[suit][curs] = card;

            final boolean ending = (curs == (hlen - 1));

            if (same) {
                run++;
                if (!ending)
                    continue;
            } else
                memory.ranks++;

            if (!same || ending)
                if (run != 0) {
                    int[] runBuf = Arrays.copyOfRange(hand, curs - run, curs + 1);
                    memory.runs.add(runBuf);
                    run = 0;
                }


            final boolean consecutive = curs == 0 || face - pface == 1;

            boolean wheel = false;
            if (consecutive) {
                straight[curs] = card;
                wheel = memory.ace1st &&
                        face == TWO.ordinal() &&
                        face(straight[0]) == FIVE.ordinal();
                if (wheel) {
                    int[] tmp = BuffUtil.allocate(hlen);
                    System.arraycopy(straight, 0, tmp, 0, hlen);
                    tmp[hlen] = hand[0];
                    straight = tmp;
                }
                if (!ending && !wheel) continue; //wheel ends straights
            }


            if (ending || wheel || curs >= memory.straight.length)  //guarantee that equal length straights favor the first
            {
                memory.straight = Arrays.copyOfRange(straight, 0, curs + 1);
                if (ending)
                    break;
                straight = allocate(hlen - curs);
            } else
                straight = Arrays.copyOfRange(straight, 0, curs);
            straight[curs] = card;
        }

        int max = 0;
        for (int i = 0; i < 4; i++) {
            final int[] flush = memory.flush[i];
            final int slen = flush.length;
            if (slen > max) {
                memory.flushidx = i;
                max = slen;
            }
        }
    }

    public int compareHand(int[] hand1, int[] hand2) {
        return compareDefault(hand1, hand2);
    }
}
