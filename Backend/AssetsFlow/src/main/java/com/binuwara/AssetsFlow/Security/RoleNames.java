package com.binuwara.AssetsFlow.Security;

import java.util.Locale;

public final class RoleNames {
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ADMIN = "ADMIN";
    public static final String DEPARTMENT_HEAD = "DEPARTMENT_HEAD";
    public static final String USER = "USER";

    private RoleNames() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "DEP_HEAD", "DEPARTMENTHEAD" -> DEPARTMENT_HEAD;
            case "USERS" -> USER;
            case "SUPERADMIN" -> SUPER_ADMIN;
            default -> normalized;
        };
    }
}
