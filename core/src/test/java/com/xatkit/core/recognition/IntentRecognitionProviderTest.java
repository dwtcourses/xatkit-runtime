package com.xatkit.core.recognition;

import com.xatkit.AbstractXatkitTest;
import com.xatkit.core.EventDefinitionRegistry;
import com.xatkit.core.session.XatkitSession;
import com.xatkit.intent.ContextInstance;
import com.xatkit.intent.ContextParameterValue;
import com.xatkit.intent.EntityDefinition;
import com.xatkit.intent.IntentDefinition;
import com.xatkit.intent.RecognizedIntent;
import com.xatkit.test.util.TestBotExecutionModel;
import com.xatkit.test.util.TestModelLoader;
import com.xatkit.util.ExecutionModelUtils;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class IntentRecognitionProviderTest<T extends IntentRecognitionProvider> extends AbstractXatkitTest {

    protected static TestBotExecutionModel testBotExecutionModel;

    @BeforeClass
    public static void setUpBeforeClass() throws ConfigurationException {
        testBotExecutionModel = TestModelLoader.loadTestBot();
    }

    protected T intentRecognitionProvider;

    protected EventDefinitionRegistry eventRegistry;

    protected IntentDefinition registeredIntentDefinition;

    protected List<EntityDefinition> registeredEntityDefinitions = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        eventRegistry = new EventDefinitionRegistry();
        eventRegistry.registerEventDefinition(testBotExecutionModel.getSimpleIntent());
        eventRegistry.registerEventDefinition(testBotExecutionModel.getSystemEntityIntent());
        eventRegistry.registerEventDefinition(testBotExecutionModel.getMappingEntityIntent());
        eventRegistry.registerEventDefinition(testBotExecutionModel.getCompositeEntityIntent());
    }

    @After
    public void tearDown() throws Exception {
        /*
         * Default implementation that shuts down the provider. Advanced providers may need to override this method
         * to perform advanced cleaning (e.g. delete registered intents and entities).
         */
        if (nonNull(intentRecognitionProvider)) {
            if (!intentRecognitionProvider.isShutdown()) {
                intentRecognitionProvider.shutdown();
            }
        }
        this.registeredIntentDefinition = null;
        this.registeredEntityDefinitions.clear();
    }

    @Test(expected = NullPointerException.class)
    public void registerNullIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        intentRecognitionProvider.registerIntentDefinition(null);
    }

    @Test
    public void registerSimpleIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredIntentDefinition = testBotExecutionModel.getSimpleIntent();
        intentRecognitionProvider.registerIntentDefinition(registeredIntentDefinition);
        /*
         * Nothing to check, the method does not return anything and does not change any visible state.
         */
    }

    @Test
    public void registerSystemEntityIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredIntentDefinition = testBotExecutionModel.getSystemEntityIntent();
        intentRecognitionProvider.registerIntentDefinition(registeredIntentDefinition);
        /*
         * Nothing to check, the method does not return anything and does not change any visible state.
         */
    }

    @Test
    public void registerMappingEntityIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredIntentDefinition = testBotExecutionModel.getMappingEntityIntent();
        intentRecognitionProvider.registerIntentDefinition(registeredIntentDefinition);
        /*
         * Nothing to check, the method does not return anything and does not change any visible state.
         */
    }

    @Test
    public void registerCompositeEntityIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredIntentDefinition = testBotExecutionModel.getCompositeEntityIntent();
        intentRecognitionProvider.registerIntentDefinition(registeredIntentDefinition);
    }

    @Test(expected = NullPointerException.class)
    public void deleteNullIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        intentRecognitionProvider.deleteIntentDefinition(null);
    }

    @Test
    public void deleteExistingIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredIntentDefinition = testBotExecutionModel.getSimpleIntent();
        intentRecognitionProvider.registerIntentDefinition(registeredIntentDefinition);
        intentRecognitionProvider.deleteIntentDefinition(registeredIntentDefinition);
        /*
         * Reset to null, it has been deleted.
         */
        registeredIntentDefinition = null;
    }

    @Test(expected = NullPointerException.class)
    public void registerNullEntity() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        intentRecognitionProvider.registerEntityDefinition(null);
    }

    @Test
    public void registerMappingEntity() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredEntityDefinitions.add(testBotExecutionModel.getMappingEntity());
        intentRecognitionProvider.registerEntityDefinition(testBotExecutionModel.getMappingEntity());
        /*
         * Nothing to check, the method does not return anything and does not change any visible state.
         */
    }

    @Test
    public void registerCompositeEntityReferencedEntitiesAlreadyRegistered() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredEntityDefinitions.add(testBotExecutionModel.getMappingEntity());
        intentRecognitionProvider.registerEntityDefinition(testBotExecutionModel.getMappingEntity());
        registeredEntityDefinitions.add(testBotExecutionModel.getCompositeEntity());
        intentRecognitionProvider.registerEntityDefinition(testBotExecutionModel.getCompositeEntity());
        /*
         * Nothing to check, the method does not return anything and does not change any visible state.
         */
    }

    @Test
    public void registerCompositeEntityReferencedEntitiesNotRegistered() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredEntityDefinitions.add(testBotExecutionModel.getCompositeEntity());
        /*
         * Add the mapping entity, it should be registered with the composite.
         */
        registeredEntityDefinitions.add(testBotExecutionModel.getMappingEntity());
        intentRecognitionProvider.registerEntityDefinition(testBotExecutionModel.getCompositeEntity());
        /*
         * Nothing to check, the method does not return anything and does not change any visible state.
         */
    }

    @Test(expected = NullPointerException.class)
    public void deleteNullEntity() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        intentRecognitionProvider.registerEntityDefinition(null);
    }

    @Test
    public void deleteEntityNotReferenced() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredEntityDefinitions.add(testBotExecutionModel.getMappingEntity());
        intentRecognitionProvider.registerEntityDefinition(testBotExecutionModel.getMappingEntity());
        intentRecognitionProvider.deleteEntityDefinition(testBotExecutionModel.getMappingEntity());
        /*
         * Clean the registered entities list if the entities has been successfully deleted.
         */
        registeredEntityDefinitions.clear();
    }

    @Test(expected = NullPointerException.class)
    public void createSessionNullSessionId() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        intentRecognitionProvider.createSession(null);
    }

    @Test
    public void createSessionValidSessionId() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        XatkitSession session = intentRecognitionProvider.createSession("TEST");
        assertThat(session).isNotNull();
    }

    @Test(expected = NullPointerException.class)
    public void getIntentNullInput() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        intentRecognitionProvider.getIntent(null, intentRecognitionProvider.createSession("TEST"));
    }

    @Test(expected = NullPointerException.class)
    public void getIntentNullSession() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        intentRecognitionProvider.getIntent("Intent", null);
    }

    @Test
    public void getIntentNotRegistered() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        XatkitSession session = intentRecognitionProvider.createSession("TEST");
        session.setState(ExecutionModelUtils.getInitState(testBotExecutionModel.getBaseModel()));
        RecognizedIntent recognizedIntent = intentRecognitionProvider.getIntent("Error", session);
        assertThatRecognizedIntentHasDefinition(recognizedIntent,
                IntentRecognitionProvider.DEFAULT_FALLBACK_INTENT.getName());
    }

    @Test
    public void getSimpleIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredIntentDefinition = testBotExecutionModel.getSimpleIntent();
        intentRecognitionProvider.registerIntentDefinition(registeredIntentDefinition);
        intentRecognitionProvider.trainMLEngine();
        XatkitSession session = intentRecognitionProvider.createSession("TEST");
        session.setState(ExecutionModelUtils.getInitState(testBotExecutionModel.getBaseModel()));
        RecognizedIntent recognizedIntent = intentRecognitionProvider.getIntent("Greetings",
                session);
        assertThatRecognizedIntentHasDefinition(recognizedIntent, registeredIntentDefinition.getName());
    }

    @Test
    public void getSystemEntityIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredIntentDefinition = testBotExecutionModel.getSystemEntityIntent();
        intentRecognitionProvider.registerIntentDefinition(registeredIntentDefinition);
        intentRecognitionProvider.trainMLEngine();
        XatkitSession session = intentRecognitionProvider.createSession("TEST");
        session.setState(ExecutionModelUtils.getInitState(testBotExecutionModel.getBaseModel()));
        RecognizedIntent recognizedIntent = intentRecognitionProvider.getIntent("Hello Test", session);
        assertThatRecognizedIntentHasDefinition(recognizedIntent, registeredIntentDefinition.getName());
        ContextInstance context = recognizedIntent.getOutContextInstance("Hello");
        assertThat(context).isNotNull();
        assertThatContextContainsParameterWithValue(context, "helloTo", "Test");
    }

    @Test
    public void getMappingEntityIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredEntityDefinitions.add(testBotExecutionModel.getMappingEntity());
        intentRecognitionProvider.registerEntityDefinition(testBotExecutionModel.getMappingEntity());
        registeredIntentDefinition = testBotExecutionModel.getMappingEntityIntent();
        intentRecognitionProvider.registerIntentDefinition(registeredIntentDefinition);
        intentRecognitionProvider.trainMLEngine();
        XatkitSession session = intentRecognitionProvider.createSession("TEST");
        session.setState(ExecutionModelUtils.getInitState(testBotExecutionModel.getBaseModel()));
        RecognizedIntent recognizedIntent = intentRecognitionProvider.getIntent("Give me some information about " +
                "Gwendal", session);
        assertThatRecognizedIntentHasDefinition(recognizedIntent, registeredIntentDefinition.getName());
        ContextInstance context = recognizedIntent.getOutContextInstance("Founder");
        assertThat(context).isNotNull();
        assertThatContextContainsParameterWithValue(context, "name", "Gwendal");
    }

    @Test
    public void getCompositeEntityIntent() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        registeredEntityDefinitions.add(testBotExecutionModel.getMappingEntity());
        intentRecognitionProvider.registerEntityDefinition(testBotExecutionModel.getMappingEntity());
        registeredEntityDefinitions.add(testBotExecutionModel.getCompositeEntity());
        intentRecognitionProvider.registerEntityDefinition(testBotExecutionModel.getCompositeEntity());
        registeredIntentDefinition = testBotExecutionModel.getCompositeEntityIntent();
        intentRecognitionProvider.registerIntentDefinition(registeredIntentDefinition);
        intentRecognitionProvider.trainMLEngine();
        XatkitSession session = intentRecognitionProvider.createSession("TEST");
        session.setState(ExecutionModelUtils.getInitState(testBotExecutionModel.getBaseModel()));
        RecognizedIntent recognizedIntent = intentRecognitionProvider.getIntent("Does Jordi knows Barcelona?", session);
        assertThatRecognizedIntentHasDefinition(recognizedIntent, registeredIntentDefinition.getName());
        ContextInstance context = recognizedIntent.getOutContextInstance("Query");
        assertThat(context).isNotNull();
        assertThatContextContainsParameterValue(context, "founderCity");
        Object parameterValue = context.getValues().stream()
                .filter(p -> p.getContextParameter().getName().equals("founderCity"))
                .map(ContextParameterValue::getValue).findAny().get();
        assertThat(parameterValue).isInstanceOf(Map.class);
        Map<String, String> mapParameterValue = (Map<String, String>) parameterValue;
        assertThat(mapParameterValue).contains(new AbstractMap.SimpleEntry<>("city", "Barcelona"));
        assertThat(mapParameterValue).contains(new AbstractMap.SimpleEntry<>("XatkitFounder", "Jordi"));
    }

    @Test
    public void shutdown() throws IntentRecognitionProviderException {
        intentRecognitionProvider = getIntentRecognitionProvider();
        intentRecognitionProvider.shutdown();
        assertThat(intentRecognitionProvider.isShutdown()).isTrue();
    }

    protected abstract T getIntentRecognitionProvider();

    protected void assertThatRecognizedIntentHasDefinition(RecognizedIntent recognizedIntent, String definitionName) {
        assertThat(recognizedIntent).isNotNull();
        assertThat(recognizedIntent.getDefinition()).isNotNull();
        assertThat(recognizedIntent.getDefinition().getName()).isEqualTo(definitionName);
    }

    protected void assertThatContextContainsParameterValue(ContextInstance contextInstance, String parameterName) {
        assertThat(contextInstance.getValues()).isNotEmpty();
        assertThat(contextInstance.getValues()).anyMatch(p -> p.getContextParameter().getName().equals(parameterName));
    }

    protected void assertThatContextContainsParameterWithValue(ContextInstance contextInstance, String parameterName,
                                                               Object value) {
        assertThatContextContainsParameterValue(contextInstance, parameterName);
        /*
         * Separate the two checks to have a better log error.
         */
        assertThat(contextInstance.getValues()).anyMatch(p -> p.getContextParameter().getName().equals(parameterName) && p.getValue().equals(value));
    }
}
