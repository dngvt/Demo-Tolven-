package org.tolven.trim.ex;

import org.tolven.trim.Device;
import org.tolven.trim.Employee;

public class EmployeeEx extends Employee {
	public void blend( Employee employeeInclude ) {
		if (employeeInclude!=null) {
			if (getHazardExposureText()==null) setHazardExposureText(employeeInclude.getHazardExposureText());
			if (getJobClassCode()==null) setJobClassCode(employeeInclude.getJobClassCode());
			if (getJobCode()==null) setJobCode(employeeInclude.getJobCode());
			if (getJobTitleName()==null) setJobTitleName(employeeInclude.getJobTitleName());
			if (getOccupationCode()==null) setOccupationCode(employeeInclude.getOccupationCode());
			if (getProtectiveEquipmentText()==null) setProtectiveEquipmentText(employeeInclude.getProtectiveEquipmentText());
			if (getSalaryQuantity()==null) setSalaryQuantity(employeeInclude.getSalaryQuantity());
			if (getSalaryTypeCode()==null) setSalaryTypeCode(employeeInclude.getSalaryTypeCode());
		}
	}

}
