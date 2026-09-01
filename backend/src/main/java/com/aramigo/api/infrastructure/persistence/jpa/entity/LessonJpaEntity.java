package com.aramigo.api.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lessons")
public class LessonJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Stable key from the curriculum data files. */
  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private Long unitId;

  @Column(nullable = false)
  private int position;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String nodeKind;

  protected LessonJpaEntity() {}

  public LessonJpaEntity(String slug) {
    this.slug = slug;
  }

  public Long getId() {
    return id;
  }

  public String getSlug() {
    return slug;
  }

  public Long getUnitId() {
    return unitId;
  }

  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getNodeKind() {
    return nodeKind;
  }

  public void setNodeKind(String nodeKind) {
    this.nodeKind = nodeKind;
  }
}
