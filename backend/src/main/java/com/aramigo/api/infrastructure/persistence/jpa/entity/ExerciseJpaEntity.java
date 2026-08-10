package com.aramigo.api.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exercises")
public class ExerciseJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long lessonId;

  @Column(nullable = false)
  private int position;

  @Column(nullable = false)
  private String type;

  @Column(nullable = false)
  private String prompt;

  private String aramaicScript;

  private String transliteration;

  @Column(nullable = false, length = 1000)
  private String correctTokens;

  @Column(nullable = false, length = 1000)
  private String distractorTokens;

  protected ExerciseJpaEntity() {}

  public ExerciseJpaEntity(
      Long lessonId,
      int position,
      String type,
      String prompt,
      String aramaicScript,
      String transliteration,
      String correctTokens,
      String distractorTokens) {
    this.lessonId = lessonId;
    this.position = position;
    this.type = type;
    this.prompt = prompt;
    this.aramaicScript = aramaicScript;
    this.transliteration = transliteration;
    this.correctTokens = correctTokens;
    this.distractorTokens = distractorTokens;
  }

  public Long getId() {
    return id;
  }

  public Long getLessonId() {
    return lessonId;
  }

  public int getPosition() {
    return position;
  }

  public String getType() {
    return type;
  }

  public String getPrompt() {
    return prompt;
  }

  public String getAramaicScript() {
    return aramaicScript;
  }

  public String getTransliteration() {
    return transliteration;
  }

  public String getCorrectTokens() {
    return correctTokens;
  }

  public String getDistractorTokens() {
    return distractorTokens;
  }
}
