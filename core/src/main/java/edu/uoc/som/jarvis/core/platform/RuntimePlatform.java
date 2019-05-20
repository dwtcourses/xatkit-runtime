package edu.uoc.som.jarvis.core.platform;

import edu.uoc.som.jarvis.common.Expression;
import edu.uoc.som.jarvis.core.JarvisCore;
import edu.uoc.som.jarvis.core.JarvisException;
import edu.uoc.som.jarvis.core.interpreter.ExecutionContext;
import edu.uoc.som.jarvis.core.platform.action.RuntimeAction;
import edu.uoc.som.jarvis.core.platform.io.RuntimeEventProvider;
import edu.uoc.som.jarvis.core.platform.io.WebhookEventProvider;
import edu.uoc.som.jarvis.core.server.JarvisServer;
import edu.uoc.som.jarvis.core.session.JarvisSession;
import edu.uoc.som.jarvis.core.session.RuntimeContexts;
import edu.uoc.som.jarvis.execution.ActionInstance;
import edu.uoc.som.jarvis.execution.ParameterValue;
import edu.uoc.som.jarvis.intent.EventInstance;
import edu.uoc.som.jarvis.platform.ActionDefinition;
import edu.uoc.som.jarvis.platform.EventProviderDefinition;
import edu.uoc.som.jarvis.platform.Parameter;
import edu.uoc.som.jarvis.platform.PlatformDefinition;
import edu.uoc.som.jarvis.util.Loader;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;
import static java.util.Objects.isNull;

/**
 * The concrete implementation of a {@link PlatformDefinition}.
 * <p>
 * A {@link RuntimePlatform} manages a set of {@link RuntimeAction}s that represent the concrete actions that can
 * be executed by the platform. This class provides primitives to enable/disable specific actions, and construct
 * {@link RuntimeAction} instances from a given {@link EventInstance}.
 * <p>
 * Note that enabling a {@link RuntimeAction} will load the corresponding class, that must be stored in the
 * <i>action</i> package of the concrete {@link RuntimePlatform} implementation. For example, enabling the action
 * <i>MyAction</i> from the {@link RuntimePlatform} <i>myPlatformPackage.MyPlatform</i> will attempt to load the class
 * <i>myPlatformPackage.action.MyAction</i>.
 */
public abstract class RuntimePlatform {

    /**
     * The {@link JarvisCore} instance containing this platform.
     */
    protected JarvisCore jarvisCore;

    /**
     * The {@link Configuration} used to initialize this class.
     * <p>
     * This {@link Configuration} is used by the {@link RuntimePlatform} to initialize the
     * {@link RuntimeEventProvider}s and
     * {@link RuntimeAction}s.
     *
     * @see #startEventProvider(EventProviderDefinition)
     * @see #createRuntimeAction(ActionInstance, JarvisSession, ExecutionContext)
     */
    protected Configuration configuration;

    /**
     * The {@link Map} containing the {@link RuntimeAction} associated to this platform.
     * <p>
     * This {@link Map} is used as a cache to retrieve {@link RuntimeAction} that have been previously loaded.
     *
     * @see #enableAction(ActionDefinition)
     * @see #disableAction(ActionDefinition)
     * @see #createRuntimeAction(ActionInstance, JarvisSession, ExecutionContext)
     */
    protected Map<String, Class<? extends RuntimeAction>> actionMap;

    /**
     * The {@link Map} containing the {@link EventProviderThread}s associated to this platform.
     * <p>
     * This {@link Map} filled when new {@link RuntimeEventProvider}s are started (see
     * {@link #startEventProvider(EventProviderDefinition)}), and is used to cache
     * {@link EventProviderThread}s and stop them when the platform is {@link #shutdown()}.
     *
     * @see #shutdown()
     */
    protected List<EventProviderThread> eventProviderThreads;


