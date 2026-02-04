package org.tolven.app;

import java.util.List;

import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.PlaceholderAssociation;

public interface PlaceholderAssociationLocal {
	/* Removes placeholder associations for a placeholder*/
	public void removePlaceholderAssociations(MenuData placeholderMD);
	/*
	 * persists a PlaceholderAssociation entity to database
	 */			
	public PlaceholderAssociation createPlaceholderAssociation(PlaceholderAssociation association);
	/*
	 * Returns a collection of placeholder associations for a placeholder 
	 */
	public List<PlaceholderAssociation> findPlaceholderAssociations(MenuData placeholderMD);
	
	/*
	 * Returns a collection of placeholder associations for a placeholder 
	 */
	public List<PlaceholderAssociation> findPlaceholderAssociations(MenuData placeholderMD,String associationName);
	
	/*
	 * Creates PlaceholderAssociations for the ActRelationships found on a placeholder act. This is used in MenuBean:populateMenuData()
	 */
	public void createPlaceholderAssociations(Object result,MenuData placeholder,String associationName);
	
}
