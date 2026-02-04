package org.tolven.app.bean;

public class TransitionPath {

	public String statusCodePath;
	public String statusReasonCodePath;
	private volatile int hashCode = 0;
	
	
	TransitionPath(String transitionPaths) {
		if (transitionPaths != null) {
			String[] codePaths = transitionPaths.split(" ");
			if (codePaths.length > 0) {
				this.statusCodePath = "#{" + codePaths[0] + "}";
			}
			if (codePaths.length > 1) {
				this.statusReasonCodePath = "#{" + codePaths[1] + "}";
			}
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
		TransitionPath tPath = (TransitionPath) obj;
		boolean test = statusCodePath.equals(tPath.getStatusCodePath())	&& statusReasonCodePath.equals(tPath.getStatusReasonCodePath());
		return test;
	}

	@Override
	public int hashCode() {
		final int multiplier = 23;
		if (hashCode == 0) {
			int code = 133;
			code = multiplier * code + statusCodePath.hashCode();
			code = multiplier * code + statusReasonCodePath.hashCode();
			hashCode = code;
		}
		return hashCode;
	}

	
	public String getStatusCodePath() {
		return statusCodePath;
	}
	public void setStatusCodePath(String statusCodePath) {
		this.statusCodePath = statusCodePath;
	}
	public String getStatusReasonCodePath() {
		return statusReasonCodePath;
	}
	public void setStatusReasonCodePath(String statusReasonCodePath) {
		this.statusReasonCodePath = statusReasonCodePath;
	}
	
	
}
