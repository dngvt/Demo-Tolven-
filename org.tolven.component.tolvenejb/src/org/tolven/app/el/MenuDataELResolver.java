package org.tolven.app.el;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.tolven.app.MenuLocal;
import org.tolven.app.entity.MenuData;
import org.tolven.app.entity.MenuStructure;
import org.tolven.core.TolvenRequest;
import org.tolven.logging.TolvenLogger;

public class MenuDataELResolver extends ELResolver {
	private MenuLocal menuBean;

	public MenuDataELResolver() {
		super();
	}

	@Override
	public Class getType(ELContext context, Object arg1, Object arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override  
	public Object getValue(ELContext context, Object base, Object property) {
		if (base instanceof MenuData && property instanceof String) {
			if ("latest".equalsIgnoreCase((String)property)) {
				MenuDataMap mdm = new MenuDataMap((MenuData)base);
				context.setPropertyResolved(true);
				return mdm;			
			}
			MenuData md = (MenuData)base;
			context.setPropertyResolved(true);
			return md.getField( (String)property );
		}  
		return null;
	}

	@Override
	public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class getCommonPropertyType(ELContext arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator getFeatureDescriptors(ELContext arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public MenuLocal getMenuBean() {
		if (menuBean == null) {
			String jndiName = "java:global/tolven/tolvenEJB/MenuBean!org.tolven.app.MenuLocal";
			try {
				InitialContext ctx = new InitialContext();
				menuBean = (MenuLocal) ctx.lookup(jndiName);
			}
			catch (NamingException ex) {
				TolvenLogger.info(" Failed to look up " + jndiName, this.getClass());
				throw new RuntimeException("Failed to look up " + jndiName, ex);
			}
		}
		
		return menuBean;
	}
	
	protected class MenuDataMap implements Map {
		private MenuData md;
		
		public MenuDataMap(MenuData md) {
			super();
			this.md = md;
		}

		@Override
		public Object get(Object arg0) {
			String[] arrayContents = ((String)arg0).split(","); 
			MenuStructure msList = getMenuBean().findAccountMenuStructure(TolvenRequest.getInstance().getAccount().getId(), arrayContents[0]);
			String condition = arrayContents[1];
			String params[] = new String[arrayContents.length-2];
			
			//Params is 2 through the end of the array
			for (int i = 2; i < arrayContents.length; i++) {
				params[i-2] = arrayContents[i];
			}
			
			return getMenuBean().onListLatest(md, msList, condition, params);
		}
		

		@Override
		public Object put(Object o1, Object o2) {
			return o2;
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean containsKey(Object arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsValue(Object arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Set entrySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Set keySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void putAll(Map arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object remove(Object arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Collection values() {
			// TODO Auto-generated method stub
			return null;
		}
	};


}
