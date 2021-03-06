package yieldide.scanner;

import beaver.Symbol;
import beaver.Scanner;
import yieldide.parser.JavaParser.Terminals;
import java.io.*;
import java.util.HashMap;

%%

%public 
%final 
%class JavaScanner
%extends Scanner

%type Symbol 
%function nextToken 
%yylexthrow Scanner.Exception

%unicode
%line %column %char

%{
  StringBuffer strbuf = new StringBuffer(128);
  int sub_line;
  int sub_column;

  private Symbol sym(short id) {
    return new Symbol(id, yyline + 1, yycolumn + 1, len(), str());
  }

  private Symbol sym(short id, String value) {
    return new Symbol(id, yyline + 1, yycolumn + 1, len(), value);
  }

  private String str() { return yytext(); }
  private int len() { return yylength(); }

  private void error(String msg) throws Scanner.Exception {
    throw new Scanner.Exception(yyline + 1, yycolumn + 1, msg);
  }
  
  private HashMap comments = new HashMap();
  public HashMap comments() { return comments; }
  private void registerComment() {
    String comment = str();
    int line = yyline;
    // the extra loop accounts from yyline starting at 0
    int pos = 0;
    do {
      line++;
      pos = comment.indexOf('\n', pos + 1);
    } while(pos != -1);
    comments.put(new Integer(line), str());
  }

  private HashMap offsets = new java.util.LinkedHashMap();
  public HashMap offsets() { return offsets; }
  private void registerOffset() {
    Integer key = new Integer(yyline + 2);
    Integer value = new Integer(yychar + len());
    offsets.put(key, value);
  }
  
%}


