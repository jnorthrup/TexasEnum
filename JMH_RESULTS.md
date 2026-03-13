# JMH Benchmark Results - Refactored Poker Engine

## Environment
- Java: 17.0.7-tem (Eclipse Adoptium)
- JVM: OpenJDK 64-Bit Server VM
- JMH: 1.35
- Benchmark Mode: Throughput (ops/s)

## Results

### GameStateBenchmark.simulateHand (AFTER optimizations)
- **Mode**: thrpt
- **Iterations**: 10
- **Score**: 174,577 ops/s
- **Error**: ± 5,938 ops/s (99.9% CI)
- **Min**: 167,108 ops/s
- **Max**: 180,724 ops/s
- **Threads**: 1
- **Forks**: 1

### GameStateBenchmark.simulateHand (BEFORE optimizations)
- **Score**: 170,042 ops/s

## Performance Summary
- **Improvement**: +4,535 ops/s (+2.7%)
- **Warmup**: 5 iterations, 1s each
- **Measurement**: 10 iterations, 1s each

## Optimizations Applied

### 1. Pre-allocated flush arrays in CardMemory
- Changed from `new int[4][7]` with array creation on each hand
- To `new int[4][12]` with length tracking (`flushLen[4]`)
- Eliminates 4 array allocations per hand

### 2. Eliminated cards.clone() in Play.assess
- Removed `final int[] hand = cards.clone();`
- Work directly on input array (safe because we don't mutate)

### 3. Fisher-Yates shuffle
- Replaced `ArrayList<Integer>` + `Collections.shuffle()`
- With primitive `int[]` + in-place swap

### 4. Cached Face/Suit.values()
- Added `FACE_VALUES[]` and `SUIT_VALUES[]` static finals
- Eliminates enum array allocation per lookup

### 5. Fixed wipe() to actually reset state
- Previously empty method - now resets all fields
- Prevents stale data between hands

### 6. Inline hand detection (bitset-style)
- Replaced sequential enum loop with direct if-checks
- Ordered by hand rank (straight flush → high card)

### 7. Inline wheel detection
- Uses in-place array shift instead of `Arrays.copyOfRange()`
- Avoids allocation on A-2-3-4-5 straights

## Remaining Optimizations (Not Implemented)
- Use 128-byte static buffer for deck+table (BuffUtil.DECK_AND_TABLE exists but not wired)
- Pre-allocate CardMemory per player instead of creating new each hand
- Object pooling for Pair<> return objects
