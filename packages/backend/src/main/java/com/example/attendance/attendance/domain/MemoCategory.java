package com.example.attendance.attendance.domain;

import java.util.Set;

public enum MemoCategory {

    DIRECT_GO("直行"),
    TRAIN_DELAY("電車遅延"),
    REMOTE("在宅"),
    INCIDENT("障害対応"),
    OTHER("その他"),
    DIRECT_RETURN("直帰"),
    EARLY_LEAVE("早退"),
    OUT_OF_OFFICE("外出");

    private static final Set<MemoCategory> CLOCK_IN_CATEGORIES = Set.of(
            DIRECT_GO, TRAIN_DELAY, REMOTE, INCIDENT, OTHER
    );

    private static final Set<MemoCategory> CLOCK_OUT_CATEGORIES = Set.of(
            DIRECT_RETURN, EARLY_LEAVE, OUT_OF_OFFICE, REMOTE, INCIDENT, OTHER
    );

    private final String label;

    MemoCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isValidFor(MemoType memoType) {
        return switch (memoType) {
            case CLOCK_IN -> CLOCK_IN_CATEGORIES.contains(this);
            case CLOCK_OUT -> CLOCK_OUT_CATEGORIES.contains(this);
        };
    }

    public static Set<MemoCategory> categoriesFor(MemoType memoType) {
        return switch (memoType) {
            case CLOCK_IN -> Set.copyOf(CLOCK_IN_CATEGORIES);
            case CLOCK_OUT -> Set.copyOf(CLOCK_OUT_CATEGORIES);
        };
    }
}
