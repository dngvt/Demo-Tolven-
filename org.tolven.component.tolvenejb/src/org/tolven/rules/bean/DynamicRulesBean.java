/**

 * Copyright (C) 2009 Tolven Inc

 * This library is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU Lesser General Public License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;  
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 *
 * Contact: info@tolvenhealth.com 
 *
 * @author Brian Krzeminski
 */

package org.tolven.rules.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.tolven.app.ApplicationMetadataLocal;
import org.tolven.app.MenuLocal;
import org.tolven.app.bean.MenuBean;
import org.tolven.app.entity.AccountMenuStructure;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuQueryControl;
import org.tolven.app.entity.MenuStructure;
import org.tolven.app.entity.PlaceholderMessage;
import org.tolven.core.TolvenRequest;
import org.tolven.core.entity.Account;
import org.tolven.doc.RuleQueueLocal;
import org.tolven.logging.TolvenLogger;
import org.tolven.menuStructure.Application;
import org.tolven.menuStructure.Extends;
import org.tolven.menuStructure.parse.ParseMenuStructure;
import org.tolven.rules.DynamicRulesLocal;

@Stateless
@Local(DynamicRulesLocal.class)
public class DynamicRulesBean implements DynamicRulesLocal {
	Logger logger = Logger.getLogger(this.getClass());
	@EJB
	private ApplicationMetadataLocal metadataLocal;
	@EJB
	private MenuLocal menuBean;
	@EJB
	RuleQueueLocal ruleQueueBean;
	@PersistenceContext
	private EntityManager em;

	/**
	 * Create a new list based on the template using the passed in menu data
	 * 
	 * @param md
	 *            The new list to create
	 * @param template
	 *            the template to follow for creating the new list
	 * @return MenuStructure associated with the new list
	 */
	public MenuStructure createDynamicList(MenuData md, MenuStructure template) {
		AccountMenuStructure ms = null;
		MenuBean mb = new MenuBean();
		ParseMenuStructure pa = new ParseMenuStructure();
		Account account = TolvenRequest.getInstance().getAccount();

		String listContents = (String) md.getExtendedField("listContents");
		if (null != listContents && listContents.trim().length() > 0) {
			try {
				Application application = pa.parseString(listContents);
				List<Extends> exts = application.getExtends();
				for (Extends ext : exts) {
					org.tolven.menuStructure.List list = ext.getList();
					ms = metadataLocal.processList(account, list,
							template.getParent(), null);
					/*
					 * // TODO Need to validate that the list doesn't exist. try
					 * { MenuStructure existingMenu =
					 * mb.findMenuStructure(account, "echr:patients:" +
					 * md.getString01()); if (null != existingMenu) {
					 * logger.info(
					 * "Menu Structure already found, reusing the current menu located at: "
					 * + "echr:patients:" + md.getString01()); return
					 * existingMenu; } } catch (Exception e) { // The
					 * findMenuStructure method throws an exception if it can't
					 * find the list in question. This isn't necessarily // a
					 * bad thing. We just need to add the list. Thus we are
					 * "eating" this exception and will add the new list.
					 * logger.info("List " + md.getString01() +
					 * " does not exist, adding it now."); }
					 */
					em.persist(ms);
				}
			} catch (JAXBException e) {
				throw new RuntimeException("Unable to create the patient list.", e);
			}
		}
		return ms;
	}

	/**
	 * This method is used to reprocess any placeholders that may be on the
	 * associated list path
	 * 
	 * @param listPath
	 */
	public void reprocessPlaceholders(MenuData mdPLD, String listPath) {
		Account account = TolvenRequest.getInstance().getAccount();
		String processExistingPatients = mdPLD.getString02();
		if ("false".compareToIgnoreCase(processExistingPatients) == 0) {
	        TolvenLogger.info("Rule " + mdPLD.getString01() + " will NOT process against existing patients.", DynamicRulesBean.class);
		}
		else {
	        TolvenLogger.info("Rule " + mdPLD.getString01() + " WILL process against existing patients.", DynamicRulesBean.class);
		
			AccountMenuStructure ams = menuBean.findAccountMenuStructure(
					account.getId(), listPath);
			MenuQueryControl ctrl = new MenuQueryControl();
			ctrl.setAccountUser(TolvenRequest.getInstance().getAccountUser());
			ctrl.setActualMenuStructure(ams);
			ctrl.setNow(TolvenRequest.getInstance().getNow());
			ctrl.setLocale(TolvenRequest.getInstance().getLocale());
			ctrl.setActualMenuStructurePath(ams.getPath());
			ctrl.setMenuStructurePath(ams.getPath());
			ctrl.setMenuStructure(ams);
	
			List<MenuData> menuDataItems = menuBean.findMenuData(ctrl);
			for (MenuData md : menuDataItems) {
				logger.info("Reprocessing placeholder: " + md.getId());
				MenuData placeholder = md.getReference();
				PlaceholderMessage m = new PlaceholderMessage(placeholder);
				ruleQueueBean.send(m);
			}
		}
	}

}
