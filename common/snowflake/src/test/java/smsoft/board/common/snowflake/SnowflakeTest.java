package smsoft.board.common.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Snowflake ID 생성기 테스트")
class SnowflakeTest {

    @Test
    @DisplayName("고유 ID 생성 테스트: 중복된 ID가 생성되지 않아야 한다")
    void testUniqueIds() {
        Snowflake snowflake = new Snowflake(1);
        Set<Long> ids = new HashSet<>();
        int count = 10000;

        for (int i = 0; i < count; i++) {
            long id = snowflake.nextId();
            boolean added = ids.add(id);
            assertTrue(added, "중복된 ID가 생성되었습니다: " + id);
        }

        assertEquals(count, ids.size(), "생성된 모든 ID는 고유해야 합니다");
    }

    @Test
    @DisplayName("ID 증가 테스트: 생성된 ID는 이전 ID보다 커야 한다")
    void testIncreasingIds() {
        Snowflake snowflake = new Snowflake(1);
        long lastId = 0;
        for (int i = 0; i < 1000; i++) {
            long id = snowflake.nextId();
            assertTrue(id > lastId, "ID는 계속 증가해야 합니다. 이전: " + lastId + ", 현재: " + id);
            lastId = id;
        }
    }

    @Test
    @DisplayName("생성자 검증 테스트: Node ID 범위를 벗어나면 예외가 발생해야 한다")
    void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Snowflake(-1));
        // 최대 Node ID는 1023 (10비트)
        assertThrows(IllegalArgumentException.class, () -> new Snowflake(1024));
        
        assertDoesNotThrow(() -> new Snowflake(0));
        assertDoesNotThrow(() -> new Snowflake(1023));
    }
    
    @Test
    @DisplayName("기본 생성자 테스트: 기본 생성자로 정상적으로 ID가 생성되어야 한다")
    void testDefaultConstructor() {
        Snowflake snowflake = new Snowflake();
        long id = snowflake.nextId();
        assertTrue(id > 0);
    }

    @Test
    @DisplayName("단일 스레드 성능 테스트: 100만 개 ID 생성 속도 측정")
    void testSingleThreadPerformance() {
        Snowflake snowflake = new Snowflake(1);
        int iterations = 1_000_000;
        
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            snowflake.nextId();
        }
        long end = System.nanoTime();
        
        double durationMs = (end - start) / 1_000_000.0;
        double tps = iterations / (durationMs / 1000.0);
        
        System.out.printf("[단일 스레드] 1,000,000개 생성 소요 시간: %.2f ms, 처리량: %.0f TPS%n", durationMs, tps);
        
        // 최소한의 성능 기준 (환경에 따라 다르지만, 일반적인 PC에서 충분히 달성 가능한 수치)
        assertTrue(tps > 100_000, "TPS가 너무 낮습니다. (최소 100,000 TPS 필요)");
    }

    @Test
    @DisplayName("멀티 스레드 성능 테스트: 10개 스레드에서 총 100만 개 ID 생성")
    void testMultiThreadPerformance() throws InterruptedException {
        int threadCount = 10;
        int requestsPerThread = 100_000;
        int totalRequests = threadCount * requestsPerThread;
        
        Snowflake snowflake = new Snowflake(1);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long start = System.nanoTime();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        snowflake.nextId();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        long end = System.nanoTime();
        
        assertTrue(completed, "테스트가 제한 시간 내에 완료되지 않았습니다.");
        
        double durationMs = (end - start) / 1_000_000.0;
        double tps = totalRequests / (durationMs / 1000.0);
        
        System.out.printf("[멀티 스레드] %d개 스레드, 총 %d개 생성 소요 시간: %.2f ms, 처리량: %.0f TPS%n", 
                threadCount, totalRequests, durationMs, tps);
                
        executor.shutdown();
    }
}