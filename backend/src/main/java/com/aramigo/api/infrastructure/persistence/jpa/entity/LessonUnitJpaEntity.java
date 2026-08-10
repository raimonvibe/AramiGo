package com.aramigo.api.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lesson_units")
public class LessonUnitJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private int sectionNumber;

  @Column(nullable = false)
  private int unitNumber;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  protected LessonUnitJpaEntity() {}

  public LessonUnitJpaEntity(int sectionNumber, int unitNumber, String title, String description) {
    this.sectionNumber = sectionNumber;
    this.unitNumber = unitNumber;
    this.title = title;
    this.description = description;
  }

  public Long getId() {
    return id;
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
