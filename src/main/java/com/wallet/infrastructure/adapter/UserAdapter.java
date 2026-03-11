package com.wallet.infrastructure.adapter;

import com.wallet.application.port.out.UserPort;
import com.wallet.domain.entity.User;
import com.wallet.infrastructure.converter.UserConverter;
import com.wallet.infrastructure.persistence.entity.UserEntity;
import com.wallet.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAdapter implements UserPort {
    private final JpaUserRepository jpaUserRepository;
    private final UserConverter userConverter;

    @Override
    public User save(User user) {
        UserEntity entity = userConverter.toEntity(user);
        UserEntity saved = jpaUserRepository.save(entity);
        return userConverter.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id).map(userConverter::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email).map(userConverter::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }
}
