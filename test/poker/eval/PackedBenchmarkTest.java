package poker.eval;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Benchmark comparing packed vs non-packed hand evaluator.
 */
public class PackedBenchmarkTest extends junit.framework.TestCase {
    
    private static final Logger log = Logger.getLogger(PackedBenchmarkTest.class.getName());
    
    private Random random;
    private PackedHandEvaluator packedEvaluator;
    private OptimizedHandEvaluator optimizedEvaluator;
    
    public void setUp() {
        random = new Random(42);
        packedEvaluator = new PackedHandEvaluator();
        optimizedEvaluator = new OptimizedHandEvaluator();
    }
    
    public void testRoyalFlush() {
        // Royal flush: Ah Kh Qh Jh Th
        long packed = packRoyalFlush();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.ROYAL_FLUSH, rank);
    }
    
    public void testStraightFlush() {
        // Straight flush: 9h 8h 7h 6h 5h
        long packed = packStraightFlush();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.STRAIGHT_FLUSH, rank);
    }
    
    public void testFourOfAKind() {
        // Four Aces with King kicker
        long packed = packFourOfAKind();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.FOUR_OF_A_KIND, rank);
    }
    
    public void testFullHouse() {
        // Three Kings, two Queens
        long packed = packFullHouse();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.FULL_HOUSE, rank);
    }
    
    public void testFlush() {
        // Heart flush (not straight)
        long packed = packFlush();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.FLUSH, rank);
    }
    
    public void testStraight() {
        // Straight: J T 9 8 7 (mixed suits)
        long packed = packStraight();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.STRAIGHT, rank);
    }
    
    public void testThreeOfAKind() {
        // Three Jacks
        long packed = packThreeOfAKind();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.THREE_OF_A_KIND, rank);
    }
    
    public void testTwoPair() {
        // Aces and Kings
        long packed = packTwoPair();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.TWO_PAIR, rank);
    }
    
    public void testPair() {
        // Pair of Aces
        long packed = packPair();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.PAIR, rank);
    }
    
    public void testHighCard() {
        // High card Ace
        long packed = packHighCard();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.HIGH_CARD, rank);
    }
    
    public void testAceLowStraight() {
        // Ace-low straight: A 2 3 4 5
        long packed = packAceLowStraight();
        PackedHandEvaluator.HandRank rank = packedEvaluator.evaluate(packed);
        assertEquals(PackedHandEvaluator.HandRank.STRAIGHT, rank);
    }
    
    public void testCompareHands() {
        // Pair of Aces vs Pair of Kings
        long hand1 = packPairOfAces();
        long hand2 = packPairOfKings();
        
        int result = packedEvaluator.compareHands(hand1, hand2);
        assertTrue("Aces should beat Kings", result > 0);
    }
    
    public void testBenchmark() {
        int iterations = 1000000;
        
        // Generate random hands
        long[] randomPackedHands = new long[iterations];
        int[][] randomHands = new int[iterations][7];
        
        for (int i = 0; i < iterations; i++) {
            randomHands[i] = generateRandomHand();
            randomPackedHands[i] = packHand(randomHands[i]);
        }
        
        // Benchmark packed evaluator
        long startPacked = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            packedEvaluator.evaluate(randomPackedHands[i]);
        }
        long timePacked = System.currentTimeMillis() - startPacked;
        
        double handsPerSecondPacked = iterations / (timePacked / 1000.0);
        
        // Benchmark optimized evaluator
        long startOptimized = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            optimizedEvaluator.evaluate(randomHands[i]);
        }
        long timeOptimized = System.currentTimeMillis() - startOptimized;
        
        double handsPerSecondOptimized = iterations / (timeOptimized / 1000.0);
        
        log.info("Packed evaluator: " + iterations + " hands in " + timePacked + "ms = " + 
                 String.format("%.0f", handsPerSecondPacked) + " hands/sec");
        log.info("Optimized evaluator: " + iterations + " hands in " + timeOptimized + "ms = " + 
                 String.format("%.0f", handsPerSecondOptimized) + " hands/sec");
        log.info("Packed speedup: " + String.format("%.1fx", handsPerSecondPacked / handsPerSecondOptimized));
        
        // Compare with original baseline (116,822 hands/sec)
        log.info("Original baseline: 116,822 hands/sec");
        log.info("Packed vs original: " + String.format("%.1fx", handsPerSecondPacked / 116822.0));
        
        assertTrue("Packed should be faster", handsPerSecondPacked > handsPerSecondOptimized);
    }
    
    public void testDeepBenchmark() {
        int warmup = 100000;
        int iterations = 5000000; // 5 million
        
        // Warm up JIT
        log.info("Warming up...");
        for (int i = 0; i < warmup; i++) {
            packedEvaluator.evaluate(packRandomHand());
        }
        
        // Generate test hands
        log.info("Generating " + iterations + " random hands...");
        long[] randomPackedHands = new long[iterations];
        int[][] randomHands = new int[iterations][7];
        
        for (int i = 0; i < iterations; i++) {
            randomHands[i] = generateRandomHand();
            randomPackedHands[i] = packHand(randomHands[i]);
        }
        
        // Run benchmark
        log.info("Running benchmark...");
        
        // Packed benchmark
        long startPacked = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            packedEvaluator.evaluate(randomPackedHands[i]);
        }
        long endPacked = System.nanoTime();
        
        double timeMsPacked = (endPacked - startPacked) / 1_000_000.0;
        double handsPerSecondPacked = iterations / (timeMsPacked / 1000.0);
        
        // Optimized benchmark
        long startOptimized = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            optimizedEvaluator.evaluate(randomHands[i]);
        }
        long endOptimized = System.nanoTime();
        
        double timeMsOptimized = (endOptimized - startOptimized) / 1_000_000.0;
        double handsPerSecondOptimized = iterations / (timeMsOptimized / 1000.0);
        
        log.info("Packed: " + iterations + " hands in " + 
                 String.format("%.1f", timeMsPacked) + "ms = " + 
                 String.format("%.0f", handsPerSecondPacked) + " hands/sec");
        log.info("Optimized: " + iterations + " hands in " + 
                 String.format("%.1f", timeMsOptimized) + "ms = " + 
                 String.format("%.0f", handsPerSecondOptimized) + " hands/sec");
        log.info("Packed speedup: " + String.format("%.1fx", handsPerSecondPacked / handsPerSecondOptimized));
        log.info("Packed vs original: " + String.format("%.1fx", handsPerSecondPacked / 116822.0));
        
        // Target: 10x speedup
        assertTrue("Should achieve significant speedup", handsPerSecondPacked > 116822 * 10);
    }
    
    private long packRoyalFlush() {
        // Ah Kh Qh Jh Th (aces high, all hearts)
        // Card 0: Ah, Card 1: Kh, etc.
        // Pack: bits [0,3] = suit, bits [4,7] = face per card
        long c0 = packCard(12, 0); // Ah
        long c1 = packCard(11, 0); // Kh
        long c2 = packCard(10, 0); // Qh
        long c3 = packCard(9, 0);  // Jh
        long c4 = packCard(8, 0);  // Th
        long c5 = packCard(7, 0);  // 9h
        long c6 = packCard(6, 0);  // 8h
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packStraightFlush() {
        // 9h 8h 7h 6h 5h (straight flush)
        long c0 = packCard(7, 0);  // 9h
        long c1 = packCard(6, 0);  // 8h
        long c2 = packCard(5, 0);  // 7h
        long c3 = packCard(4, 0);  // 6h
        long c4 = packCard(3, 0);  // 5h
        long c5 = packCard(12, 1); // Ad (kicker)
        long c6 = packCard(11, 1); // Kd (kicker)
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packFourOfAKind() {
        // Ah Ad Ac As (Aces) + Kh Kd
        long c0 = packCard(12, 0); // Ah
        long c1 = packCard(12, 1); // Ad
        long c2 = packCard(12, 2); // Ac
        long c3 = packCard(12, 3); // As
        long c4 = packCard(11, 0); // Kh
        long c5 = packCard(11, 1); // Kd
        long c6 = packCard(10, 0); // Qh
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packFullHouse() {
        // Kh Kd Kc (Kings) + Qh Qd As
        long c0 = packCard(11, 0); // Kh
        long c1 = packCard(11, 1); // Kd
        long c2 = packCard(11, 2); // Kc
        long c3 = packCard(10, 0); // Qh
        long c4 = packCard(10, 1); // Qd
        long c5 = packCard(12, 0); // Ah
        long c6 = packCard(12, 1); // Ad
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packFlush() {
        // Ah Kh Qh Jh 9h (heart flush)
        long c0 = packCard(12, 0); // Ah
        long c1 = packCard(11, 0); // Kh
        long c2 = packCard(10, 0); // Qh
        long c3 = packCard(9, 0);  // Jh
        long c4 = packCard(7, 0);  // 9h
        long c5 = packCard(11, 1); // Kd
        long c6 = packCard(10, 1); // Qd
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packStraight() {
        // J T 9 8 7 (mixed suits, not flush)
        long c0 = packCard(9, 0);  // Jh
        long c1 = packCard(8, 1);  // Td
        long c2 = packCard(7, 2);  // 9c
        long c3 = packCard(6, 3);  // 8s
        long c4 = packCard(5, 0);  // 7h
        long c5 = packCard(12, 1); // Ad
        long c6 = packCard(11, 2); // Kc
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packThreeOfAKind() {
        // Jh Jd Jc (Jacks) + Ah Kd
        long c0 = packCard(9, 0);  // Jh
        long c1 = packCard(9, 1);  // Jd
        long c2 = packCard(9, 2);  // Jc
        long c3 = packCard(12, 0); // Ah
        long c4 = packCard(11, 1); // Kd
        long c5 = packCard(10, 2); // Qc
        long c6 = packCard(8, 0);  // Th
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packTwoPair() {
        // Ah Ad (Aces) + Kh Kd (Kings) + Qh
        long c0 = packCard(12, 0); // Ah
        long c1 = packCard(12, 1); // Ad
        long c2 = packCard(11, 0); // Kh
        long c3 = packCard(11, 1); // Kd
        long c4 = packCard(10, 0); // Qh
        long c5 = packCard(9, 0);  // Jh
        long c6 = packCard(8, 0);  // Th
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packPair() {
        // Ah Ad (Aces) + Kh Qh Jh 9h (not flush)
        long c0 = packCard(12, 0); // Ah
        long c1 = packCard(12, 1); // Ad
        long c2 = packCard(11, 1); // Kd
        long c3 = packCard(10, 2); // Qc
        long c4 = packCard(9, 0);  // Jh
        long c5 = packCard(7, 1);  // 9d
        long c6 = packCard(6, 2);  // 8c
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packHighCard() {
        // Ah Kd Qc Js 9h (mixed suits, no pair)
        long c0 = packCard(12, 0); // Ah
        long c1 = packCard(11, 1); // Kd
        long c2 = packCard(10, 2); // Qc
        long c3 = packCard(9, 3);  // Js
        long c4 = packCard(7, 0);  // 9h
        long c5 = packCard(6, 1);  // 8d
        long c6 = packCard(5, 2);  // 7c
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packAceLowStraight() {
        // Ah 2d 3c 4s 5h (Ace-low straight)
        long c0 = packCard(12, 0); // Ah
        long c1 = packCard(0, 1);  // 2d
        long c2 = packCard(1, 2);  // 3c
        long c3 = packCard(2, 3);  // 4s
        long c4 = packCard(3, 0);  // 5h
        long c5 = packCard(11, 1); // Kd
        long c6 = packCard(10, 2); // Qc
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packPairOfAces() {
        // Ah Ad + Kd Qc Jh 9h
        long c0 = packCard(12, 0); // Ah
        long c1 = packCard(12, 1); // Ad
        long c2 = packCard(11, 1); // Kd
        long c3 = packCard(10, 2); // Qc
        long c4 = packCard(9, 3);  // Js
        long c5 = packCard(7, 0);  // 9h
        long c6 = packCard(6, 1);  // 8d
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private long packPairOfKings() {
        // Kh Kd + Ah Qc Jh 9h
        long c0 = packCard(11, 0); // Kh
        long c1 = packCard(11, 1); // Kd
        long c2 = packCard(12, 0); // Ah
        long c3 = packCard(10, 2); // Qc
        long c4 = packCard(9, 3);  // Js
        long c5 = packCard(7, 0);  // 9h
        long c6 = packCard(6, 1);  // 8d
        return c0 | (c1 << 6) | (c2 << 12) | (c3 << 18) | (c4 << 24) | (c5 << 30) | (c6 << 36);
    }
    
    private int[] generateRandomHand() {
        int[] hand = new int[7];
        boolean[] used = new boolean[52];
        
        for (int i = 0; i < 7; i++) {
            int card;
            do {
                card = random.nextInt(52);
            } while (used[card]);
            
            used[card] = true;
            // Convert 0-51 to card encoding: face << 16 | suit
            int face = card / 4;
            int suit = card % 4;
            hand[i] = OptimizedHandEvaluator.makeCard(face, suit);
        }
        
        return hand;
    }
    
    private long packRandomHand() {
        int[] hand = generateRandomHand();
        return packHand(hand);
    }
    
    private long packHand(int[] hand) {
        long packed = 0;
        for (int i = 0; i < 7; i++) {
            int card = hand[i];
            int face = (card >>> 16) & 0xF;
            int suit = card & 0x3;
            // Pack: bits [0,3] = suit, bits [4,7] = face
            packed |= ((long)suit << (i * 6)) | ((long)face << (i * 6 + 4));
        }
        return packed;
    }
    
    private static long packCard(int face, int suit) {
        // Pack: bits [0,3] = suit, bits [4,7] = face
        return ((long)(face & 0xF) << 4) | (suit & 0x3);
    }
    
    public static void main(String[] args) {
        PackedBenchmarkTest test = new PackedBenchmarkTest();
        test.setUp();
        
        System.out.println("Running PackedHandEvaluator benchmarks...");
        
        try {
            test.testRoyalFlush();
            test.testStraightFlush();
            test.testFourOfAKind();
            test.testFullHouse();
            test.testFlush();
            test.testStraight();
            test.testThreeOfAKind();
            test.testTwoPair();
            test.testPair();
            test.testHighCard();
            test.testAceLowStraight();
            test.testCompareHands();
            
            System.out.println("All correctness tests passed.");
            
            // Run benchmark
            test.testBenchmark();
            test.testDeepBenchmark();
            
        } catch (AssertionError e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}