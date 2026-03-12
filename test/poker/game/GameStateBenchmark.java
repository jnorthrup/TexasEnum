package poker.game;

import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import poker.player.Dealer;
import poker.player.Player;
import poker.strat.DefaultStrategy;
import poker.strat.Strategy;

import java.util.concurrent.TimeUnit;

public class GameStateBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        public Dealer dealer;
        public Strategy strategy;
        
        @Setup(Level.Iteration)
        public void setup() {
            strategy = new DefaultStrategy();
            GameState.test = false;
            Dealer.test = false;
        }
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.runner.options.Options options = new OptionsBuilder()
                .include("poker.game.GameStateBenchmark")
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupIterations(5)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(10)
                .measurementTime(TimeValue.seconds(1))
                .forks(0)
                .threads(1)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public void simulateHand(BenchmarkState state, Blackhole bh) {
        Dealer dealer = new Dealer();
        
        for (int i = 0; i < 6; i++) {
            Player player = new Player();
            player.strategy = state.strategy;
            dealer.players.add(player);
        }

        dealer.lowerLimit = 5.0;
        dealer.deck.shuffle();
        
        GameState.start(dealer);
        
        bh.consume(dealer);
    }
}
