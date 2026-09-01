package com.aramigo.api.infrastructure.seed;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.aramigo.api.application.port.out.CurriculumRepositoryPort;
import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.ExerciseType;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.domain.model.NodeKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Loads {@code resources/curriculum/*.json} into the database on startup.
 *
 * <p>Idempotent by slug: content edits are applied in place and content removed
 * from the files is pruned, but learner rows are never touched. Restarting the
 * app does not cost anyone their progress.
 */
@Component
public class CurriculumDataSeeder {

  private static final Logger log = LoggerFactory.getLogger(CurriculumDataSeeder.class);
  private static final String LOCATION = "classpath:curriculum/*.json";

  private final CurriculumRepositoryPort curriculum;
  private final ObjectMapper objectMapper;

  public CurriculumDataSeeder(CurriculumRepositoryPort curriculum, ObjectMapper objectMapper) {
    this.curriculum = curriculum;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public void seed() {
    List<CurriculumFile> files = load();
    if (files.isEmpty()) {
      log.warn("No curriculum files found at {} — the learning path will be empty", LOCATION);
      return;
    }

    files.sort(
        Comparator.comparingInt(CurriculumFile::sectionNumber)
            .thenComparingInt(CurriculumFile::unitNumber));

    Set<String> unitSlugs = new LinkedHashSet<>();
    Set<String> lessonSlugs = new LinkedHashSet<>();
    Set<String> exerciseSlugs = new LinkedHashSet<>();

    // Path progress is a single linear position, so lessons are numbered across
    // the whole curriculum rather than restarting inside each unit.
    int position = 0;

    for (CurriculumFile file : files) {
      LessonUnit unit =
          curriculum.upsertUnit(
              new LessonUnit(
                  null,
                  file.slug(),
                  file.sectionNumber(),
                  file.unitNumber(),
                  file.title(),
                  file.description()));
      unitSlugs.add(unit.slug());

      for (CurriculumFile.LessonFile lessonFile : file.lessons()) {
        position++;
        Lesson lesson =
            curriculum.upsertLesson(
                new Lesson(
                    null,
                    lessonFile.slug(),
                    unit.id(),
                    position,
                    lessonFile.title(),
                    NodeKind.valueOf(lessonFile.nodeKind())));
        lessonSlugs.add(lesson.slug());

        int exercisePosition = 0;
        for (CurriculumFile.ExerciseFile exerciseFile : lessonFile.exercises()) {
          exercisePosition++;
          Exercise exercise =
              curriculum.upsertExercise(
                  new Exercise(
                      null,
                      exerciseFile.slug(),
                      lesson.id(),
                      exercisePosition,
                      ExerciseType.valueOf(exerciseFile.type()),
                      exerciseFile.prompt(),
                      exerciseFile.aramaicScript(),
                      exerciseFile.transliteration(),
                      exerciseFile.correctTokens(),
                      exerciseFile.distractorTokens() == null
                          ? ""
                          : exerciseFile.distractorTokens()));
          exerciseSlugs.add(exercise.slug());
        }
      }
    }

    // Prune in child-first order so nothing is left orphaned.
    curriculum.deleteExercisesNotIn(exerciseSlugs);
    curriculum.deleteLessonsNotIn(lessonSlugs);
    curriculum.deleteUnitsNotIn(unitSlugs);

    log.info(
        "Curriculum synced: {} unit(s), {} lesson(s), {} exercise(s)",
        unitSlugs.size(),
        lessonSlugs.size(),
        exerciseSlugs.size());
  }

  private List<CurriculumFile> load() {
    List<CurriculumFile> files = new ArrayList<>();
    try {
      Resource[] resources = new PathMatchingResourcePatternResolver().getResources(LOCATION);
      for (Resource resource : resources) {
        try (InputStream in = resource.getInputStream()) {
          files.add(objectMapper.readValue(in, CurriculumFile.class));
        } catch (IOException e) {
          throw new IllegalStateException(
              "Could not parse curriculum file " + resource.getFilename(), e);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Could not read curriculum from " + LOCATION, e);
    }
    return files;
  }
}
