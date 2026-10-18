package com.iam.identity.domain.event;

/**
 * Group profile changed event.
 *
 * @param groupId IAM group id
 */
public record GroupProfileChangedEvent(String groupId) {}
