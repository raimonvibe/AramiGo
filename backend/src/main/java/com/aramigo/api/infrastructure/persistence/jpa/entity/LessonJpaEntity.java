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

  @Column(nullable = false)
  private Long unitId;

  @Column(nullable = false)
  private int position;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String nodeKind;

  protected LessonJpaEntity() {}

  public LessonJpaEntity(Long unitId, int position, String title, String nodeKind) {
    this.unitId = unitId;
    this.position = position;
    this.title = title;
    this.nodeKind = nodeKind;
  }

  public Long getId() {
    return id;
  }

  public Long getUnitId() {
    return unitId;
  }

  public int getPosition() {
    return position;
  }

  public String getTitle() {
    return title;
  }

  public String getNodeKind() {
    return nodeKind;
  }
}
