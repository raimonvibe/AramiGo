package com.aramigo.api.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TomcatConfigTest {

  @LocalServerPort
  int port;

  @Test
  void acceptsRequestsWithMoreThanTenLocales() throws Exception {
    String languages = IntStream.range(0, 15)
        .mapToObj(i -> "x-lang" + i + ";q=0." + (9 - i % 9))
        .collect(Collectors.joining(","));
    HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/health"))
        .header("Accept-Language", "en-US,en;q=0.9," + languages)
        .build();

    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
  }
}
