package uk.gov.digital.ho.pttg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.digital.ho.pttg.dto.AuditCsvView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Component
public class AuditResource {

    @Autowired
    AuditRepository repo;

    @RequestMapping(value = "/audit", method = RequestMethod.GET)
    public String allAudit(Model model) {
        List<Audit> records = repo.findAllByOrderByTimestampDesc();
        final List<AuditCsvView> csvViews = records.stream().map(f -> new AuditCsvView(f.getTimestamp(), f.getUserId(), f.getDetail())).collect(Collectors.toList());

        model.addAttribute("audit", csvViews);
        return "data";
    }
}