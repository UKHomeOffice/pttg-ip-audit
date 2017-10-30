package uk.gov.digital.ho.pttg;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.pttg.alert.CountByUser;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditEntryJpaRepository extends PagingAndSortingRepository<AuditEntry, Long> {

    @Query("SELECT COUNT(audit) FROM AuditEntry audit WHERE audit.timestamp BETWEEN :startDate AND :endDate AND audit.type = :type and audit.namespace = :namespace")
    Long countEntriesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("type") AuditEventType type, @Param("namespace") String namespace);

    @Query("SELECT audit FROM AuditEntry audit WHERE audit.timestamp BETWEEN :startDate AND :endDate AND audit.type = :type and audit.namespace = :namespace ORDER BY audit.timestamp")
    List<AuditEntry> getEntriesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("type") AuditEventType type, @Param("namespace") String namespace);

    @Query("SELECT new uk.gov.digital.ho.pttg.alert.CountByUser(COUNT(audit), audit.userId) FROM AuditEntry audit WHERE audit.timestamp BETWEEN :startDate AND :endDate AND audit.type = :type  and audit.namespace = :namespace GROUP BY audit.userId ORDER BY audit.userId")
    List<CountByUser> countEntriesBetweenDatesGroupedByUser(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("type") AuditEventType type, @Param("namespace") String namespace);

    List<AuditEntry> findAllByOrderByTimestampDesc(Pageable pageable);

}


