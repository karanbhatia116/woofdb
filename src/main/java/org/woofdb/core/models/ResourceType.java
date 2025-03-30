package org.woofdb.core.models;

public enum ResourceType {
    DATABASE,
    TABLE,
    INDEX;


    public static ResourceType from(String value) {
        return ResourceType.valueOf(value.toUpperCase());
    }
}
