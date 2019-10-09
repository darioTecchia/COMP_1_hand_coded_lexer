# YASP

Programming language definited for the Compiler course from University.

## Lexical Specification

|Token |Lexeme|Attribute|
|------|------|---------|
|**Delimiters**|
|*-*|Any WS|-|
|**Separators**|
|LPAR|(|-|
|RPAR|)|-|
|LBRA|{|-|
|RBRA|}|-|
|COMMA|,|-|
|SEMI|;|-|
|**Keywords**|
|IF|if|-|
|THEN|then|-|
|ELSE|else|-|
|WHILE|while|-|
|**Identifiers**|
|Any ID|id|Pointer to table entry|
|**Numbers**|
|Any number|NUM|Pointer to table entry|
|**Relops**|
|<|relop|LT|
|<=|relop|LE|
|=|relop|EQ|
|<>|relop|NE|
|>|relop|GT|
|>=|relop|GE|
|<--|relop|ASSIGN|

## Grammar Specification

--

## Notes
The number DFA implementation is from the Book.