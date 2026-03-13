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
                return memory.straightflush;
            }
            if (memory.ranks < 5) return null;
            int[] fbuf = memory.flush[memory.flushidx];
            int flen = memory.flushLen[memory.flushidx];
            int[] sbuf = memory.straight;
            int slen = memory.straightLen;
            if (slen < 5 || flen < 5) return null;

            int fidx = 0;
            int sidx = 0;
            int hit = 0;

            while (fidx < flen && sidx < slen) {
                int f = fbuf[fidx++];
                int s = sbuf[sidx++];
                final int ff = face(f);
                final int sf = face(s);

                if (ff == sf) {
                    hit++;
                    if (hit == 5) {
                        int[] res = new int[5];
                        for (int i = 0; i < 5; i++) res[i] = fbuf[fidx - 5 + i];
                        memory.straightflush = res;
                        return res;
                    }
                } else {
                    hit = 0;
                    if (flen - fidx < 5 || slen - sidx < 5)
                        break;
                    if (ff > sf)
                        s = sbuf[sidx++];
                    else
                        f = fbuf[fidx - 1];
                }
            }
            memory.straightflush = EMPTYCARDS;
            return null;
        }
    },

    FOUROFAKIND(4) {
        public int compareHand(int[] cards, int[] cards1) {
            return compareDefault(cards, cards1);
        }

        int[] recognize2(int[] hand, CardMemory memory) {
            final int len = hand.length;
            int alen = len - 4;
            if (memory.ranks <= 1 + alen) {
                for (int i = 0; i < memory.runsLen; i += 2) {
                    if (memory.runsBuf[i + 1] == 4) {
                        int start = memory.runsBuf[i];
                        int[] res = new int[4];
                        System.arraycopy(hand, start, res, 0, 4);
                        return res;
                    }
                }
            }
            return null;
        }
    },

    FULLHOUSE(5) {
        public int compareHand(int[] cards, int[] cards1) {
            return HoldemRules.compareBoat(cards, cards1);
        }

        int[] recognize2(int[] hand, CardMemory memory) {
            int tripStart = -1, pairStart = -1;
            for (int i = 0; i < memory.runsLen; i += 2) {
                int runLen = memory.runsBuf[i + 1];
                if (runLen == 3 && tripStart < 0) {
                    tripStart = memory.runsBuf[i];
                } else if (runLen == 2 && pairStart < 0) {
                    pairStart = memory.runsBuf[i];
                }
                if (tripStart >= 0 && pairStart >= 0) {
                    int[] res = new int[5];
                    System.arraycopy(hand, tripStart, res, 0, 3);
                    System.arraycopy(hand, pairStart, res, 3, 2);
                    return res;
                }
            }
            return null;
        }
    },
    FLUSH(5) {
        int[] recognize2(int[] hand, CardMemory memory) {
            final int flen = memory.flushLen[memory.flushidx];
            return flen > 4 ? memory.flush[memory.flushidx] : null;
        }
    },


    STRAIGHT(5) {
        int[] recognize2(int[] hand, CardMemory memory) {
            return memory.straightLen > 4 ? memory.straight : null;
        }
    },

    THREEOFAKIND(3) {
        int[] recognize2(int[] hand, CardMemory memory) {
            int alen = hand.length - minimum;
            if (memory.ranks > 1 + alen) return null;
            for (int i = 0; i < memory.runsLen; i += 2) {
                if (memory.runsBuf[i + 1] == 3) {
                    int start = memory.runsBuf[i];
                    int[] res = new int[3];
                    System.arraycopy(hand, start, res, 0, 3);
                    return res;
                }
            }
            return null;
        }
    },

    TWOPAIR(4) {
        int[] recognize2(int[] hand, CardMemory memory) {
            int pairCount = 0;
            for (int i = 0; i < memory.runsLen; i += 2) {
                if (memory.runsBuf[i + 1] == 2) pairCount++;
            }
            if (pairCount >= 2) {
                int[] swap = new int[4];
                int idx = 0;
                for (int i = 0; i < memory.runsLen && idx < 4; i += 2) {
                    if (memory.runsBuf[i + 1] == 2) {
                        int start = memory.runsBuf[i];
                        swap[idx++] = hand[start];
                        swap[idx++] = hand[start + 1];
                    }
                }
                return swap;
            }
            return null;
        }
    },
    PAIR(2) {
        int[] recognize2(int[] hand, CardMemory memory) {
            if (memory.runsLen == 0) return null;
            int start = memory.runsBuf[0];
            int[] res = new int[2];
            res[0] = hand[start];
            res[1] = hand[start + 1];
            return res;
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

        if (memory.lastCount != cards.length) {
            memory.wipe();
            handRanks(cards, memory);
        }

        memory.lastCount = cards.length;

        if (memory.ranks >= 5) {
            int[] straightBuf = memory.straight;
            int straightLen = memory.straightLen;
            if (straightLen >= 5) {
                int[] fbuf = memory.flush[memory.flushidx];
                int flen = memory.flushLen[memory.flushidx];
                if (flen >= 5) {
                    int hit = 0;
                    int sidx = 0;
                    for (int f = 0; f < flen && sidx < straightLen; f++) {
                        if (face(fbuf[f]) == face(straightBuf[sidx])) {
                            hit++;
                            sidx++;
                            if (hit == 5) {
                                int[] res = new int[5];
                                for (int i = 0; i < 5; i++) res[i] = fbuf[f - 4 + i];
                                if (face(res[0]) == ACE.ordinal()) {
                                    return new Pair<Play, int[]>() {
                                        public Play getFirst() { return ROYALSTRAIGHTFLUSH; }
                                        public int[] getSecond() { return res; }
                                    };
                                }
                                return new Pair<Play, int[]>() {
                                    public Play getFirst() { return STRAIGHTFLUSH; }
                                    public int[] getSecond() { return res; }
                                };
                            }
                        } else {
                            hit = 0;
                            sidx = 0;
                        }
                    }
                }
            }
        }

        if (memory.ranks <= 2) {
            for (int i = 0; i < memory.runsLen; i += 2) {
                int runStart = memory.runsBuf[i];
                int runLen = memory.runsBuf[i + 1];
                if (runLen == 4) {
                    int[] res = new int[4];
                    System.arraycopy(cards, runStart, res, 0, 4);
                    return new Pair<Play, int[]>() {
                        public Play getFirst() { return FOUROFAKIND; }
                        public int[] getSecond() { return res; }
                    };
                }
            }
        }

        if (memory.ranks <= 3) {
            int tripLen = 0, pairLen = 0;
            for (int i = 0; i < memory.runsLen; i += 2) {
                int runLen = memory.runsBuf[i + 1];
                if (runLen == 3) tripLen++;
                else if (runLen == 2) pairLen++;
            }
            if (tripLen > 0 && pairLen > 0) {
                int[] res = new int[5];
                int tripStart = -1, pairStart = -1;
                for (int i = 0; i < memory.runsLen; i += 2) {
                    int runLen = memory.runsBuf[i + 1];
                    int start = memory.runsBuf[i];
                    if (runLen == 3 && tripStart < 0) tripStart = start;
                    else if (runLen == 2 && pairStart < 0) pairStart = start;
                }
                if (tripStart >= 0 && pairStart >= 0) {
                    System.arraycopy(cards, tripStart, res, 0, 3);
                    System.arraycopy(cards, pairStart, res, 3, 2);
                }
                return new Pair<Play, int[]>() {
                    public Play getFirst() { return FULLHOUSE; }
                    public int[] getSecond() { return res; }
                };
            }
        }

        int flen = memory.flushLen[memory.flushidx];
        if (flen >= 5) {
            int[] res = new int[5];
            System.arraycopy(memory.flush[memory.flushidx], 0, res, 0, 5);
            return new Pair<Play, int[]>() {
                public Play getFirst() { return FLUSH; }
                public int[] getSecond() { return res; }
            };
        }

        if (memory.straightLen >= 5) {
            int[] res = new int[5];
            System.arraycopy(memory.straight, 0, res, 0, 5);
            return new Pair<Play, int[]>() {
                public Play getFirst() { return STRAIGHT; }
                public int[] getSecond() { return res; }
            };
        }

        if (memory.ranks <= 3) {
            for (int i = 0; i < memory.runsLen; i += 2) {
                if (memory.runsBuf[i + 1] == 3) {
                    int runStart = memory.runsBuf[i];
                    int[] res = new int[3];
                    System.arraycopy(cards, runStart, res, 0, 3);
                    return new Pair<Play, int[]>() {
                        public Play getFirst() { return THREEOFAKIND; }
                        public int[] getSecond() { return res; }
                    };
                }
            }
        }

        if (memory.ranks <= 3) {
            int pairCount = 0;
            for (int i = 0; i < memory.runsLen; i += 2) {
                if (memory.runsBuf[i + 1] == 2) pairCount++;
            }
            if (pairCount >= 2) {
                int[] res = new int[4];
                for (int i = 0, idx = 0; i < memory.runsLen && idx < 4; i += 2) {
                    if (memory.runsBuf[i + 1] == 2) {
                        int runStart = memory.runsBuf[i];
                        res[idx++] = cards[runStart];
                        res[idx++] = cards[runStart + 1];
                    }
                }
                return new Pair<Play, int[]>() {
                    public Play getFirst() { return TWOPAIR; }
                    public int[] getSecond() { return res; }
                };
            }
        }

        if (memory.runsLen > 0) {
            int runStart = memory.runsBuf[0];
            int[] res = new int[2];
            res[0] = cards[runStart];
            res[1] = cards[runStart + 1];
            return new Pair<Play, int[]>() {
                public Play getFirst() { return PAIR; }
                public int[] getSecond() { return res; }
            };
        }

        int[] res = new int[1];
        res[0] = cards[0];
        return new Pair<Play, int[]>() {
            public Play getFirst() { return HIGH; }
            public int[] getSecond() { return res; }
        };
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

        memory.straightflush = null;
        memory.straightLen = 0;
        
        for (int i = 0; i < 4; i++) {
            memory.flushLen[i] = 0;
        }

        int card = -1;
        int prev = card;
        memory.ace1st = ACE.ordinal() == face(hand[0]);
        int run = 0;

        for (int curs = 0; curs < hlen; curs++, prev = card) {
            card = hand[curs];
            final int f = face(card);
            final int pface = face(prev);
            final boolean same = f == pface;
            final int suit = suit(card);
            
            int[] flushBuf = memory.flush[suit];
            int flushPos = memory.flushLen[suit];
            flushBuf[flushPos] = card;
            memory.flushLen[suit] = flushPos + 1;

            final boolean ending = (curs == (hlen - 1));

            if (same) {
                if (!ending)
                    continue;
            } else
                memory.ranks++;

            if (!same || ending)
                if (curs - run >= 0) {
                    int runStart = curs - run;
                    int runLen = run + 1;
                    if (runLen == 3 || runLen == 4) {
                        int[] runBuf = memory.runsBuf;
                        int rpos = memory.runsLen;
                        runBuf[rpos] = runStart;
                        runBuf[rpos + 1] = runLen;
                        memory.runsLen += 2;
                    }
                    run = 0;
                }


            final boolean consecutive = curs == 0 || f - pface == 1;

            if (consecutive) {
                int[] straightBuf = memory.straight;
                if (memory.straightLen < 7) {
                    straightBuf[memory.straightLen] = card;
                    memory.straightLen++;
                }
                
                boolean wheel = memory.ace1st &&
                        f == TWO.ordinal() &&
                        face(straightBuf[0]) == FIVE.ordinal();
                
                if (wheel) {
                    // Wheel straight: A-2-3-4-5, use position 0 for Ace
                    // straight currently has 2,3,4,5, we need A at position 0
                    // Shift and prepend Ace
                    for (int w = memory.straightLen - 1; w >= 1; w--) {
                        straightBuf[w + 1] = straightBuf[w];
                    }
                    straightBuf[0] = hand[0];
                    memory.straightLen++;
                }
                
                if (!ending && !wheel) continue;
            }


            if (ending || memory.straightLen >= 5)  
            {
                if (memory.straightLen >= 5) {
                    memory.straightLen = 5;
                }
                if (ending)
                    break;
                memory.straightLen = 0;
                if (curs + 1 < hlen) {
                    memory.straight[0] = hand[curs + 1];
                    memory.straightLen = 1;
                }
            } else if (memory.straightLen > 0) {
                memory.straight[memory.straightLen - 1] = card;
            }
        }

        int max = 0;
        for (int i = 0; i < 4; i++) {
            final int slen = memory.flushLen[i];
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
