package org.tolven.surescripts.entity;

public enum ValidMessageTypes {
	GetPrescriber,
	AddPrescriber,
	AddPrescriberLocation,
	UpdatePrescriberLocation,
	DirectoryDownload,
	NewRx,
	RefillResponse,
	RefillRequest,
	Status,
	Error,
	Verify;

    public String value() {
        return name();
    }

    public static ValidMessageTypes fromValue(String v) {
        return valueOf(v);
    }
}
