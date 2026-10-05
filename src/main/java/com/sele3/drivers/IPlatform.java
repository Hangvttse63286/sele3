package com.sele3.drivers;

import java.util.Locale;
import java.util.ServiceLoader;

public interface IPlatform {

    /**
     * The stable name used to select this platform from configuration (e.g. a system property),
     * matched case-insensitively by {@link #fromString}. Enum implementations get this for free
     * from {@link Enum#name()}.
     *
     * @return this platform's name
     */
    String name();

    /**
     * The config file loaded for this platform by {@link com.sele3.configs.ConfigLoader} when no
     * explicit config file is specified: this platform's lower-case {@link #name()} plus
     * {@code .json} (e.g. {@code chrome.json}). Override it to use a different file name.
     *
     * @return the default config file name for this platform
     */
    default String getDefaultConfigFile() {
        return name().toLowerCase(Locale.ROOT) + ".json";
    }

    /**
     * Resolves an {@link IPlatform} by name, discovering candidates the same way
     * {@link DriverFactory} discovers driver factories: via every {@link IDriverFactory}
     * registered in a {@code META-INF/services/com.sele3.drivers.IDriverFactory} file on the
     * classpath. A new platform added purely by registering its own {@link IDriverFactory}
     * therefore becomes resolvable here too, with no change to this class.
     *
     * @param name the platform's name (e.g. "chrome", "SAFARI"), matched case-insensitively
     * @return the matching {@link IPlatform}
     * @throws IllegalArgumentException if no registered platform matches {@code name}
     */
    static IPlatform fromString(String name) {
        return ServiceLoader.load(IDriverFactory.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .map(IDriverFactory::getPlatform)
            .filter(platform -> platform.name().equalsIgnoreCase(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unknown platform: " + name + ". Add a META-INF/services/"
                    + IDriverFactory.class.getName()
                    + " entry for an IDriverFactory implementation whose getPlatform().name() matches it."));
    }
}
