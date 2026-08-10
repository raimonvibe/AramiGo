package com.aramigo.api.infrastructure.web;

import com.aramigo.api.application.port.in.LearningUseCases;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CheckAnswerRequest;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CheckAnswerResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CompleteLessonRequest;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CompleteLessonResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LearningPathResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LessonSessionResponse;
import com.aramigo.api.infrastructure.web.mapper.LearningApiMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LearningController {

  private final LearningUseCases learning;

  public LearningController(LearningUseCases learning) {
    this.learning = learning;
  }

  @GetMapping("/path")
  public LearningPathResponse path(
      @RequestHeader(value = "X-Guest-Key", defaultValue = "guest") String guestKey) {
    return LearningApiMapper.toResponse(learning.getPath(guestKey));
  }

  @GetMapping("/lessons/{lessonId}")
  public LessonSessionResponse lesson(
      @PathVariable long lessonId,
      @RequestHeader(value = "X-Guest-Key", defaultValue = "guest") String guestKey) {
    return LearningApiMapper.toResponse(learning.startLesson(guestKey, lessonId));
  }

  @PostMapping("/exercises/check")
  public CheckAnswerResponse check(
      @Valid @RequestBody CheckAnswerRequest request,
      @RequestHeader(value = "X-Guest-Key", defaultValue = "guest") String guestKey) {
    return LearningApiMapper.toResponse(
        learning.checkAnswer(guestKey, request.exerciseId(), request.tokens()));
  }

  @PostMapping("/lessons/complete")
  public CompleteLessonResponse complete(
      @Valid @RequestBody CompleteLessonRequest request,
      @RequestHeader(value = "X-Guest-Key", defaultValue = "guest") String guestKey) {
    return LearningApiMapper.toResponse(learning.completeLesson(guestKey, request.lessonId()));
  }
}
