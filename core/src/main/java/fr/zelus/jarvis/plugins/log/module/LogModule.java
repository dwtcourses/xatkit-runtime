package fr.zelus.jarvis.plugins.log.module;

import fr.zelus.jarvis.core.JarvisCore;
import fr.zelus.jarvis.core.JarvisModule;
import org.apache.commons.configuration2.Configuration;

/**
 * A {@link JarvisModule} concrete implementation providing logging capabilities.
 * <p>
 * This module defines a set of {@link fr.zelus.jarvis.plugins.log.module.action.LogAction}s that log messages
 * with various severity levels:
 * <ul>
 * <li>{@link fr.zelus.jarvis.plugins.log.module.action.LogInfo}: logs an information message</li>
 * <li>{@link fr.zelus.jarvis.plugins.log.module.action.LogWarning}: logs a warning message</li>
 * <li>{@link fr.zelus.jarvis.plugins.log.module.action.LogError}: logs an error message</li>
 * </ul>
 * <p>
 * This class is part of jarvis' core modules, and can be used in an orchestration model by importing the
 * <i>LogModule</i> package.
 *
 * @see fr.zelus.jarvis.plugins.log.module.action.LogAction
 */
public class LogModule extends JarvisModule {

    /**
     * Constructs a new {@link LogModule} instance from the provided {@link JarvisCore} and {@link Configuration}.
     *
     * @param jarvisCore    the {@link JarvisCore} instance associated to this module
     * @param configuration the {@link Configuration} used to initialize the {@link LogModule}
     * @throws NullPointerException if the provided {@code jarvisCore} or {@code configuration} is {@code null}
     */
    public LogModule(JarvisCore jarvisCore, Configuration configuration) {
        super(jarvisCore, configuration);
    }
}
