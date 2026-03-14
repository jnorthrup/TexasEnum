package poker.eval;

import java.nio.IntBuffer;
import java.util.Random;
import java.util.logging.Logger;

import static poker.eval.CardUtil.*;

/**
 * Benchmark comparing original vs optimized hand evaluator.
 */
public class OptimizedBenchmarkTest extends junit.framework.TestCase {
    
    private static final Logger log = Logger.getLogger(OptimizedBenchmarkTest.class.getName());
    
    // Card encoding: face << 16 | suit
    private static final int TWO_H = OptimizedHandEvaluator.makeCard(0, 0);  // 2h
    private static final int THREE_H = OptimizedHandEvaluator.makeCard(1, 0); // 3h
    private static final int FOUR_H = OptimizedHandEvaluator.makeCard(2, 0);  // 4h
    private static final int FIVE_H = OptimizedHandEvaluator.makeCard(3, 0);  // 5h
    private static final int SIX_H = OptimizedHandEvaluator.makeCard(4, 0);   // 6h
    private static final int SEVEN_H = OptimizedHandEvaluator.makeCard(5, 0); // 7h
    private static final int EIGHT_H = OptimizedHandEvaluator.makeCard(6, 0); // 8h
    private static final int NINE_H = OptimizedHandEvaluator.makeCard(7, 0);  // 9h
    private static final int TEN_H = OptimizedHandEvaluator.makeCard(8, 0);   // Th
    private static final int JACK_H = OptimizedHandEvaluator.makeCard(9, 0);  // Jh
    private static final int QUEEN_H = OptimizedHandEvaluator.makeCard(10, 0); // Qh
    private static final int KING_H = OptimizedHandEvaluator.makeCard(11, 0); // Kh
    private static final int ACE_H = OptimizedHandEvaluator.makeCard(12, 0);  // Ah
    
    private static final int TWO_D = OptimizedHandEvaluator.makeCard(0, 1);   // 2d
    private static final int THREE_D = OptimizedHandEvaluator.makeCard(1, 1); // 3d
    private static final int FOUR_D = OptimizedHandEvaluator.makeCard(2, 1);  // 4d
    private static final int FIVE_D = OptimizedHandEvaluator.makeCard(3, 1);  // 5d
    private static final int SIX_D = OptimizedHandEvaluator.makeCard(4, 1);   // 6d
    private static final int SEVEN_D = OptimizedHandEvaluator.makeCard(5, 1); // 7d
    private static final int EIGHT_D = OptimizedHandEvaluator.makeCard(6, 1); // 8d
    private static final int NINE_D = OptimizedHandEvaluator.makeCard(7, 1);  // 9d
    private static final int TEN_D = OptimizedHandEvaluator.makeCard(8, 1);   // Td
    private static final int JACK_D = OptimizedHandEvaluator.makeCard(9, 1);  // Jd
    private static final int QUEEN_D = OptimizedHandEvaluator.makeCard(10, 1); // Qd
    private static final int KING_D = OptimizedHandEvaluator.makeCard(11, 1); // Kd
    private static final int ACE_D = OptimizedHandEvaluator.makeCard(12, 1);  // Ad
    
    private static final int TWO_C = OptimizedHandEvaluator.makeCard(0, 2);   // 2c
    private static final int THREE_C = OptimizedHandEvaluator.makeCard(1, 2); // 3c
    private static final int FOUR_C = OptimizedHandEvaluator.makeCard(2, 2);  // 4c
    private static final int FIVE_C = OptimizedHandEvaluator.makeCard(3, 2);  // 5c
    private static final int SIX_C = OptimizedHandEvaluator.makeCard(4, 2);   // 6c
    private static final int SEVEN_C = OptimizedHandEvaluator.makeCard(5, 2); // 7c
    private static final int EIGHT_C = OptimizedHandEvaluator.makeCard(6, 2); // 8c
    private static final int NINE_C = OptimizedHandEvaluator.makeCard(7, 2);  // 9c
    private static final int TEN_C = OptimizedHandEvaluator.makeCard(8, 2);   // Tc
    private static final int JACK_C = OptimizedHandEvaluator.makeCard(9, 2);  // Jc
    private static final int QUEEN_C = OptimizedHandEvaluator.makeCard(10, 2); // Qc
    private static final int KING_C = OptimizedHandEvaluator.makeCard(11, 2); // Kc
    private static final int ACE_C = OptimizedHandEvaluator.makeCard(12, 2);  // Ac
    
