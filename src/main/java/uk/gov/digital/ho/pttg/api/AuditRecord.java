package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.pttg.AuditEventType;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@Getter
public class AuditRecord {

    @JsonProperty(value="id")
    private String id;

    @JsonProperty(value="date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime date;

    @JsonProperty(value="email")
    private String email;

    @JsonProperty(value="ref")
    private AuditEventType ref;

    @JsonProperty(value="detail")
    private Map<String, Object> detail;

    @JsonProperty(value="nino")
    private String nino;
}
