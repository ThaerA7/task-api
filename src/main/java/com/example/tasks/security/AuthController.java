package com.example.tasks.security;

import com.example.tasks.user.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AppUserRepository users;
  private final RoleRepository roles;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final UserDetailsService uds;

  public AuthController(AppUserRepository users, RoleRepository roles,
                        PasswordEncoder encoder, JwtService jwt, UserDetailsService uds) {
    this.users = users; this.roles = roles; this.encoder = encoder; this.jwt = jwt; this.uds = uds;
  }

record RegisterReq(@NotBlank String username, @NotBlank String password, boolean admin) {}
record LoginReq(@NotBlank String username, @NotBlank String password) {}   // ← add this
record AuthResp(String token) {}

@PostMapping("/register")
public AuthResp register(@RequestBody @Valid RegisterReq req) {
  if (users.existsByUsername(req.username())) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
  }
  var roleUser = roles.findByName("ROLE_USER").orElseGet(() -> {
    var r = new Role(); r.setName("ROLE_USER"); return roles.save(r);
  });
  var roleAdmin = roles.findByName("ROLE_ADMIN").orElseGet(() -> {
    var r = new Role(); r.setName("ROLE_ADMIN"); return roles.save(r);
  });

  var u = new AppUser();
  u.setUsername(req.username());
  u.setPassword(encoder.encode(req.password()));
  u.setRoles(req.admin() ? Set.of(roleUser, roleAdmin) : Set.of(roleUser));
  users.save(u);

  var ud = uds.loadUserByUsername(req.username());
  return new AuthResp(jwt.generate(ud.getUsername(), ud.getAuthorities()));
}

  @PostMapping("/login")
  public AuthResp login(@RequestBody @Valid LoginReq req) {
    var u = users.findByUsername(req.username())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));
    if (!encoder.matches(req.password(), u.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
    }
    var ud = uds.loadUserByUsername(req.username());
    return new AuthResp(jwt.generate(ud.getUsername(), ud.getAuthorities()));
  }
}