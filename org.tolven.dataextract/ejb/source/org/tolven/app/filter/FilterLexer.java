// $ANTLR 3.3 Nov 30, 2010 12:50:56 ejb\\source\\org\\tolven\\app\\filter\\Filter.g 2011-06-12 00:36:17
 package org.tolven.app.filter; 

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class FilterLexer extends Lexer {
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

    public FilterLexer() {;} 
    public FilterLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public FilterLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "ejb\\source\\org\\tolven\\app\\filter\\Filter.g"; }

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:5:7: ( 'or' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:5:9: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:6:7: ( 'OR' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:6:9: 'OR'
            {
            match("OR"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:7:7: ( 'Or' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:7:9: 'Or'
            {
            match("Or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:8:7: ( 'and' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:8:9: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:9:7: ( 'AND' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:9:9: 'AND'
            {
            match("AND"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:10:7: ( 'And' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:10:9: 'And'
            {
            match("And"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:11:7: ( '=' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:11:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:12:7: ( ':' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:12:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:13:7: ( '(' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:13:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:14:7: ( ')' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:14:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:47:8: ( ( DIGIT )+ )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:47:10: ( DIGIT )+
            {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:47:10: ( DIGIT )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:47:11: DIGIT
            	    {
            	    mDIGIT(); 

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMBER"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:49:5: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:49:9: ( ' ' | '\\t' | '\\r' | '\\n' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:57:5: ( ( '0' .. '9' | 'a' .. 'z' | 'A' .. 'Z' )* )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:57:8: ( '0' .. '9' | 'a' .. 'z' | 'A' .. 'Z' )*
            {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:57:8: ( '0' .. '9' | 'a' .. 'z' | 'A' .. 'Z' )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "HEX_DIGIT"
    public final void mHEX_DIGIT() throws RecognitionException {
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:62:11: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:62:13: ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' )
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "HEX_DIGIT"

    // $ANTLR start "ESC_SEQ"
    public final void mESC_SEQ() throws RecognitionException {
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:66:5: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UNICODE_ESC | OCTAL_ESC )
            int alt3=3;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='\\') ) {
                switch ( input.LA(2) ) {
                case '\"':
                case '\'':
                case '\\':
                case 'b':
                case 'f':
                case 'n':
                case 'r':
                case 't':
                    {
                    alt3=1;
                    }
                    break;
                case 'u':
                    {
                    alt3=2;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    {
                    alt3=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:66:9: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 
                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:67:9: UNICODE_ESC
                    {
                    mUNICODE_ESC(); 

                    }
                    break;
                case 3 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:68:9: OCTAL_ESC
                    {
                    mOCTAL_ESC(); 

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "ESC_SEQ"

    // $ANTLR start "OCTAL_ESC"
    public final void mOCTAL_ESC() throws RecognitionException {
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:73:5: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt4=3;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='\\') ) {
                int LA4_1 = input.LA(2);

                if ( ((LA4_1>='0' && LA4_1<='3')) ) {
                    int LA4_2 = input.LA(3);

                    if ( ((LA4_2>='0' && LA4_2<='7')) ) {
                        int LA4_4 = input.LA(4);

                        if ( ((LA4_4>='0' && LA4_4<='7')) ) {
                            alt4=1;
                        }
                        else {
                            alt4=2;}
                    }
                    else {
                        alt4=3;}
                }
                else if ( ((LA4_1>='4' && LA4_1<='7')) ) {
                    int LA4_3 = input.LA(3);

                    if ( ((LA4_3>='0' && LA4_3<='7')) ) {
                        alt4=2;
                    }
                    else {
                        alt4=3;}
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:73:9: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:73:14: ( '0' .. '3' )
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:73:15: '0' .. '3'
                    {
                    matchRange('0','3'); 

                    }

                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:73:25: ( '0' .. '7' )
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:73:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:73:36: ( '0' .. '7' )
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:73:37: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 2 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:74:9: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:74:14: ( '0' .. '7' )
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:74:15: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:74:25: ( '0' .. '7' )
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:74:26: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 3 :
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:75:9: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:75:14: ( '0' .. '7' )
                    // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:75:15: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "OCTAL_ESC"

    // $ANTLR start "UNICODE_ESC"
    public final void mUNICODE_ESC() throws RecognitionException {
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:80:5: ( '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:80:9: '\\\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
            {
            match('\\'); 
            match('u'); 
            mHEX_DIGIT(); 
            mHEX_DIGIT(); 
            mHEX_DIGIT(); 
            mHEX_DIGIT(); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "UNICODE_ESC"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:82:16: ( '0' .. '9' )
            // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:82:18: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "DIGIT"

    public void mTokens() throws RecognitionException {
        // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:8: ( T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | NUMBER | WS | STRING )
        int alt5=13;
        alt5 = dfa5.predict(input);
        switch (alt5) {
            case 1 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:10: T__19
                {
                mT__19(); 

                }
                break;
            case 2 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:16: T__20
                {
                mT__20(); 

                }
                break;
            case 3 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:22: T__21
                {
                mT__21(); 

                }
                break;
            case 4 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:28: T__22
                {
                mT__22(); 

                }
                break;
            case 5 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:34: T__23
                {
                mT__23(); 

                }
                break;
            case 6 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:40: T__24
                {
                mT__24(); 

                }
                break;
            case 7 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:46: T__25
                {
                mT__25(); 

                }
                break;
            case 8 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:52: T__26
                {
                mT__26(); 

                }
                break;
            case 9 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:58: T__27
                {
                mT__27(); 

                }
                break;
            case 10 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:64: T__28
                {
                mT__28(); 

                }
                break;
            case 11 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:70: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 12 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:77: WS
                {
                mWS(); 

                }
                break;
            case 13 :
                // ejb\\source\\org\\tolven\\app\\filter\\Filter.g:1:80: STRING
                {
                mSTRING(); 

                }
                break;

        }

    }


    protected DFA5 dfa5 = new DFA5(this);
    static final String DFA5_eotS =
        "\5\13\4\uffff\1\22\2\uffff\1\23\1\24\1\25\3\13\4\uffff\1\31\1\32"+
        "\1\33\3\uffff";
    static final String DFA5_eofS =
        "\34\uffff";
    static final String DFA5_minS =
        "\1\11\1\162\1\122\1\156\1\116\4\uffff\1\60\2\uffff\3\60\1\144\1"+
        "\104\1\144\4\uffff\3\60\3\uffff";
    static final String DFA5_maxS =
        "\1\157\2\162\2\156\4\uffff\1\172\2\uffff\3\172\1\144\1\104\1\144"+
        "\4\uffff\3\172\3\uffff";
    static final String DFA5_acceptS =
        "\5\uffff\1\7\1\10\1\11\1\12\1\uffff\1\14\1\15\6\uffff\1\13\1\1\1"+
        "\2\1\3\3\uffff\1\4\1\5\1\6";
    static final String DFA5_specialS =
        "\34\uffff}>";
    static final String[] DFA5_transitionS = {
            "\2\12\2\uffff\1\12\22\uffff\1\12\7\uffff\1\7\1\10\6\uffff\12"+
            "\11\1\6\2\uffff\1\5\3\uffff\1\4\15\uffff\1\2\21\uffff\1\3\15"+
            "\uffff\1\1",
            "\1\14",
            "\1\15\37\uffff\1\16",
            "\1\17",
            "\1\20\37\uffff\1\21",
            "",
            "",
            "",
            "",
            "\12\11\7\uffff\32\13\6\uffff\32\13",
            "",
            "",
            "\12\13\7\uffff\32\13\6\uffff\32\13",
            "\12\13\7\uffff\32\13\6\uffff\32\13",
            "\12\13\7\uffff\32\13\6\uffff\32\13",
            "\1\26",
            "\1\27",
            "\1\30",
            "",
            "",
            "",
            "",
            "\12\13\7\uffff\32\13\6\uffff\32\13",
            "\12\13\7\uffff\32\13\6\uffff\32\13",
            "\12\13\7\uffff\32\13\6\uffff\32\13",
            "",
            "",
            ""
    };

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | NUMBER | WS | STRING );";
        }
    }
 

}