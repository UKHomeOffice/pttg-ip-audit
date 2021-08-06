package uk.gov.digital.ho.pttg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.pttg.alert.CountByUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * As we are unable to run integration tests against an embedded Postgres DB, the method
 * countNinosAfterDate causes errors in other embedded databases due to the JSONB syntax.
 * <p>
 * This class is a wrapper which delegates most methods to the real AuditEntryJpaRepository, but
 * overrides the countNinosAfterDate method with a stub.
 * <p>
 * When this bean is active - the test has annotation ActiveProfiles("stubAuditRepoCountNinosAfterDate")
 * - an ArchiveRequest with lastArchiveDate the same as NINOS_EXIST_AFTER_LAST_ARCHIVE_DATE will
 * simulate finding a nino after the last archive date to allow testing of that scenario.  Any other
 * date will not.
 */
@Component
@Primary
@Profile("stubAuditRepoCountNinosAfterDate")
class AuditEntryJpaRepositoryStubCountNinosAfterDate implements AuditEntryJpaRepository {

  private static final LocalDate NINOS_EXIST_AFTER_LAST_ARCHIVE_DATE = LocalDate.of(2017, 9, 30);

  private AuditEntryJpaRepository realRepository;

  public AuditEntryJpaRepositoryStubCountNinosAfterDate(
      @Autowired @Qualifier("auditEntryJpaRepository") AuditEntryJpaRepository realRepository
  ) {
    this.realRepository = realRepository;
  }

  public Long countNinosAfterDate(LocalDateTime afterDate, String nino) {
    if (afterDate.toLocalDate().isEqual(NINOS_EXIST_AFTER_LAST_ARCHIVE_DATE)) {
      return 1L;
    } else {
      return 0L;
    }
  }

  @Override
  public Long countEntriesBetweenDates(LocalDateTime startDate, LocalDateTime endDate,
      AuditEventType type, String namespace) {
    return realRepository.countEntriesBetweenDates(startDate, endDate, type, namespace);
  }

  @Override
  public List<AuditEntry> getEntriesBetweenDates(LocalDateTime startDate, LocalDateTime endDate,
      AuditEventType type, String namespace) {
    return realRepository.getEntriesBetweenDates(startDate, endDate, type, namespace);
  }

  @Override
  public List<CountByUser> countEntriesBetweenDatesGroupedByUser(LocalDateTime startDate,
      LocalDateTime endDate, AuditEventType type, String namespace) {
    return realRepository
        .countEntriesBetweenDatesGroupedByUser(startDate, endDate, type, namespace);
  }

  @Override
  public List<AuditEntry> findAllByOrderByTimestampDesc(Pageable pageable) {
    return realRepository.findAllByOrderByTimestampDesc(pageable);
  }

  @Override
  public List<AuditEntry> findAuditHistory(LocalDateTime toDate, List<AuditEventType> eventTypes,
      Pageable pageable) {
    return realRepository.findAuditHistory(toDate, eventTypes, pageable);
  }

  @Override
  public List<String> getAllCorrelationIds(List<AuditEventType> eventTypes) {
    return realRepository.getAllCorrelationIds(eventTypes);
  }

  @Override
  public List<String> getAllCorrelationIds(List<AuditEventType> eventTypes, LocalDateTime toDate) {
    return realRepository.getAllCorrelationIds(eventTypes, toDate);
  }

  @Override
  @Modifying
  @Transactional
  public void deleteAllCorrelationIds(List<String> correlationIds) {
    realRepository.deleteAllCorrelationIds(correlationIds);
  }

  @Override
  public List<AuditEntry> findArchivedResults(LocalDateTime fromDate, LocalDateTime toDate) {
    return realRepository.findArchivedResults(fromDate, toDate);
  }

  @Override
  public List<AuditEntry> findEntriesByCorrelationId(String correlationId,
      List<AuditEventType> eventTypes) {
    return realRepository.findEntriesByCorrelationId(correlationId, eventTypes);
  }

  @Override
  public List<AuditEntry> findAllIpsStatistics() {
    return realRepository.findAllIpsStatistics();
  }

  @Override
  public Iterable<AuditEntry> findAll(Sort sort) {
    return realRepository.findAll(sort);
  }

  @Override
  public Page<AuditEntry> findAll(Pageable pageable) {
    return realRepository.findAll(pageable);
  }

  @Override
  public <S extends AuditEntry> S save(S entity) {
    return realRepository.save(entity);
  }

  @Override
  public <S extends AuditEntry> Iterable<S> saveAll(Iterable<S> entities) {
    return realRepository.saveAll(entities);
  }

  @Override
  public Optional<AuditEntry> findById(Long aLong) {
    return realRepository.findById(aLong);
  }

  @Override
  public boolean existsById(Long aLong) {
    return realRepository.existsById(aLong);
  }

  @Override
  public Iterable<AuditEntry> findAll() {
    return realRepository.findAll();
  }

  @Override
  public Iterable<AuditEntry> findAllById(Iterable<Long> longs) {
    return realRepository.findAllById(longs);
  }

  @Override
  public long count() {
    return realRepository.count();
  }

  @Override
  public void deleteById(Long aLong) {
    realRepository.deleteById(aLong);
  }

  @Override
  public void delete(AuditEntry entity) {
    realRepository.delete(entity);
  }

  @Override
  public void deleteAllById(Iterable<? extends Long> ids) {
    realRepository.deleteAllById(ids);
  }

  @Override
  public void deleteAll(Iterable<? extends AuditEntry> entities) {
    realRepository.deleteAll(entities);
  }

  @Override
  public void deleteAll() {
    realRepository.deleteAll();
  }
}
