package com.iam.identity.infrastructure.persistence;

import com.iam.identity.domain.model.IamUser;
import com.iam.identity.domain.repository.IamUserRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaIamUserRepository implements IamUserRepository {

    private final SpringDataIamUserRepository springData;

    public JpaIamUserRepository(SpringDataIamUserRepository springData) {
        this.springData = springData;
    }

    @Override
    public Optional<IamUser> findByUsername(String username) {
        return springData.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<IamUser> findById(String id) {
        return springData.findById(id).map(this::toDomain);
    }

    @Override
    public IamUser save(IamUser user) {
        IamUserEntity entity = springData
                .findById(user.getId())
                .orElseGet(() -> new IamUserEntity(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPasswordHash(),
                        user.isEnabled(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()));
        entity.copyFrom(
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isEnabled(),
                user.getUpdatedAt());
        return toDomain(springData.save(entity));
    }

    private IamUser toDomain(IamUserEntity entity) {
        return IamUser.reconstitute(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
