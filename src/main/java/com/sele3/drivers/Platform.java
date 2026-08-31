package com.sele3.drivers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Platform {
    CHROME,
    FIREFOX,
    EDGE,
    ;

    /**
     * Resolves a {@link Platform} from its name, case-insensitively.
     *
     * @param platform the platform name (e.g. "chrome", "Firefox", "EDGE")
     * @return the matching {@link Platform}
     * @throws IllegalArgumentException if no matching platform exists
     */
    public static Platform fromString(String platform) {
        return Platform.valueOf(platform.trim().toUpperCase());
    }
}
