package com.aramigo.api.infrastructure.web;

import com.aramigo.api.application.port.in.LearningUseCases;
import com.aramigo.api.infrastructure.auth.LearnerIdentity;
import com.aramigo.api.infrastructure.auth.LearnerIdentityResolver;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CheckAnswerRequest;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CheckAnswerResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CompleteLessonRequest;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.CompleteLessonResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LearningPathResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.LessonSessionResponse;
import com.aramigo.api.infrastructure.web.dto.LearningApiDtos.ProfileResponse;
import com.aramigo.api.infrastructure.web.mapper.LearningApiMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
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

  private static final String GUEST_HEADER = "X-Guest-Key";

  private final LearningUseCases learning;
  private final LearnerIdentityResolver identities;

  public LearningController(LearningUseCases learning, LearnerIdentityResolver identities) {
    this.learning = learning;
    this.identities = identities;
  }

  @GetMapping("/path")
  public LearningPathResponse path(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @RequestHeader(value = GUEST_HEADER, required = false) String guestKey) {
    return LearningApiMapper.toResponse(learning.getPath(identity(authorization, guestKey).key()));
  }

  @GetMapping("/lessons/{lessonId}")
  public LessonSessionResponse lesson(
      @PathVariable long lessonId,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @RequestHeader(value = GUEST_HEADER, required = false) String guestKey) {
    return LearningApiMapper.toResponse(
        learning.startLesson(identity(authorization, guestKey).key(), lessonId));
  }

  @PostMapping("/exercises/check")
  public CheckAnswerResponse check(
      @Valid @RequestBody CheckAnswerRequest request,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @RequestHeader(value = GUEST_HEADER, required = false) String guestKey) {
    return LearningApiMapper.toResponse(
        learning.checkAnswer(
            identity(authorization, guestKey).key(), request.exerciseId(), request.tokens()));
  }

  @PostMapping("/lessons/complete")
  public CompleteLessonResponse complete(
      @Valid @RequestBody CompleteLessonRequest request,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @RequestHeader(value = GUEST_HEADER, required = false) String guestKey) {
    return LearningApiMapper.toResponse(
        learning.completeLesson(identity(authorization, guestKey).key(), request.lessonId()));
  }

  @GetMapping("/me")
  public ProfileResponse me(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @RequestHeader(value = GUEST_HEADER, required = false) String guestKey) {
    LearnerIdentity identity = identity(authorization, guestKey);
    return LearningApiMapper.toResponse(
        learning.profile(identity.key(), identity.displayName()),
        identity.email(),
        identity.pictureUrl());
  }

  /**
   * Called once right after signing in: everything done as a guest moves onto the
   * account, so nobody is punished for trying the app before making one.
   */
  @PostMapping("/auth/link")
  public ProfileResponse link(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @RequestHeader(value = GUEST_HEADER, required = false) String guestKey) {
    LearnerIdentity identity = identity(authorization, guestKey);
    return LearningApiMapper.toResponse(
        learning.linkGuestProgress(
            identity.key(), identity.displayName(), identities.guestKey(guestKey)),
        identity.email(),
        identity.pictureUrl());
  }

  private LearnerIdentity identity(String authorization, String guestKey) {
    return identities.resolve(authorization, guestKey);
  }
}
