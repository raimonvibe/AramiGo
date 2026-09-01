package com.aramigo.api.domain.model;

/** A node on the learning path. {@code slug} is the stable key from the data files. */
public record Lesson(
    Long id, String slug, Long unitId, int position, String title, NodeKind nodeKind) {

  public Lesson withIdentity(Long id, Long unitId) {
    return new Lesson(id, slug, unitId, position, title, nodeKind);
  }
}
