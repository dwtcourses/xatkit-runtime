package edu.uoc.som.jarvis.core.session;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;

/**
 * A session holding user-related information.
 * <p>
 * A {@link JarvisSession} is bound to a user, and holds all the volatile information related to the current
 * conversation. A {@link JarvisSession} contains a {@link RuntimeContexts}, that represents the contextual variables
 * of the current conversation.
 *
 * @see RuntimeContexts
 * @see edu.uoc.som.jarvis.core.JarvisCore#getOrCreateJarvisSession(String)
 */
public class JarvisSession {

    /**
     * The unique identifier of the {@link JarvisSession}.
     */
    private String sessionId;

    /**
     * The {@link RuntimeContexts} used to store context-related variables.
     */
    private RuntimeContexts runtimeContexts;

    /**
     * The {@link Map} used to store session-related variables.
     * <p>
     * Session-related variables are persistent across intent and events, and can contain any {@link Object}. They
     * are used to store results of specific actions, or set global variables that can be accessed along the
     * conversation.
     */
    private Map<String, Object> sessionVariables;

    /**
     * Constructs a new, empty {@link JarvisSession} with the provided {@code sessionId}.
     * See {@link #JarvisSession(String, Configuration)} to construct a {@link JarvisSession} with a given
     * {@link Configuration}.
     *
     * @param sessionId the unique identifier of the {@link JarvisSession}
     */
    public JarvisSession(String sessionId) {
        this(sessionId, new BaseConfiguration());
    }

    /**
     * Constructs a new, empty {@link JarvisSession} with the provided {@code sessionId} and {@code configuration}.
     * <p>
     * This constructor forwards the provided {@link Configuration} to the underlying {@link RuntimeContexts} and can
     * be used to customize {@link RuntimeContexts} properties.
     *
     * @param sessionId     the unique identifier of the {@link JarvisSession}
     * @param configuration the {@link Configuration} parameterizing the {@link JarvisSession}
     * @throws NullPointerException if the provided {@code sessionId} or {@code configuration} is {@code null}
     */
    public JarvisSession(String sessionId, Configuration configuration) {
        checkNotNull(sessionId, "Cannot construct a %s with the session Id %s", JarvisSession.class.getSimpleName(),
                sessionId);
        checkNotNull(configuration, "Cannot construct a %s with the provided %s: %s", JarvisSession.class
                .getSimpleName(), Configuration.class.getSimpleName(), configuration);
        this.sessionId = sessionId;
        this.runtimeContexts = new RuntimeContexts(configuration);
        this.sessionVariables = new HashMap<>();
    }

    /**
     * Returns the unique identifier of the {@link JarvisSession}.
     *
     * @return the unique identifier of the {@link JarvisSession}
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the session's {@link RuntimeContexts} holding context-related variables.
     *
     * @return the session's {@link RuntimeContexts} holding context-related variables
     */
    public RuntimeContexts getRuntimeContexts() {
        return runtimeContexts;
    }

    /**
     * Store the provided {@code value} with the given {@code key} as a session variable.
     * <p>
     * Session-related variables are persistent across intent and events, and can contain any {@link Object}. They
     * are used to store results of specific actions, or set global variables that can be accessed along the
     * conversation.
     * <p>
     * <b>Note:</b> this method erases the previous value associated to the provided {@code key} with the new one.
     *
     * @param key   the key to store and retrieve the provided value
     * @param value the value to store
     * @throws NullPointerException if the provided {@code key} is {@code null}
     */
    public void store(String key, Object value) {
        checkNotNull(key, "Cannot store the provided session variable %s (value=%s), please provide a non-null key",
                key, value);
        this.sessionVariables.put(key, value);
    }

    /**
     * Store the provided {@code value} in the {@link List} associated to the provided {@code key} as a session
     * variable.
     * <p>
     * This method creates a new {@link List} with the provided {@code value} if the session variables do not contain
     * any record associated to the provided {@code key}.
     * <p>
     * <b>Note:</b> if the {@link JarvisSession} contains a single-valued entry for the provided {@code key} this
     * value will be erased and replaced by the created {@link List}.
     *
     * @param key   the key of the {@link List} to store the provided {@code value}
     * @param value the value to store in a {@link List}
     */
    public void storeList(String key, Object value) {
        checkNotNull(key, "Cannot store the provided session variable %s (value=%s), please provide a non-null key",
                key, value);
        Object storedValue = this.sessionVariables.get(key);
        List list;
        if (storedValue instanceof List) {
            list = (List) storedValue;
        } else {
            list = new ArrayList();
            this.sessionVariables.put(key, list);
        }
        list.add(value);
    }

    /**
     * Retrieves the session value associated to the provided {@code key}.
     *
     * @param key the key to retrieve the value for
     * @return the session value associated to the provided {@code key} if it exists, {@code null} otherwise
     */
    public Object get(String key) {
        return this.sessionVariables.get(key);
    }
}
