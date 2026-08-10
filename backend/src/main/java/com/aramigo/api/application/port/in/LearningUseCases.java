package com.aramigo.api.application.port.in;

import java.util.List;

import com.aramigo.api.application.dto.CheckAnswerResult;
import com.aramigo.api.application.dto.CompleteLessonResult;
import com.aramigo.api.application.dto.LearningPathResult;
import com.aramigo.api.application.dto.LessonSessionResult;

/** Inbound ports — what the outside world can ask the learning module to do. */
public interface LearningUseCases {

  LearningPathResult getPath(String guestKey);

  LessonSessionResult startLesson(String guestKey, long lessonId);

  CheckAnswerResult checkAnswer(String guestKey, long exerciseId, List<String> tokens);

  CompleteLessonResult completeLesson(String guestKey, long lessonId);
}