    /**
     * Constructs a new {@link RuntimePlatform} from the provided {@link JarvisCore} and {@link Configuration}.
     * <p>
     * <b>Note</b>: this constructor will be called by jarvis internal engine when initializing the
     * {@link RuntimePlatform}s. Subclasses implementing this constructor typically need additional parameters to be
     * initialized, that can be provided in the {@code configuration}.
     *
     * @param jarvisCore    the {@link JarvisCore} instance associated to this platform
     * @param configuration the {@link Configuration} used to initialize the {@link RuntimePlatform}
     * @throws NullPointerException if the provided {@code jarvisCore} or {@code configuration} is {@code null}
     * @see #RuntimePlatform(JarvisCore)
     */
    public RuntimePlatform(JarvisCore jarvisCore, Configuration configuration) {
        checkNotNull(jarvisCore, "Cannot construct a %s from the provided %s %s", this.getClass().getSimpleName(),
                JarvisCore.class.getSimpleName(), jarvisCore);
        checkNotNull(configuration, "Cannot construct a %s from the provided %s %s", this.getClass().getSimpleName(),
                Configuration.class.getSimpleName(), configuration);
        this.jarvisCore = jarvisCore;
        this.configuration = configuration;
        this.actionMap = new HashMap<>();
        this.eventProviderThreads = new ArrayList<>();
    }

    /**
     * Constructs a new {@link RuntimePlatform} from the provided {@link JarvisCore}.
     * <p>
     * <b>Note</b>: this constructor should be used by {@link RuntimePlatform}s that do not require additional
     * parameters to be initialized. In that case see {@link #RuntimePlatform(JarvisCore, Configuration)}.
     *
     * @throws NullPointerException if the provided {@code jarvisCore} is {@code null}
     * @see #RuntimePlatform(JarvisCore, Configuration)
     */
    public RuntimePlatform(JarvisCore jarvisCore) {
        this(jarvisCore, new BaseConfiguration());
    }

    /**
     * Returns the name of the platform.
     * <p>
     * This method returns the value of {@link Class#getSimpleName()}, and can not be overridden by concrete
     * subclasses. {@link RuntimePlatform}'s names are part of jarvis' naming convention, and are used to dynamically
     * load platforms and actions.
     *
     * @return the name of the platform.
     */
    public final String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns the {@link JarvisCore} instance associated to this platform.
     *
     * @return the {@link JarvisCore} instance associated to this platform
     */
    public final JarvisCore getJarvisCore() {
        return this.jarvisCore;
    }

    /**
     * Starts the {@link RuntimeEventProvider} corresponding to the provided {@code eventProviderDefinition}.
     * <p>
     * This method dynamically loads the {@link RuntimeEventProvider} corresponding to the provided {@code
     * eventProviderDefinition}, and starts it in a dedicated {@link Thread}.
     * <p>
     * This method also registers {@link WebhookEventProvider}s to the underlying {@link JarvisServer} (see
     * {@link JarvisServer#registerWebhookEventProvider(WebhookEventProvider)}).
     *
     * @param eventProviderDefinition the {@link EventProviderDefinition} representing the
     * {@link RuntimeEventProvider} to
     *                                start
     * @throws NullPointerException if the provided {@code eventProviderDefinition} or {@code jarvisCore} is {@code
     *                              null}
     * @see RuntimeEventProvider#run()
     * @see JarvisServer#registerWebhookEventProvider(WebhookEventProvider)
     */
    public final void startEventProvider(EventProviderDefinition eventProviderDefinition) {
        checkNotNull(eventProviderDefinition, "Cannot start the provided %s %s", EventProviderDefinition.class
                .getSimpleName(), eventProviderDefinition);
        checkNotNull(jarvisCore, "Cannot start the provided %s with the given %s %s", eventProviderDefinition
                .getClass().getSimpleName(), JarvisCore.class.getSimpleName(), jarvisCore);
        Log.info("Starting {0}", eventProviderDefinition.getName());
        String eventProviderQualifiedName = this.getClass().getPackage().getName() + ".io." + eventProviderDefinition
                .getName();
        Class<? extends RuntimeEventProvider> eventProviderClass = Loader.loadClass(eventProviderQualifiedName,
                RuntimeEventProvider.class);
        RuntimeEventProvider runtimeEventProvider = Loader.constructRuntimeEventProvider(eventProviderClass, this,
                configuration);
        if (runtimeEventProvider instanceof WebhookEventProvider) {
            /*
             * Register the WebhookEventProvider in the JarvisServer
             */
            Log.info("Registering {0} in the {1}", runtimeEventProvider, JarvisServer.class.getSimpleName());
            jarvisCore.getJarvisServer().registerWebhookEventProvider((WebhookEventProvider) runtimeEventProvider);
        }
        Log.info("Starting RuntimeEventProvider {0}", eventProviderClass.getSimpleName());
        EventProviderThread eventProviderThread = new EventProviderThread(runtimeEventProvider);
        eventProviderThreads.add(eventProviderThread);
        eventProviderThread.start();
    }

