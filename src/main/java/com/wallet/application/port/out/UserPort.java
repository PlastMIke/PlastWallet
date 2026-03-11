package com.wallet.application.port.out;

import com.wallet.domain.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
