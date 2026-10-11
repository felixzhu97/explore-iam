package com.iam.identity.infra.security;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.http.HttpClient;
import javax.sql.DataSource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/** Builds a RestClient session that completed password + OTT MFA. */
public final class MfaTestSessions {

  private MfaTestSessions() {}

  /**
   * Logs in as the demo user through form login and One-Time Token.
   *
   * @param port local server port
   * @param dataSource datasource used to read the generated OTT
   * @return RestClient with session cookie and CSRF header
   */
  public static RestClient demoUserWithMfa(int port, DataSource dataSource) {
    var cookieStore = new CookieManager();
    var httpClient =
        HttpClient.newBuilder()
            .cookieHandler(cookieStore)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    RestClient rest =
        RestClient.builder()
            .baseUrl("http://localhost:" + port)
            .requestFactory(new JdkClientHttpRequestFactory(httpClient))
            .build();

    rest.get().uri("/login").retrieve().toBodilessEntity();
    String csrf = csrf(cookieStore);

    MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
    login.add("username", "demo");
    login.add("password", "demo-password");
    login.add("_csrf", csrf);
    rest.post()
        .uri("/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(login)
        .retrieve()
        .toBodilessEntity();

    csrf = csrf(cookieStore);
    MultiValueMap<String, String> generate = new LinkedMultiValueMap<>();
    generate.add("username", "demo");
    generate.add("_csrf", csrf);
    rest.post()
        .uri("/ott/generate")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(generate)
        .retrieve()
        .toBodilessEntity();

    String token =
        new JdbcTemplate(dataSource)
            .queryForObject(
                "select token_value from one_time_tokens"
                    + " where username = ? order by expires_at desc",
                String.class,
                "demo");

    csrf = csrf(cookieStore);
    MultiValueMap<String, String> ott = new LinkedMultiValueMap<>();
    ott.add("token", token);
    ott.add("_csrf", csrf);
    rest.post()
        .uri("/login/ott")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(ott)
        .retrieve()
        .toBodilessEntity();

    String csrfAfter = csrf(cookieStore);
    return RestClient.builder()
        .baseUrl("http://localhost:" + port)
        .requestFactory(new JdkClientHttpRequestFactory(httpClient))
        .defaultHeader("X-XSRF-TOKEN", csrfAfter)
        .build();
  }

  private static String csrf(CookieManager cookieStore) {
    return cookieStore.getCookieStore().getCookies().stream()
        .filter((c) -> "XSRF-TOKEN".equals(c.getName()))
        .map(HttpCookie::getValue)
        .findFirst()
        .orElseThrow();
  }
}
