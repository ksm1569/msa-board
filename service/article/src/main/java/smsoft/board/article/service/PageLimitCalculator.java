package smsoft.board.article.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageLimitCalculator {
    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount) {
        // 현재 페이지 기준으로 이동 가능한 최대 페이지 범위를 계산하여 limit 설정
        return (((page - 1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }
}
