package org.fever.utils;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassArgumentParser {
    /**
     * Example input: "logger: Logger,"
     * Example output: "Logger"
     *
     * @param argumentLine Argument line to parse
     *
     * @return Class name of the argument
     */
    public static @Nullable String parse(String argumentLine) {
        Matcher argumentTypeMatcher = Pattern.compile("\\w+:\\s*(\\w+)").matcher(argumentLine);
        return argumentTypeMatcher.find() ? argumentTypeMatcher.group(1) : null;
    }
}
