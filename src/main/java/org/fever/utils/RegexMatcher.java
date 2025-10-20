package org.fever.utils;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class RegexMatcher {
    public static String[] getAllMatches(String text, String regex) {
        return Pattern.compile(regex)
            .matcher(text)
            .results()
            .map(MatchResult::group)
            .toArray(String[]::new);
    }
}
