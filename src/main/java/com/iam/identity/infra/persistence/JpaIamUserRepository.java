package com.iam.identity.infra.persistence;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** JPA adapter for {@link IamUserRepository}. */
@Repository
public class JpaIamUserRepository implements IamUserRepository {

  private final SpringDataIamUserRepository springData;

  /**
   * Creates the JPA IAM user repository.
   *
   * @param springData Spring Data repository
   */
  public JpaIamUserRepository(SpringDataIamUserRepository springData) {
    this.springData = springData;
  }

  @Override
  public Optional<IamUser> findByUsername(String username) {
    return springData.findByUsername(username);
  }

  @Override
  public Optional<IamUser> findById(String id) {
    return springData.findById(id);
  }

  @Override
  public IamUser save(IamUser user) {
    return springData.save(user);
  }

  @Override
  public List<IamUser> findAll() {
    return springData.findAll();
  }
}
