package com.iam.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "iam_users")
class IamUserEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, unique = true, length = 128)
  private String username;

  @Column(length = 320)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(nullable = false)
  private boolean enabled;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected IamUserEntity() {}

  IamUserEntity(
      String id,
      String username,
      String email,
      String passwordHash,
      boolean enabled,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.enabled = enabled;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  String getId() {
    return id;
  }

  String getUsername() {
    return username;
  }

  String getEmail() {
    return email;
  }

  String getPasswordHash() {
    return passwordHash;
  }

  boolean isEnabled() {
    return enabled;
  }

  Instant getCreatedAt() {
    return createdAt;
  }

  Instant getUpdatedAt() {
    return updatedAt;
  }

  void copyFrom(
      String username, String email, String passwordHash, boolean enabled, Instant updatedAt) {
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.enabled = enabled;
    this.updatedAt = updatedAt;
  }
}
