package smsoft.board.article.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smsoft.board.article.entity.Article;
import smsoft.board.article.repository.ArticleRepository;
import smsoft.board.article.service.request.ArticleCreateRequest;
import smsoft.board.article.service.request.ArticleUpdateRequest;
import smsoft.board.article.service.response.ArticlePageResponse;
import smsoft.board.article.service.response.ArticleResponse;
import smsoft.board.common.snowflake.Snowflake;

@RequiredArgsConstructor
@Service
public class ArticleService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository articleRepository;

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article article = articleRepository.save(
                Article.create(
                        snowflake.nextId(),
                        request.getTitle(),
                        request.getContent(),
                        request.getBoardId(),
                        request.getWriterId()
                )
        );

        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());

        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        articleRepository.deleteById(articleId);
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticlePageResponse.of(
                articleRepository.findAll(boardId, pageSize, (page - 1) * pageSize).stream()
                        .map(ArticleResponse::from)
                        .toList(),
                // 총 게시글 수 계산 (이동 가능한 페이지 수에 따라 제한된 카운트 조회)
                articleRepository.count(
                        boardId,
                        PageLimitCalculator.calculatePageLimit(page, pageSize, 10L)
                )
        );
    }
}