    /**
     * Retrieves and loads the {@link RuntimeAction} defined by the provided {@link ActionDefinition}.
     * <p>
     * This method loads the corresponding {@link RuntimeAction} based on jarvis' naming convention. The
     * {@link RuntimeAction} must be located under the {@code actionDefinition} sub-package of the
     * {@link RuntimePlatform}
     * concrete subclass package.
     *
     * @param actionDefinition the {@link ActionDefinition} representing the {@link RuntimeAction} to enable
     * @see Loader#loadClass(String, Class)
     */
    public void enableAction(ActionDefinition actionDefinition) {
        String actionQualifiedName = this.getClass().getPackage().getName() + ".action." + actionDefinition.getName();
        Class<? extends RuntimeAction> runtimeAction = Loader.loadClass(actionQualifiedName, RuntimeAction.class);
        actionMap.put(actionDefinition.getName(), runtimeAction);
    }

    /**
     * Disables the {@link RuntimeAction} defined by the provided {@link ActionDefinition}.
     *
     * @param actionDefinition the {@link ActionDefinition} representing the {@link RuntimeAction} to disable
     */
    public void disableAction(ActionDefinition actionDefinition) {
        actionMap.remove(actionDefinition.getName());
    }

    /**
     * Disables all the {@link RuntimeAction}s of the {@link RuntimePlatform}.
     */
    public final void disableAllActions() {
        actionMap.clear();
    }

    /**
     * Returns all the {@link RuntimeAction} {@link Class}es associated to this {@link RuntimePlatform}.
     * <p>
     * This method returns the {@link Class}es describing the {@link RuntimeAction}s associated to this platform. To
     * construct a new {@link RuntimeAction} from a {@link EventInstance} see
     * {@link #createRuntimeAction(ActionInstance, JarvisSession, ExecutionContext)} .
     *
     * @return all the {@link RuntimeAction} {@link Class}es associated to this {@link RuntimePlatform}
     * @see #createRuntimeAction(ActionInstance, JarvisSession, ExecutionContext)
     */
    public final Collection<Class<? extends RuntimeAction>> getActions() {
        return actionMap.values();
    }

