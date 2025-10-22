package com.example.tasks.task;

import com.example.tasks.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "tasks")
public class Task {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  private String title;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskStatus status = TaskStatus.TODO;

  private Instant dueDate;

  // NEW: who owns this task
  @ManyToOne
  @JoinColumn(name = "owner_id", nullable = false)
  private AppUser owner;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist void onCreate() {
    var now = Instant.now();
    createdAt = now; updatedAt = now;
  }
  @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

  // getters/setters
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public TaskStatus getStatus() { return status; }
  public void setStatus(TaskStatus status) { this.status = status; }
  public Instant getDueDate() { return dueDate; }
  public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }

  public AppUser getOwner() { return owner; }
  public void setOwner(AppUser owner) { this.owner = owner; }

  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
