package org.tolven.app;

import java.util.Collection;

import org.tolven.app.entity.MSAccessRole;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.entity.AccountUser;

public interface AccessRoleLocal {
	public void deleteAccessRoles(Collection<MSAccessRole> roles);
	public void addAccessRoles(Collection<MSAccessRole> roles);
	/** Method to find accessible children node of a menu structure. This will
	 * first check if the child with node same defaultPathSuffix is accessible first.
	 * @param ms
	 * @param au
	 * @return
	 */
	public String findAccessibleChild(MenuStructure ms, AccountUser au);
}
