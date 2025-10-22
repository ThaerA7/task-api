package com.example.tasks.task;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.tasks.user.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskRepository repo;
  private final AppUserRepository users;

  public TaskController(TaskRepository repo, AppUserRepository users) {
    this.repo = repo; this.users = users;
  }

  private Long meId() {
    var username = SecurityContextHolder.getContext().getAuthentication().getName();
    return users.findByUsername(username).orElseThrow().getId();
  }

  private boolean isAdmin() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }

  @GetMapping
  public Page<Task> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) TaskStatus status,
      @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

    var spec = Specification.where(TaskSpecifications.ownedBy(meId()))
        .and(TaskSpecifications.textContains(q))
        .and(TaskSpecifications.hasStatus(status));

    return repo.findAll(spec, pageable);
  }

  @GetMapping("/{id}")
  public Task get(@PathVariable Long id) {
    var t = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!isAdmin() && !t.getOwner().getId().equals(meId()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return t;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Task create(@RequestBody @Valid Task incoming) {
    incoming.setId(null);
    incoming.setOwner(users.findById(meId()).orElseThrow());
    return repo.save(incoming);
  }

  @PutMapping("/{id}")
  public Task update(@PathVariable Long id, @RequestBody @Valid Task incoming) {
    var t = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!isAdmin() && !t.getOwner().getId().equals(meId()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);

    t.setTitle(incoming.getTitle());
    t.setDescription(incoming.getDescription());
    t.setStatus(incoming.getStatus());
    t.setDueDate(incoming.getDueDate());
    return repo.save(t);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    var t = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!isAdmin() && !t.getOwner().getId().equals(meId()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    repo.delete(t);
  }

  // Admin can view everyone's tasks
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin/all")
  public Page<Task> all(@PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
    return repo.findAll(pageable);
  }
}
