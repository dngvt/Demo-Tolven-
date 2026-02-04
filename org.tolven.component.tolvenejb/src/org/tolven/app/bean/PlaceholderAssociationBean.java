package org.tolven.app.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.tolven.app.MenuLocal;
import org.tolven.app.PlaceholderAssociationLocal;
import org.tolven.app.el.ELFunctions;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.PlaceholderAssociation;
import org.tolven.trim.ActRelationship;

@Local(PlaceholderAssociationLocal.class)
@Stateless
public class PlaceholderAssociationBean implements PlaceholderAssociationLocal {
	Logger logger = Logger.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;
	
	public EntityManager getEm() {
		return em;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

	/* (non-Javadoc)
	 * @see org.tolven.app.PlaceholderAssociationLocal#removePlaceholderAssociations(org.tolven.app.entity.MenuData)
	 */
	@Override
	public void removePlaceholderAssociations(MenuData placeholderMD) {
		 Query query = em.createQuery("delete FROM PlaceholderAssociation pa WHERE pa.placeholder = :id");
         query.setParameter("id", placeholderMD);
         query.executeUpdate();
	}

	/* (non-Javadoc)
	 * @see org.tolven.app.PlaceholderAssociationLocal#findPlaceholderAssociations(org.tolven.app.entity.MenuData)
	 */
	@Override
	public List<PlaceholderAssociation> findPlaceholderAssociations(MenuData placeholderMD) {
		 Query query = em.createQuery("select pa FROM PlaceholderAssociation pa WHERE pa.placeholder = :id");
         query.setParameter("id", placeholderMD);
         return (List<PlaceholderAssociation>)query.getResultList();
	}

	public List<PlaceholderAssociation> findPlaceholderAssociations(MenuData placeholderMD,String associationName) {
		 Query query = em.createQuery("select pa FROM PlaceholderAssociation pa WHERE pa.placeholder = :id and associationName =:associationName");
		 query.setParameter("id", placeholderMD);
		 query.setParameter("associationName", associationName);	        
        return (List<PlaceholderAssociation>)query.getResultList();
	}
	/* (non-Javadoc)
	 * @see org.tolven.app.PlaceholderAssociationLocal#createPlaceholderAssociation(org.tolven.app.entity.PlaceholderAssociation)
	 */
	@Override
	public PlaceholderAssociation createPlaceholderAssociation(PlaceholderAssociation association) {
		if(logger.isDebugEnabled())
			logger.debug("creating association for "+association);
		getEm().persist(association);
		return association;
	}

	/* (non-Javadoc)
	 * @see org.tolven.app.PlaceholderAssociationLocal#createPlaceholderAssociations(java.lang.Object, org.tolven.app.entity.MenuData, java.lang.String)
	 */
	@Override
	public void createPlaceholderAssociations(Object result,MenuData placeholder,String associationName) {
		if(logger.isDebugEnabled())
			logger.debug("creating associations for "+placeholder);
		try{
			@SuppressWarnings("unchecked")
			List<ActRelationship> relationships = (List<ActRelationship>) result;
			for(ActRelationship act:relationships){			
				PlaceholderAssociation association = new PlaceholderAssociation();
				association.setAssociationName(associationName);
				association.setPlaceholder(placeholder);
				String placeholderPath = ELFunctions.internalId(placeholder.getAccount(),act.getAct().getId());
				//association.setAssociatedPlaceholder(getMenuBean().findMenuDataItem(placeholder.getAccount().getId(), placeholderPath));
				association.setAssociatedPlaceholder(em.find(MenuData.class, Long.parseLong(placeholderPath.substring(placeholderPath.lastIndexOf("-")+1))));
				createPlaceholderAssociation(association);
			}			
		}catch(Exception e){
			Log.error(e);
			throw new RuntimeException("Error in evaluating association field for column"+associationName +" on "+placeholder,e);
		}
	}

}
