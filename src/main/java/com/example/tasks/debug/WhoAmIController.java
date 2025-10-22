package com.example.tasks.debug;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class WhoAmIController {
  @GetMapping("/api/me")
  public Map<String, Object> me(Authentication auth) {
    return Map.of(
      "name", auth.getName(),
      "roles", auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
    );
  }
}
