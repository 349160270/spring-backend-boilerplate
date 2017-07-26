package in.clouthink.daas.sbb.dashboard.rest.support;

import in.clouthink.daas.sbb.dashboard.rest.dto.SmsHistorySummary;
import in.clouthink.daas.sbb.event.sms.history.domain.request.SmsHistoryQueryRequest;
import org.springframework.data.domain.Page;

public interface SmsHistorySupport {
    
    Page<SmsHistorySummary> findPage(SmsHistoryQueryRequest request);
}