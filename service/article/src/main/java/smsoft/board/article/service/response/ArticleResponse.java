package smsoft.board.article.service.response;

import lombok.Getter;
import lombok.ToString;
import smsoft.board.article.entity.Article;

import java.time.LocalDateTime;

@ToString
@Getter
public class ArticleResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ArticleResponse from(Article article) {
        ArticleResponse articleResponse = new ArticleResponse();
        articleResponse.articleId = article.getArticleId();
        articleResponse.title = article.getTitle();
        articleResponse.content = article.getContent();
        articleResponse.writerId = article.getWriterId();
        articleResponse.createdAt = article.getCreatedAt();
        articleResponse.modifiedAt = article.getModifiedAt();

        return articleResponse;
    }
}
