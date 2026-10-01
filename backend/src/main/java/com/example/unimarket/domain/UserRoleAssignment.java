package com.example.unimarket.domain;

import com.example.unimarket.domain.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Grant of one {@link Role} to one account.
 *
 * <p>Modelled as its own entity rather than a collection column so that the
 * granting actor and the revocation time are recorded. Revoking a role sets
 * {@code revokedAt} instead of deleting the row, which preserves the history
 * of who held what and when.
 */
@Entity
@Table(
        name = "user_role_assignment",
        indexes = @Index(name = "idx_role_assignment_user", columnList = "user_id, role, revoked_at")
)
public class UserRoleAssignment extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt;

    /** Null when the platform granted the role automatically. */
    @Column(name = "granted_by_user_id")
    private UUID grantedByUserId;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoked_by_user_id")
    private UUID revokedByUserId;

    /** Required by JPA. Not intended for application use. */
    protected UserRoleAssignment() {
        super();
    }

    private UserRoleAssignment(Builder builder) {
        super(builder.id);
        this.userId = builder.userId;
        this.role = builder.role;
        this.grantedAt = builder.grantedAt;
        this.grantedByUserId = builder.grantedByUserId;
        this.revokedAt = builder.revokedAt;
        this.revokedByUserId = builder.revokedByUserId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public Instant getGrantedAt() {
        return grantedAt;
    }

    public UUID getGrantedByUserId() {
        return grantedByUserId;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public UUID getRevokedByUserId() {
        return revokedByUserId;
    }

    public boolean isActive() {
        return revokedAt == null;
    }

    /** Revokes this active assignment while preserving its original grant. */
    public boolean revoke(UUID actorUserId, Instant at) {
        if (!isActive() || at == null) {
            return false;
        }
        this.revokedAt = at;
        this.revokedByUserId = actorUserId;
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserRoleAssignment assignment)) {
            return false;
        }
        return getId() != null && getId().equals(assignment.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UserRoleAssignment{"
                + "userId=" + userId
                + ", role=" + role
                + ", active=" + isActive()
                + '}';
    }

    public static class Builder {

        private UUID id;
        private UUID userId;
        private Role role;
        private Instant grantedAt;
        private UUID grantedByUserId;
        private Instant revokedAt;
        private UUID revokedByUserId;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setUserId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder setRole(Role role) {
            this.role = role;
            return this;
        }

        public Builder setGrantedAt(Instant grantedAt) {
            this.grantedAt = grantedAt;
            return this;
        }

        public Builder setGrantedByUserId(UUID grantedByUserId) {
            this.grantedByUserId = grantedByUserId;
            return this;
        }

        public Builder setRevokedAt(Instant revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder setRevokedByUserId(UUID revokedByUserId) {
            this.revokedByUserId = revokedByUserId;
            return this;
        }

        public Builder copy(UserRoleAssignment assignment) {
            this.id = assignment.getId();
            this.userId = assignment.getUserId();
            this.role = assignment.getRole();
            this.grantedAt = assignment.getGrantedAt();
            this.grantedByUserId = assignment.getGrantedByUserId();
            this.revokedAt = assignment.getRevokedAt();
            this.revokedByUserId = assignment.getRevokedByUserId();
            return this;
        }

        public UserRoleAssignment build() {
            return new UserRoleAssignment(this);
        }
    }
}
