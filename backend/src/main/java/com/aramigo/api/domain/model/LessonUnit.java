package com.aramigo.api.domain.model;

public class LessonUnit {

  private Long id;
  private final int sectionNumber;
  private final int unitNumber;
  private final String title;
  private final String description;

  public LessonUnit(int sectionNumber, int unitNumber, String title, String description) {
    this(null, sectionNumber, unitNumber, title, description);
  }

  public LessonUnit(
      Long id, int sectionNumber, int unitNumber, String title, String description) {
    this.id = id;
    this.sectionNumber = sectionNumber;
    this.unitNumber = unitNumber;
    this.title = title;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public void assignId(Long id) {
    this.id = id;
  }

  public int getSectionNumber() {
    return sectionNumber;
  }

  public int getUnitNumber() {
    return unitNumber;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }
}
