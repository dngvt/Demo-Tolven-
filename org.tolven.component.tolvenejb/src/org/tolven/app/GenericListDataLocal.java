package org.tolven.app;

import org.tolven.app.entity.ListQueryControl;
import org.tolven.app.entity.MDQueryResults;

public interface GenericListDataLocal {
	public MDQueryResults findDataByColumns(ListQueryControl ctrl);
	public long countListData(ListQueryControl ctrl  );
}
