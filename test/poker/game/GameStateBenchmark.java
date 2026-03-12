package poker.game;

import poker.player.Dealer;
import poker.player.Player;
import poker.strat.DefaultStrategy;
import poker.strat.Strategy;

public class GameStateBenchmark {

    public static void main(String[] args) {
        // Warmup
        for (int i = 0; i < 10; i++) {
            simulateHand();
        }
        
        // Benchmark
        long start = System.nanoTime();
        int iterations = 1_000_000;
        for (int i = 0; i < iterations; i++) {
            simulateHand();
        }
        long end = System.nanoTime();
        
        double seconds = (end - start) / 1e9;
        double opsPerSec = iterations / seconds;
        
        System.out.printf("%,.0f hands/sec%n", opsPerSec);
    }
    
    public static void simulateHand() {
        Dealer dealer = new Dealer();
        Strategy strategy = new DefaultStrategy();
        
        for (int i = 0; i < 6; i++) {
            Player player = new Player();
            player.strategy = strategy;
            dealer.players.add(player);
        }
        
        GameState.test = false;
        Dealer.test = false;
        
        dealer.lowerLimit = 5.0;
        dealer.deck.shuffle();
        
        GameState.start(dealer);
    }
}
