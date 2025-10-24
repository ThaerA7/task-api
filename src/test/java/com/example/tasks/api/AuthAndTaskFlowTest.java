package com.example.tasks.api;

import com.example.tasks.it.AbstractIntegrationTest;
import com.example.tasks.task.Task;
import com.example.tasks.task.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthAndTaskFlowTest extends AbstractIntegrationTest {

  @LocalServerPort int port;
  @Autowired TestRestTemplate http;

  private String url(String path){ return "http://localhost:" + port + path; }

  @Test
  void registerLoginAndCrud() {
    // register (user)
    var reg = Map.of("username","alice","password","secret","admin",false);
    var regResp = http.postForEntity(url("/api/auth/register"), reg, Map.class);
    assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    var token = (String) regResp.getBody().get("token");

    // create task (authorized)
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setContentType(MediaType.APPLICATION_JSON);

    var t = new Task();
    t.setTitle("My first");
    t.setDescription("secured");
    t.setStatus(TaskStatus.TODO);

    var createResp = http.exchange(url("/api/tasks"),
        HttpMethod.POST, new HttpEntity<>(t, headers), Task.class);
    assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var saved = createResp.getBody();
    assertThat(saved.getId()).isNotNull();

  
    var listResp = http.exchange(url("/api/tasks"),
        HttpMethod.GET, new HttpEntity<>(headers), String.class);
    assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listResp.getBody()).contains("My first");
  }
}
