package org.tolven.app;

import java.util.Comparator;

import org.tolven.app.entity.MenuStructure;

/**
 * Comparator used to sort menu items by sequence number. The sort only
 * occurs within a single menu level and thus the list is usually small, say two
 * up to about ten items.
 * 
 * @author John Churin
 * 
 */
public class MenuSeqSort implements Comparator<Object> {

	public int compare(Object o1, Object o2) {
		MenuStructure ms1 = (MenuStructure) o1;
		MenuStructure ms2 = (MenuStructure) o2;
		if (ms1.getSequence() == ms2.getSequence())
			return 0;
		if (ms1.getSequence() < ms2.getSequence())
			return -1;
		return 1;
	}

}