    /**
     * Creates a new {@link RuntimeAction} instance from the provided {@link ActionInstance}.
     * <p>
     * This methods attempts to construct a {@link RuntimeAction} defined by the provided {@code actionInstance} by
     * matching the {@code eventInstance} variables to the {@link ActionDefinition}'s parameters, and reusing the
     * provided
     * {@link ActionInstance#getValues()}.
     *
     * @param actionInstance the {@link ActionInstance} representing the {@link RuntimeAction} to create
     * @param session        the {@link JarvisSession} associated to the action
     * @param context        the execution context used to evaluate the action parameter values
     * @return a new {@link RuntimeAction} instance from the provided {@link ActionInstance}
     * @throws NullPointerException if the provided {@code actionInstance} or {@code session} is {@code null}
     * @throws JarvisException      if the provided {@link ActionInstance} does not match any {@link RuntimeAction},
     *                              or if an error occurred when building the {@link RuntimeAction}
     * @see #getParameterValues(ActionInstance, RuntimeContexts, ExecutionContext)
     */
    public RuntimeAction createRuntimeAction(ActionInstance actionInstance, JarvisSession
            session, ExecutionContext context) {
        checkNotNull(actionInstance, "Cannot construct a %s from the provided %s %s", RuntimeAction.class
                .getSimpleName(), ActionInstance.class.getSimpleName(), actionInstance);
        checkNotNull(session, "Cannot construct a %s from the provided %s %s", RuntimeAction.class.getSimpleName(),
                JarvisSession.class.getSimpleName(), session);
        ActionDefinition actionDefinition = actionInstance.getAction();
        Class<? extends RuntimeAction> runtimeActionClass = actionMap.get(actionDefinition.getName());
        if (isNull(runtimeActionClass)) {
            throw new JarvisException(MessageFormat.format("Cannot create the {0} {1}, the action is not " +
                    "loaded in the platform", RuntimeAction.class.getSimpleName(), actionDefinition.getName()));
        }
        Object[] parameterValues = getParameterValues(actionInstance, session.getRuntimeContexts(), context);
        /*
         * Append the mandatory parameters to the parameter values.
         */
        Object[] fullParameters = new Object[parameterValues.length + 2];
        fullParameters[0] = this;
        fullParameters[1] = session;
        RuntimeAction runtimeAction;
        if (parameterValues.length > 0) {
            System.arraycopy(parameterValues, 0, fullParameters, 2, parameterValues.length);
        }
        try {
            /**
             * The types of the parameters are not known, use {@link Loader#construct(Class, Object[])} to try to
             * find a constructor that accepts them.
             */
            runtimeAction = Loader.construct(runtimeActionClass, fullParameters);
        } catch (NoSuchMethodException e) {
            throw new JarvisException(MessageFormat.format("Cannot find a {0} constructor for the provided parameter " +
                    "types ({1})", runtimeActionClass.getSimpleName(), printClassArray(fullParameters)), e);
        }
        runtimeAction.init();
        return runtimeAction;
    }

