package com.iam.policy.domain.event;

import com.iam.common.domain.model.Effect;

/**
 * Access evaluated event.
 *
 * @param principalId evaluated principal id
 * @param action requested action
 * @param resource requested resource
 * @param effect decision effect
 * @param reasonCode decision reason code
 */
public record AccessEvaluatedEvent(
    String principalId, String action, String resource, Effect effect, String reasonCode) {}
