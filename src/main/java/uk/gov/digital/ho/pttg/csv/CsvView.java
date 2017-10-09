package uk.gov.digital.ho.pttg.csv;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import uk.gov.digital.ho.pttg.dto.AuditCsvView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class CsvView extends AbstractCsvView {

    private static final String DETAIL = "detail";
    private static final String USER_ID = "userId";
    private static final String TIMESTAMP = "timestamp";

    @Override
    protected void buildCsvDocument(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setHeader("content-type", "text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "inline; filename=\"audit-"+ getFormattedTime() +".csv\"");

        List<AuditCsvView> auditList = (List<AuditCsvView>) model.get("audit");
        String[] header = {USER_ID, TIMESTAMP, DETAIL};
        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        csvWriter.writeHeader(header);

        for (AuditCsvView audit : auditList) {
            csvWriter.write(audit, header);
        }
        csvWriter.close();
    }

    private String getFormattedTime() {
        return DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now());
    }
}