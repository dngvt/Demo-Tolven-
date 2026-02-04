package org.tolven.app.process;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;
import org.drools.core.StatefulSession;
import org.tolven.app.AppEvalAdaptor;
import org.tolven.app.ProcessPlaceholderMessageLocal;
import org.tolven.app.TouchLocal;
import org.tolven.app.TouchPlaceholderMessage;
import org.tolven.app.el.TrimExpressionEvaluator;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.PlaceholderMessage;
import org.tolven.app.entity.Touch;
import org.tolven.doc.entity.DocBase;
import org.tolven.el.ExpressionEvaluator;

@Stateless
@Local(ProcessPlaceholderMessageLocal.class)
public class ProcessPlaceholderMessage extends AppEvalAdaptor implements ProcessPlaceholderMessageLocal {

    private Logger logger = Logger.getLogger(this.getClass());
    private MenuData placeholder;
    private TrimExpressionEvaluator trimExpEval;
    private Object workingMemoryObject;

    @EJB
    private TouchLocal touchBean;

    private Object getWorkingMemoryObject() {
        return workingMemoryObject;
    }

    private void setWorkingMemoryObject(Object workingMemoryObject) {
        this.workingMemoryObject = workingMemoryObject;
    }

    @Override
    public void process(Object message, Date now) {
        if (message instanceof PlaceholderMessage) {
            PlaceholderMessage placeholderMessage = (PlaceholderMessage) message;
            logger.info("Processing placeholder message: " + placeholderMessage.getPlaceholderId());
            placeholder = menuBean.findMenuDataItem(placeholderMessage.getPlaceholderId());
            if (placeholder.getAccount().getId() != placeholderMessage.getAccountId()) {
                // TODO finish this error
                throw new RuntimeException("Database placeholder (" + placeholder.getId() + ") account id of " + placeholder.getAccount().getId() + " does not equal message placeholder account id of " + placeholderMessage.getAccountId());
            }
            try {
                initializeMessage(placeholderMessage, now);
                runRules();
            } catch (Exception e) {
                // TODO remove the stack trace
                e.printStackTrace();
                throw new RuntimeException("Exception in placeholder processor", e);
            }
        } else if (message instanceof TouchPlaceholderMessage) {
            TouchPlaceholderMessage touchPlaceholderMessage = (TouchPlaceholderMessage) message;
            initializeMessage(touchPlaceholderMessage, now);
            logger.info("Processing touchplaceholder message referencing placeholder: " + touchPlaceholderMessage.getPlaceholderId());
            long placeholderMessageAccountId = touchPlaceholderMessage.getAccountId();
            if (placeholderMessageAccountId != getAccount().getId()) {
                throw new RuntimeException("Account " + placeholderMessageAccountId + " in placeholder  message referencing (" + touchPlaceholderMessage.getPlaceholderId() + ") does not match current account " + getAccount().getId());
            }
            MenuData placeholderMD = menuBean.findMenuDataItem(touchPlaceholderMessage.getPlaceholderId());
            if (placeholderMD.getAccount().getId() != getAccount().getId()) {
                throw new RuntimeException("List item (" + placeholderMD.getId() + ") of account " + placeholderMD.getAccount().getId() + " does not match current account " + getAccount().getId());
            }
            List<Touch> touches = touchBean.findTouches(placeholderMD);
            if (!touches.isEmpty()) {
                setWorkingMemoryObject(placeholderMD);
                try {
                    runRules();
                } catch (Exception ex) {
                    throw new RuntimeException("Exception running rules for TouchPlaceholderMessage with placeholderId: " + touchPlaceholderMessage.getPlaceholderId(), ex);
                }
            }
            for (Touch touch : touches) {
                touch.setDeleted(true);
            }
        }
    }

    @Override
    protected DocBase scanInboundDocument(DocBase doc) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void loadWorkingMemory(StatefulSession workingMemory) throws Exception {
        if (placeholder != null) {
            assertPlaceholder(placeholder);
        }
        if (getWorkingMemoryObject() != null) {
            getWorkingMemory().insert(getWorkingMemoryObject());
        }
    }

    @Override
    protected ExpressionEvaluator getExpressionEvaluator() {
        if (trimExpEval == null) {
            trimExpEval = new TrimExpressionEvaluator();
            trimExpEval.addVariable("now", getNow());
            trimExpEval.addVariable(TrimExpressionEvaluator.ACCOUNT, getAccount());
        }
        return trimExpEval;
    }

}
