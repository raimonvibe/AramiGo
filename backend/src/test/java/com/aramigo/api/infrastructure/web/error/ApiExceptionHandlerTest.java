package com.aramigo.api.infrastructure.web.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class ApiExceptionHandlerTest {

  private final ApiExceptionHandler handler = new ApiExceptionHandler();

  @Test
  void answersAPathNothingServesWithANotFound() {
    // Render's deploy probe hits "/", which has no handler. That used to fall through
    // to the catch-all and come back as a 500 with a stack trace in the log.
    ResponseEntity<ErrorResponse> response =
        handler.noResource(new NoResourceFoundException(HttpMethod.HEAD, "/", ""));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().code()).isEqualTo("not_found");
  }
}
