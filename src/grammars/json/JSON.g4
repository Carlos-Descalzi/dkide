
/** Taken from "The Definitive ANTLR 4 Reference" by Terence Parr */

// Derived from http://json.org
grammar JSON;

@header {
package io.datakitchen.ide.json.parser;
}

json
   : value
   ;

obj
   : '{' pair (',' pair)* '}'
   | '{' '}'
   ;

pair
   : STRING ':' value
   ;

arr
   : '[' value (',' value)* ']'
   | '[' ']'
   ;

varRef: '{{' ID '}}'
   ;

value
   : STRING
   | NUMBER
   | varRef
   | obj
   | arr
   | BOOL
   | NULL
   ;

NULL: 'null'
    ;

BOOL: 'true'
    | 'false'
    ;


STRING
   : '"' (ESC | SAFECODEPOINT)* '"'
   ;

ID: [a-zA-Z] ([0-9] | '.' | '[' | ']' | '\'' | [a-zA-Z])*
    ;

fragment ESC
   : '\\' (["\\/bfnrt] | UNICODE)
   ;


fragment UNICODE
   : 'u' HEX HEX HEX HEX
   ;


fragment HEX
   : [0-9a-fA-F]
   ;


fragment SAFECODEPOINT
   : ~ ["\\\u0000-\u001F]
   ;


NUMBER
   : '-'? INT ('.' [0-9] +)? EXP?
   ;


fragment INT
   : '0' | [1-9] [0-9]*
   ;

// no leading zeros

fragment EXP
   : [Ee] [+\-]? INT
   ;

// \- since - means "range" inside [...]

WS
   : [ \t\n\r] + -> skip
   ;