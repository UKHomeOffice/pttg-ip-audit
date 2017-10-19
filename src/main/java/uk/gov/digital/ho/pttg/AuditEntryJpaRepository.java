package uk.gov.digital.ho.pttg;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEntryJpaRepository extends CrudRepository<AuditEntry, Long> {

    List<AuditEntry> findAllByOrderByTimestampDesc();

}


