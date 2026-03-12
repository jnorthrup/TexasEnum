package poker.game;

import org.openjdk.jmh.annotations.*;
import poker.player.Dealer;
import poker.player.Player;
import poker.strat.DefaultStrategy;
import poker.strat.Strategy;

import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for simulating a full poker game using JMH.
 * Measures throughput in hands per second.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
public class GameStateBenchmark {

    private Dealer dealer;
    private Strategy strategy;

    @Setup(Level.Trial)
    public void setUp() {
        // Create dealer with 6 players using DefaultStrategy
        dealer = new Dealer();
        strategy = new DefaultStrategy();
        
        for (int i = 0; i < 6; i++) {
            Player player = new Player();
            player.strategy = strategy;
            dealer.players.add(player);
        }
        
        // Disable test logging for performance benchmarking
        GameState.test = false;
        Dealer.test = false;
        
        // Set initial dealer state
        dealer.lowerLimit = 5.0; // For blinds: small blind = lowerLimit/2, big blind = lowerLimit
    }

    @Benchmark
    public void simulateHand() {
        // Reset dealer state for each hand
        dealer.deck.shuffle();
        dealer.pot = 0;
        dealer.blind = 5;
        dealer.bet = 5;
        dealer.button = null;
        dealer.leadin = null;
        dealer.marker = 0;
        dealer.lastCalled = 0;
        dealer.isCalled = false;
        dealer.canCheck = false;
        dealer.canCall = false;
        dealer.smallBlind = 0;
        dealer.bigBlind = 0;
        dealer.bettingRound = 0;
        
        // Reset player state for each hand
        for (Player player : dealer.players) {
            player.refresh();
            player.stack = 1000; // Reset stack to initial value
            player.wins = 0;
            // Note: lastPlayed is package-private and shot is private, 
            // but refresh() should handle resetting internal state
        }
        
        // Simulate one hand
        GameState.start(dealer);
    }

    /**
     * Main method to run the benchmark.
     * @param args command line arguments
     * @throws Exception if benchmark execution fails
     */
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}