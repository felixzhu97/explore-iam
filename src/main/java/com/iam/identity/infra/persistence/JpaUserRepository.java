package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** JPA adapter for {@link UserRepository}. */
@Repository
public class JpaUserRepository implements UserRepository {

  private final SpringDataUserRepository springData;

  /**
   * Creates the JPA IAM user repository.
   *
   * @param springData Spring Data repository
   */
  public JpaUserRepository(SpringDataUserRepository springData) {
    this.springData = springData;
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return springData.findByUsername(username);
  }

  @Override
  public Optional<User> findById(String id) {
    return springData.findById(id);
  }

  @Override
  public User save(User user) {
    return springData.save(user);
  }

  @Override
  public List<User> findAll() {
    return springData.findAll();
  }
}
