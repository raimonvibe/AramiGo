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

  /** Stable key from the curriculum data files. */
  @Column(nullable = false, unique = true)
  private String slug;

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

  public ExerciseJpaEntity(String slug) {
    this.slug = slug;
  }

  public Long getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public Long getLessonId() {
    return lessonId;
  }

  public void setLessonId(Long lessonId) {
    this.lessonId = lessonId;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public String getAramaicScript() {
    return aramaicScript;
  }

  public void setAramaicScript(String aramaicScript) {
    this.aramaicScript = aramaicScript;
  }

  public String getTransliteration() {
    return transliteration;
  }

  public void setTransliteration(String transliteration) {
    this.transliteration = transliteration;
  }

  public String getCorrectTokens() {
    return correctTokens;
  }

  public void setCorrectTokens(String correctTokens) {
    this.correctTokens = correctTokens;
  }

  public String getDistractorTokens() {
    return distractorTokens;
  }

  public void setDistractorTokens(String distractorTokens) {
    this.distractorTokens = distractorTokens;
  }
}
