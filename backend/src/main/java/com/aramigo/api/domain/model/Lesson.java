package com.aramigo.api.domain.model;

public class Lesson {

  private Long id;
  private final Long unitId;
  private final int position;
  private final String title;
  private final NodeKind nodeKind;

  public Lesson(Long unitId, int position, String title, NodeKind nodeKind) {
    this(null, unitId, position, title, nodeKind);
  }

  public Lesson(Long id, Long unitId, int position, String title, NodeKind nodeKind) {
    this.id = id;
    this.unitId = unitId;
    this.position = position;
    this.title = title;
    this.nodeKind = nodeKind;
  }

  public Long getId() {
    return id;
  }

  public void assignId(Long id) {
    this.id = id;
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

  public NodeKind getNodeKind() {
    return nodeKind;
  }
}
