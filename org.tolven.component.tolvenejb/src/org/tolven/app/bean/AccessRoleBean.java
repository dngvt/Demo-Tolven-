package org.tolven.app.bean;

import java.util.Collection;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.tolven.app.AccessRoleLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.entity.MSAccessRole;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.entity.AccountUser;

@Local(AccessRoleLocal.class)
@Stateless
public class AccessRoleBean implements AccessRoleLocal {
	@PersistenceContext
	private EntityManager em;

	@EJB
	private MenuLocal menuBean;
	
	
	public MenuLocal getMenuBean() {
		return menuBean;
	}

	public void setMenuBean(MenuLocal menuBean) {
		this.menuBean = menuBean;
	}

	@Override
	public void deleteAccessRoles(Collection<MSAccessRole> roles) {
		for (MSAccessRole role : roles) {
			em.remove(role);
		}

	}

	@Override
	public void addAccessRoles(Collection<MSAccessRole> roles) {
		for (MSAccessRole role : roles) {
			em.persist(role);
		}
	}

	
	/** Method to find accessible children node of a menu structure. This will
	 * first check if the child with node same defaultPathSuffix is accessible.
	 * @param ms
	 * @param au
	 * @return
	 */
	public String findAccessibleChild(MenuStructure ms, AccountUser au) {
		List<MenuStructure> children = getMenuBean().findSortedChildren(au, ms);
		for(MenuStructure child:children){
			String nodes[] = ms.getDefaultPathSuffix().split(":");
			if(nodes.length>1 && child.getNode().equals(nodes[1])){
				return ":"+child.getNode();
			}
		}
		if(children.size() != 0)
			return ":"+children.get(0).getNode();
		return null;
	}
	
	
}
