package uk.gov.digital.ho.pttg;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends CrudRepository<Audit, Long> {

    List<Audit> findAllByOrderByTimestampDesc();

}


