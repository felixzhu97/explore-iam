package com.iam.identity.infra.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("MFA session (password + OTT)")
class MfaSessionSmokeTest {

  @LocalServerPort private int port;

  @Test
  @DisplayName("should deny protected api when only password factor is present")
  void shouldDenyProtectedApiWhenOnlyPasswordFactorIsPresent() throws Exception {
    HttpClient client =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    HttpResponse<String> loginPage =
        client.send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + "/login"))
                .GET()
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
                .build(),
            HttpResponse.BodyHandlers.ofString());
    Optional<String> csrf = csrfFrom(loginPage);
    String cookie = cookieHeader(loginPage);

    String body =
        "username=demo&password=demo-password"
            + csrf.map((token) -> "&_csrf=" + token).orElse("");
    HttpRequest.Builder loginReq =
        HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + "/login"))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString(body));
    if (!cookie.isBlank()) {
      loginReq.header(HttpHeaders.COOKIE, cookie);
    }
    HttpResponse<String> login =
        client.send(loginReq.build(), HttpResponse.BodyHandlers.ofString());

    assertThat(login.statusCode()).isBetween(200, 399);
    String sessionCookie = mergeCookies(cookie, cookieHeader(login));

    HttpRequest.Builder usersReq =
        HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + "/api/v1/users"))
            .GET()
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    if (!sessionCookie.isBlank()) {
      usersReq.header(HttpHeaders.COOKIE, sessionCookie);
    }
    HttpResponse<String> users =
        client.send(usersReq.build(), HttpResponse.BodyHandlers.ofString());

    assertThat(users.statusCode()).isIn(302, 401, 403);
    if (users.statusCode() == 302) {
      String location = users.headers().firstValue(HttpHeaders.LOCATION).orElse("");
      assertThat(location).containsAnyOf("/login/verify", "/login/ott", "/login");
    }
  }

  @Test
  @DisplayName("should redirect to verify page after password-only login")
  void shouldRedirectToVerifyPageAfterPasswordOnlyLogin() throws Exception {
    HttpClient client =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    HttpResponse<String> loginPage =
        client.send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + "/login"))
                .GET()
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
                .build(),
            HttpResponse.BodyHandlers.ofString());
    Optional<String> csrf = csrfFrom(loginPage);
    String cookie = cookieHeader(loginPage);

    String body =
        "username=demo&password=demo-password"
            + csrf.map((token) -> "&_csrf=" + token).orElse("");
    HttpRequest.Builder loginReq =
        HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + "/login"))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString(body));
    if (!cookie.isBlank()) {
      loginReq.header(HttpHeaders.COOKIE, cookie);
    }
    HttpResponse<String> login =
        client.send(loginReq.build(), HttpResponse.BodyHandlers.ofString());

    assertThat(login.statusCode()).isEqualTo(302);
    assertThat(login.headers().firstValue(HttpHeaders.LOCATION).orElse(""))
        .contains("/login/verify");
  }

  private static Optional<String> csrfFrom(HttpResponse<?> response) {
    return response.headers().allValues(HttpHeaders.SET_COOKIE).stream()
        .filter((c) -> c.startsWith("XSRF-TOKEN="))
        .map((c) -> c.substring("XSRF-TOKEN=".length()).split(";", 2)[0])
        .findFirst();
  }

  private static String cookieHeader(HttpResponse<?> response) {
    return String.join(
        "; ",
        response.headers().allValues(HttpHeaders.SET_COOKIE).stream()
            .map((c) -> c.split(";", 2)[0])
            .toList());
  }

  private static String mergeCookies(String existing, String next) {
    if (existing.isBlank()) {
      return next;
    }
    if (next.isBlank()) {
      return existing;
    }
    return existing + "; " + next;
  }
}
