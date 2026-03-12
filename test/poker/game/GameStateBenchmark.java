package poker.game;

import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.annotations.*;
import poker.player.Dealer;
import poker.player.Player;
import poker.strat.DefaultStrategy;
import poker.strat.Strategy;

import java.util.concurrent.TimeUnit;

public class GameStateBenchmark {

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
    public void simulateHand() {
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

        for (Player player : dealer.players) {
            player.refresh();
            player.stack = 1000;
            player.wins = 0;
        }

        GameState.start(dealer);
    }
}
