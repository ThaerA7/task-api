package com.example.tasks.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

  private final AppUserRepository users;

  public AppUserDetailsService(AppUserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var u = users.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    var authorities = u.getRoles().stream()
        .map(r -> new SimpleGrantedAuthority(r.getName()))
        .toList();
    return new org.springframework.security.core.userdetails.User(
        u.getUsername(), u.getPassword(), u.isEnabled(),
        true, true, true, authorities);
  }
}
