package com.example.tasks.task;

import org.springframework.data.jpa.domain.Specification;

public class TaskSpecifications {

  public static Specification<Task> hasStatus(TaskStatus status) {
    return (root, query, cb) ->
        status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
  }

  public static Specification<Task> textContains(String q) {
    return (root, query, cb) -> {
      if (q == null || q.isBlank()) return cb.conjunction();
      var like = "%" + q.toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("title")), like),
          cb.like(cb.lower(root.get("description")), like)
      );
    };
  }

  public static Specification<Task> ownedBy(Long userId) {
    return (root, query, cb) -> cb.equal(root.get("owner").get("id"), userId);
  }
}
