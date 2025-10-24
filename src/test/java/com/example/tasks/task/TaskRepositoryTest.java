package com.example.tasks.task;

import com.example.tasks.it.AbstractIntegrationTest;
import com.example.tasks.user.AppUser;
import com.example.tasks.user.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTest extends AbstractIntegrationTest {

  @Autowired TaskRepository tasks;
  @Autowired AppUserRepository users;

  @Test
  void saveAndFindTask() {
    var u = new AppUser();
    u.setUsername("alice");
    u.setPassword("hashed"); // value irrelevant for repo test
    users.save(u);

    var t = new Task();
    t.setTitle("Write docs");
    t.setDescription("Swagger");
    t.setStatus(TaskStatus.TODO);
    t.setDueDate(Instant.now().plusSeconds(86400));
    t.setOwner(u);

    var saved = tasks.save(t);
    assertThat(saved.getId()).isNotNull();

    var found = tasks.findById(saved.getId()).orElseThrow();
    assertThat(found.getTitle()).isEqualTo("Write docs");
    assertThat(found.getOwner().getUsername()).isEqualTo("alice");
  }
}
