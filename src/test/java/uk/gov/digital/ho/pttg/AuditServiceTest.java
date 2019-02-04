package uk.gov.digital.ho.pttg;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.digital.ho.pttg.alert.AppropriateUsageChecker;
import uk.gov.digital.ho.pttg.alert.sysdig.SuspectUsage;
import uk.gov.digital.ho.pttg.api.AuditRecord;
import uk.gov.digital.ho.pttg.api.AuditableData;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.pttg.AuditEventType.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.PTTG_AUDIT_CONFIG_MISMATCH;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    private LocalDateTime now;

    @Mock private AuditEntryJpaRepository mockRepository;
    @Mock private AppropriateUsageChecker mockChecker;
    @Mock private Appender<ILoggingEvent> mockAppender;

    private AuditService auditService;

    @Captor private ArgumentCaptor<AuditEntry> captorAuditEntry;
    @Captor private ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor;

    @Before
    public void setup() {
        now = LocalDateTime.now();
        auditService = new AuditService(mockRepository, mockChecker, "some deployment namespace");
        Logger logger = (Logger) LoggerFactory.getLogger(AuditService.class);
        logger.addAppender(mockAppender);
    }

    @Test
    public void shouldTransformAuditableDataToAuditEntry() {

        AuditableData auditableData = new AuditableData("some event id",
                now,
                "some session id",
                "some correlation id",
                "some user id",
                "some deployment name",
                "some deployment namespace",
                INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
                "{}");

        AuditEntry auditEntry = auditService.transformToAuditEntry(auditableData);

        assertThat(auditEntry.getUuid()).isEqualTo("some event id");
        assertThat(auditEntry.getTimestamp()).isEqualTo(now);
        assertThat(auditEntry.getSessionId()).isEqualTo("some session id");
        assertThat(auditEntry.getCorrelationId()).isEqualTo("some correlation id");
        assertThat(auditEntry.getUserId()).isEqualTo("some user id");
        assertThat(auditEntry.getDeployment()).isEqualTo("some deployment name");
        assertThat(auditEntry.getNamespace()).isEqualTo("some deployment namespace");
        assertThat(auditEntry.getType()).isEqualTo(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        assertThat(auditEntry.getDetail()).isEqualTo("{}");
    }

    @Test
    public void shouldTransformAuditEntryWithoutNinoToAuditRecord() {

        LocalDateTime now = LocalDateTime.now();
        AuditEntry auditEntryWithoutNino = new AuditEntry("some uuid",
                now,
                "any session id",
                "some correlation id",
                "some email address",
                "any deployment",
                "any namespace",
                INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                "{}"
                );

        AuditRecord auditRecord = auditService.transformToAuditRecord(auditEntryWithoutNino);

        assertThat(auditRecord.getId()).isEqualTo("some correlation id");
        assertThat(auditRecord.getDate()).isEqualTo(now);
        assertThat(auditRecord.getEmail()).isEqualTo("some email address");
        assertThat(auditRecord.getRef()).isEqualTo(INCOME_PROVING_FINANCIAL_STATUS_RESPONSE);
        assertThat(auditRecord.getDetail()).isEqualTo(Collections.emptyMap());
        assertThat(auditRecord.getNino()).isNull();
    }

    @Test
    public void shouldTransformAuditEntryWithNinoToAuditRecord() {

        LocalDateTime now = LocalDateTime.now();
        AuditEntry auditEntryWithNino = new AuditEntry("some uuid",
                now,
                "any session id",
                "some correlation id",
                "some email address",
                "any deployment",
                "any namespace",
                INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
                "{\"nino\": \"some NINO\"}"
        );

        AuditRecord auditRecord = auditService.transformToAuditRecord(auditEntryWithNino);

        assertThat(auditRecord.getId()).isEqualTo("some correlation id");
        assertThat(auditRecord.getDate()).isEqualTo(now);
        assertThat(auditRecord.getEmail()).isEqualTo("some email address");
        assertThat(auditRecord.getRef()).isEqualTo(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        assertThat(auditRecord.getDetail()).isNotEmpty();
        assertThat(auditRecord.getNino()).isEqualTo("some NINO");
    }

    @Test
    public void shouldPersistAuditEntry() {
        LocalDateTime now = LocalDateTime.now();

        AuditableData auditableData = new AuditableData("some event id",
                now,
                "some session id",
                "some correlation id",
                "some user id",
                "some deployment name",
                "some deployment namespace",
                INCOME_PROVING_FINANCIAL_STATUS_REQUEST,
                "some json");

        auditService.add(auditableData);

        verify(mockRepository).save(captorAuditEntry.capture());

        AuditEntry arg = captorAuditEntry.getValue();

        assertThat(arg.getUuid()).isEqualTo("some event id");
        assertThat(arg.getTimestamp()).isEqualTo(now);
        assertThat(arg.getSessionId()).isEqualTo("some session id");
        assertThat(arg.getCorrelationId()).isEqualTo("some correlation id");
        assertThat(arg.getUserId()).isEqualTo("some user id");
        assertThat(arg.getDeployment()).isEqualTo("some deployment name");
        assertThat(arg.getNamespace()).isEqualTo("some deployment namespace");
        assertThat(arg.getType()).isEqualTo(INCOME_PROVING_FINANCIAL_STATUS_REQUEST);
        assertThat(arg.getDetail()).isEqualTo("some json");
    }

    @Test
    public void shouldNotAlert() {
        LocalDateTime now = LocalDateTime.now();

        AuditableData auditableData = new AuditableData("some event id",
                now,
                "some session id",
                "some correlation id",
                "some user id",
                "some deployment name",
                "some deployment namespace",
                HMRC_INCOME_REQUEST,
                "some json");

        auditService.add(auditableData);

        verify(mockRepository).save(any(AuditEntry.class));
        verify(mockChecker, never()).precheck();
        verify(mockChecker, never()).postcheck(any(SuspectUsage.class));
    }

    @Test
    public void shouldAlert() {

        SuspectUsage someSuspectUsage = mock(SuspectUsage.class);
        when(mockChecker.precheck()).thenReturn(someSuspectUsage);

        LocalDateTime now = LocalDateTime.now();

        AuditableData auditableData = new AuditableData("some event id",
                now,
                "some session id",
                "some correlation id",
                "some user id",
                "some deployment name",
                "some deployment namespace",
                INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                "some json");

        auditService.add(auditableData);

        verify(mockRepository).save(any(AuditEntry.class));
        verify(mockChecker).precheck();
        verify(mockChecker).postcheck(any(SuspectUsage.class));
    }

    @Test
    public void add_eventAuditingDeploymentNamespaceMatches_noWarningLog() {
        String deploymentNamespace = "some namespace";
        AuditService auditService = new AuditService(mockRepository, mockChecker, deploymentNamespace);

        AuditableData auditableDataSameNamespace = new AuditableData(
                "some event id",
                now,
                "some session id",
                "some correlation id",
                "some user id",
                "some deployment name",
                deploymentNamespace,
                INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                "some data");

        auditService.add(auditableDataSameNamespace);
        verify(mockAppender, never()).doAppend(any());
    }

    @Test
    public void add_eventAuditingDeploymentNamespaceDoesNotMatch_noWarningLog() {
        String deploymentNamespace = "some namespace";
        String auditEventDeploymentNamespace = "another namespace";
        AuditService auditService = new AuditService(mockRepository, mockChecker, deploymentNamespace);

        AuditableData auditableDataAnotherNamespace = new AuditableData(
                "some event id",
                now,
                "some session id",
                "some correlation id",
                "some user id",
                "some deployment name",
                auditEventDeploymentNamespace,
                INCOME_PROVING_FINANCIAL_STATUS_RESPONSE,
                "some data");

        auditService.add(auditableDataAnotherNamespace);
        verify(mockAppender).doAppend(loggingEventArgumentCaptor.capture());

        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        assertThat(loggingEvent.getLevel())
                .isEqualTo(Level.WARN);

        String expectedLogMessage = String.format("Auditing Deployment Namespace of AuditEvent = '%s' does not match '%s' so no suspicious behaviour will be detected.",
                auditEventDeploymentNamespace, deploymentNamespace);

        assertThat(loggingEvent.getFormattedMessage())
                .isEqualTo(expectedLogMessage);

        assertThat((loggingEvent.getArgumentArray()[2]))
                .isEqualTo(new ObjectAppendingMarker("event_id", PTTG_AUDIT_CONFIG_MISMATCH));
    }
}