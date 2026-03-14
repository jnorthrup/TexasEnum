package poker.eval;

import java.util.Arrays;

/**
 * Optimized hand evaluator for Texas Hold'em using primitive arrays only.
 * No heap allocations, no IntBuffer overhead, single-pass evaluation.
 */
public class OptimizedHandEvaluator {
    
    // Face values: 2=0, 3=1, ..., A=12
    // Suit values: 0-3
    private static final int FACE_SHIFT = 16;
    private static final int SUIT_MASK = 0x3;
    private static final int ACE_FACE = 12; // ACE.ordinal()
    
    // Working arrays reused across evaluations
    private final int[] hand = new int[7];
    private final int[] faceCount = new int[13];
    private final int[] suitCount = new int[4];
    private final int[] cardsByFace = new int[13];
    private final int[] flushSuitCards = new int[7];
    
    // Result arrays
    private final int[] bestHand = new int[5];
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
    public HandRank evaluate(int[] cards) {
        if (cards.length != 7) {
            throw new IllegalArgumentException("Need exactly 7 cards");
        }
        
        // Copy to working array (could eliminate with direct processing)
        System.arraycopy(cards, 0, hand, 0, 7);
        
        // Sort by face (descending) for efficient evaluation
        sortByFaceDescending(hand);
        
        // Clear working arrays
        Arrays.fill(faceCount, 0);
        Arrays.fill(suitCount, 0);
        Arrays.fill(cardsByFace, 0);
        
        // Single pass: count faces and suits
        for (int i = 0; i < 7; i++) {
            int card = hand[i];
            int face = (card >>> FACE_SHIFT) & 0xF;
            int suit = card & SUIT_MASK;
            
            faceCount[face]++;
            suitCount[suit]++;
            cardsByFace[face] = card; // Store last card for this face
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
            for (int i = 0; i < 7; i++) {
                int card = hand[i];
                if ((card & SUIT_MASK) == flushSuit) {
                    flushSuitCards[flushCardCount++] = card;
                }
            }
            // Sort flush cards by face
            sortByFaceDescending(flushSuitCards, flushCardCount);
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
        if (!hasStraight && faceCount[ACE_FACE] > 0 &&
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
                    for (int j = 0; j < 7; j++) {
                        int card = hand[j];
                        if (((card >>> FACE_SHIFT) & 0xF) == face && 
                            (card & SUIT_MASK) == flushSuit) {
                            hasCardOfSuit = true;
                            if (i == 0) straightHighCard = card;
                            break;
                        }
                    }
                    if (!hasCardOfSuit) {
                        straightFlush = false;
                        break;
                    }
                }
            } else { // Ace-low straight
                int[] aceLowFaces = {ACE_FACE, 0, 1, 2, 3};
                straightFlush = true;
                for (int i = 0; i < 5; i++) {
                    int face = aceLowFaces[i];
                    boolean hasCardOfSuit = false;
                    for (int j = 0; j < 7; j++) {
                        int card = hand[j];
                        if (((card >>> FACE_SHIFT) & 0xF) == face && 
                            (card & SUIT_MASK) == flushSuit) {
                            hasCardOfSuit = true;
                            if (face == 3) straightHighCard = card; // 5 is high
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
                if (straightHighFace == ACE_FACE) {
                    handRank = HandRank.ROYAL_FLUSH.ordinal();
                } else {
                    handRank = HandRank.STRAIGHT_FLUSH.ordinal();
                }
                
                // For Ace-low straight flush, we need to reorder
                if (straightHighFace < 4) { // Ace-low
                    for (int i = 0; i < 5; i++) {
                        int face = (i == 0 ? 3 : (i == 4 ? ACE_FACE : (3 - i)));
                        for (int j = 0; j < 7; j++) {
                            int card = hand[j];
                            if (((card >>> FACE_SHIFT) & 0xF) == face && 
                                (card & SUIT_MASK) == flushSuit) {
                                bestHand[i] = card;
                                break;
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < 5; i++) {
                        int face = straightHighFace - i;
                        for (int j = 0; j < 7; j++) {
                            int card = hand[j];
                            if (((card >>> FACE_SHIFT) & 0xF) == face && 
                                (card & SUIT_MASK) == flushSuit) {
                                bestHand[i] = card;
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
            for (int i = 0; i < 7; i++) {
                int card = hand[i];
                if (((card >>> FACE_SHIFT) & 0xF) == quadFace) {
                    bestHand[idx++] = card;
                }
            }
            // Add kicker
            for (int i = 0; i < 7 && idx < 5; i++) {
                int card = hand[i];
                if (((card >>> FACE_SHIFT) & 0xF) != quadFace) {
                    bestHand[idx++] = card;
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
                for (int i = 0; i < 7 && idx < 3; i++) {
                    int card = hand[i];
                    if (((card >>> FACE_SHIFT) & 0xF) == tripsFace) {
                        bestHand[idx++] = card;
                    }
                }
                // Add pair (take first 2 cards of that face)
                for (int i = 0; i < 7 && idx < 5; i++) {
                    int card = hand[i];
                    if (((card >>> FACE_SHIFT) & 0xF) == pairFace) {
                        bestHand[idx++] = card;
                        if (idx == 5) break;
                    }
                }
                return HandRank.FULL_HOUSE;
            }
        }
        
        // Check for flush (already know flushSuit != -1 if we get here)
        if (flushSuit != -1) {
            handRank = HandRank.FLUSH.ordinal();
            System.arraycopy(flushSuitCards, 0, bestHand, 0, 5);
            return HandRank.FLUSH;
        }
        
        // Check for straight (already computed)
        if (hasStraight) {
            handRank = HandRank.STRAIGHT.ordinal();
            if (straightHighFace >= 4) { // Normal straight
                int idx = 0;
                for (int i = 0; i < 5; i++) {
                    int face = straightHighFace - i;
                    // Take first card of each face (they're sorted descending)
                    for (int j = 0; j < 7; j++) {
                        int card = hand[j];
                        if (((card >>> FACE_SHIFT) & 0xF) == face) {
                            bestHand[idx++] = card;
                            break;
                        }
                    }
                }
            } else { // Ace-low straight
                int[] faces = {3, 2, 1, 0, ACE_FACE}; // 5,4,3,2,A
                for (int i = 0; i < 5; i++) {
                    int face = faces[i];
                    for (int j = 0; j < 7; j++) {
                        int card = hand[j];
                        if (((card >>> FACE_SHIFT) & 0xF) == face) {
                            bestHand[i] = card;
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
            for (int i = 0; i < 7; i++) {
                int card = hand[i];
                if (((card >>> FACE_SHIFT) & 0xF) == tripsFace) {
                    bestHand[idx++] = card;
                }
            }
            // Add kickers (highest remaining cards)
            for (int i = 0; i < 7 && idx < 5; i++) {
                int card = hand[i];
                if (((card >>> FACE_SHIFT) & 0xF) != tripsFace) {
                    bestHand[idx++] = card;
                    if (idx == 5) break;
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
            for (int i = 0; i < 7 && idx < 2; i++) {
                int card = hand[i];
                if (((card >>> FACE_SHIFT) & 0xF) == pair1Face) {
                    bestHand[idx++] = card;
                }
            }
            // Add second pair
            for (int i = 0; i < 7 && idx < 4; i++) {
                int card = hand[i];
                if (((card >>> FACE_SHIFT) & 0xF) == pair2Face) {
                    bestHand[idx++] = card;
                }
            }
            // Add kicker
            for (int i = 0; i < 7 && idx < 5; i++) {
                int card = hand[i];
                int face = (card >>> FACE_SHIFT) & 0xF;
                if (face != pair1Face && face != pair2Face) {
                    bestHand[idx++] = card;
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
            for (int i = 0; i < 7 && idx < 2; i++) {
                int card = hand[i];
                if (((card >>> FACE_SHIFT) & 0xF) == pair1Face) {
                    bestHand[idx++] = card;
                }
            }
            // Add kickers (highest remaining cards)
            for (int i = 0; i < 7 && idx < 5; i++) {
                int card = hand[i];
                if (((card >>> FACE_SHIFT) & 0xF) != pair1Face) {
                    bestHand[idx++] = card;
                }
            }
            return HandRank.PAIR;
        }
        
        // High card
        handRank = HandRank.HIGH_CARD.ordinal();
        System.arraycopy(hand, 0, bestHand, 0, 5);
        return HandRank.HIGH_CARD;
    }
    
    /**
     * Compare two hands. Returns negative if hand1 < hand2, 
     * positive if hand1 > hand2, 0 if equal.
     */
    public int compareHands(int[] hand1, int[] hand2) {
        evaluate(hand1);
        int rank1 = handRank;
        int[] best1 = bestHand.clone();
        
        evaluate(hand2);
        int rank2 = handRank;
        int[] best2 = bestHand.clone();
        
        // Compare ranks first
        if (rank1 != rank2) {
            return rank2 - rank1; // Higher rank is better
        }
        
        // Same rank, compare card faces
        for (int i = 0; i < 5; i++) {
            int face1 = (best1[i] >>> FACE_SHIFT) & 0xF;
            int face2 = (best2[i] >>> FACE_SHIFT) & 0xF;
            if (face1 != face2) {
                return face2 - face1; // Higher face is better
            }
        }
        
        return 0; // Exactly equal
    }
    
    /**
     * Get the best hand cards (call after evaluate()).
     */
    public int[] getBestHand() {
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
    public static String cardToString(int card) {
        char[] faces = {'2','3','4','5','6','7','8','9','T','J','Q','K','A'};
        char[] suits = {'h','d','c','s'};
        
        int face = (card >>> FACE_SHIFT) & 0xF;
        int suit = card & SUIT_MASK;
        
        return "" + faces[face] + suits[suit];
    }
    
    /**
     * Sort cards by face descending (highest first).
     * Simple insertion sort for small arrays.
     */
    private void sortByFaceDescending(int[] cards) {
        for (int i = 1; i < cards.length; i++) {
            int key = cards[i];
            int j = i - 1;
            
            int keyFace = (key >>> FACE_SHIFT) & 0xF;
            
            while (j >= 0 && ((cards[j] >>> FACE_SHIFT) & 0xF) < keyFace) {
                cards[j + 1] = cards[j];
                j--;
            }
            cards[j + 1] = key;
        }
    }
    
    private void sortByFaceDescending(int[] cards, int count) {
        for (int i = 1; i < count; i++) {
            int key = cards[i];
            int j = i - 1;
            
            int keyFace = (key >>> FACE_SHIFT) & 0xF;
            
            while (j >= 0 && ((cards[j] >>> FACE_SHIFT) & 0xF) < keyFace) {
                cards[j + 1] = cards[j];
                j--;
            }
            cards[j + 1] = key;
        }
    }
    
    /**
     * Convert card from face/suit to int representation.
     */
    public static int makeCard(int face, int suit) {
        return (face << FACE_SHIFT) | (suit & SUIT_MASK);
    }
}