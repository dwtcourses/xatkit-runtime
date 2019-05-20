package edu.uoc.som.jarvis.core.recognition;

import edu.uoc.som.jarvis.AbstractJarvisTest;
import edu.uoc.som.jarvis.core.session.JarvisSession;
import edu.uoc.som.jarvis.core.session.RuntimeContexts;
import edu.uoc.som.jarvis.intent.*;
import edu.uoc.som.jarvis.test.util.ElementFactory;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static edu.uoc.som.jarvis.test.util.ElementFactory.createBaseEntityDefinitionReference;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultIntentRecognitionProviderTest extends AbstractJarvisTest {

    private DefaultIntentRecognitionProvider provider;

    private static Context VALID_OUT_CONTEXT;

    private static IntentDefinition VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT;

    private static IntentDefinition VALID_INTENT_DEFINITION_NO_OUT_CONTEXT;

    private static IntentDefinition INTENT_MAPPING_OUT_CONTEXT;

    private static Context MAPPING_CONTEXT;

    private static MappingEntityDefinition MAPPING_ENTITY;

    private static IntentDefinition INTENT_COMPOSITE_OUT_CONTEXT;

    private static Context COMPOSITE_CONTEXT;

    private static CompositeEntityDefinition COMPOSITE_ENTITY;

    @BeforeClass
    public static void setUpBeforeClass() {
        VALID_OUT_CONTEXT = IntentFactory.eINSTANCE.createContext();
        VALID_OUT_CONTEXT.setName("ValidContext");
        ContextParameter contextParameter = IntentFactory.eINSTANCE.createContextParameter();
        contextParameter.setName("param");
        contextParameter.setTextFragment("test");
        EntityDefinitionReference entityReference = createBaseEntityDefinitionReference(EntityType.ANY);
        contextParameter.setEntity(entityReference);
        VALID_OUT_CONTEXT.getParameters().add(contextParameter);
        VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT = IntentFactory.eINSTANCE.createIntentDefinition();
        VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT.setName("TestIntentDefinition");
        VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT.getTrainingSentences().add("test intent definition");
        VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT.getOutContexts().add(VALID_OUT_CONTEXT);

        VALID_INTENT_DEFINITION_NO_OUT_CONTEXT = IntentFactory.eINSTANCE.createIntentDefinition();
        VALID_INTENT_DEFINITION_NO_OUT_CONTEXT.setName("TestIntentDefinitionNoOutContext");
        VALID_INTENT_DEFINITION_NO_OUT_CONTEXT.getTrainingSentences().add("this is a test");

        setMappingIntent();
        setCompositeIntent();
    }

    private static void setMappingIntent() {
        MAPPING_ENTITY = ElementFactory.createMappingEntityDefinition();
        INTENT_MAPPING_OUT_CONTEXT = ElementFactory.createIntentDefinitionNoOutContext();
        MAPPING_CONTEXT = IntentFactory.eINSTANCE.createContext();
        MAPPING_CONTEXT.setName("Context");
        ContextParameter parameter = IntentFactory.eINSTANCE.createContextParameter();
        parameter.setName("param");
        parameter.setTextFragment("test");
        parameter.setEntity(ElementFactory.createEntityDefinitionReference(MAPPING_ENTITY));
        MAPPING_CONTEXT.getParameters().add(parameter);
        INTENT_MAPPING_OUT_CONTEXT.getOutContexts().add(MAPPING_CONTEXT);
    }

    private static void setCompositeIntent() {
        COMPOSITE_ENTITY = ElementFactory.createCompositeEntityDefinition();
        INTENT_COMPOSITE_OUT_CONTEXT = ElementFactory.createIntentDefinitionNoOutContext();
        COMPOSITE_CONTEXT = IntentFactory.eINSTANCE.createContext();
        COMPOSITE_CONTEXT.setName("Composite");
        ContextParameter parameter = IntentFactory.eINSTANCE.createContextParameter();
        parameter.setName("param");
        parameter.setTextFragment("test");
        parameter.setEntity(ElementFactory.createEntityDefinitionReference(COMPOSITE_ENTITY));
        COMPOSITE_CONTEXT.getParameters().add(parameter);
        INTENT_COMPOSITE_OUT_CONTEXT.getOutContexts().add(COMPOSITE_CONTEXT);
    }

    @Before
    public void setUp() {
        provider = new DefaultIntentRecognitionProvider(new BaseConfiguration());
    }

    @After
    public void tearDown() {
        provider.shutdown();
        provider = null;
    }

    @Test(expected = NullPointerException.class)
    public void constructNullConfiguration() {
        provider = new DefaultIntentRecognitionProvider(null);
    }

    @Test
    public void constructValidConfiguration() {
        provider = new DefaultIntentRecognitionProvider(new BaseConfiguration());
        assertThat(provider.isShutdown()).as("Provider not shut down").isFalse();
    }

    @Test
    public void registerIntentDefinitionWithOutContext() {
        provider.registerIntentDefinition(VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT);
        assertThat(provider.intentPatterns).as("Intent pattern map contains the registered intent definition")
                .containsKey(VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT);
        /*
         * Do not test the pattern, it is done in getIntent tests
         */
    }


    @Test
    public void registerIntentDefinitionWithOutContextMapping() {
        provider.registerEntityDefinition(MAPPING_ENTITY);
        provider.registerIntentDefinition(INTENT_MAPPING_OUT_CONTEXT);
        assertThat(provider.intentPatterns).as("Intent pattern map contains the registered intent definition")
                .containsKeys(INTENT_MAPPING_OUT_CONTEXT);
        /*
         * Do not test the pattern, it is done in getIntent tests
         */
    }

    @Test
    public void registerIntentDefinitionWithOutContextComposite() {
        provider.registerEntityDefinition(COMPOSITE_ENTITY);
        provider.registerIntentDefinition(INTENT_COMPOSITE_OUT_CONTEXT);
        assertThat(provider.intentPatterns).as("Intent pattern map contains the registered intent definition")
                .containsKeys(INTENT_COMPOSITE_OUT_CONTEXT);
    }

    @Test
    public void registerIntentDefinitionNoOutContext() {
        provider.registerIntentDefinition(VALID_INTENT_DEFINITION_NO_OUT_CONTEXT);
        assertThat(provider.intentPatterns).as("Intent pattern map contains the registered intent definition")
                .containsKeys(VALID_INTENT_DEFINITION_NO_OUT_CONTEXT);
        assertThat(provider.intentPatterns.get(VALID_INTENT_DEFINITION_NO_OUT_CONTEXT).get(0).pattern()).as("Intent " +
                "pattern map contains the correct pattern").isEqualTo("^this is a test$");
    }

    @Test
    public void registerIntentDefinitionFollowUp() {
        IntentDefinition parentIntent = ElementFactory.createIntentDefinitionNoOutContext();
        IntentDefinition childIntent = ElementFactory.createFollowUpIntent(parentIntent);
        provider.registerIntentDefinition(childIntent);
        /*
         * No need to register the parent, if it is not registered the intent will not be matched, but it won't break
         * the provider initialization.
         */
        assertThat(provider.intentPatterns).as("Intent map contains the child intent").containsKeys(childIntent);
    }

    @Test
    public void getIntentValidIntentDefinitionWithOutContextUnmatchedInput() {
        provider.registerIntentDefinition(VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT);
        RecognizedIntent recognizedIntent = provider.getIntent("test test intent definition", new JarvisSession(
                "sessionID"));
        assertThat(recognizedIntent).as("Not null recognized intent").isNotNull();
        assertThat(recognizedIntent.getDefinition()).as("Unmatched input returns default fallback intent").
                isEqualTo(DefaultIntentRecognitionProvider.DEFAULT_FALLBACK_INTENT);
    }

    @Test
    public void getIntentValidIntentDefinitionWithOutContext() {
        provider.registerIntentDefinition(VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT);
        RecognizedIntent recognizedIntent = provider.getIntent("Value intent definition", new JarvisSession(
                "sessionID"));
        assertThat(recognizedIntent).as("Not null recognized intent").isNotNull();
        assertThat(recognizedIntent.getDefinition()).as("Valid intent definition").isEqualTo(VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT);
        assertThat(recognizedIntent.getOutContextInstances()).as("Recognized intent contains one out context").hasSize(1);
        ContextInstance contextInstance = recognizedIntent.getOutContextInstances().get(0);
        assertThat(contextInstance.getDefinition().getName()).as("Valid out context name").isEqualTo(VALID_OUT_CONTEXT.getName());
        assertThat(contextInstance.getValues()).as("Out context contains 1 value").hasSize(1);
        ContextParameterValue value = contextInstance.getValues().get(0);
        assertThat(value.getContextParameter()).as("Valid value context parameter").isEqualTo(VALID_OUT_CONTEXT.getParameters().get(0));
        assertThat(value.getValue()).as("Valid value").isEqualTo("Value");
    }

    @Test
    public void getIntentValidIntentDefinitionWithOutContextMapping() {
        provider.registerEntityDefinition(MAPPING_ENTITY);
        provider.registerIntentDefinition(INTENT_MAPPING_OUT_CONTEXT);
        RecognizedIntent recognizedIntent = provider.getIntent("this is a Person", new JarvisSession("sessionID"));
        assertThat(recognizedIntent).as("Not null recognized intent").isNotNull();
        assertThat(recognizedIntent.getDefinition()).as("Valid intent definition").isEqualTo(INTENT_MAPPING_OUT_CONTEXT);
        assertThat(recognizedIntent.getOutContextInstances()).as("Recognized intent contains one out context").hasSize(1);
        ContextInstance contextInstance = recognizedIntent.getOutContextInstances().get(0);
        assertThat(contextInstance.getDefinition().getName()).as("Valid out context name").isEqualTo(MAPPING_CONTEXT.getName());
        assertThat(contextInstance.getValues()).as("Out context contains 1 value").hasSize(1);
        ContextParameterValue value = contextInstance.getValues().get(0);
        assertThat(value.getContextParameter()).as("Valid value context parameter").isEqualTo(MAPPING_CONTEXT.getParameters().get(0));
        assertThat(value.getValue()).as("Valid value").isEqualTo("Person");
    }

    @Test
    public void getIntentValidIntentDefinitionWithOutContextComposite() {
        provider.registerEntityDefinition(COMPOSITE_ENTITY);
        provider.registerIntentDefinition(INTENT_COMPOSITE_OUT_CONTEXT);
        RecognizedIntent recognizedIntent = provider.getIntent("this is a Person with 23", new JarvisSession(
                "sessionID"));
        assertThat(recognizedIntent).as("Not null recognized intent").isNotNull();
        assertThat(recognizedIntent.getDefinition()).as("Valid intent definition").isEqualTo(INTENT_COMPOSITE_OUT_CONTEXT);
        assertThat(recognizedIntent.getOutContextInstances()).as("Recognized intent contains one out context").hasSize(1);
        ContextInstance contextInstance = recognizedIntent.getOutContextInstances().get(0);
        assertThat(contextInstance.getDefinition().getName()).as("Valid out context name").isEqualTo(COMPOSITE_CONTEXT.getName());
        assertThat(contextInstance.getValues()).as("Out context contains 1 value").hasSize(1);
        ContextParameterValue value = contextInstance.getValues().get(0);
        assertThat(value.getContextParameter()).as("Valid value context parameter").isEqualTo(COMPOSITE_CONTEXT.getParameters().get(0));
        assertThat(value.getValue()).as("Valid value").isEqualTo("Person with 23");
    }

    @Test
    public void getIntentValidIntentDefinitionNoOutContextUnmatchedInput() {
        RecognizedIntent recognizedIntent = provider.getIntent("test", new JarvisSession("sessionID"));
        assertThat(recognizedIntent).as("Not null recognized intent").isNotNull();
        assertThat(recognizedIntent.getDefinition()).as("Unmatched input returns default fallback intent")
                .isEqualTo(DefaultIntentRecognitionProvider.DEFAULT_FALLBACK_INTENT);
    }

    @Test
    public void getIntentValidIntentDefinitionNoOutContext() {
        provider.registerIntentDefinition(VALID_INTENT_DEFINITION_NO_OUT_CONTEXT);
        RecognizedIntent recognizedIntent = provider.getIntent("this is a test", new JarvisSession("sessionID"));
        assertThat(recognizedIntent).as("Not null recognized intent").isNotNull();
        assertThat(recognizedIntent.getDefinition()).as("Valid intent definition").isEqualTo(VALID_INTENT_DEFINITION_NO_OUT_CONTEXT);
    }

    @Test
    public void getIntentValidIntentDefinitionWithReservedRegExpCharacters() {
        IntentDefinition intentDefinition = ElementFactory.createIntentDefinitionNoOutContext();
        intentDefinition.getTrainingSentences().add("$test");
        provider.registerIntentDefinition(intentDefinition);
        RecognizedIntent recognizedIntent = provider.getIntent("$test", new JarvisSession("sessionID"));
        assertThat(recognizedIntent).as("Not null recognized intent").isNotNull();
        assertThat(recognizedIntent.getDefinition()).as("Valid intent definition").isEqualTo(intentDefinition);
    }

    @Test
    public void getIntentAndFollowUp() {
        IntentDefinition parentIntent = ElementFactory.createIntentDefinitionNoOutContext();
        IntentDefinition childIntent = ElementFactory.createFollowUpIntent(parentIntent);
        provider.registerIntentDefinition(parentIntent);
        provider.registerIntentDefinition(childIntent);
        JarvisSession session = new JarvisSession("sessionID");
        RecognizedIntent recognizedParent = provider.getIntent("this is a test", session);
        assertThat(recognizedParent.getDefinition()).as("Correct parent intent matched").isEqualTo(parentIntent);
        ContextInstance parentFollowContextInstance =
                recognizedParent.getOutContextInstance(parentIntent.getName() + DefaultIntentRecognitionProvider.FOLLOW_CONTEXT_NAME_SUFFIX);
        assertThat(parentFollowContextInstance).as("Follow context set").isNotNull();
        /*
         * Manually set the context in the session, this is done at the ExecutionService level.
         */
        session.getRuntimeContexts().setContext(parentFollowContextInstance);
        RecognizedIntent recognizedChild = provider.getIntent("test followUp", session);
        assertThat(recognizedChild.getDefinition()).as("Correct child intent matched").isEqualTo(childIntent);
    }

    @Test
    public void getIntentWithInContextWithoutContextRegisteredInSession() {
        IntentDefinition intentDefinition = ElementFactory.createIntentDefinitionNoOutContext();
        Context inContext = IntentFactory.eINSTANCE.createContext();
        inContext.setName("InContext");
        intentDefinition.getInContexts().add(inContext);
        provider.registerIntentDefinition(intentDefinition);
        RecognizedIntent recognizedIntent = provider.getIntent("this is a test", new JarvisSession("sessionID"));
        assertThat(recognizedIntent.getDefinition()).as("Default fallback intent matched").isEqualTo(DefaultIntentRecognitionProvider.DEFAULT_FALLBACK_INTENT);
    }

    @Test
    public void getIntentWithInContextWithContextRegisteredInSession() {
        IntentDefinition intentDefinition = ElementFactory.createIntentDefinitionNoOutContext();
        Context inContext = IntentFactory.eINSTANCE.createContext();
        inContext.setName("InContext");
        intentDefinition.getInContexts().add(inContext);
        provider.registerIntentDefinition(intentDefinition);
        JarvisSession session = new JarvisSession("sessionID");
        session.getRuntimeContexts().setContext("InContext", 5);
        RecognizedIntent recognizedIntent = provider.getIntent("this is a test", session);
        assertThat(recognizedIntent.getDefinition()).as("Correct intent matched").isEqualTo(intentDefinition);
    }

    @Test
    public void deleteIntentDefinitionNotRegisteredIntentDefinition() {
        provider.deleteIntentDefinition(IntentFactory.eINSTANCE.createIntentDefinition());
        assertThat(provider.intentPatterns).as("Intent pattern map is empty").isEmpty();
    }

    @Test
    public void deleteIntentDefinitionRegisteredIntentDefinition() {
        provider.registerIntentDefinition(VALID_INTENT_DEFINITION_NO_OUT_CONTEXT);
        provider.registerIntentDefinition(VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT);
        provider.deleteIntentDefinition(VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT);
        assertThat(provider.intentPatterns).as("Intent pattern map contains 1 element").hasSize(1);
        assertThat(provider.intentPatterns.get(VALID_INTENT_DEFINITION_WITH_OUT_CONTEXT)).as("Intent 1 has been " +
                "removed").isNull();
        ;
        assertThat(provider.intentPatterns.get(VALID_INTENT_DEFINITION_NO_OUT_CONTEXT)).as("Intent 2 hasn't been " +
                "removed").isNotNull();
    }

    @Test
    public void trainMLEngine() {
        provider.registerIntentDefinition(VALID_INTENT_DEFINITION_NO_OUT_CONTEXT);
        provider.trainMLEngine();
        assertThat(provider.intentPatterns).as("Training didn't change the intent pattern map size").hasSize(1);
        assertThat(provider.intentPatterns.get(VALID_INTENT_DEFINITION_NO_OUT_CONTEXT)).as("Training didn't change " +
                "the intent pattern map content").isNotNull();
    }

    @Test
    public void createSessionEmptyConfiguration() {
        JarvisSession session = provider.createSession("SessionID");
        assertThat(session).as("Not null session").isNotNull();
        assertThat(session.getSessionId()).as("Valid session id").isEqualTo("SessionID");
        assertThat(session.getRuntimeContexts()).as("Not null context").isNotNull();
        assertThat(session.getRuntimeContexts().getVariableTimeout()).as("Default variable timeout").isEqualTo
                (RuntimeContexts.DEFAULT_VARIABLE_TIMEOUT_VALUE);
    }

    @Test
    public void createSessionCustomTimeoutValue() {
        Configuration configuration = new BaseConfiguration();
        configuration.addProperty(RuntimeContexts.VARIABLE_TIMEOUT_KEY, 10);
        provider = new DefaultIntentRecognitionProvider(configuration);
        JarvisSession session = provider.createSession("SessionID");
        assertThat(session).as("Not null session").isNotNull();
        assertThat(session.getSessionId()).as("Valid sessio id").isEqualTo("SessionID");
        assertThat(session.getRuntimeContexts()).as("Not null context").isNotNull();
        assertThat(session.getRuntimeContexts().getVariableTimeout()).as("Custom variable timeout").isEqualTo(10);
    }

    @Test
    public void shutdown() {
        provider.shutdown();
        assertThat(provider.isShutdown()).as("Provider is shutdown").isTrue();
    }

    @Test
    public void isShutdownNotShutdown() {
        assertThat(provider.isShutdown()).as("Provider is not shutdown").isFalse();
    }
}
