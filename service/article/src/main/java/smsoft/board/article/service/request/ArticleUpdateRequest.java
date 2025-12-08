package smsoft.board.article.service.request;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ArticleUpdateRequest {
    private String title;
    private String content;
}
