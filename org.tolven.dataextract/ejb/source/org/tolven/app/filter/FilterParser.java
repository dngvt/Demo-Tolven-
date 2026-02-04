// $ANTLR 3.3 Nov 30, 2010 12:50:56 ejb\\source\\org\\tolven\\app\\filter\\Filter.g 2011-06-12 00:36:16
 package org.tolven.app.filter; 

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

public class FilterParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "BLOCK", "FIELD", "EXACTFIELD", "OR", "SIMPLE", "TERM", "STRING", "NUMBER", "DIGIT", "WS", "HEX_DIGIT", "UNICODE_ESC", "OCTAL_ESC", "ESC_SEQ", "'or'", "'OR'", "'Or'", "'and'", "'AND'", "'And'", "'='", "':'", "'('", "')'"
    };
    public static final int EOF=-1;
    public static final int T__19=19;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int AND=4;
    public static final int BLOCK=5;
    public static final int FIELD=6;
    public static final int EXACTFIELD=7;
    public static final int OR=8;
    public static final int SIMPLE=9;
    public static final int TERM=10;
    public static final int STRING=11;
    public static final int NUMBER=12;
    public static final int DIGIT=13;
    public static final int WS=14;
    public static final int HEX_DIGIT=15;
    public static final int UNICODE_ESC=16;
    public static final int OCTAL_ESC=17;
    public static final int ESC_SEQ=18;

    // delegates
    // delegators


        public FilterParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public FilterParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return FilterParser.tokenNames; }
    public String getGrammarFileName() { return "ejb\\source\\org\\tolven\\app\\filter\\Filter.g"; }


    public static class root_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "root"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:20:1: root : expression ;
    public final FilterParser.root_return root() throws RecognitionException {
        FilterParser.root_return retval = new FilterParser.root_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FilterParser.expression_return expression1 = null;



        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:20:16: ( expression )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:20:18: expression
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_expression_in_root82);
            expression1=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression1.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "root"

    public static class expression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expression"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:22:1: expression : orExpression ;
    public final FilterParser.expression_return expression() throws RecognitionException {
        FilterParser.expression_return retval = new FilterParser.expression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FilterParser.orExpression_return orExpression2 = null;



        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:22:16: ( orExpression )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:22:18: orExpression
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_orExpression_in_expression95);
            orExpression2=orExpression();

            state._fsp--;

            adaptor.addChild(root_0, orExpression2.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expression"

    public static class orExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "orExpression"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:1: orExpression : (x= andExpression -> $x) ( ( 'or' | 'OR' | 'Or' ) y= andExpression -> ^( OR $orExpression $y) )* ;
    public final FilterParser.orExpression_return orExpression() throws RecognitionException {
        FilterParser.orExpression_return retval = new FilterParser.orExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal3=null;
        Token string_literal4=null;
        Token string_literal5=null;
        FilterParser.andExpression_return x = null;

        FilterParser.andExpression_return y = null;


        Object string_literal3_tree=null;
        Object string_literal4_tree=null;
        Object string_literal5_tree=null;
        RewriteRuleTokenStream stream_21=new RewriteRuleTokenStream(adaptor,"token 21");
        RewriteRuleTokenStream stream_20=new RewriteRuleTokenStream(adaptor,"token 20");
        RewriteRuleTokenStream stream_19=new RewriteRuleTokenStream(adaptor,"token 19");
        RewriteRuleSubtreeStream stream_andExpression=new RewriteRuleSubtreeStream(adaptor,"rule andExpression");
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:16: ( (x= andExpression -> $x) ( ( 'or' | 'OR' | 'Or' ) y= andExpression -> ^( OR $orExpression $y) )* )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:20: (x= andExpression -> $x) ( ( 'or' | 'OR' | 'Or' ) y= andExpression -> ^( OR $orExpression $y) )*
            {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:20: (x= andExpression -> $x)
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:21: x= andExpression
            {
            pushFollow(FOLLOW_andExpression_in_orExpression111);
            x=andExpression();

            state._fsp--;

            stream_andExpression.add(x.getTree());


            // AST REWRITE
            // elements: x
            // token labels: 
            // rule labels: retval, x
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_x=new RewriteRuleSubtreeStream(adaptor,"rule x",x!=null?x.tree:null);

            root_0 = (Object)adaptor.nil();
            // 24:36: -> $x
            {
                adaptor.addChild(root_0, stream_x.nextTree());

            }

            retval.tree = root_0;
            }

            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:43: ( ( 'or' | 'OR' | 'Or' ) y= andExpression -> ^( OR $orExpression $y) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=19 && LA2_0<=21)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:45: ( 'or' | 'OR' | 'Or' ) y= andExpression
            	    {
            	    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:45: ( 'or' | 'OR' | 'Or' )
            	    int alt1=3;
            	    switch ( input.LA(1) ) {
            	    case 19:
            	        {
            	        alt1=1;
            	        }
            	        break;
            	    case 20:
            	        {
            	        alt1=2;
            	        }
            	        break;
            	    case 21:
            	        {
            	        alt1=3;
            	        }
            	        break;
            	    default:
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 1, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt1) {
            	        case 1 :
            	            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:46: 'or'
            	            {
            	            string_literal3=(Token)match(input,19,FOLLOW_19_in_orExpression121);  
            	            stream_19.add(string_literal3);


            	            }
            	            break;
            	        case 2 :
            	            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:51: 'OR'
            	            {
            	            string_literal4=(Token)match(input,20,FOLLOW_20_in_orExpression123);  
            	            stream_20.add(string_literal4);


            	            }
            	            break;
            	        case 3 :
            	            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:56: 'Or'
            	            {
            	            string_literal5=(Token)match(input,21,FOLLOW_21_in_orExpression125);  
            	            stream_21.add(string_literal5);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_andExpression_in_orExpression130);
            	    y=andExpression();

            	    state._fsp--;

            	    stream_andExpression.add(y.getTree());


            	    // AST REWRITE
            	    // elements: orExpression, y
            	    // token labels: 
            	    // rule labels: retval, y
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_y=new RewriteRuleSubtreeStream(adaptor,"rule y",y!=null?y.tree:null);

            	    root_0 = (Object)adaptor.nil();
            	    // 24:78: -> ^( OR $orExpression $y)
            	    {
            	        // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:24:81: ^( OR $orExpression $y)
            	        {
            	        Object root_1 = (Object)adaptor.nil();
            	        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(OR, "OR"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_y.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "orExpression"

    public static class andExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "andExpression"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:1: andExpression : (x= primary -> $x) ( ( 'and' | 'AND' | 'And' ) y= primary -> ^( AND $andExpression $y) )* ;
    public final FilterParser.andExpression_return andExpression() throws RecognitionException {
        FilterParser.andExpression_return retval = new FilterParser.andExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal6=null;
        Token string_literal7=null;
        Token string_literal8=null;
        FilterParser.primary_return x = null;

        FilterParser.primary_return y = null;


        Object string_literal6_tree=null;
        Object string_literal7_tree=null;
        Object string_literal8_tree=null;
        RewriteRuleTokenStream stream_22=new RewriteRuleTokenStream(adaptor,"token 22");
        RewriteRuleTokenStream stream_23=new RewriteRuleTokenStream(adaptor,"token 23");
        RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
        RewriteRuleSubtreeStream stream_primary=new RewriteRuleSubtreeStream(adaptor,"rule primary");
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:16: ( (x= primary -> $x) ( ( 'and' | 'AND' | 'And' ) y= primary -> ^( AND $andExpression $y) )* )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:19: (x= primary -> $x) ( ( 'and' | 'AND' | 'And' ) y= primary -> ^( AND $andExpression $y) )*
            {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:19: (x= primary -> $x)
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:21: x= primary
            {
            pushFollow(FOLLOW_primary_in_andExpression160);
            x=primary();

            state._fsp--;

            stream_primary.add(x.getTree());


            // AST REWRITE
            // elements: x
            // token labels: 
            // rule labels: retval, x
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_x=new RewriteRuleSubtreeStream(adaptor,"rule x",x!=null?x.tree:null);

            root_0 = (Object)adaptor.nil();
            // 26:30: -> $x
            {
                adaptor.addChild(root_0, stream_x.nextTree());

            }

            retval.tree = root_0;
            }

            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:37: ( ( 'and' | 'AND' | 'And' ) y= primary -> ^( AND $andExpression $y) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>=22 && LA4_0<=24)) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:39: ( 'and' | 'AND' | 'And' ) y= primary
            	    {
            	    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:39: ( 'and' | 'AND' | 'And' )
            	    int alt3=3;
            	    switch ( input.LA(1) ) {
            	    case 22:
            	        {
            	        alt3=1;
            	        }
            	        break;
            	    case 23:
            	        {
            	        alt3=2;
            	        }
            	        break;
            	    case 24:
            	        {
            	        alt3=3;
            	        }
            	        break;
            	    default:
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 3, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt3) {
            	        case 1 :
            	            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:40: 'and'
            	            {
            	            string_literal6=(Token)match(input,22,FOLLOW_22_in_andExpression170);  
            	            stream_22.add(string_literal6);


            	            }
            	            break;
            	        case 2 :
            	            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:46: 'AND'
            	            {
            	            string_literal7=(Token)match(input,23,FOLLOW_23_in_andExpression172);  
            	            stream_23.add(string_literal7);


            	            }
            	            break;
            	        case 3 :
            	            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:52: 'And'
            	            {
            	            string_literal8=(Token)match(input,24,FOLLOW_24_in_andExpression174);  
            	            stream_24.add(string_literal8);


            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_primary_in_andExpression179);
            	    y=primary();

            	    state._fsp--;

            	    stream_primary.add(y.getTree());


            	    // AST REWRITE
            	    // elements: andExpression, y
            	    // token labels: 
            	    // rule labels: retval, y
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_y=new RewriteRuleSubtreeStream(adaptor,"rule y",y!=null?y.tree:null);

            	    root_0 = (Object)adaptor.nil();
            	    // 26:69: -> ^( AND $andExpression $y)
            	    {
            	        // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:26:72: ^( AND $andExpression $y)
            	        {
            	        Object root_1 = (Object)adaptor.nil();
            	        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(AND, "AND"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        adaptor.addChild(root_1, stream_y.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "andExpression"

    public static class primary_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "primary"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:28:1: primary : ( parExpression | fieldExpression | exactFieldExpression | implicitAnd | plainTerm );
    public final FilterParser.primary_return primary() throws RecognitionException {
        FilterParser.primary_return retval = new FilterParser.primary_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FilterParser.parExpression_return parExpression9 = null;

        FilterParser.fieldExpression_return fieldExpression10 = null;

        FilterParser.exactFieldExpression_return exactFieldExpression11 = null;

        FilterParser.implicitAnd_return implicitAnd12 = null;

        FilterParser.plainTerm_return plainTerm13 = null;



        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:28:11: ( parExpression | fieldExpression | exactFieldExpression | implicitAnd | plainTerm )
            int alt5=5;
            switch ( input.LA(1) ) {
            case 27:
                {
                alt5=1;
                }
                break;
            case STRING:
                {
                switch ( input.LA(2) ) {
                case 26:
                    {
                    alt5=2;
                    }
                    break;
                case 25:
                    {
                    alt5=3;
                    }
                    break;
                case STRING:
                case NUMBER:
                    {
                    alt5=4;
                    }
                    break;
                case EOF:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 28:
                    {
                    alt5=5;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 2, input);

                    throw nvae;
                }

                }
                break;
            case NUMBER:
                {
                int LA5_3 = input.LA(2);

                if ( ((LA5_3>=STRING && LA5_3<=NUMBER)) ) {
                    alt5=4;
                }
                else if ( (LA5_3==EOF||(LA5_3>=19 && LA5_3<=24)||LA5_3==28) ) {
                    alt5=5;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:28:13: parExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_parExpression_in_primary206);
                    parExpression9=parExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, parExpression9.getTree());

                    }
                    break;
                case 2 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:29:7: fieldExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_fieldExpression_in_primary214);
                    fieldExpression10=fieldExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, fieldExpression10.getTree());

                    }
                    break;
                case 3 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:30:7: exactFieldExpression
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_exactFieldExpression_in_primary222);
                    exactFieldExpression11=exactFieldExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, exactFieldExpression11.getTree());

                    }
                    break;
                case 4 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:31:7: implicitAnd
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_implicitAnd_in_primary230);
                    implicitAnd12=implicitAnd();

                    state._fsp--;

                    adaptor.addChild(root_0, implicitAnd12.getTree());

                    }
                    break;
                case 5 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:32:7: plainTerm
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_plainTerm_in_primary238);
                    plainTerm13=plainTerm();

                    state._fsp--;

                    adaptor.addChild(root_0, plainTerm13.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "primary"

    public static class exactFieldExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exactFieldExpression"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:35:1: exactFieldExpression : x= STRING '=' y= term -> ^( EXACTFIELD $x $y) ;
    public final FilterParser.exactFieldExpression_return exactFieldExpression() throws RecognitionException {
        FilterParser.exactFieldExpression_return retval = new FilterParser.exactFieldExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token x=null;
        Token char_literal14=null;
        FilterParser.term_return y = null;


        Object x_tree=null;
        Object char_literal14_tree=null;
        RewriteRuleTokenStream stream_25=new RewriteRuleTokenStream(adaptor,"token 25");
        RewriteRuleTokenStream stream_STRING=new RewriteRuleTokenStream(adaptor,"token STRING");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:35:23: (x= STRING '=' y= term -> ^( EXACTFIELD $x $y) )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:35:26: x= STRING '=' y= term
            {
            x=(Token)match(input,STRING,FOLLOW_STRING_in_exactFieldExpression255);  
            stream_STRING.add(x);

            char_literal14=(Token)match(input,25,FOLLOW_25_in_exactFieldExpression257);  
            stream_25.add(char_literal14);

            pushFollow(FOLLOW_term_in_exactFieldExpression261);
            y=term();

            state._fsp--;

            stream_term.add(y.getTree());


            // AST REWRITE
            // elements: x, y
            // token labels: x
            // rule labels: retval, y
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleTokenStream stream_x=new RewriteRuleTokenStream(adaptor,"token x",x);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_y=new RewriteRuleSubtreeStream(adaptor,"rule y",y!=null?y.tree:null);

            root_0 = (Object)adaptor.nil();
            // 35:46: -> ^( EXACTFIELD $x $y)
            {
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:35:49: ^( EXACTFIELD $x $y)
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXACTFIELD, "EXACTFIELD"), root_1);

                adaptor.addChild(root_1, stream_x.nextNode());
                adaptor.addChild(root_1, stream_y.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "exactFieldExpression"

    public static class fieldExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldExpression"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:37:1: fieldExpression : x= STRING ':' y= term -> ^( FIELD $x $y) ;
    public final FilterParser.fieldExpression_return fieldExpression() throws RecognitionException {
        FilterParser.fieldExpression_return retval = new FilterParser.fieldExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token x=null;
        Token char_literal15=null;
        FilterParser.term_return y = null;


        Object x_tree=null;
        Object char_literal15_tree=null;
        RewriteRuleTokenStream stream_26=new RewriteRuleTokenStream(adaptor,"token 26");
        RewriteRuleTokenStream stream_STRING=new RewriteRuleTokenStream(adaptor,"token STRING");
        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:37:18: (x= STRING ':' y= term -> ^( FIELD $x $y) )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:37:21: x= STRING ':' y= term
            {
            x=(Token)match(input,STRING,FOLLOW_STRING_in_fieldExpression285);  
            stream_STRING.add(x);

            char_literal15=(Token)match(input,26,FOLLOW_26_in_fieldExpression287);  
            stream_26.add(char_literal15);

            pushFollow(FOLLOW_term_in_fieldExpression291);
            y=term();

            state._fsp--;

            stream_term.add(y.getTree());


            // AST REWRITE
            // elements: x, y
            // token labels: x
            // rule labels: retval, y
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleTokenStream stream_x=new RewriteRuleTokenStream(adaptor,"token x",x);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_y=new RewriteRuleSubtreeStream(adaptor,"rule y",y!=null?y.tree:null);

            root_0 = (Object)adaptor.nil();
            // 37:41: -> ^( FIELD $x $y)
            {
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:37:44: ^( FIELD $x $y)
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(FIELD, "FIELD"), root_1);

                adaptor.addChild(root_1, stream_x.nextNode());
                adaptor.addChild(root_1, stream_y.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "fieldExpression"

    public static class parExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "parExpression"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:39:1: parExpression : '(' e= expression ')' -> ^( BLOCK $e) ;
    public final FilterParser.parExpression_return parExpression() throws RecognitionException {
        FilterParser.parExpression_return retval = new FilterParser.parExpression_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal16=null;
        Token char_literal17=null;
        FilterParser.expression_return e = null;


        Object char_literal16_tree=null;
        Object char_literal17_tree=null;
        RewriteRuleTokenStream stream_27=new RewriteRuleTokenStream(adaptor,"token 27");
        RewriteRuleTokenStream stream_28=new RewriteRuleTokenStream(adaptor,"token 28");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:39:16: ( '(' e= expression ')' -> ^( BLOCK $e) )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:39:18: '(' e= expression ')'
            {
            char_literal16=(Token)match(input,27,FOLLOW_27_in_parExpression312);  
            stream_27.add(char_literal16);

            pushFollow(FOLLOW_expression_in_parExpression316);
            e=expression();

            state._fsp--;

            stream_expression.add(e.getTree());
            char_literal17=(Token)match(input,28,FOLLOW_28_in_parExpression318);  
            stream_28.add(char_literal17);



            // AST REWRITE
            // elements: e
            // token labels: 
            // rule labels: retval, e
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_e=new RewriteRuleSubtreeStream(adaptor,"rule e",e!=null?e.tree:null);

            root_0 = (Object)adaptor.nil();
            // 39:39: -> ^( BLOCK $e)
            {
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:39:43: ^( BLOCK $e)
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(BLOCK, "BLOCK"), root_1);

                adaptor.addChild(root_1, stream_e.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "parExpression"

    public static class implicitAnd_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "implicitAnd"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:1: implicitAnd : (x= term -> ^( SIMPLE $x) ) (y= term -> ^( AND $implicitAnd ^( SIMPLE $y) ) )+ ;
    public final FilterParser.implicitAnd_return implicitAnd() throws RecognitionException {
        FilterParser.implicitAnd_return retval = new FilterParser.implicitAnd_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FilterParser.term_return x = null;

        FilterParser.term_return y = null;


        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:13: ( (x= term -> ^( SIMPLE $x) ) (y= term -> ^( AND $implicitAnd ^( SIMPLE $y) ) )+ )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:15: (x= term -> ^( SIMPLE $x) ) (y= term -> ^( AND $implicitAnd ^( SIMPLE $y) ) )+
            {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:15: (x= term -> ^( SIMPLE $x) )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:16: x= term
            {
            pushFollow(FOLLOW_term_in_implicitAnd340);
            x=term();

            state._fsp--;

            stream_term.add(x.getTree());


            // AST REWRITE
            // elements: x
            // token labels: 
            // rule labels: retval, x
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_x=new RewriteRuleSubtreeStream(adaptor,"rule x",x!=null?x.tree:null);

            root_0 = (Object)adaptor.nil();
            // 41:23: -> ^( SIMPLE $x)
            {
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:26: ^( SIMPLE $x)
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SIMPLE, "SIMPLE"), root_1);

                adaptor.addChild(root_1, stream_x.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:40: (y= term -> ^( AND $implicitAnd ^( SIMPLE $y) ) )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>=STRING && LA6_0<=NUMBER)) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:42: y= term
            	    {
            	    pushFollow(FOLLOW_term_in_implicitAnd356);
            	    y=term();

            	    state._fsp--;

            	    stream_term.add(y.getTree());


            	    // AST REWRITE
            	    // elements: y, implicitAnd
            	    // token labels: 
            	    // rule labels: retval, y
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            	    RewriteRuleSubtreeStream stream_y=new RewriteRuleSubtreeStream(adaptor,"rule y",y!=null?y.tree:null);

            	    root_0 = (Object)adaptor.nil();
            	    // 41:49: -> ^( AND $implicitAnd ^( SIMPLE $y) )
            	    {
            	        // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:52: ^( AND $implicitAnd ^( SIMPLE $y) )
            	        {
            	        Object root_1 = (Object)adaptor.nil();
            	        root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(AND, "AND"), root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());
            	        // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:41:71: ^( SIMPLE $y)
            	        {
            	        Object root_2 = (Object)adaptor.nil();
            	        root_2 = (Object)adaptor.becomeRoot((Object)adaptor.create(SIMPLE, "SIMPLE"), root_2);

            	        adaptor.addChild(root_2, stream_y.nextTree());

            	        adaptor.addChild(root_1, root_2);
            	        }

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }

            	    retval.tree = root_0;
            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "implicitAnd"

    public static class plainTerm_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "plainTerm"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:43:1: plainTerm : s= term -> ^( SIMPLE $s) ;
    public final FilterParser.plainTerm_return plainTerm() throws RecognitionException {
        FilterParser.plainTerm_return retval = new FilterParser.plainTerm_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        FilterParser.term_return s = null;


        RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:43:13: (s= term -> ^( SIMPLE $s) )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:43:15: s= term
            {
            pushFollow(FOLLOW_term_in_plainTerm387);
            s=term();

            state._fsp--;

            stream_term.add(s.getTree());


            // AST REWRITE
            // elements: s
            // token labels: 
            // rule labels: retval, s
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_s=new RewriteRuleSubtreeStream(adaptor,"rule s",s!=null?s.tree:null);

            root_0 = (Object)adaptor.nil();
            // 43:22: -> ^( SIMPLE $s)
            {
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:43:25: ^( SIMPLE $s)
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SIMPLE, "SIMPLE"), root_1);

                adaptor.addChild(root_1, stream_s.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "plainTerm"

    public static class term_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "term"
    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:45:1: term : s= ( STRING | NUMBER ) ;
    public final FilterParser.term_return term() throws RecognitionException {
        FilterParser.term_return retval = new FilterParser.term_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token s=null;

        Object s_tree=null;

        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:45:8: (s= ( STRING | NUMBER ) )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:45:10: s= ( STRING | NUMBER )
            {
            root_0 = (Object)adaptor.nil();

            s=(Token)input.LT(1);
            if ( (input.LA(1)>=STRING && input.LA(1)<=NUMBER) ) {
                input.consume();
                adaptor.addChild(root_0, (Object)adaptor.create(s));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "term"

    // Delegated rules


 

    public static final BitSet FOLLOW_expression_in_root82 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_orExpression_in_expression95 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_andExpression_in_orExpression111 = new BitSet(new long[]{0x0000000000380002L});
    public static final BitSet FOLLOW_19_in_orExpression121 = new BitSet(new long[]{0x0000000008001800L});
    public static final BitSet FOLLOW_20_in_orExpression123 = new BitSet(new long[]{0x0000000008001800L});
    public static final BitSet FOLLOW_21_in_orExpression125 = new BitSet(new long[]{0x0000000008001800L});
    public static final BitSet FOLLOW_andExpression_in_orExpression130 = new BitSet(new long[]{0x0000000000380002L});
    public static final BitSet FOLLOW_primary_in_andExpression160 = new BitSet(new long[]{0x0000000001C00002L});
    public static final BitSet FOLLOW_22_in_andExpression170 = new BitSet(new long[]{0x0000000008001800L});
    public static final BitSet FOLLOW_23_in_andExpression172 = new BitSet(new long[]{0x0000000008001800L});
    public static final BitSet FOLLOW_24_in_andExpression174 = new BitSet(new long[]{0x0000000008001800L});
    public static final BitSet FOLLOW_primary_in_andExpression179 = new BitSet(new long[]{0x0000000001C00002L});
    public static final BitSet FOLLOW_parExpression_in_primary206 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldExpression_in_primary214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exactFieldExpression_in_primary222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_implicitAnd_in_primary230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_plainTerm_in_primary238 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_exactFieldExpression255 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_25_in_exactFieldExpression257 = new BitSet(new long[]{0x0000000000001800L});
    public static final BitSet FOLLOW_term_in_exactFieldExpression261 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_fieldExpression285 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_fieldExpression287 = new BitSet(new long[]{0x0000000000001800L});
    public static final BitSet FOLLOW_term_in_fieldExpression291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_parExpression312 = new BitSet(new long[]{0x0000000008001800L});
    public static final BitSet FOLLOW_expression_in_parExpression316 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_parExpression318 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_implicitAnd340 = new BitSet(new long[]{0x0000000000001800L});
    public static final BitSet FOLLOW_term_in_implicitAnd356 = new BitSet(new long[]{0x0000000000001802L});
    public static final BitSet FOLLOW_term_in_plainTerm387 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_term408 = new BitSet(new long[]{0x0000000000000002L});

}