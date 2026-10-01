package com.example.unimarket.repository;

import com.example.unimarket.domain.LoyaltyPointEntry;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.EngagementTrack;
import com.example.unimarket.domain.enums.LoyaltyEventType;
import com.example.unimarket.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LoyaltyPointEntryJpaRepository extends JpaRepository<LoyaltyPointEntry, UUID> {
    Optional<LoyaltyPointEntry> findByUserIdAndTrackAndEventKey(
            UUID userId, EngagementTrack track, String eventKey);
    Page<LoyaltyPointEntry> findByUserIdOrderByOccurredAtDesc(UUID userId, Pageable pageable);
    long countByUserIdAndTrackAndEventType(UUID userId, EngagementTrack track, LoyaltyEventType eventType);

    @Query("select coalesce(sum(l.points), 0) from LoyaltyPointEntry l "
            + "where l.userId = :userId and l.track = :track")
    Long totalPoints(@Param("userId") UUID userId, @Param("track") EngagementTrack track);

    @Query(value = "select l.userId as userId, sum(l.points) as points "
            + "from LoyaltyPointEntry l where l.track = :track "
            + "and exists (select a.id from UserAccount a where a.id = l.userId and a.status = :accountStatus) "
            + "and exists (select r.id from UserRoleAssignment r where r.userId = l.userId "
            + "and r.role = :role and r.revokedAt is null) "
            + "group by l.userId order by sum(l.points) desc, l.userId asc",
            countQuery = "select count(distinct l.userId) from LoyaltyPointEntry l where l.track = :track "
                    + "and exists (select a.id from UserAccount a where a.id = l.userId and a.status = :accountStatus) "
                    + "and exists (select r.id from UserRoleAssignment r where r.userId = l.userId "
                    + "and r.role = :role and r.revokedAt is null)")
    Page<LoyaltyLeaderboardProjection> leaderboard(
            @Param("track") EngagementTrack track,
            @Param("role") Role role,
            @Param("accountStatus") AccountStatus accountStatus,
            Pageable pageable);
}
