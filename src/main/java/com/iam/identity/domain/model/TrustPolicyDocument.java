package com.iam.identity.domain.model;

import java.util.Objects;

/** JSON trust policy document attached to an IAM role. */
public record TrustPolicyDocument(String json) {

  /**
   * Validates the trust policy JSON.
   *
   * @param json policy document JSON
   */
  public TrustPolicyDocument {
    Objects.requireNonNull(json, "json");
    if (json.isBlank()) {
      throw new IllegalArgumentException("trust policy json must not be blank");
    }
    json = json.trim();
  }

  /** Returns an empty allow-all trust policy for bootstrap roles. */
  public static TrustPolicyDocument allowAll() {
    return new TrustPolicyDocument(
        """
        {
          "Version": "2012-10-17",
          "Statement": [{
            "Effect": "Allow",
            "Principal": {"AWS": "*"},
            "Action": "sts:AssumeRole"
          }]
        }
        """);
  }
}