    private static final int TWO_S = OptimizedHandEvaluator.makeCard(0, 3);   // 2s
    private static final int THREE_S = OptimizedHandEvaluator.makeCard(1, 3); // 3s
    private static final int FOUR_S = OptimizedHandEvaluator.makeCard(2, 3);  // 4s
    private static final int FIVE_S = OptimizedHandEvaluator.makeCard(3, 3);  // 5s
    private static final int SIX_S = OptimizedHandEvaluator.makeCard(4, 3);   // 6s
    private static final int SEVEN_S = OptimizedHandEvaluator.makeCard(5, 3); // 7s
    private static final int EIGHT_S = OptimizedHandEvaluator.makeCard(6, 3); // 8s
    private static final int NINE_S = OptimizedHandEvaluator.makeCard(7, 3);  // 9s
    private static final int TEN_S = OptimizedHandEvaluator.makeCard(8, 3);   // Ts
    private static final int JACK_S = OptimizedHandEvaluator.makeCard(9, 3);  // Js
    private static final int QUEEN_S = OptimizedHandEvaluator.makeCard(10, 3); // Qs
    private static final int KING_S = OptimizedHandEvaluator.makeCard(11, 3); // Ks
    private static final int ACE_S = OptimizedHandEvaluator.makeCard(12, 3);  // As
    
    private Random random;
    private OptimizedHandEvaluator evaluator;
    
    public void setUp() {
        random = new Random(42);
        evaluator = new OptimizedHandEvaluator();
    }
    
    public void testRoyalFlush() {
        // Royal flush: Ah Kh Qh Jh Th
        int[] hand = {
            ACE_H, KING_H, QUEEN_H, JACK_H, TEN_H,
            TWO_D, THREE_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.ROYAL_FLUSH, rank);
        
        log.info("Royal flush test passed: " + rank);
    }
    
    public void testStraightFlush() {
        // Straight flush: 9h 8h 7h 6h 5h
        int[] hand = {
            NINE_H, EIGHT_H, SEVEN_H, SIX_H, FIVE_H,
            TWO_D, THREE_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.STRAIGHT_FLUSH, rank);
    }
    
    public void testFourOfAKind() {
        // Four Aces with King kicker
        int[] hand = {
            ACE_H, ACE_D, ACE_C, ACE_S,
            KING_H,
            TWO_D, THREE_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.FOUR_OF_A_KIND, rank);
    }
    
    public void testFullHouse() {
        // Three Kings, two Queens
        int[] hand = {
            KING_H, KING_D, KING_C,
            QUEEN_H, QUEEN_D,
            TWO_D, THREE_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.FULL_HOUSE, rank);
    }
    
    public void testFlush() {
        // Heart flush (not straight)
        int[] hand = {
            ACE_H, KING_H, QUEEN_H, JACK_H, NINE_H,
            TWO_D, THREE_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.FLUSH, rank);
    }
    
    public void testStraight() {
        // Straight: J T 9 8 7 (mixed suits)
        int[] hand = {
            JACK_H, TEN_D, NINE_C, EIGHT_S, SEVEN_H,
            TWO_D, THREE_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.STRAIGHT, rank);
    }
    
    public void testThreeOfAKind() {
        // Three Jacks
        int[] hand = {
            JACK_H, JACK_D, JACK_C,
            ACE_H, KING_H,
            TWO_D, THREE_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.THREE_OF_A_KIND, rank);
    }
    
    public void testTwoPair() {
        // Aces and Kings
        int[] hand = {
            ACE_H, ACE_D,
            KING_H, KING_D,
            QUEEN_H,
            TWO_D, THREE_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.TWO_PAIR, rank);
    }
    
