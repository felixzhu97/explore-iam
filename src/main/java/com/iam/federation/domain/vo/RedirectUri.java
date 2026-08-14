package com.iam.federation.domain.vo;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * Absolute HTTP(S) redirect URI for an OIDC Relying Party. Fragments and non-http schemes are
 * rejected.
 */
public record RedirectUri(String value) {

    public RedirectUri {
        Objects.requireNonNull(value, "value");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("redirect_uri must not be blank");
        }
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("redirect_uri is not a valid URI: " + trimmed, ex);
        }
        if (uri.getScheme() == null) {
            throw new IllegalArgumentException("redirect_uri must be absolute: " + trimmed);
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException("redirect_uri scheme must be http or https: " + trimmed);
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("redirect_uri must include a host: " + trimmed);
        }
        if (uri.getFragment() != null) {
            throw new IllegalArgumentException("redirect_uri must not contain a fragment: " + trimmed);
        }
        value = trimmed;
    }

    @Override
    public String toString() {
        return value;
    }
}
