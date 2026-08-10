package com.aramigo.api.infrastructure.seed;

import com.aramigo.api.application.port.out.CurriculumRepositoryPort;
import com.aramigo.api.domain.model.Exercise;
import com.aramigo.api.domain.model.Lesson;
import com.aramigo.api.domain.model.LessonUnit;
import com.aramigo.api.infrastructure.seed.BeginnerSyriacCurriculumFactory.CurriculumBlueprint;
import com.aramigo.api.infrastructure.seed.BeginnerSyriacCurriculumFactory.LessonBlueprint;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CurriculumDataSeeder {

  @Bean
  CommandLineRunner seedCurriculum(CurriculumRepositoryPort curriculum) {
    return args -> {
      // Re-seed while the catalogue is tiny so content edits always ship.
      curriculum.deleteAllCurriculum();

      CurriculumBlueprint blueprint = BeginnerSyriacCurriculumFactory.unitOne();
      LessonUnit unit = curriculum.saveUnit(blueprint.unit());

      for (LessonBlueprint lessonBlueprint : blueprint.lessons()) {
        Lesson lesson =
            curriculum.saveLesson(
                new Lesson(
                    unit.getId(),
                    lessonBlueprint.lesson().getPosition(),
                    lessonBlueprint.lesson().getTitle(),
                    lessonBlueprint.lesson().getNodeKind()));

        for (Exercise draft : lessonBlueprint.exercises()) {
          curriculum.saveExercise(
              new Exercise(
                  lesson.getId(),
                  draft.getPosition(),
                  draft.getType(),
                  draft.getPrompt(),
                  draft.getAramaicScript(),
                  draft.getTransliteration(),
                  draft.getCorrectTokens(),
                  draft.getDistractorTokens()));
        }
      }
    };
  }
}