    /**
     * Shuts down the {@link RuntimePlatform}.
     * <p>
     * This method attempts to terminate all the running {@link RuntimeEventProvider} threads, close the corresponding
     * {@link RuntimeEventProvider}s, and disables all the platform's actions.
     *
     * @see RuntimeEventProvider#close()
     * @see #disableAllActions()
     */
    public void shutdown() {
        for (EventProviderThread thread : eventProviderThreads) {
            thread.getRuntimeEventProvider().close();
            thread.interrupt();
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                Log.warn("Caught an {0} while waiting for {1} thread to finish", e.getClass().getSimpleName(), thread
                        .getRuntimeEventProvider().getClass().getSimpleName());
            }
        }
        this.eventProviderThreads.clear();
        /*
         * Disable the actions at the end, in case a running EventProviderThread triggers an action computation
         * before it is closed.
         */
        this.disableAllActions();
    }

    /**
     * Retrieves the {@code actionInstance}'s parameter values from the provided {@code context}.
     * <p>
     * This method iterates through the {@link ActionInstance}'s {@link ParameterValue}s and matches them
     * against the describing {@link ActionDefinition}'s {@link Parameter}s. The concrete value associated to the
     * {@link ActionInstance}'s {@link ParameterValue}s are retrieved from the provided {@code context}.
     * <p>
     * The retrieved values are used by the {@link RuntimePlatform} to instantiate concrete {@link RuntimeAction}s (see
     * {@link #createRuntimeAction(ActionInstance, JarvisSession, ExecutionContext)}).
     *
     * @param actionInstance   the {@link ActionInstance} to match the parameters from
     * @param context          the {@link RuntimeContexts} storing event contextual informations
     * @param executionContext the {@link ExecutionContext} used to evaluate parameter's expressions
     * @return an array containing the concrete {@link ActionInstance}'s parameters
     * @throws JarvisException if one of the concrete value is not stored in the provided {@code context}, or if the
     *                         {@link ActionInstance}'s {@link ParameterValue}s do not match the describing
     *                         {@link ActionDefinition}'s {@link Parameter}s.
     * @see #createRuntimeAction(ActionInstance, JarvisSession, ExecutionContext)
     */
    private Object[] getParameterValues(ActionInstance actionInstance, RuntimeContexts context,
                                        ExecutionContext executionContext) {
        ActionDefinition actionDefinition = actionInstance.getAction();
        List<Parameter> actionParameters = actionDefinition.getParameters();
        List<ParameterValue> actionInstanceParameterValues = actionInstance.getValues();
        if ((actionParameters.size() == actionInstanceParameterValues.size())) {
            Object[] actionInstanceParameterValuesArray = StreamSupport.stream(actionInstanceParameterValues
                    .spliterator(), false).map(paramValue -> {

                        Expression paramExpression = paramValue.getExpression();
                        try {
                            Object paramExpValue = jarvisCore.getExecutionService().evaluate(paramExpression,
                                    executionContext);
                            if (paramExpValue instanceof String) {
                                return context.fillContextValues((String) paramExpValue);
                            } else {
                                return paramExpValue;
                            }
                            // should be interpreter exception
                        } catch (Exception e) {
                            throw new JarvisException(e);
                        }
                    }
            ).toArray();
            return actionInstanceParameterValuesArray;
        }
        String errorMessage = MessageFormat.format("The {0} action does not define the good amount of parameters: " +
                        "expected {1}, found {2}", actionDefinition.getName(), actionParameters.size(),
                actionInstanceParameterValues.size());
        Log.error(errorMessage);
        throw new JarvisException(errorMessage);
    }

    /**
     * Formats the provided {@code array} in a {@link String} used representing their {@link Class}es.
     * <p>
     * The returned {@link String} is "a1.getClass().getSimpleName(), a2.getClass().getSimpleName(), an.getClass()
     * .getSimpleName()", where <i>a1</i>, <i>a2</i>, and <i>an</i> are elements in the provided {@code array}.
     *
     * @param array the array containing the elements to print the {@link Class}es of
     * @return a {@link String} containing the formatted elements' {@link Class}es
     */
    private String printClassArray(Object[] array) {
        List<String> toStringList = StreamSupport.stream(Arrays.asList(array).spliterator(), false).map(o ->
        {
            if (isNull(o)) {
                return "null";
            } else {
                return o.getClass().getSimpleName();
            }
        }).collect(Collectors.toList());
        return String.join(", ", toStringList);
    }

    /**
     * The {@link Thread} class used to start {@link RuntimeEventProvider}s.
     * <p>
     * <b>Note:</b> this class is protected for testing purposes, and should not be called by client code.
     */
    protected static class EventProviderThread extends Thread {

        /**
         * The {@link RuntimeEventProvider} run by this {@link Thread}.
         */
        private RuntimeEventProvider runtimeEventProvider;

        /**
         * Constructs a new {@link EventProviderThread} to run the provided {@code runtimeEventProvider}
         *
         * @param runtimeEventProvider the {@link RuntimeEventProvider} to run
         */
        public EventProviderThread(RuntimeEventProvider runtimeEventProvider) {
            super(runtimeEventProvider);
            this.runtimeEventProvider = runtimeEventProvider;
        }

        /**
         * Returns the {@link RuntimeEventProvider} run by this {@link Thread}.
         *
         * @return the {@link RuntimeEventProvider} run by this {@link Thread}
         */
        public RuntimeEventProvider getRuntimeEventProvider() {
            return runtimeEventProvider;
        }

    }
}
