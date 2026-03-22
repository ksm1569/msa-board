package smsoft.board.article.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import smsoft.board.article.service.response.ArticlePageResponse;
import smsoft.board.article.service.response.ArticleResponse;

import java.util.List;

public class ArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:8000");

    @Test
    @DisplayName("게시글 생성 API 테스트")
    void createTest() {
        ArticleResponse articleResponse = create(new ArticleCreateRequest(
                "제목1234", "내용입니다", 1L, 1L
        ));
        System.out.println("response = " + articleResponse);
    }

    @Test
    @DisplayName("게시글 읽기 API 테스트")
    void readTest() {
        ArticleResponse response = read(256395380272410624L);
        System.out.println("response = " + response);
    }

    @Test
    void updateTest() {
        update(256395380272410624L);
        ArticleResponse response = read(256395380272410624L);
        System.out.println("response = " + response);
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("v1/articles/{articleId}", 256395380272410624L)
                .retrieve();
    }

    void update(Long articleId) {
        restClient.put()
                .uri("v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("제목5678", "내용입니다222"))
                .retrieve();
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&page=50000&pageSize=30")
                .retrieve()
                .body(ArticlePageResponse.class);

        System.out.println("response.getArticlesCount() = " + response.getArticleCount());
        for (ArticleResponse article : response.getArticles()) {
            System.out.println("article = " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScrollTest() {
        List<ArticleResponse> articles1 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=10")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        for (ArticleResponse articleResponse : articles1) {
            System.out.println("article = " + articleResponse.getArticleId());
        }

        System.out.println("first page complete");

        Long lastArticleId = articles1.getLast().getArticleId();
        List<ArticleResponse> articles2 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=10&lastArticleId=%s".formatted(lastArticleId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleResponse>>() {
                });

        for (ArticleResponse articleResponse : articles2) {
            System.out.println("article = " + articleResponse.getArticleId());
        }
    }

    @AllArgsConstructor
    @Getter
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @AllArgsConstructor
    @Getter
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }
}
