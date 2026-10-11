package com.iam.identity.infra.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authorization.AllRequiredFactorsAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.DefaultAuthorizationManagerFactory;
import org.springframework.stereotype.Component;

/**
 * Builds MFA authorization managers without publishing a global {@code
 * AuthorizationManagerFactory} bean (that would also force factors on the SAS token
 * endpoint client authentication).
 *
 * <p>Policy: {@code anyOf(PASSWORD+OTT, PASSWORD+WEBAUTHN, PASSWORD+TOTP)} — combinations
 * grow as each factor lands in the stack.
 */
@Component
public class MfaAuthorizationManagers {

  private final DefaultAuthorizationManagerFactory<Object> factory =
      new DefaultAuthorizationManagerFactory<>();

  private final List<AllRequiredFactorsAuthorizationManager<Object>> combinations =
      new ArrayList<>();

  MfaAuthorizationManagers() {
    addCombination(
        AllRequiredFactorsAuthorizationManager.<Object>builder()
            .requireFactor((factor) -> factor.passwordAuthority())
            .requireFactor((factor) -> factor.ottAuthority())
            .build());
  }

  /**
   * Replaces {@code authenticated()} with password + any registered second-factor path.
   *
   * @return authorization manager for HTTP security rules
   */
  public AuthorizationManager<Object> authenticated() {
    return this.factory.authenticated();
  }

  /**
   * Registers another acceptable factor combination (e.g. PASSWORD+WEBAUTHN).
   *
   * @param combination all-required-factors manager for one path
   */
  public void addCombination(AllRequiredFactorsAuthorizationManager<Object> combination) {
    this.combinations.add(combination);
    @SuppressWarnings("unchecked")
    AllRequiredFactorsAuthorizationManager<Object>[] array =
        this.combinations.toArray(AllRequiredFactorsAuthorizationManager[]::new);
    this.factory.setAdditionalAuthorization(AllRequiredFactorsAuthorizationManager.anyOf(array));
  }
}
