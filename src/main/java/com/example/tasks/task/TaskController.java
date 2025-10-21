package com.example.tasks.task;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskRepository repo;
  public TaskController(TaskRepository repo){ this.repo = repo; }

  @GetMapping
  public Page<Task> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) TaskStatus status,
      @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

    var spec = Specification.where(TaskSpecifications.textContains(q))
                            .and(TaskSpecifications.hasStatus(status));

    return repo.findAll(spec, pageable);
  }

  @GetMapping("/{id}")
  public Task get(@PathVariable Long id){
    return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Task create(@RequestBody @Valid Task incoming){
    incoming.setId(null);
    return repo.save(incoming);
  }

  @PutMapping("/{id}")
  public Task update(@PathVariable Long id, @RequestBody @Valid Task incoming){
    var t = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    t.setTitle(incoming.getTitle());
    t.setDescription(incoming.getDescription());
    t.setStatus(incoming.getStatus());
    t.setDueDate(incoming.getDueDate());
    return repo.save(t);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id){
    if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    repo.deleteById(id);
  }
}
