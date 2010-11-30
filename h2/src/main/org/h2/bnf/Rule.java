/*
 * Copyright 2004-2010 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.bnf;

import java.util.HashMap;

/**
 * Represents a BNF rule.
 */
public interface Rule {

    /**
     * Get the name of the rule.
     *
     * @return the name
     */
    String name();

    /**
     * Update cross references.
     *
     * @param ruleMap the reference map
     */
    void setLinks(HashMap<String, RuleHead> ruleMap);

    /**
     * Add the next possible token(s). If there was a match, the query in the
     * sentence is updated (the matched token is removed).
     *
     * @param sentence the sentence context
     * @return true if a full match
     */
    boolean autoComplete(Sentence sentence);

    /**
     * Call the visit method in the given visitor.
     *
     * @param visitor the visitor
     */
    void accept(BnfVisitor visitor);

}