    public void testPair() {
        // Pair of Aces
        int[] hand = {
            ACE_H, ACE_D,
            KING_H, QUEEN_H, JACK_H,
            TWO_D, THREE_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.PAIR, rank);
    }
    
    public void testHighCard() {
        // High card Ace (mixed suits to avoid flush)
        int[] hand = {
            ACE_H, KING_D, QUEEN_C, JACK_S, NINE_H,
            TWO_D, THREE_C
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.HIGH_CARD, rank);
    }
    
    public void testAceLowStraight() {
        // Ace-low straight: A 2 3 4 5
        int[] hand = {
            ACE_H, TWO_D, THREE_C, FOUR_S, FIVE_H,
            KING_D, QUEEN_D
        };
        
        OptimizedHandEvaluator.HandRank rank = evaluator.evaluate(hand);
        assertEquals(OptimizedHandEvaluator.HandRank.STRAIGHT, rank);
    }
    
    public void testCompareHands() {
        // Pair of Aces vs Pair of Kings
        int[] hand1 = {
            ACE_H, ACE_D,
            QUEEN_C, JACK_S, TEN_H,
            EIGHT_D, NINE_D
        };
        
        int[] hand2 = {
            KING_H, KING_D,
            ACE_C, QUEEN_H, JACK_D,
            TEN_D, NINE_C
        };
        
        int result = evaluator.compareHands(hand1, hand2);
        assertTrue("Aces should beat Kings", result > 0);
    }
    
    public void testBenchmark() {
        int iterations = 1000000;
        
        // Generate random hands
        int[][] randomHands = new int[iterations][7];
        for (int i = 0; i < iterations; i++) {
            randomHands[i] = generateRandomHand();
        }
        
        // Benchmark optimized evaluator
        long startOpt = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate(randomHands[i]);
        }
        long timeOpt = System.currentTimeMillis() - startOpt;
        
        double handsPerSecondOpt = iterations / (timeOpt / 1000.0);
        
        log.info("Optimized evaluator: " + iterations + " hands in " + timeOpt + "ms = " + 
                 String.format("%.0f", handsPerSecondOpt) + " hands/sec");
        
        // Compare with original baseline (116,822 hands/sec)
        log.info("Original baseline: 116,822 hands/sec");
        log.info("Speedup: " + String.format("%.1fx", handsPerSecondOpt / 116822.0));
        
        assertTrue("Should be faster than baseline", handsPerSecondOpt > 116822);
    }
    
    public void testDeepBenchmark() {
        int warmup = 100000;
        int iterations = 5000000; // 5 million
        
        // Warm up JIT
        log.info("Warming up...");
        for (int i = 0; i < warmup; i++) {
            evaluator.evaluate(generateRandomHand());
        }
        
        // Generate test hands
        log.info("Generating " + iterations + " random hands...");
        int[][] randomHands = new int[iterations][7];
        for (int i = 0; i < iterations; i++) {
            randomHands[i] = generateRandomHand();
        }
        
        // Run benchmark
        log.info("Running benchmark...");
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            evaluator.evaluate(randomHands[i]);
        }
        long end = System.nanoTime();
        
        double timeMs = (end - start) / 1_000_000.0;
        double handsPerSecond = iterations / (timeMs / 1000.0);
        
        log.info("Optimized: " + iterations + " hands in " + 
                 String.format("%.1f", timeMs) + "ms = " + 
                 String.format("%.0f", handsPerSecond) + " hands/sec");
        log.info("Original: 116,822 hands/sec");
        log.info("Speedup: " + String.format("%.1fx", handsPerSecond / 116822.0));
        
        // Target: 10x speedup
        assertTrue("Should achieve significant speedup", handsPerSecond > 116822 * 5);
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
    
    public static void main(String[] args) {
        OptimizedBenchmarkTest test = new OptimizedBenchmarkTest();
        test.setUp();
        
        System.out.println("Running OptimizedHandEvaluator benchmarks...");
        
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