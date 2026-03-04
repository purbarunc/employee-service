package com.purbarun.employee.enums;

/**
 * Enum representing the available bulk creation strategies.
 * Provides type-safe strategy identification for bulk employee creation operations.
 */
public enum BulkCreationStrategy {
    ASYNC("ASYNC"),
    BATCH("BATCH");
    
    private final String displayName;
    
    BulkCreationStrategy(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the strategy from its display name.
     * 
     * @param displayName The display name to look up
     * @return The corresponding BulkCreationStrategy
     * @throws IllegalArgumentException if no strategy matches the display name
     */
    public static BulkCreationStrategy fromDisplayName(String displayName) {
        for (BulkCreationStrategy strategy : values()) {
            if (strategy.displayName.equals(displayName)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown strategy display name: " + displayName);
    }
}
