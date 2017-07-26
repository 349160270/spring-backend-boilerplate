package in.clouthink.daas.sbb.dashboard.rest.dto;

import in.clouthink.daas.sbb.event.sms.history.domain.model.SmsHistory;
import in.clouthink.daas.sbb.event.sms.history.domain.request.SmsHistoryQueryRequest;
import in.clouthink.daas.sbb.shared.domain.request.impl.DateRangedQueryParameter;

public class SmsHistoriesQueryParameter extends DateRangedQueryParameter implements SmsHistoryQueryRequest {

	private String cellphone;

	private String category;

	private SmsHistory.SmsStatus status;

	@Override
	public String getCellphone() {
		return cellphone;
	}

	public void setCellphone(String cellphone) {
		this.cellphone = cellphone;
	}

	@Override
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public SmsHistory.SmsStatus getStatus() {
		return status;
	}

	public void setStatus(SmsHistory.SmsStatus status) {
		this.status = status;
	}
}