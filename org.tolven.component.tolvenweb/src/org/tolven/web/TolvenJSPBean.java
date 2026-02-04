package org.tolven.web;

import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.tolven.app.MenuLocal;
import org.tolven.app.bean.MenuPath;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.entity.AccountUser;

public class TolvenJSPBean extends TolvenBean {
	private String element;
	private MenuPath elementMenuPath;

	private Date tolvenNow;
	private AccountUser accountUser;

	private MenuStructure ms;
	private MenuData mdItem;

	public TolvenJSPBean() {
		if (getMenuLocal()== null) {
		    String jndiName = "java:global/tolven/tolvenEJB/MenuBean!org.tolven.app.MenuLocal";
            try {
            	InitialContext ctx = new InitialContext();
            	this.menuLocal = ((MenuLocal) ctx.lookup(jndiName));
            } catch (NamingException ex) {
                throw new RuntimeException("Failed to look up " + jndiName, ex);
            }
        }	       
	}

	public Date getTolvenNow() {
		return tolvenNow;
	}

	public void setTolvenNow(Date tolvenNow) {
		this.tolvenNow = tolvenNow;
	}

	public AccountUser getAccountUser() {
		return accountUser;
	}

	public void setAccountUser(AccountUser accountUser) {
		this.accountUser = accountUser;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public MenuPath getElementMenuPath() {
		if (elementMenuPath == null) {
			elementMenuPath = new MenuPath(getElement());
		}
		return elementMenuPath;
	}

	public MenuStructure getMenuStructure() {
		if (ms == null)
			ms = getMenuLocal().findMenuStructure(getAccountUser(),
					getElementMenuPath().getPath());
		return ms;
	}

	public MenuData getMenuDataItem() throws Exception {
		if (mdItem == null) {
			MenuQueryControl ctrl = new MenuQueryControl();
			ctrl.setMenuStructure(getMenuStructure().getAccountMenuStructure());
			ctrl.setNow(getTolvenNow());
			ctrl.setAccountUser(getAccountUser());
			ctrl.setOriginalTargetPath(getElementMenuPath());
			ctrl.setRequestedPath(getElementMenuPath());
			mdItem = getMenuLocal().findMenuDataItem(ctrl);
		}
		return mdItem;
	}

	public List<MenuData> getMenuDataList() {
		MenuQueryControl ctrl = new MenuQueryControl();
		ctrl.setLimit(10000); // This is a hard coded hard query limit
		ctrl.setMenuStructure(getMenuStructure().getAccountMenuStructure());
		ctrl.setAccountUser(getAccountUser());
		ctrl.setNow(getTolvenNow());
		ctrl.setOriginalTargetPath(getElementMenuPath());
		ctrl.setRequestedPath(getElementMenuPath());
		ctrl.setOffset(0);
		ctrl.setSortDirection("ASC");
		ctrl.setSortOrder("id");
		return getMenuLocal().findMenuData(ctrl);
	}

}
