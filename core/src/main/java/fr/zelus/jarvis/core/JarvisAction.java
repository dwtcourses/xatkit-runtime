package fr.zelus.jarvis.core;

import fr.zelus.jarvis.core.session.JarvisSession;
import fr.zelus.jarvis.intent.EventInstance;

import java.util.concurrent.Callable;

import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;

/**
 * The concrete implementation of an {@link fr.zelus.jarvis.module.Action} definition.
 * <p>
 * A {@link JarvisAction} represents an atomic action that are automatically executed by the
 * {@link OrchestrationService}
 * component. Instances of this class are created by the associated {@link JarvisModule} from an input
 * {@link fr.zelus.jarvis.intent.RecognizedIntent}.
 * <p>
 * Note that {@link JarvisAction}s implementations must be stored in the <i>action</i> package of their associated
 * concrete {@link JarvisModule} implementation to enable their automated loading. For example, the action
 * <i>MyAction</i> defined in the module <i>myModulePackage.MyModule</i> should be stored in the package
 * <i>myModulePackage.action</i>
 *
 * @param <T> the concrete {@link JarvisModule} subclass type containing the action
 * @see fr.zelus.jarvis.module.Action
 * @see OrchestrationService
 * @see JarvisModule
 */
public abstract class JarvisAction<T extends JarvisModule> implements Callable<JarvisActionResult> {

    /**
     * The {@link JarvisModule} subclass containing this action.
     */
    protected T module;

    /**
     * The {@link JarvisSession} associated to this action.
     */
    protected JarvisSession session;

    /**
     * The name of the variable to use to store the result of the {@link #call()} method.
     * <p>
     * The value of this attribute is used by
     * {@link OrchestrationService#handleEventInstance(EventInstance, JarvisSession)} to store
     * the result of each {@link JarvisAction} in the variable defined in the provided orchestration model.
     *
     * @see OrchestrationService#handleEventInstance(EventInstance, JarvisSession)
     * @see #getReturnVariable()
     */
    protected String returnVariable;

    /**
     * Constructs a new {@link JarvisModule} with the provided {@code containingModule} and {@code session}.
     *
     * @param containingModule the {@link JarvisModule} containing this action
     * @param session          the {@link JarvisSession} associated to this action
     * @throws NullPointerException if the provided {@code containingModule} or {@code session} is {@code null}
     */
    public JarvisAction(T containingModule, JarvisSession session) {
        checkNotNull(containingModule, "Cannot construct a %s with the provided %s %s", this.getClass().getSimpleName
                (), JarvisModule.class.getSimpleName(), containingModule);
        checkNotNull(session, "Cannot construct a %s with the provided %s %s", this.getClass().getSimpleName(),
                JarvisSession.class.getSimpleName(), session);
        this.module = containingModule;
        this.session = session;
    }

    /**
     * A hook method that is called after {@link JarvisAction}.
     * <p>
     * This method can be extended by subclasses to add post-construction computation, such as setting additional
     * fields, checking invariants once the {@link JarvisAction} has been initialized, etc.
     */
    public void init() {

    }

    public final void setReturnVariable(String variableName) {
        this.returnVariable = variableName;
    }

    /**
     * Return the name of the variable to use to store the result of the {@link #call()} method.
     * <p>
     * This method is used by {@link OrchestrationService#handleEventInstance(EventInstance, JarvisSession)}  to
     * store the result of each {@link JarvisAction} in the variable defined in the provided orchestration model.
     *
     * @return the name of the variable to use to store the result of the {@link #call()} method
     * @see OrchestrationService#handleEventInstance(EventInstance, JarvisSession)
     */
    public final String getReturnVariable() {
        return returnVariable;
    }

    /**
     * Disable the default constructor, JarvisActions must be constructed with their containing module.
     */
    private JarvisAction() {
        /*
         * Disable the default constructor, JarvisActions must be constructed with their containing module.
         */
    }

    /**
     * Runs the {@link JarvisAction} and returns its result wrapped in a {@link JarvisActionResult}.
     * <p>
     * This method should not be called manually, and is handled by the {@link OrchestrationService} component that
     * manages and executes {@link JarvisAction}s.
     * <p>
     * This method does not throw any {@link Exception} if the underlying {@link JarvisAction}'s computation does not
     * complete. Exceptions thrown during the {@link JarvisAction}'s computation can be accessed through the
     * {@link JarvisActionResult#getThrownException()} method.
     *
     * @return the {@link JarvisActionResult} containing the raw result of the computation and monitoring information
     * @see OrchestrationService
     * @see JarvisActionResult
     */
    @Override
    public JarvisActionResult call() {
        Object computationResult = null;
        Exception thrownException = null;
        long before = System.currentTimeMillis();
        try {
            computationResult = compute();
        } catch (Exception e) {
            thrownException = e;
        }
        long after = System.currentTimeMillis();
        /*
         * Construct the JarvisAction result from the gathered information. Note that the constructor accepts a null
         * value for the thrownException parameter, that will set accordingly the isError() helper.
         */
        return new JarvisActionResult(computationResult, thrownException, (after - before));
    }

    /**
     * The concrete implementation of the {@link JarvisAction}'s computation.
     * <p>
     * This method is internally called by the {@link #call()} method to perform the raw computation and wrap the
     * results in a {@link JarvisActionResult}. Note that {@link #compute()} can return raw computation result, and
     * do not have to deal with setting the monitoring information of the created {@link JarvisActionResult}.
     * <p>
     * This method should be overriden by subclasses to implement the {@link JarvisAction}'s computation logic.
     *
     * @return the raw result of the {@link JarvisAction}'s computation
     * @throws Exception if an error occurred when computing the {@link JarvisAction}
     */
    protected abstract Object compute() throws Exception;

}
