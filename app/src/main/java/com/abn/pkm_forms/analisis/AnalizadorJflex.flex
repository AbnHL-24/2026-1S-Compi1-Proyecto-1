package com.abn.pkm_forms.analisis;

import java_cup.runtime.Symbol;
import java.util.LinkedList;

%%
%{
    public LinkedList<ErrorLexico> erroresLexicos = new LinkedList<>();

    private Symbol simbolo(int tipo) {
        return new Symbol(tipo, yyline + 1, yycolumn + 1, yytext());
    }

    private Symbol simboloCadena() {
        String valor = yytext();
        valor = valor.substring(1, valor.length() - 1);
        return new Symbol(sym.CADENA, yyline + 1, yycolumn + 1, valor);
    }
%}

%init{
    yyline = 0;
    yycolumn = 0;
    erroresLexicos = new LinkedList<>();
%init}

%cup
%class AnalizadorJflex
%public
%line
%column
%char
%full
%ignorecase

ESPACIOS = [ \t\r\f\n]+
ID = [a-zA-Z_][a-zA-Z0-9_]*
DECIMAL = [0-9]+\.[0-9]+
ENTERO = [0-9]+
CADENA = [\"]([^\"\n]|\\\")*[\"]

SECTION = "SECTION"
TEXT = "TEXT"
OPEN_QUESTION = "OPEN_QUESTION"
DROP_QUESTION = "DROP_QUESTION"
SELECT_QUESTION = "SELECT_QUESTION"
MULTIPLE_QUESTION = "MULTIPLE_QUESTION"
NUMBER = "number"
STRING = "string"
TRUE = "true"
FALSE = "false"
OPTIONS = "options"
CORRECT = "correct"

MAS = "+"
MENOS = "-"
MULT = "*"
DIV = "/"
MOD = "%"
NOT = "~"
OR = "||"
AND = "&&"
IGUALDAD = "=="
DIFERENTE = "!="
MAYOR_IGUAL = ">="
MENOR_IGUAL = "<="
MAYOR = ">"
MENOR = "<"
ASIGNACION = "="
PUNTO_Y_COMA = ";"
DOS_PUNTOS = ":"
COMA = ","
PARENTESIS_IZQ = "("
PARENTESIS_DER = ")"
LLAVE_IZQ = "{"
LLAVE_DER = "}"
CORCHETE_IZQ = "["
CORCHETE_DER = "]"

%%

<YYINITIAL> {SECTION} { return simbolo(sym.SECTION); }
<YYINITIAL> {TEXT} { return simbolo(sym.TEXT); }
<YYINITIAL> {OPEN_QUESTION} { return simbolo(sym.OPEN_QUESTION); }
<YYINITIAL> {DROP_QUESTION} { return simbolo(sym.DROP_QUESTION); }
<YYINITIAL> {SELECT_QUESTION} { return simbolo(sym.SELECT_QUESTION); }
<YYINITIAL> {MULTIPLE_QUESTION} { return simbolo(sym.MULTIPLE_QUESTION); }
<YYINITIAL> {NUMBER} { return simbolo(sym.NUMBER); }
<YYINITIAL> {STRING} { return simbolo(sym.STRING); }
<YYINITIAL> {TRUE} { return simbolo(sym.TRUE); }
<YYINITIAL> {FALSE} { return simbolo(sym.FALSE); }
<YYINITIAL> {OPTIONS} { return simbolo(sym.OPTIONS); }
<YYINITIAL> {CORRECT} { return simbolo(sym.CORRECT); }

<YYINITIAL> {MAS} { return simbolo(sym.MAS); }
<YYINITIAL> {MENOS} { return simbolo(sym.MENOS); }
<YYINITIAL> {MULT} { return simbolo(sym.MULT); }
<YYINITIAL> {DIV} { return simbolo(sym.DIV); }
<YYINITIAL> {MOD} { return simbolo(sym.MOD); }
<YYINITIAL> {NOT} { return simbolo(sym.NOT); }
<YYINITIAL> {OR} { return simbolo(sym.OR); }
<YYINITIAL> {AND} { return simbolo(sym.AND); }
<YYINITIAL> {IGUALDAD} { return simbolo(sym.IGUALDAD); }
<YYINITIAL> {DIFERENTE} { return simbolo(sym.DIFERENTE); }
<YYINITIAL> {MAYOR_IGUAL} { return simbolo(sym.MAYOR_IGUAL); }
<YYINITIAL> {MENOR_IGUAL} { return simbolo(sym.MENOR_IGUAL); }
<YYINITIAL> {MAYOR} { return simbolo(sym.MAYOR); }
<YYINITIAL> {MENOR} { return simbolo(sym.MENOR); }
<YYINITIAL> {ASIGNACION} { return simbolo(sym.ASIGNACION); }
<YYINITIAL> {PUNTO_Y_COMA} { return simbolo(sym.PUNTO_Y_COMA); }
<YYINITIAL> {DOS_PUNTOS} { return simbolo(sym.DOS_PUNTOS); }
<YYINITIAL> {COMA} { return simbolo(sym.COMA); }
<YYINITIAL> {PARENTESIS_IZQ} { return simbolo(sym.PARENTESIS_IZQ); }
<YYINITIAL> {PARENTESIS_DER} { return simbolo(sym.PARENTESIS_DER); }
<YYINITIAL> {LLAVE_IZQ} { return simbolo(sym.LLAVE_IZQ); }
<YYINITIAL> {LLAVE_DER} { return simbolo(sym.LLAVE_DER); }
<YYINITIAL> {CORCHETE_IZQ} { return simbolo(sym.CORCHETE_IZQ); }
<YYINITIAL> {CORCHETE_DER} { return simbolo(sym.CORCHETE_DER); }

<YYINITIAL> "$"[^\n]* { }
<YYINITIAL> "/*"([^*]|\*+[^*/])*\*+"/" { }
<YYINITIAL> {CADENA} { return simboloCadena(); }
<YYINITIAL> {DECIMAL} { return simbolo(sym.DECIMAL); }
<YYINITIAL> {ENTERO} { return simbolo(sym.ENTERO); }
<YYINITIAL> {ID} { return simbolo(sym.ID); }
<YYINITIAL> {ESPACIOS} { }

<YYINITIAL> . {
    erroresLexicos.add(new ErrorLexico(
        "Caracter no reconocido: '" + yytext() + "'",
        yyline + 1,
        yycolumn + 1
    ));
}
