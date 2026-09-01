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

  /** Stable key from the curriculum data files. */
  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private int sectionNumber;

  @Column(nullable = false)
  private int unitNumber;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  protected LessonUnitJpaEntity() {}

  public LessonUnitJpaEntity(String slug) {
    this.slug = slug;
  }

  public Long getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public int getSectionNumber() {
    return sectionNumber;
  }

  public void setSectionNumber(int sectionNumber) {
    this.sectionNumber = sectionNumber;
  }

  public int getUnitNumber() {
    return unitNumber;
  }

  public void setUnitNumber(int unitNumber) {
    this.unitNumber = unitNumber;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
