# JMH Benchmark Results - Refactored Poker Engine

## Environment
- Java: 25.0.2-tem (Eclipse Adoptium)
- JVM: OpenJDK 64-Bit Server VM
- JMH: 1.35
- Benchmark Mode: Throughput (ops/s)

## Results

### GameStateBenchmark.simulateHand
- **Mode**: thrpt
- **Iterations**: 10
- **Score**: 170,042.222 ops/s
- **Error**: ± 5,487.780 ops/s (99.9% CI)
- **Min**: 163,265.942 ops/s
- **Max**: 174,883.316 ops/s

### Performance Characteristics
- **Warmup**: 5 iterations, 1s each
- **Measurement**: 10 iterations, 1s each
- **Threads**: 1
- **Forks**: 0 (non-forked run for debugging)

## Refactoring Details

### Changes Made
1. **Removed NIO**: Eliminated all `java.nio.*` imports and `IntBuffer`/`ByteBuffer` usage
2. **Primitive Arrays**: Replaced with `int[]` and `byte[]` for zero-cost abstractions
3. **1-byte Card Encoding**: Cards now use `(face << 2) | suit` (6 bits per card)
4. **Cache Alignment**: 128-byte buffer aligns with 2 cache lines (64 bytes each)
5. **Zero-Cost Abstractions**: Direct array access eliminates wrapper overhead

### Expected vs Actual Performance
- **Expected**: 3-4x improvement over NIO-based implementation
- **Actual**: 170,042 ops/s benchmark score
- **Notes**: Non-forked JMH runs may have JVM warmup artifacts; for production, use forked runs

## Code Quality
- All compilation errors fixed
- Test files updated to use primitive arrays
- Benchmark runs successfully without exceptions
