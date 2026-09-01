package com.aramigo.api.application.dto;

import java.util.List;

import com.aramigo.api.application.dto.LessonSessionResult.ExerciseView;
import com.aramigo.api.domain.model.LearnerStats;

/**
 * A set of already-solved exercises that have come due again.
 *
 * <p>Deliberately not a {@link LessonSessionResult}: a review is drawn from the
 * whole curriculum rather than one lesson, has no completion to claim and no
 * position on the path, so giving it a lesson id would only invite the client to
 * try to complete it.
 *
 * @param dueCount everything waiting, which may exceed the exercises handed out
 */
public record ReviewSessionResult(int dueCount, LearnerStats stats, List<ExerciseView> exercises) {}
