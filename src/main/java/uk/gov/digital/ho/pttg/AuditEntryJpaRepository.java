package uk.gov.digital.ho.pttg;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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

    @Query("SELECT audit FROM AuditEntry audit WHERE audit.timestamp <= :toDate AND audit.type in (:eventTypes) ORDER BY audit.timestamp")
    List<AuditEntry> findAuditHistory(@Param("toDate") LocalDateTime toDate, @Param("eventTypes") List<AuditEventType> eventTypes, Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT count(audit) FROM audit WHERE timestamp > :afterDate and detail ->> 'nino' = :nino")
    Long countNinosAfterDate(@Param("afterDate") LocalDateTime afterDate, @Param("nino") String nino);

    @Transactional
    @Modifying
    @Query("DELETE from AuditEntry audit where audit.correlationId in :correlationIds")
    void deleteAllCorrelationIds(@Param("correlationIds") List<String> correlationIds);

    @Query("SELECT audit from AuditEntry audit WHERE audit.timestamp >= :fromDate AND audit.timestamp < :toDate AND audit.type = 'ARCHIVED_RESULTS'")
    List<AuditEntry> findArchivedResults(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    @Query("SELECT DISTINCT(audit.correlationId) from AuditEntry audit WHERE audit.type in (:eventTypes)")
    List<String> getAllCorrelationIds(@Param("eventTypes") List<AuditEventType> eventTypes);

    @Query("SELECT DISTINCT(audit.correlationId) FROM AuditEntry audit WHERE audit.type in (:eventTypes) AND audit.timestamp <= :toDate")
    List<String> getAllCorrelationIds(@Param("eventTypes") List<AuditEventType> eventTypes, @Param("toDate") LocalDateTime toDate);

    @Query("SELECT audit from AuditEntry audit WHERE audit.correlationId = :correlationId AND audit.type in (:eventTypes)")
    List<AuditEntry> findEntriesByCorrelationId(@Param("correlationId") String correlationId, @Param("eventTypes") List<AuditEventType> eventTypes);

    @Query("SELECT audit FROM AuditEntry audit WHERE audit.type = 'IPS_STATISTICS'")
    List<AuditEntry> findAllIpsStatistics();
}


