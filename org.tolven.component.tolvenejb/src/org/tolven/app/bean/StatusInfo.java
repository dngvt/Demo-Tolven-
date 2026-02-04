package org.tolven.app.bean;

public class StatusInfo {
	public String status;
	public String statusReason;
	public String statusAndReasonStr;
	private volatile int hashCode = 0;
	
	
	StatusInfo(String statusAndReasonStr) {
		this.statusAndReasonStr = statusAndReasonStr;
		if (statusAndReasonStr != null) {
			String[] sr = statusAndReasonStr.split(" ");
			if (sr.length > 0) {
				this.status = sr[0];
			}
			if (sr.length > 1) {
				this.statusReason = sr[1];
			}
		}
	}
	
	StatusInfo(String status, String statusReason) {
		this.status = status;
		this.statusReason = statusReason;
		if (getStatus() != null) {
			StringBuffer statusReasonSB = new StringBuffer();
			statusReasonSB.append(getStatus());
			if (getStatusReason() != null && !getStatusReason().equalsIgnoreCase("")) {
				statusReasonSB.append(" ");
				statusReasonSB.append(getStatusReason());
			}
			this.statusAndReasonStr = statusReasonSB.toString();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof StatusInfo)) {
			return false;
		}
		StatusInfo sInfo = (StatusInfo) obj;
		boolean test = status.equals(sInfo.getStatus())	&& statusReason.equals(sInfo.getStatusReason());
		boolean test2 = statusAndReasonStr.equalsIgnoreCase(sInfo.getStatusAndReasonStr());
		return test || test2;
	}

	@Override
	public int hashCode() {
		final int multiplier = 23;
		if (hashCode == 0) {
			int code = 133;
			code = multiplier * code + status.hashCode();
			code = multiplier * code + statusReason.hashCode();
			hashCode = code;
		}
		return hashCode;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}

	public String getStatusAndReasonStr() {
		return statusAndReasonStr;
	}

	public String getStatusReason() {
		return statusReason;
	}
	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public void setStatusAndReasonStr(String statusAndReasonStr) {
		this.statusAndReasonStr = statusAndReasonStr;
	}
	

}
