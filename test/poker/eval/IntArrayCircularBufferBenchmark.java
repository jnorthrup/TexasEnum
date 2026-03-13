package poker.eval;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class IntArrayCircularBufferBenchmark {

    private static final int CAPACITY = 1000000;

    private IntArrayCircularBuffer intArrayBuffer;
    private IntBuffer intBuffer;
    private int[] intArray;

    @Setup(Level.Trial)
    public void setUp() {
        intArrayBuffer = new IntArrayCircularBuffer(CAPACITY);
        intBuffer = IntBuffer.allocate(CAPACITY);
        intArray = new int[CAPACITY];
    }

    @Benchmark
    public void intArrayOffer(Blackhole bh) {
        IntArrayCircularBuffer buf = new IntArrayCircularBuffer(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.offer(i);
        }
        bh.consume(buf);
    }

    @Benchmark
    public void intBufferOffer(Blackhole bh) {
        IntBuffer buf = IntBuffer.allocate(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.put(i);
        }
        bh.consume(buf);
    }

    @Benchmark
    public void intArrayPoll(Blackhole bh) {
        IntArrayCircularBuffer buf = new IntArrayCircularBuffer(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.offer(i);
        }
        int sum = 0;
        while (!buf.isEmpty()) {
            sum += buf.poll();
        }
        bh.consume(sum);
    }

    @Benchmark
    public void intBufferPoll(Blackhole bh) {
        IntBuffer buf = IntBuffer.allocate(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.put(i);
        }
        buf.flip();
        int sum = 0;
        while (buf.hasRemaining()) {
            sum += buf.get();
        }
        bh.consume(sum);
    }

    @Benchmark
    public void intArrayPeek(Blackhole bh) {
        IntArrayCircularBuffer buf = new IntArrayCircularBuffer(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.offer(i);
        }
        int sum = 0;
        for (int i = 0; i < CAPACITY; i++) {
            sum += buf.peek();
            buf.poll();
            buf.offer(i);
        }
        bh.consume(sum);
    }

    @Benchmark
    public void intBufferPeek(Blackhole bh) {
        IntBuffer buf = IntBuffer.allocate(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.put(i);
        }
        int pos = buf.position();
        int sum = 0;
        for (int i = 0; i < CAPACITY; i++) {
            buf.position(0);
            sum += buf.get();
        }
        bh.consume(sum);
    }

    @Benchmark
    public void intArrayGet(Blackhole bh) {
        IntArrayCircularBuffer buf = new IntArrayCircularBuffer(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.offer(i);
        }
        int sum = 0;
        for (int i = 0; i < CAPACITY; i++) {
            sum += buf.get(i);
        }
        bh.consume(sum);
    }

    @Benchmark
    public void intBufferGet(Blackhole bh) {
        IntBuffer buf = IntBuffer.allocate(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.put(i);
        }
        int sum = 0;
        for (int i = 0; i < CAPACITY; i++) {
            sum += buf.get(i);
        }
        bh.consume(sum);
    }

    @Benchmark
    public void intArrayToArray(Blackhole bh) {
        IntArrayCircularBuffer buf = new IntArrayCircularBuffer(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.offer(i);
        }
        bh.consume(buf.toArray());
    }

    @Benchmark
    public void intBufferToArray(Blackhole bh) {
        IntBuffer buf = IntBuffer.allocate(CAPACITY);
        for (int i = 0; i < CAPACITY; i++) {
            buf.put(i);
        }
        bh.consume(buf.array());
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.runner.options.Options options = new OptionsBuilder()
                .include(IntArrayCircularBufferBenchmark.class.getName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(2))
                .measurementIterations(5)
                .measurementTime(TimeValue.seconds(2))
                .forks(1)
                .threads(1)
                .build();

        new Runner(options).run();
    }
}
