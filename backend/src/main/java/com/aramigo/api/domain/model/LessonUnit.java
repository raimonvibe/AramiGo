package com.aramigo.api.domain.model;

/** A unit groups lessons. {@code slug} is the stable key from the data files. */
public record LessonUnit(
    Long id,
    String slug,
    int sectionNumber,
    int unitNumber,
    String title,
    String description) {

  public LessonUnit withId(Long id) {
    return new LessonUnit(id, slug, sectionNumber, unitNumber, title, description);
  }
}
