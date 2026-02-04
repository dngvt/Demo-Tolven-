/**
 * 
 */
package org.tolven.web;

import java.io.Serializable;
import java.util.ArrayList;

import javax.ejb.EJB;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.tolven.surescripts.SurescriptsLocal;
import org.tolven.surescripts.entity.MessageDetails;

/**
 * @author root
 *
 */
public class SurescriptsMessageAction extends TopAction implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private @EJB SurescriptsLocal sureBean;
	private ArrayList<MessageDetails> surescriptsMessages;
	private String searchMessageType;
	private String searchMessageId;
	private String searchDate;
	private String rowId;
	private String total;
	private long priorAccountId;
	private String maxCount;

	public SurescriptsMessageAction(){
		super();
		this.rowId="0";
	}

	/**
	 * @return the sureBean
	 */
	public SurescriptsLocal getSureBean() {
		return sureBean;
	}

	/**
	 * @param sureBean the sureBean to set
	 */
	public void setSureBean(SurescriptsLocal sureBean) {
		this.sureBean = sureBean;
	}

	

	/**
	 * @return the surescriptsMessages
	 */
	public ArrayList<MessageDetails> getSurescriptsMessages() {
		if (getPriorAccountId() != getAccountId()) {
			surescriptsMessages = null;
		}
		if(null == surescriptsMessages){
			surescriptsMessages = sureBean.retreiveAllSurescriptsMessages("0", getAccountId());
			setPriorAccountId(getAccountId());
			this.total = String.valueOf(this.surescriptsMessages.size());
		}
		return surescriptsMessages;
	}

	/**
	 * @param surescriptsMessages the surescriptsMessages to set
	 */
	public void setSurescriptsMessages(ArrayList<MessageDetails> surescriptsMessages) {
		this.surescriptsMessages = surescriptsMessages;
	}

	/**
	 * @return the searchMessageType
	 */
	public String getSearchMessageType() {
		return searchMessageType;
	}

	/**
	 * @param searchMessageType the searchMessageType to set
	 */
	public void setSearchMessageType(String searchMessageType) {
		this.searchMessageType = searchMessageType;
	}

	/**
	 * @return the searchMessageId
	 */
	public String getSearchMessageId() {
		return searchMessageId;
	}

	/**
	 * @param searchMessageId the searchMessageId to set
	 */
	public void setSearchMessageId(String searchMessageId) {
		this.searchMessageId = searchMessageId;
	}

	/**
	 * @return the searchDate
	 */
	public String getSearchDate() {
		return searchDate;
	}

	/**
	 * @param searchDate the searchDate to set
	 */
	public void setSearchDate(String searchDate) {
		this.searchDate = searchDate;
	}

	/**
	 * ActionListener for Search Button in the SurescriptsMessageCenter screen
	 * @param ae
	 */
	public void retrieveFilteredSurescriptsMessages(ActionEvent ae){
		if(ae.getComponent().getId().equals("search")){
			this.rowId="0";
		}
		if(null == this.searchMessageType)
			this.searchMessageType = "";
		if(null == this.searchMessageId)
			this.searchMessageId = "";
		if(null == this.searchMessageId)
			this.searchMessageId = "";
		if(null != this.searchDate && !this.searchDate.equals("")){
			this.surescriptsMessages = sureBean.retrieveFilteredSurescriptsMessages(this.searchMessageType, this.searchDate , this.searchMessageId , this.rowId, getAccountId());
		}else{
			this.surescriptsMessages = sureBean.retrieveFilteredSurescriptsMessages(this.searchMessageType, "" , this.searchMessageId , this.rowId, getAccountId());
		}	
		this.total = String.valueOf(this.surescriptsMessages.size());
		this.maxCount = String.valueOf(sureBean.getMessageCount()/10);
		
	}

	/**
	 * Action Event on clicking Next button
	 * @param ae
	 */
	public void clickNext(ActionEvent ae){
		if(null == this.searchMessageType)
			this.searchMessageType = "";
		if(null == this.searchMessageId)
			this.searchMessageId = "";
		this.maxCount = String.valueOf(sureBean.getMessageCount()/10);
		
		this.rowId = String.valueOf(Integer.parseInt(this.rowId)+1);
		retrieveFilteredSurescriptsMessages(ae);
	}
	/**
	 * Action Event on clicking Next button
	 * @param ae
	 */
	public void clickPrevious(ActionEvent ae){
		if(null == this.searchMessageType)
			this.searchMessageType = "";
		if(null == this.searchMessageId)
			this.searchMessageId = "";
		this.maxCount = String.valueOf(sureBean.getMessageCount()/10);
		if(Integer.parseInt(this.rowId) > 0)
			this.rowId = String.valueOf(Integer.parseInt(this.rowId)-1);
		else
			this.rowId = "0";
		retrieveFilteredSurescriptsMessages(ae);
	}
	/**
	 * Action Event on clicking Next button
	 * @param ae
	 */
	public void clickFirst(ActionEvent ae){
		if(null == this.searchMessageType)
			this.searchMessageType = "";
		if(null == this.searchMessageId)
			this.searchMessageId = "";
		this.rowId = "0";
		this.maxCount = String.valueOf(sureBean.getMessageCount()/10);
		retrieveFilteredSurescriptsMessages(ae);
	}
	/**
	 * Action Event on clicking Next button
	 * @param ae
	 */
	public void clickLast(ActionEvent ae){
		if(null == this.searchMessageType)
			this.searchMessageType = "";
		if(null == this.searchMessageId)
			this.searchMessageId = "";
		this.maxCount = String.valueOf(sureBean.getMessageCount()/10);
		this.rowId = String.valueOf(sureBean.getMessageCount()/10);
		retrieveFilteredSurescriptsMessages(ae);
	}
	/**
	 * @return the rowId
	 */
	public String getRowId() {
		return rowId;
	}

	/**
	 * @param rowId the rowId to set
	 */
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	/**
	 * @return the total
	 */
	public String getTotal() {
		return total;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(String total) {
		this.total = total;
	}
	/**
	 * Reset rowId on value change
	 * @param ae
	 */
	public void changeRowId(ValueChangeEvent ae){
		this.rowId = "0";
	}
	
	public long getPriorAccountId() {
		return priorAccountId;
	}

	public void setPriorAccountId(long priorAccountId) {
		this.priorAccountId = priorAccountId;
	}

	/**
	 * @return the maxCount
	 */
	public String getMaxCount() {
		return maxCount;
	}

	/**
	 * @param maxCount the maxCount to set
	 */
	public void setMaxCount(String maxCount) {
		this.maxCount = maxCount;
	}
}
