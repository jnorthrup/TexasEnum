package poker.game;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.TimeValue;
import poker.player.Dealer;
import poker.player.Player;
import poker.strat.DefaultStrategy;
import poker.strat.Strategy;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class GameStateBenchmark {

    private Strategy strategy;

    @Setup(Level.Trial)
    public void setUp() {
        strategy = new DefaultStrategy();
        GameState.test = false;
        Dealer.test = false;
    }

    @Benchmark
    public void simulateHand(Blackhole bh) {
        Dealer dealer = new Dealer();
        
        for (int i = 0; i < 6; i++) {
            Player player = new Player();
            player.strategy = strategy;
            dealer.players.add(player);
        }

        dealer.lowerLimit = 5.0;
        dealer.deck.shuffle();
        
        GameState.start(dealer);
        
        bh.consume(dealer);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.runner.options.Options options = new OptionsBuilder()
                .include(GameStateBenchmark.class.getName())
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
}
