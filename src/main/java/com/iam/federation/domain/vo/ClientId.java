package com.iam.federation.domain.vo;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** OAuth2 / OIDC client_id. */
public record ClientId(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,100}$");

    public ClientId {
        Objects.requireNonNull(value, "value");
        String trimmed = value.trim();
        if (!PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "client_id must be 3–100 chars of [a-zA-Z0-9._-]: " + trimmed);
        }
        value = trimmed.toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }
}
