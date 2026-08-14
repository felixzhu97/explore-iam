package com.iam.identity.domain.repository;

import com.iam.identity.domain.model.IamUser;
import java.util.Optional;

public interface IamUserRepository {

    Optional<IamUser> findByUsername(String username);

    Optional<IamUser> findById(String id);

    IamUser save(IamUser user);
}
