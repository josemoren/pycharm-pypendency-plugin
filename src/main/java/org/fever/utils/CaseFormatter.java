package org.fever.utils;

public class CaseFormatter {
    public static String camelCaseToSnakeCase(String fqn) {
        return fqn.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }
}
