package com.example.tasks.user;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String name; // e.g. ROLE_USER, ROLE_ADMIN

  public Long getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}
