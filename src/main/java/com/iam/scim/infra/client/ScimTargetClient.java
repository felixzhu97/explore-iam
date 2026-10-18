package com.iam.scim.infra.client;

import com.iam.identity.domain.model.User;
import com.iam.scim.domain.model.ProvisioningTarget;
import com.iam.scim.domain.service.ScimTargetGateway;
import com.iam.scim.infra.ScimJsonHttpMessageConverter;
import com.iam.scim.mapper.ScimUserMapper;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.filters.Filter;
import com.unboundid.scim2.common.messages.ListResponse;
import com.unboundid.scim2.common.messages.PatchOperation;
import com.unboundid.scim2.common.messages.PatchRequest;
import com.unboundid.scim2.common.types.UserResource;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Calls a Provisioning Target's SCIM {@code /Users} endpoint with a {@code client_credentials}
 * token. Transient failures (5xx, 429, I/O) are retried with exponential backoff.
 */
@Component
@Retryable(
    includes = {
      HttpServerErrorException.class,
      HttpClientErrorException.TooManyRequests.class,
      ResourceAccessException.class
    },
    maxRetries = 3,
    delay = 500,
    multiplier = 2,
    maxDelay = 5000,
    jitter = 100)
public class ScimTargetClient implements ScimTargetGateway {

  private static final Duration MAX_RETRY_AFTER = Duration.ofSeconds(10);

  private static final Authentication PRINCIPAL =
      new AnonymousAuthenticationToken(
          "iam-scim-outbound",
          "iam-scim-outbound",
          AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

  private static final ParameterizedTypeReference<ListResponse<UserResource>> USER_LIST =
      new ParameterizedTypeReference<>() {};

  private final RestClient.Builder restClientBuilder;
  private final OAuth2AuthorizedClientManager authorizedClientManager;
  private final ScimUserMapper userMapper;

  /**
   * Creates the client.
   *
   * @param restClientBuilder Boot-configured builder
   * @param authorizedClientManager manager that obtains and caches client tokens
   * @param userMapper maps IAM users to SCIM resources
   */
  public ScimTargetClient(
      RestClient.Builder restClientBuilder,
      OAuth2AuthorizedClientManager authorizedClientManager,
      ScimUserMapper userMapper) {
    this.restClientBuilder = restClientBuilder;
    this.authorizedClientManager = authorizedClientManager;
    this.userMapper = userMapper;
  }

  @Override
  public Optional<String> findRemoteId(ProvisioningTarget target, String iamUserId) {
    String filter = externalIdFilter(iamUserId);
    ListResponse<UserResource> response =
        call(
            () ->
                client(target)
                    .get()
                    .uri(uri -> uri.path("/Users").queryParam("filter", "{filter}").build(filter))
                    .retrieve()
                    .body(USER_LIST));
    if (response == null || response.getResources() == null) {
      return Optional.empty();
    }
    return response.getResources().stream().map(UserResource::getId).findFirst();
  }

  @Override
  public String createAccount(ProvisioningTarget target, User user) {
    UserResource resource = userMapper.toTargetResource(user);
    try {
      return create(target, resource);
    } catch (HttpClientErrorException.Conflict conflict) {
      String existing = findRemoteId(target, user.getId()).orElseThrow(() -> conflict);
      replace(target, existing, resource);
      return existing;
    }
  }

  @Override
  public boolean replaceAccount(ProvisioningTarget target, String remoteId, User user) {
    return replace(target, remoteId, userMapper.toTargetResource(user));
  }

  private String create(ProvisioningTarget target, UserResource resource) {
    UserResource created =
        call(
            () ->
                client(target)
                    .post()
                    .uri("/Users")
                    .contentType(ScimJsonHttpMessageConverter.SCIM_JSON)
                    .body(resource)
                    .retrieve()
                    .body(UserResource.class));
    if (created == null || created.getId() == null) {
      throw new IllegalStateException("Provisioning Target returned no id");
    }
    return created.getId();
  }

  private boolean replace(ProvisioningTarget target, String remoteId, UserResource resource) {
    try {
      call(
          () ->
              client(target)
                  .put()
                  .uri("/Users/{id}", remoteId)
                  .contentType(ScimJsonHttpMessageConverter.SCIM_JSON)
                  .body(resource)
                  .retrieve()
                  .toBodilessEntity());
      return true;
    } catch (HttpClientErrorException.NotFound ex) {
      return false;
    }
  }

  @Override
  public boolean setAccountActive(ProvisioningTarget target, String remoteId, boolean active) {
    PatchRequest patch = activePatch(active);
    try {
      call(
          () ->
              client(target)
                  .patch()
                  .uri("/Users/{id}", remoteId)
                  .contentType(ScimJsonHttpMessageConverter.SCIM_JSON)
                  .body(patch)
                  .retrieve()
                  .toBodilessEntity());
      return true;
    } catch (HttpClientErrorException.NotFound ex) {
      return false;
    }
  }

  @Override
  public void deleteAccount(ProvisioningTarget target, String remoteId) {
    try {
      call(
          () ->
              client(target)
                  .delete()
                  .uri("/Users/{id}", remoteId)
                  .retrieve()
                  .toBodilessEntity());
    } catch (HttpClientErrorException.NotFound ex) {
      // already gone
    }
  }

  private static String externalIdFilter(String iamUserId) {
    try {
      return Filter.eq("externalId", iamUserId).toString();
    } catch (ScimException ex) {
      throw new IllegalArgumentException("Invalid externalId", ex);
    }
  }

  private static PatchRequest activePatch(boolean active) {
    try {
      return new PatchRequest(PatchOperation.replace("active", active));
    } catch (ScimException ex) {
      throw new IllegalStateException("Cannot build active patch", ex);
    }
  }

  private RestClient client(ProvisioningTarget target) {
    OAuth2ClientHttpRequestInterceptor interceptor =
        new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
    interceptor.setClientRegistrationIdResolver(request -> target.getRegistrationId());
    interceptor.setPrincipalResolver(request -> PRINCIPAL);
    return restClientBuilder
        .clone()
        .baseUrl(target.getBaseUrl())
        .requestInterceptor(interceptor)
        .configureMessageConverters(
            converters -> converters.addCustomConverter(new ScimJsonHttpMessageConverter()))
        .defaultHeader(HttpHeaders.ACCEPT, ScimJsonHttpMessageConverter.SCIM_JSON_VALUE)
        .build();
  }

  private static <T> T call(Supplier<T> request) {
    try {
      return request.get();
    } catch (HttpClientErrorException.TooManyRequests ex) {
      pauseForRetryAfter(ex.getResponseHeaders());
      throw ex;
    }
  }

  private static void pauseForRetryAfter(HttpHeaders headers) {
    String retryAfter = headers == null ? null : headers.getFirst(HttpHeaders.RETRY_AFTER);
    if (retryAfter == null || !retryAfter.matches("\\d{1,6}")) {
      return;
    }
    Duration wait = Duration.ofSeconds(Long.parseLong(retryAfter));
    if (wait.compareTo(MAX_RETRY_AFTER) > 0) {
      wait = MAX_RETRY_AFTER;
    }
    try {
      Thread.sleep(wait);
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
    }
  }
}
