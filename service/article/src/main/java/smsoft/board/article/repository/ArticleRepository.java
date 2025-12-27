package smsoft.board.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import smsoft.board.article.entity.Article;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // 커버링 인덱스를 활용한 페이징 최적화 쿼리 (Article 테이블의 모든 컬럼을 조회하지 않고 ID만 먼저 조회 후 조인)
    @Query(
            value = "select article.article_id, article.title, article.content, article.board_id, article.writer_id, " +
                    "article.created_at, article.modified_at " +
                    "from (" +
                    "   select article_id from article " +
                    "   where board_id = :boardId " +
                    "   order by article_id desc " +
                    "   limit :limit offset :offset " +
                    ") t left join article on article.article_id = t.article_id",
            nativeQuery = true
    )
    List<Article> findAll(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit,
            @Param("offset") Long offset
    );

    // 전체 카운트 쿼리 최적화 (limit을 걸어서 불필요한 전체 스캔 방지)
    @Query(
        value = "select count(*) from (" +
                "   select article_id from article " +
                "   where board_id = :boardId " +
                "   limit :limit " +
                ") t",
        nativeQuery = true
    )
    Long count(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit
    );

}
