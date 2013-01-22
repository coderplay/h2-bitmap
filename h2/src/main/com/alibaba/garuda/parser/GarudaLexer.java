/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.alibaba.garuda.parser;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.sql.parser.Keywords;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.Token;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class GarudaLexer extends Lexer {

    public final static Keywords DEFAULT_GARUDA_KEYWORDS;

    static {
        Map<String, Token> map = new HashMap<String, Token>();

        map.putAll(Keywords.DEFAULT_KEYWORDS.getKeywords());

        map.put("FALSE", Token.FALSE);
        map.put("LIMIT", Token.LIMIT);
        map.put("TRUE", Token.TRUE);

        DEFAULT_GARUDA_KEYWORDS = new Keywords(map);
    }

    public GarudaLexer(char[] input, int inputLength, boolean skipComment) {
        super(input, inputLength, skipComment);
        super.keywods = DEFAULT_GARUDA_KEYWORDS;
    }


    public GarudaLexer(String input) {
        super(input);
        super.keywods = DEFAULT_GARUDA_KEYWORDS;
    }
    
    
}
