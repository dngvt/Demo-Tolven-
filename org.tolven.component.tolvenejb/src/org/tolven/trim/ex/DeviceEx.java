package org.tolven.trim.ex;

import org.tolven.trim.Device;

public class DeviceEx extends Device {
	public void blend( Device deviceInclude ) {
		if (deviceInclude!=null) {
			if (getAlertLevelCode()==null) setAlertLevelCode(deviceInclude.getAlertLevelCode());
			if (getLastCalibrationTime()==null) setLastCalibrationTime(deviceInclude.getLastCalibrationTime());
			if (getLocalRemoteControlStateCode()==null) setLocalRemoteControlStateCode(deviceInclude.getLocalRemoteControlStateCode());
			if (getManufacturerModelName()==null) setManufacturerModelName(deviceInclude.getManufacturerModelName());
			if (getSoftwareNam()==null) setSoftwareNam(deviceInclude.getSoftwareNam());
		}
	}

}
