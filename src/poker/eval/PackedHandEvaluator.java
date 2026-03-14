package poker.eval;

import java.util.Arrays;

/**
 * Optimized hand evaluator using 64-bit packed card encoding.
 * Each card: 6 bits (4-bit face + 2-bit suit)
 * 7 cards = 42 bits, fits in single 64-bit long.
 */
public class PackedHandEvaluator {
    
    // Face values: 2=0, 3=1, ..., A=12
    // Suit values: 0-3
    private static final int FACE_BITS = 4;
    private static final int SUIT_BITS = 2;
    private static final int CARDS_PER_LONG = 7;
    private static final int BITS_PER_CARD = FACE_BITS + SUIT_BITS;
    
    // Working arrays reused across evaluations
    private final long[] hands = new long[1];
    private final int[] faceCount = new int[13];
    private final int[] suitCount = new int[4];
    private final int[] cardsByFace = new int[13];
    private final int[] flushSuitCards = new int[7];
    
    // Result arrays
    private final long[] bestHand = new long[1];
    private int handRank;
    
    public enum HandRank {
        HIGH_CARD,
        PAIR,
        TWO_PAIR,
        THREE_OF_A_KIND,
        STRAIGHT,
        FLUSH,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        STRAIGHT_FLUSH,
        ROYAL_FLUSH
    }
    
