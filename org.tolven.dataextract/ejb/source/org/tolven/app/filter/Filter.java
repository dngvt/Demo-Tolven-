package org.tolven.app.filter;

import java.util.Locale;
import java.util.Map;

import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.Tree;
import org.apache.log4j.Logger;

public class Filter {
	private int mdw = 0;
	private int wordNo = 0;
	private int fieldNo = 0;
	private String andOr = "START";
	private StringBuffer sbFrom;
	private StringBuffer sbWhere;
	private Map<String, Object>params;
	Logger logger = Logger.getLogger(this.getClass());

	public Filter(	StringBuffer sbFrom, StringBuffer sbWhere, Map<String, Object>params ) {
		this.sbFrom = sbFrom;
		this.sbWhere = sbWhere;
		this.params = params;
	}

	protected void addMDW() {
		mdw++;
		if ("OR".equals(andOr)) {
			if(mdw==1) {
				sbFrom.append(String.format(Locale.US, ", MenuDataWord mdw1", mdw));
			} else {
				sbWhere.append("\n ) OR ( ");				
			}
			sbWhere.append( String.format(Locale.US, "\n ( mdw1.menuStructure = md.menuStructure ", mdw ));
			sbWhere.append( String.format(Locale.US, "\nAND mdw1.menuData = md\nAND ", mdw ));
		} else {
			if(mdw>1) {
				sbWhere.append("\nAND ");
			}
			sbFrom.append(String.format(Locale.US, ", MenuDataWord mdw%d", mdw));
			sbWhere.append( String.format(Locale.US, "\n ( mdw%d.menuStructure = md.menuStructure ", mdw ));
			sbWhere.append( String.format(Locale.US, "\nAND mdw%d.menuData = md\nAND ", mdw ));
		}
	}

	protected void addExactWordFilter( String word, String field ) {
		addMDW();
		wordNo++;
		word=word.toLowerCase();
		if("OR".equals(andOr)) {
			sbWhere.append( String.format(Locale.US, " mdw1.word = :wf%d) ", wordNo));
			if (field !=null) {
				fieldNo++;
				sbWhere.append( String.format(Locale.US, "AND mdw1.field = :fld%d ", fieldNo));
				params.put("fld" + fieldNo, field );
			}
		} else {
			sbWhere.append( String.format(Locale.US, " mdw%d.word = :wf%d) ", mdw, wordNo));
			
			if (field !=null) {
				fieldNo++;
				sbWhere.append( String.format(Locale.US, "AND mdw%d.field = :fld%d ", mdw, fieldNo));
				params.put("fld" + fieldNo, field );
			}

		}
//		sbWhere.append( String.format(Locale.US, ") "));
		params.put("wf" + wordNo, word );
	}

	protected void addWordFilter( String word, String field ) {
		addMDW();
		wordNo++;
		word=word.toLowerCase();
		if("OR".equals(andOr)) {
			sbWhere.append( String.format(Locale.US, " mdw1.word BETWEEN :wfl%d AND :wfh%<d ) ", mdw, wordNo));
		} else {
			sbWhere.append( String.format(Locale.US, " mdw%d.word BETWEEN :wfl%d AND :wfh%<d ) ", mdw, wordNo));
		}
		
		if (field !=null) {
			fieldNo++;
			sbWhere.append( String.format(Locale.US, "AND mdw%d.field = :fld%d ", mdw, fieldNo));
			params.put("fld" + fieldNo, field );
		}
//		sbWhere.append( String.format(Locale.US, ") "));
		params.put("wfl" + wordNo, word );
		params.put("wfh" + wordNo, word + "zzzzzzzzzzzzzzzzzzzz");
	}
	
	public void doNode( Tree tree ) {
		switch (tree.getType()) {
			case FilterLexer.AND:
				if (tree.getChildCount()!=2) {
					throw new RuntimeException( "Misplaced AND");
				}
				if (tree.getChild(0) instanceof CommonErrorNode) {
					throw new RuntimeException("Missing AND operand " + tree.getChild(0).toString());
				}
				if (tree.getChild(1) instanceof CommonErrorNode) {
					throw new RuntimeException("Missing AND operand " + tree.getChild(1).toString());
				}
//				sbWhere.append( "(");
				doNode( tree.getChild(0));
				andOr = "AND";
				doNode( tree.getChild(1));
//				sbWhere.append( ")");
				break;
			case FilterLexer.OR:
				if (tree.getChildCount()!=2) {
					throw new RuntimeException( "Misplaced OR");
				}
				if (tree.getChild(0) instanceof CommonErrorNode) {
					throw new RuntimeException("Missing OR operand " + tree.getChild(0).toString());
				}
				if (tree.getChild(1) instanceof CommonErrorNode) {
					throw new RuntimeException("Missing OR operand " + tree.getChild(1).toString());
				}
				sbWhere.append( "(");
				doNode( tree.getChild(0));
//				sbWhere.append( ")");
				andOr = "OR";
//				sbWhere.append( " OR ");
//				sbWhere.append( "(");
				doNode( tree.getChild(1));
				sbWhere.append( ")");
				break;
			case FilterLexer.BLOCK:
//				sbWhere.append( "(");
				doNode( tree.getChild(0));
//				sbWhere.append( ")");
				break;
			case FilterLexer.EXACTFIELD:
				addExactWordFilter( tree.getChild(1).getText(), tree.getChild(0).getText() );
				break;
			case FilterLexer.FIELD:
				addWordFilter( tree.getChild(1).getText(), tree.getChild(0).getText() );
				break;
			case FilterLexer.SIMPLE:
				addWordFilter( tree.getChild(0).getText(), null );
				break;
		}
	}

}
