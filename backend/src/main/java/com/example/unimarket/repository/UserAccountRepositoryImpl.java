package com.example.unimarket.repository;

import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.util.Helper;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Singleton implementation of {@link IUserAccountRepository}.
 *
 * <p><strong>Singleton:</strong> the constructor is private and the only route
 * to an instance is {@link #getInstance(UserAccountJpaRepository)}, which
 * creates the instance once and returns that same instance thereafter. The
 * double-checked lock makes this safe when concurrent web requests race on
 * first access.
 *
 * <p>This class is intentionally not component-scanned. Component scanning
 * would ask Spring to construct its own instance, which would sit alongside
 * the one held here and defeat the pattern. Instead
 * {@code RepositoryConfig} publishes the result of {@link #getInstance} as the
 * bean, so the container and this class share one object.
 *
 * <p><strong>Abstraction:</strong> services depend on
 * {@link IUserAccountRepository}. This class holds the only reference to the
 * Spring Data interface, so swapping the persistence mechanism affects this
 * file alone.
 */
@Repository
public class UserAccountRepositoryImpl implements IUserAccountRepository {

    private static volatile UserAccountRepositoryImpl instance;

    private final UserAccountJpaRepository jpaRepository;

    public UserAccountRepositoryImpl(UserAccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Returns the single instance, creating it on first call.
     */
    public static UserAccountRepositoryImpl getInstance(UserAccountJpaRepository jpaRepository) {
        if (instance == null) {
            synchronized (UserAccountRepositoryImpl.class) {
                if (instance == null) {
                    instance = new UserAccountRepositoryImpl(jpaRepository);
                }
            }
        }
        return instance;
    }

    @Override
    public UserAccount create(UserAccount entity) {
        if (entity == null) {
            return null;
        }
        return jpaRepository.save(entity);
    }

    @Override
    public UserAccount read(UUID id) {
        if (id == null) {
            return null;
        }
        return jpaRepository.findById(id).orElse(null);
    }

    @Override
    public UserAccount update(UserAccount entity) {
        if (entity == null || entity.getId() == null) {
            return null;
        }
        if (!jpaRepository.existsById(entity.getId())) {
            return null;
        }
        return jpaRepository.save(entity);
    }

    @Override
    public boolean delete(UUID id) {
        if (id == null || !jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    @Override
    public List<UserAccount> getAll() {
        return jpaRepository.findAll();
    }

    @Override
    public UserAccount readByEmail(String email) {
        String normalized = Helper.normalizeEmail(email);
        if (normalized == null) {
            return null;
        }
        return jpaRepository.findByEmail(normalized).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        String normalized = Helper.normalizeEmail(email);
        return normalized != null && jpaRepository.existsByEmail(normalized);
    }
}
