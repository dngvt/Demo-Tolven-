package org.tolven.app;

import org.tolven.doc.bean.TolvenMessage;

public class TouchPlaceholderMessage extends TolvenMessage {

    private long placeholderId;

    public TouchPlaceholderMessage(long placeholderId, long accountId) {
        this.placeholderId = placeholderId;
        setAccountId(accountId);
    }

    public long getPlaceholderId() {
        return placeholderId;
    }

    public void setPlaceholderId(long placeholderId) {
        this.placeholderId = placeholderId;
    }

}