    /**
     * Evaluate the best 5-card hand from 7 cards (2 hole + 5 community).
     * Returns hand rank and populates bestHand array.
     */
    public HandRank evaluate(long packedCards) {
        hands[0] = packedCards;
        
        // Clear working arrays
        Arrays.fill(faceCount, 0);
        Arrays.fill(suitCount, 0);
        Arrays.fill(cardsByFace, 0);
        
        // Single pass: unpack and count faces/suits
        for (int i = 0; i < CARDS_PER_LONG; i++) {
            long card = unpackCard(packedCards, i);
            if (card == 0) break; // No more cards
            
            int face = (int)((card >>> FACE_BITS) & 0xF);
            int suit = (int)(card & 0x3);
            
            faceCount[face]++;
            suitCount[suit]++;
            cardsByFace[face] = (int)card; // Store last card for this face
        }
        
        // Check for flush
        int flushSuit = -1;
        for (int s = 0; s < 4; s++) {
            if (suitCount[s] >= 5) {
                flushSuit = s;
                break;
            }
        }
        
        // Collect flush cards if present
        int flushCardCount = 0;
        if (flushSuit != -1) {
            for (int i = 0; i < CARDS_PER_LONG; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int suit = (int)(card & 0x3);
                if (suit == flushSuit) {
                    flushSuitCards[flushCardCount++] = (int)card;
                }
            }
            // Sort flush cards by face (descending)
            Arrays.sort(flushSuitCards, 0, flushCardCount);
            reverseArray(flushSuitCards, flushCardCount);
        }
        
        // Check for straight (including Ace-low)
        boolean hasStraight = false;
        int straightHighFace = -1;
        
        // Normal straight (A-high down to 5-high)
        for (int highFace = 12; highFace >= 4; highFace--) {
            int count = 0;
            for (int i = 0; i < 5; i++) {
                if (faceCount[highFace - i] > 0) {
                    count++;
                }
            }
            if (count == 5) {
                hasStraight = true;
                straightHighFace = highFace;
                break;
            }
        }
        
        // Check Ace-low straight (A-2-3-4-5)
        if (!hasStraight && faceCount[12] > 0 && // Ace
            faceCount[0] > 0 && faceCount[1] > 0 && 
            faceCount[2] > 0 && faceCount[3] > 0) {
            hasStraight = true;
            straightHighFace = 3; // 5 is high card in Ace-low straight
        }
        
        // Check for straight flush and royal flush
        if (flushSuit != -1 && hasStraight) {
            // Check if straight cards are all of flush suit
            boolean straightFlush = true;
            int straightHighCard = -1;
            
            if (straightHighFace >= 4) { // Normal straight
                for (int i = 0; i < 5; i++) {
                    int face = straightHighFace - i;
                    boolean hasCardOfSuit = false;
                    for (int j = 0; j < CARDS_PER_LONG; j++) {
                        long card = unpackCard(packedCards, j);
                        if (card == 0) break;
                        int cardFace = (int)((card >>> FACE_BITS) & 0xF);
                        int suit = (int)(card & 0x3);
                        if (cardFace == face && suit == flushSuit) {
                            hasCardOfSuit = true;
                            if (i == 0) straightHighCard = (int)card;
                            break;
                        }
                    }
                    if (!hasCardOfSuit) {
                        straightFlush = false;
                        break;
                    }
                }
            } else { // Ace-low straight
                int[] aceLowFaces = {12, 0, 1, 2, 3};
                straightFlush = true;
                for (int i = 0; i < 5; i++) {
                    int face = aceLowFaces[i];
                    boolean hasCardOfSuit = false;
                    for (int j = 0; j < CARDS_PER_LONG; j++) {
                        long card = unpackCard(packedCards, j);
                        if (card == 0) break;
                        int cardFace = (int)((card >>> FACE_BITS) & 0xF);
                        int suit = (int)(card & 0x3);
                        if (cardFace == face && suit == flushSuit) {
                            hasCardOfSuit = true;
                            if (face == 3) straightHighCard = (int)card; // 5 is high
                            break;
                        }
                    }
                    if (!hasCardOfSuit) {
                        straightFlush = false;
                        break;
                    }
                }
            }
            
            if (straightFlush) {
                // Build straight flush hand
                if (straightHighFace == 12) {
                    handRank = HandRank.ROYAL_FLUSH.ordinal();
                } else {
                    handRank = HandRank.STRAIGHT_FLUSH.ordinal();
                }
                
                // For Ace-low straight flush, we need to reorder
                if (straightHighFace < 4) { // Ace-low
                    for (int i = 0; i < 5; i++) {
                        int face = (i == 0 ? 3 : (i == 4 ? 12 : (3 - i)));
                        for (int j = 0; j < CARDS_PER_LONG; j++) {
                            long card = unpackCard(packedCards, j);
                            if (card == 0) break;
                            int cardFace = (int)((card >>> FACE_BITS) & 0xF);
                            int suit = (int)(card & 0x3);
                            if (cardFace == face && suit == flushSuit) {
                                bestHand[0] = packCard(face, suit);
                                break;
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < 5; i++) {
                        int face = straightHighFace - i;
                        for (int j = 0; j < CARDS_PER_LONG; j++) {
                            long card = unpackCard(packedCards, j);
                            if (card == 0) break;
                            int cardFace = (int)((card >>> FACE_BITS) & 0xF);
                            int suit = (int)(card & 0x3);
                            if (cardFace == face && suit == flushSuit) {
                                bestHand[0] = packCard(face, suit);
                                break;
                            }
                        }
                    }
                }
                return handRank == HandRank.ROYAL_FLUSH.ordinal() ? 
                    HandRank.ROYAL_FLUSH : HandRank.STRAIGHT_FLUSH;
            }
        }
        
        // Check for four of a kind
        int quadFace = -1;
        for (int f = 12; f >= 0; f--) {
            if (faceCount[f] == 4) {
                quadFace = f;
                break;
            }
        }
        
        if (quadFace != -1) {
            handRank = HandRank.FOUR_OF_A_KIND.ordinal();
            // Add quad cards
            int idx = 0;
            for (int i = 0; i < CARDS_PER_LONG && idx < 4; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int face = (int)((card >>> FACE_BITS) & 0xF);
                if (face == quadFace) {
                    bestHand[0] = packCard(face, (int)(card & 0x3));
                    idx++;
                }
            }
            // Add kicker
            for (int i = 0; i < CARDS_PER_LONG && idx < 5; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int face = (int)((card >>> FACE_BITS) & 0xF);
                if (face != quadFace) {
                    bestHand[0] = packCard(face, (int)(card & 0x3));
                    if (idx == 5) break;
                }
            }
            return HandRank.FOUR_OF_A_KIND;
        }
        
        // Check for full house
        int tripsFace = -1;
        int pairFace = -1;
        
        for (int f = 12; f >= 0; f--) {
            if (faceCount[f] == 3 && tripsFace == -1) {
                tripsFace = f;
            } else if (faceCount[f] >= 2 && pairFace == -1 && f != tripsFace) {
                pairFace = f;
            }
        }
        
        // For full house we need three of one face and two of another
        if (tripsFace != -1) {
            // Look for second trips or pair
            for (int f = 12; f >= 0; f--) {
                if (f != tripsFace && faceCount[f] >= 2) {
                    pairFace = f;
                    break;
                }
            }
            
            if (pairFace != -1) {
                handRank = HandRank.FULL_HOUSE.ordinal();
                // Add trips
                int idx = 0;
                for (int i = 0; i < CARDS_PER_LONG && idx < 3; i++) {
                    long card = unpackCard(packedCards, i);
                    if (card == 0) break;
                    int face = (int)((card >>> FACE_BITS) & 0xF);
                    if (face == tripsFace) {
                        bestHand[0] = packCard(face, (int)(card & 0x3));
                        idx++;
                    }
                }
                // Add pair (take first 2 cards of that face)
                for (int i = 0; i < CARDS_PER_LONG && idx < 5; i++) {
                    long card = unpackCard(packedCards, i);
                    if (card == 0) break;
                    int face = (int)((card >>> FACE_BITS) & 0xF);
                    if (face == pairFace) {
                        bestHand[0] = packCard(face, (int)(card & 0x3));
                        idx++;
                    }
                }
                return HandRank.FULL_HOUSE;
            }
        }
        
        // Check for flush (already know flushSuit != -1 if we get here)
        if (flushSuit != -1) {
            handRank = HandRank.FLUSH.ordinal();
            for (int i = 0; i < 5; i++) {
                int suit = flushSuitCards[i] & 0x3;
                int face = flushSuitCards[i] >>> FACE_BITS;
                bestHand[0] = packCard(face, suit);
            }
            return HandRank.FLUSH;
        }
        
        // Check for straight (already computed)
        if (hasStraight) {
            handRank = HandRank.STRAIGHT.ordinal();
            if (straightHighFace >= 4) { // Normal straight
                for (int i = 0; i < 5; i++) {
                    int face = straightHighFace - i;
                    // Take first card of each face (they're sorted descending)
                    for (int j = 0; j < CARDS_PER_LONG; j++) {
                        long card = unpackCard(packedCards, j);
                        if (card == 0) break;
                        int cardFace = (int)((card >>> FACE_BITS) & 0xF);
                        if (cardFace == face) {
                            bestHand[0] = packCard(face, (int)(card & 0x3));
                            break;
                        }
                    }
                }
            } else { // Ace-low straight
                int[] faces = {3, 2, 1, 0, 12}; // 5,4,3,2,A
                for (int i = 0; i < 5; i++) {
                    int face = faces[i];
                    for (int j = 0; j < CARDS_PER_LONG; j++) {
                        long card = unpackCard(packedCards, j);
                        if (card == 0) break;
                        int cardFace = (int)((card >>> FACE_BITS) & 0xF);
                        if (cardFace == face) {
                            bestHand[0] = packCard(face, (int)(card & 0x3));
                            break;
                        }
                    }
                }
            }
            return HandRank.STRAIGHT;
        }
        
        // Check for three of a kind
        if (tripsFace != -1) {
            handRank = HandRank.THREE_OF_A_KIND.ordinal();
            int idx = 0;
            // Add trips
            for (int i = 0; i < CARDS_PER_LONG; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int face = (int)((card >>> FACE_BITS) & 0xF);
                if (face == tripsFace) {
                    bestHand[0] = packCard(face, (int)(card & 0x3));
                    idx++;
                }
            }
            // Add kickers (highest remaining cards)
            for (int i = 0; i < CARDS_PER_LONG && idx < 5; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int face = (int)((card >>> FACE_BITS) & 0xF);
                if (face != tripsFace) {
                    bestHand[0] = packCard(face, (int)(card & 0x3));
                    idx++;
                }
            }
            return HandRank.THREE_OF_A_KIND;
        }
        
        // Check for two pair
        int pair1Face = -1;
        int pair2Face = -1;
        
        for (int f = 12; f >= 0; f--) {
            if (faceCount[f] >= 2) {
                if (pair1Face == -1) {
                    pair1Face = f;
                } else if (pair2Face == -1) {
                    pair2Face = f;
                    break;
                }
            }
        }
        
        if (pair2Face != -1) {
            handRank = HandRank.TWO_PAIR.ordinal();
            int idx = 0;
            // Add first pair (higher face)
            for (int i = 0; i < CARDS_PER_LONG && idx < 2; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int face = (int)((card >>> FACE_BITS) & 0xF);
                if (face == pair1Face) {
                    bestHand[0] = packCard(face, (int)(card & 0x3));
                    idx++;
                }
            }
            // Add second pair
            for (int i = 0; i < CARDS_PER_LONG && idx < 4; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int face = (int)((card >>> FACE_BITS) & 0xF);
                if (face == pair2Face) {
                    bestHand[0] = packCard(face, (int)(card & 0x3));
                    idx++;
                }
            }
            // Add kicker
            for (int i = 0; i < CARDS_PER_LONG && idx < 5; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int face = (int)((card >>> FACE_BITS) & 0xF);
                if (face != pair1Face && face != pair2Face) {
                    bestHand[0] = packCard(face, (int)(card & 0x3));
                    break;
                }
            }
            return HandRank.TWO_PAIR;
        }
        
        // Check for pair
        if (pair1Face != -1) {
            handRank = HandRank.PAIR.ordinal();
            int idx = 0;
            // Add pair
            for (int i = 0; i < CARDS_PER_LONG && idx < 2; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int face = (int)((card >>> FACE_BITS) & 0xF);
                if (face == pair1Face) {
                    bestHand[0] = packCard(face, (int)(card & 0x3));
                    idx++;
                }
            }
            // Add kickers (highest remaining cards)
            for (int i = 0; i < CARDS_PER_LONG && idx < 5; i++) {
                long card = unpackCard(packedCards, i);
                if (card == 0) break;
                int face = (int)((card >>> FACE_BITS) & 0xF);
                if (face != pair1Face) {
                    bestHand[0] = packCard(face, (int)(card & 0x3));
                    idx++;
                }
            }
            return HandRank.PAIR;
        }
        
        // High card
        handRank = HandRank.HIGH_CARD.ordinal();
        for (int i = 0; i < 5; i++) {
            long card = unpackCard(packedCards, i);
            if (card == 0) break;
            int face = (int)((card >>> FACE_BITS) & 0xF);
            int suit = (int)(card & 0x3);
            bestHand[0] = packCard(face, suit);
        }
        return HandRank.HIGH_CARD;
    }
    
    /**
     * Compare two hands. Returns negative if hand1 < hand2, 
     * positive if hand1 > hand2, 0 if equal.
     */
    public int compareHands(long packedHand1, long packedHand2) {
        HandRank rank1 = evaluate(packedHand1);
        long[] best1 = bestHand.clone();
        
        HandRank rank2 = evaluate(packedHand2);
        long[] best2 = bestHand.clone();
        
        // Compare ranks first
        if (rank1.ordinal() != rank2.ordinal()) {
            return rank2.ordinal() - rank1.ordinal(); // Higher rank is better
        }
        
        // Same rank, compare card faces
        for (int i = 0; i < 5; i++) {
            long card1 = best1[i];
            long card2 = best2[i];
            int face1 = (int)((card1 >>> FACE_BITS) & 0xF);
            int face2 = (int)((card2 >>> FACE_BITS) & 0xF);
            if (face1 != face2) {
                return face2 - face1; // Higher face is better
            }
        }
        
        return 0; // Exactly equal
    }
    
    /**
     * Get the best hand cards (call after evaluate()).
     */
    public long[] getBestHand() {
        return bestHand.clone();
    }
    
    /**
     * Get hand rank value (call after evaluate()).
     */
    public int getHandRankValue() {
        return handRank;
    }
    
    /**
     * Convert card to readable string.
     */
    public static String cardToString(long card) {
        char[] faces = {'2','3','4','5','6','7','8','9','T','J','Q','K','A'};
        char[] suits = {'h','d','c','s'};
        
        int face = (int)((card >>> FACE_BITS) & 0xF);
        int suit = (int)(card & 0x3);
        
        return "" + faces[face] + suits[suit];
    }
    
    /**
     * Pack face and suit into 6-bit card.
     */
    public static long packCard(int face, int suit) {
        return ((face & 0xF) << FACE_BITS) | (suit & 0x3);
    }
    
    /**
     * Unpack card from packed position.
     */
    private long unpackCard(long packedCards, int position) {
        int shift = BITS_PER_CARD * position;
        return (packedCards >>> shift) & ((1L << BITS_PER_CARD) - 1);
    }
    
    /**
     * Reverse array in place.
     */
    private void reverseArray(int[] array, int count) {
        for (int i = 0; i < count / 2; i++) {
            int temp = array[i];
            array[i] = array[count - 1 - i];
            array[count - 1 - i] = temp;
        }
    }
    
    /**
     * Create packed card from face/suit.
     */
    public static long makeCard(int face, int suit) {
        return packCard(face, suit);
    }
}