grammar Filter;
options { 
	output=AST;
}

tokens {
	AND;
	BLOCK;
	FIELD;
	EXACTFIELD;
	OR;
	SIMPLE;
	TERM;
}

@parser::header { package org.tolven.app.filter; }
@lexer::header { package org.tolven.app.filter; }
//AND (md IN (SELECT mdw%d.menuData FROM MenuDataWord mdw%d WHERE mdw%d.menuStructure = :m and mdw%d.word BETWEEN :wfl%d AND :wfh%d))
// java -cp C:\antlr\antlr-3.3-complete.jar org.antlr.Tool ejb\source\org\tolven\app\filter\Filter2.g -report
root           : expression ;

expression     : orExpression ;

orExpression   :   (x=andExpression->$x ) ( ('or'|'OR'|'Or') y=andExpression -> ^(OR $orExpression $y) )* ;

andExpression  :  ( x=primary->$x ) ( ('and'|'AND'|'And') y=primary -> ^( AND $andExpression $y ) )*;

primary   : parExpression
		  | fieldExpression
		  | exactFieldExpression
		  | implicitAnd
		  | plainTerm
		  ;

exactFieldExpression  :  x=STRING '=' y=term -> ^(EXACTFIELD $x $y);

fieldExpression  :  x=STRING ':' y=term -> ^(FIELD $x $y);

parExpression  : '(' e=expression ')' ->  ^(BLOCK $e) ;

implicitAnd : (x=term -> ^(SIMPLE $x)) ( y=term -> ^(AND $implicitAnd ^(SIMPLE $y)))+ ;

plainTerm	  : s=term -> ^(SIMPLE $s);

term	  : s=(STRING|NUMBER);

NUMBER	: (DIGIT)+ ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

STRING
    :  ( '0'..'9'|'a'..'z'|'A'..'Z' )* 
    ;


fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;
fragment DIGIT	: '0'..'9' ;
