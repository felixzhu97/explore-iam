package com.iam.identity.infra.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AllRequiredFactorsAuthorizationManager;
import org.springframework.security.core.authority.FactorGrantedAuthority;

/** Registers PASSWORD + TOTP as an acceptable MFA combination. */
@Configuration
class TotpMfaConfig {

  TotpMfaConfig(MfaAuthorizationManagers mfa) {
    mfa.addCombination(
        AllRequiredFactorsAuthorizationManager.<Object>builder()
            .requireFactor((factor) -> factor.passwordAuthority())
            .requireFactor(
                (factor) ->
                    factor.authority(FactorGrantedAuthority.fromFactor("TOTP").getAuthority()))
            .build());
  }
}
