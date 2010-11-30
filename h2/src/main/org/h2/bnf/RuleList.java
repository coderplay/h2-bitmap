/*
 * Copyright 2004-2010 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.bnf;

import java.util.ArrayList;
import java.util.HashMap;
import org.h2.util.New;

/**
 * Represents a sequence of BNF rules, or a list of alternative rules.
 */
public class RuleList implements Rule {

    private boolean or;
    private ArrayList<Rule> list;
    private boolean mapSet;

    RuleList(Rule first, Rule next, boolean or) {
        list = New.arrayList();
        if (first instanceof RuleList && ((RuleList) first).or == or) {
            list.addAll(((RuleList) first).list);
        } else {
            list.add(first);
        }
        if (next instanceof RuleList && ((RuleList) next).or == or) {
            list.addAll(((RuleList) next).list);
        } else {
            list.add(next);
        }
        this.or = or;
    }

    public void accept(BnfVisitor visitor) {
        visitor.visitRuleList(or, list);
    }

    public String name() {
        return null;
    }

    public void setLinks(HashMap<String, RuleHead> ruleMap) {
        if (!mapSet) {
            for (Rule r : list) {
                r.setLinks(ruleMap);
            }
            mapSet = true;
        }
    }

    public boolean autoComplete(Sentence sentence) {
        if (sentence.shouldStop()) {
            return false;
        }
        String old = sentence.getQuery();
        if (or) {
            for (Rule r : list) {
                sentence.setQuery(old);
                if (r.autoComplete(sentence)) {
                    return true;
                }
            }
            return false;
        } else {
            for (Rule r : list) {
                if (!r.autoComplete(sentence)) {
                    sentence.setQuery(old);
                    return false;
                }
            }
            return true;
        }
    }

    public String toString() {
        return or ? "or: " : "" + list.toString();
    }

}
