package smsoft.board.common.snowflake;

import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Snowflake ID 생성기.
 * <p>
 * Twitter Snowflake 알고리즘과 호환되는 고유 ID를 생성합니다.
 * 운영 환경에서의 안정성을 고려하여 시계 역행(Clock Rollback) 처리 및
 * CPU 사용 최적화가 적용되어 있습니다.
 * </p>
 * 
 * <b>참고:</b> 이 클래스는 순수 POJO(Plain Old Java Object)이며 Spring Framework에 의존하지 않습니다.
 */
public class Snowflake {
    private static final long EPOCH_BITS = 41L;
    private static final long NODE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_NODE_ID = (1L << NODE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    // 커스텀 Epoch (2024-01-01 00:00:00 UTC)
    private static final long CUSTOM_EPOCH = 1704067200000L;

    private static final long NODE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + NODE_ID_BITS;

    private final long nodeId;
    private long lastTimeMillis = -1L;
    private long sequence = 0L;

    /**
     * 기본 생성자.
     * 환경 변수 또는 하드웨어 주소(MAC)를 기반으로 Node ID를 자동으로 결정합니다.
     */
    public Snowflake() {
        this(generateNodeId());
    }

    /**
     * Node ID를 명시적으로 지정하는 생성자.
     *
     * @param nodeId 사용할 Node ID (0 ~ 1023)
     */
    public Snowflake(long nodeId) {
        if (nodeId < 0 || nodeId > MAX_NODE_ID) {
            throw new IllegalArgumentException(String.format("Node ID는 %d에서 %d 사이여야 합니다.", 0, MAX_NODE_ID));
        }
        this.nodeId = nodeId;
    }

    /**
     * 다음 고유 ID를 생성합니다.
     *
     * @return 64비트 고유 ID
     */
    public synchronized long nextId() {
        long currentTimestamp = System.currentTimeMillis();

        if (currentTimestamp < lastTimeMillis) {
            long offset = lastTimeMillis - currentTimestamp;
            if (offset < 5) {
                try {
                    // 시계가 따라잡을 때까지 대기
                    Thread.sleep(offset + 1);
                    currentTimestamp = System.currentTimeMillis();
                    
                    if (currentTimestamp < lastTimeMillis) {
                        throw new IllegalStateException(String.format("시스템 시계가 %d밀리초 역행했습니다. ID 생성을 거부합니다.", lastTimeMillis - currentTimestamp));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("시계 역행 대기 중 인터럽트가 발생했습니다.", e);
                }
            } else {
                throw new IllegalStateException(String.format("시스템 시계가 %d밀리초 역행했습니다. ID 생성을 거부합니다.", offset));
            }
        }

        if (currentTimestamp == lastTimeMillis) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimeMillis = currentTimestamp;

        return ((currentTimestamp - CUSTOM_EPOCH) << TIMESTAMP_SHIFT)
                | (nodeId << NODE_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp <= lastTimeMillis) {
            // Busy-wait 동안 CPU 사용 최적화
            Thread.onSpinWait(); 
            currentTimestamp = System.currentTimeMillis();
        }
        return currentTimestamp;
    }

    /**
     * 환경 변수 또는 MAC 주소를 기반으로 Node ID를 생성합니다.
     * 
     * 전략:
     * 1. "NODE_ID" 환경 변수를 확인합니다.
     * 2. 없다면, MAC 주소를 해시(Hash)하여 사용합니다.
     * 3. 0-1023 범위 내에 있도록 마스크를 적용합니다.
     * 
     * @return 생성된 Node ID
     */
    private static long generateNodeId() {
        // 1. 환경 변수 시도
        try {
            String nodeIdEnv = System.getenv("NODE_ID");
            if (nodeIdEnv != null && !nodeIdEnv.trim().isEmpty()) {
                return Long.parseLong(nodeIdEnv.trim()) & MAX_NODE_ID;
            }
        } catch (Exception e) {
            // 잘못된 환경 변수는 무시
        }

        // 2. MAC 주소 해시 시도
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    int hashCode = 0;
                    for (byte b : mac) {
                        hashCode = 31 * hashCode + (b & 0xFF);
                    }
                    return hashCode & MAX_NODE_ID;
                }
            }
        } catch (Exception e) {
            // 네트워크 오류 무시
        }
        
        // 대체 값 (네트워크 인터페이스가 없는 경우)
        // 안전한 기본값으로 0을 반환하지만, 이는 충돌 위험을 내포할 수 있음.
        return 0L;
    }
}
