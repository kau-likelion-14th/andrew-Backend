package likelion14th.lte.youtube.client;

import tools.jackson.databind.JsonNode;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class YouTubeClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${youtube.api-key}")
    private String apiKey;

    @Value("${youtube.api-base}")
    private String apiBase;

    public JsonNode searchVideosRaw(String query, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));

        URI uri = UriComponentsBuilder
                .fromUriString(apiBase + "/search")
                .queryParam("part", "snippet")
                .queryParam("q", query)
                .queryParam("type", "video")
                .queryParam("maxResults", safeLimit)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();

        return request(uri);
    }

    public JsonNode getVideoRaw(String videoId) {
        URI uri = UriComponentsBuilder
                .fromUriString(apiBase + "/videos")
                .queryParam("part", "snippet,contentDetails")
                .queryParam("id", videoId)
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();

        return request(uri);
    }

    // restTemplate.exchange(String, ...)에 이미 인코딩된 URL 문자열을 넘기면
    // 내부 UriTemplateHandler가 '%'까지 다시 인코딩해 이중 인코딩이 발생하므로,
    // 완성된 URI 객체를 그대로 전달합니다.
    private JsonNode request(URI uri) {
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    JsonNode.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new GeneralException(ErrorCode.YOUTUBE_API_FAILED);
            }

            return response.getBody();

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.YOUTUBE_API_FAILED);
        }
    }
}
