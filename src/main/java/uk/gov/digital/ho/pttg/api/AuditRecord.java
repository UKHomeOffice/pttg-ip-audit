package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@Getter
public class AuditRecord {

    private String id;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime date;

    private String email;

    private AuditEventType ref;

    private Map<String, Object> detail;

    private String nino;
}
