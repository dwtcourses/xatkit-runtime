package com.xatkit.core.interpreter;

import com.xatkit.common.CommonFactory;
import com.xatkit.common.CommonPackage;
import com.xatkit.common.Program;
import com.xatkit.core.ExecutionService;
import com.xatkit.core.interpreter.operation.OperationException;
import com.xatkit.core.session.XatkitSession;
import com.xatkit.intent.EventInstance;
import com.xatkit.intent.RecognizedIntent;
import com.xatkit.test.util.ElementFactory;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonInterpreterTest {

    private static String baseURI = "interpreter_inputs/";

    private static ResourceSet rSet;

    private static EventInstance TEST_EVENT_INSTANCE = ElementFactory.createEventInstance();

    private static RecognizedIntent TEST_RECOGNIZED_INTENT = ElementFactory.createRecognizedIntent();

    private CommonInterpreter interpreter;

    @BeforeClass
    public static void setUpBeforeClass() {
        EPackage.Registry.INSTANCE.put(CommonPackage.eNS_URI, CommonPackage.eINSTANCE);
        rSet = new ResourceSetImpl();
        rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
    }

    @Before
    public void setUp() {
        interpreter = new CommonInterpreter();
    }

    @Test(expected = NullPointerException.class)
    public void computeNullResource() {
        interpreter.compute((Resource) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void computeEmptyResource() {
        Resource resource = rSet.createResource(URI.createURI("test.xmi"));
        interpreter.compute(resource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void computeResourceMultiplePrograms() {
        Resource resource = rSet.createResource(URI.createURI("test.xmi"));
        Program program1 = CommonFactory.eINSTANCE.createProgram();
        resource.getContents().add(program1);
        Program program2 = CommonFactory.eINSTANCE.createProgram();
        resource.getContents().add(program2);
        interpreter.compute(resource);
    }

    @Test
    public void string_literal() {
        Object result = interpreter.compute(getProgram("string_literal"));
        assertThat(result).as("correct String result").isEqualTo("Value");
    }

    @Test
    public void string_literal_length() {
        Object result = interpreter.compute(getProgram("string_literal_length"));
        assertThat(result).as("correct size result").isEqualTo("Value".length());
    }

    @Test
    public void int_literal() {
        Object result = interpreter.compute(getProgram("int_literal"));
        assertThat(result).as("correct Number result").isEqualTo(2);
    }

    @Test
    public void int_literal_toString() {
        Object result = interpreter.compute(getProgram("int_literal_toString"));
        assertThat(result).as("correct result type").isInstanceOf(String.class);
        assertThat(result).as("correct String result").isEqualTo("2");
    }

    @Test
    public void variable_declaration_no_value() {
        Object result = interpreter.compute(getProgram("variable_declaration_no_value"));
        assertThat(result).as("null result").isNull();
    }

    @Test
    public void variable_declaration_string_value() {
        ExecutionContext context = new ExecutionContext();
        Object result = interpreter.compute(getProgram("variable_declaration_string_value"), context);
        assertThat(result).as("correct String result").isEqualTo("Value");
        assertThat(context.getValueCount()).as("context contains a single variable").isEqualTo(1);
        assertThat(context.getValue("myVar")).as("context contains the declared variable with the correct String " +
                "value").isEqualTo("Value");
    }

    @Test
    public void variable_declaration_int_value() {
        ExecutionContext context = new ExecutionContext();
        Object result = interpreter.compute(getProgram("variable_declaration_int_value"), context);
        assertThat(result).as("correct Number value").isEqualTo(2);
        assertThat(context.getValueCount()).as("context contains a single variable").isEqualTo(1);
        assertThat(context.getValue("myVar")).as("context contains the declared variable with the correrct Number " +
                "value").isEqualTo(2);
    }

    @Test
    public void variable_declaration_string_value_variable_access() {
        ExecutionContext context = new ExecutionContext();
        Object result = interpreter.compute(getProgram("variable_declaration_string_value_variable_access"), context);
        assertThat(result).as("correct String value").isEqualTo("Value");
        assertThat(context.getValueCount()).as("context contains a single variable").isEqualTo(1);
        assertThat(context.getValue("myVar")).as("context contains the declared variable with the correct String " +
                "value").isEqualTo("Value");
    }

    @Test(expected = OperationException.class)
    public void string_variable_size_call_invalid() {
        interpreter.compute(getProgram("string_variable_size_call_invalid"));
    }

    @Test
    public void string_variable_length_call() {
        ExecutionContext context = new ExecutionContext();
        Object result = interpreter.compute(getProgram("string_variable_length_call"), context);
        assertThat(result).as("correct length value").isEqualTo(5);
        assertThat(context.getValueCount()).as("context contains a single variable").isEqualTo(1);
        /*
         * Make sure the variable value hasn't been updated
         */
        assertThat(context.getValue("myVar")).as("context contains the declared variable with the correct String " +
                "value").isEqualTo("Value");
    }

    @Test
    public void string_variable_toString_length() {
        ExecutionContext context = new ExecutionContext();
        Object result = interpreter.compute(getProgram("string_variable_toString_length"), context);
        assertThat(result).as("correct length value").isEqualTo(5);
        assertThat(context.getValueCount()).as("context contains a single variable").isEqualTo(1);
        /*
         * Make sure the variable value hasn't been updated
         */
        assertThat(context.getValue("myVar")).as("context contains the declared variable with the correct String " +
                "value").isEqualTo("Value");
    }

    @Test(expected = OperationException.class)
    public void string_variable_equals_no_argument() {
        interpreter.compute(getProgram("string_variable_equals_no_argument"));
    }

    @Test(expected = OperationException.class)
    public void string_variable_equals_too_many_arguments() {
        interpreter.compute(getProgram("string_variable_equals_too_many_arguments"));
    }

    @Test
    public void string_variable_equals_true() {
        ExecutionContext context = new ExecutionContext();
        Object result = interpreter.compute(getProgram("string_variable_equals_true"), context);
        assertThat(result).as("result is true").isEqualTo(true);
        assertThat(context.getValueCount()).as("context contains a single variable").isEqualTo(1);
        assertThat(context.getValue("myVar")).as("context contains the declared variable with the correct String " +
                "value").isEqualTo("Value");
    }

    @Test
    public void string_variable_equals_false() {
        ExecutionContext context = new ExecutionContext();
        Object result = interpreter.compute(getProgram("string_variable_equals_false"), context);
        assertThat(result).as("result is false").isEqualTo(false);
        assertThat(context.getValueCount()).as("context contains a single variable").isEqualTo(1);
        assertThat(context.getValue("myVar")).as("context contains the declared variable with the correct String " +
                "value").isEqualTo("Value");
    }

    @Test
    public void string_variable_indexOf_two_arguments() {
        ExecutionContext context = new ExecutionContext();
        Object result = interpreter.compute(getProgram("string_variable_indexOf_two_arguments"), context);
        assertThat(result).as("result is 0").isEqualTo(0);
    }

    @Test
    public void context_access() {
        ExecutionContext context = new ExecutionContext();
        XatkitSession session = new XatkitSession("sessionID");
        session.getRuntimeContexts().setContextValue("test", 5, "var", "val");
        context.setSession(session);
        Object result = interpreter.compute(getProgram("context_access"), context);
        assertThat(result).as("result is a RuntimeContexts instance").isInstanceOf(Map.class);
        Map<String, Object> contextValues = (Map<String, Object>) result;
        assertThat(contextValues.get("var")).as("result contains the correct value").isEqualTo("val");
    }

    @Test
    public void context_access_get() {
        ExecutionContext context = new ExecutionContext();
        XatkitSession session = new XatkitSession("sessionID");
        session.getRuntimeContexts().setContextValue("test", 5, "var", "val");
        context.setSession(session);
        Object result = interpreter.compute(getProgram("context_access_get"), context);
        assertThat(result).as("correct context value result").isEqualTo("val");
    }

    @Test
    public void string_literal_plus_string_literal() {
        Object result = interpreter.compute(getProgram("string_literal_+_string_literal"));
        assertThat(result).as("valid concat result").isEqualTo("anothervalue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void string_literal_minus_string_literal() {
        interpreter.compute(getProgram("string_literal_-_string_literal"));
    }

    @Test
    public void int_literal_plus_int_literal() {
        Object result = interpreter.compute(getProgram("int_literal_+_int_literal"));
        assertThat(result).as("valid sum").isEqualTo(6);
    }

    @Test
    public void int_literal_minus_int_literal() {
        Object result = interpreter.compute(getProgram("int_literal_-_int_literal"));
        assertThat(result).as("valid substraction").isEqualTo(2);
    }

    @Test
    public void boolean_true_literal() {
        Object result = interpreter.compute(getProgram("boolean_true_literal"));
        assertThat(result).as("valid boolean (true) result").isEqualTo(true);
    }

    @Test
    public void boolean_false_literal() {
        Object result = interpreter.compute(getProgram("boolean_false_literal"));
        assertThat(result).as("valid boolean (false) result").isEqualTo(false);
    }

    @Test
    public void false_and_true() {
        Object result = interpreter.compute(getProgram("false_and_true"));
        assertThat(result).as("valid boolean (false) result").isEqualTo(false);
    }

    @Test
    public void false_and_false() {
        Object result = interpreter.compute(getProgram("false_and_false"));
        assertThat(result).as("valid boolean (false) result").isEqualTo(false);
    }

    @Test
    public void true_and_false() {
        Object result = interpreter.compute(getProgram("true_and_false"));
        assertThat(result).as("valid boolean (false) result").isEqualTo(false);
    }

    @Test
    public void true_and_true() {
        Object result = interpreter.compute(getProgram("true_and_true"));
        assertThat(result).as("valid boolean (true) result").isEqualTo(true);
    }

    @Test
    public void false_or_true() {
        Object result = interpreter.compute(getProgram("false_or_true"));
        assertThat(result).as("valid boolean (true) result").isEqualTo(true);
    }

    @Test
    public void false_or_false() {
        Object result = interpreter.compute(getProgram("false_or_false"));
        assertThat(result).as("valid boolean (false) result").isEqualTo(false);
    }

    @Test
    public void true_or_false() {
        Object result = interpreter.compute(getProgram("true_or_false"));
        assertThat(result).as("valid boolean (true) result").isEqualTo(true);
    }

    @Test
    public void true_or_true() {
        Object result = interpreter.compute(getProgram("true_or_true"));
        assertThat(result).as("valid boolean (true) result").isEqualTo(true);
    }

    @Test
    public void if_true_then_true_else_false() {
        Object result = interpreter.compute(getProgram("if_true_then_true_else_false"));
        assertThat(result).as("valid boolean (true) result").isEqualTo(true);
    }

    @Test
    public void if_true_then_string_else_int() {
        Object result = interpreter.compute(getProgram("if_true_then_string_else_int"));
        assertThat(result).as("valid String result").isEqualTo("Value");
    }

    @Test
    public void if_false_then_string_else_int() {
        Object result = interpreter.compute(getProgram("if_false_then_string_else_int"));
        assertThat(result).as("valid int result").isEqualTo(2);
    }

    @Test
    public void nested_ifs() {
        Object result = interpreter.compute(getProgram("nested_ifs"));
        assertThat(result).as("valid String result").isEqualTo("Value2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void if_string_then_string_else_int() {
        interpreter.compute(getProgram("if_string_then_string_else_int"));
    }

    @Test
    public void session() {
        ExecutionContext context = new ExecutionContext();
        XatkitSession session = new XatkitSession("sessionID");
        context.setSession(session);
        Object result = interpreter.compute(getProgram("session"), context);
        assertThat(result).as("valid XatkitSession result").isInstanceOf(XatkitSession.class);
        XatkitSession sessionResult = (XatkitSession) result;
        assertThat(sessionResult.getSessionId()).as("valid session ID").isEqualTo(session.getSessionId());
    }

    @Test
    public void session_get() {
        ExecutionContext context = new ExecutionContext();
        XatkitSession session = new XatkitSession("sessionID");
        session.store("test", "value");
        context.setSession(session);
        Object result = interpreter.compute(getProgram("session_get"), context);
        assertThat(result).as("valid String result").isInstanceOf(String.class);
        assertThat(result).as("valid String value").isEqualTo("value");
    }

    @Test
    public void session_store() {
        ExecutionContext context = new ExecutionContext();
        XatkitSession session = new XatkitSession("sessionID");
        context.setSession(session);
        Object result = interpreter.compute(getProgram("session_store"), context);
        assertThat(result).as("null result").isNull();
        assertThat(session.get("test")).as("valid session value").isEqualTo("value");
    }

    @Test
    public void session_store_get() {
        ExecutionContext context = new ExecutionContext();
        XatkitSession session = new XatkitSession("sessionID");
        context.setSession(session);
        Object result = interpreter.compute(getProgram("session_store_get"), context);
        assertThat(result).as("valid String result").isInstanceOf(String.class);
        assertThat(result).as("valid String value").isEqualTo("value");
        assertThat(session.get("test")).as("valid session value").isEqualTo("value");
    }

    @Test
    public void string_literal_isNull() {
        Object result = interpreter.compute(getProgram("string_literal_isNull"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void string_literal_nonNull() {
        Object result = interpreter.compute(getProgram("string_literal_nonNull"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void int_literal_greater_int_literal_true() {
        Object result = interpreter.compute(getProgram("int_literal_greater_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void int_literal_greater_int_literal_false() {
        Object result = interpreter.compute(getProgram("int_literal_greater_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void int_literal_greater_string_literal() {
        interpreter.compute(getProgram("int_literal_greater_string_literal"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void string_literal_greater_int_literal() {
        interpreter.compute(getProgram("string_literal_greater_int_literal"));
    }

    @Test
    public void int_literal_geq_int_literal_true() {
        Object result = interpreter.compute(getProgram("int_literal_geq_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void int_literal_geq_int_literal_false() {
        Object result = interpreter.compute(getProgram("int_literal_geq_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void int_literal_geq_int_literal_equals() {
        Object result = interpreter.compute(getProgram("int_literal_geq_int_literal_equals"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void int_literal_geq_string_literal() {
        interpreter.compute(getProgram("int_literal_geq_string_literal"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void string_literal_geq_int_literal() {
        interpreter.compute(getProgram("string_literal_geq_int_literal"));
    }

    @Test
    public void int_literal_lesser_int_literal_true() {
        Object result = interpreter.compute(getProgram("int_literal_lesser_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void int_literal_lesser_int_literal_false() {
        Object result = interpreter.compute(getProgram("int_literal_lesser_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void int_literal_lesser_string_literal() {
        interpreter.compute(getProgram("int_literal_lesser_string_literal"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void string_literal_lesser_int_literal() {
        interpreter.compute(getProgram("string_literal_lesser_int_literal"));
    }

    @Test
    public void int_literal_leq_int_literal_true() {
        Object result = interpreter.compute(getProgram("int_literal_leq_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void int_literal_leq_int_literal_false() {
        Object result = interpreter.compute(getProgram("int_literal_leq_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void int_literal_leq_int_literal_equals() {
        Object result = interpreter.compute(getProgram("int_literal_leq_int_literal_equals"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void int_literal_leq_string_literal() {
        interpreter.compute(getProgram("int_literal_leq_string_literal"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void string_literal_leq_int_literal() {
        interpreter.compute(getProgram("string_literal_leq_int_literal"));
    }

    @Test
    public void int_literal_equals_int_literal_true() {
        Object result = interpreter.compute(getProgram("int_literal_equals_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void int_literal_equals_int_literal_false() {
        Object result = interpreter.compute(getProgram("int_literal_equals_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void string_literal_equals_string_literal_true() {
        Object result = interpreter.compute(getProgram("string_literal_equals_string_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void string_literal_equals_string_literal_false() {
        Object result = interpreter.compute(getProgram("string_literal_equals_string_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void int_literal_equals_string_literal() {
        Object result = interpreter.compute(getProgram("int_literal_equals_string_literal"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void string_literal_equals_int_literal() {
        Object result = interpreter.compute(getProgram("string_literal_equals_int_literal"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void int_literal_not_equals_int_literal_true() {
        Object result = interpreter.compute(getProgram("int_literal_not_equals_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void int_literal_not_equals_int_literal_false() {
        Object result = interpreter.compute(getProgram("int_literal_not_equals_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void string_literal_not_equals_string_literal_true() {
        Object result = interpreter.compute(getProgram("string_literal_not_equals_string_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void string_literal_not_equals_string_literal_false() {
        Object result = interpreter.compute(getProgram("string_literal_not_equals_string_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void int_literal_not_equals_string_literal() {
        Object result = interpreter.compute(getProgram("int_literal_not_equals_string_literal"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void string_literal_not_equals_int_literal() {
        Object result = interpreter.compute(getProgram("string_literal_not_equals_int_literal"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void config_access_key_in_config() {
        Configuration configuration = new BaseConfiguration();
        configuration.addProperty("test.test", "value");
        XatkitSession session = new XatkitSession("sessionID", configuration);
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setSession(session);
        Object result = interpreter.compute(getProgram("config_access"), executionContext);
        assertThat(result).as("Result retrieved from Configuration").isEqualTo("value");
    }

    @Test
    public void config_access_key_not_in_config() {
        Object result = interpreter.compute(getProgram("config_access"));
        assertThat(result).as("Result is null").isNull();
    }

    @Test
    public void event_access_no_event_in_session() {
        Object result = interpreter.compute(getProgram("event_access"));
        assertThat(result).as("Result is null").isNull();
    }

    @Test
    public void event_access_event_in_session() {
        ExecutionContext context = createContextWithEventInstance();
        Object result = interpreter.compute(getProgram("event_access"), context);
        assertThat(result).as("Correct result").isEqualTo(TEST_EVENT_INSTANCE);
    }

    @Test
    public void event_access_get_definition() {
        ExecutionContext context = createContextWithEventInstance();
        Object result = interpreter.compute(getProgram("event_access_get_definition"), context);
        assertThat(result).as("Correct EventDefinition").isEqualTo(TEST_EVENT_INSTANCE.getDefinition());
    }

    @Test
    public void intent_access_no_intent_in_session() {
        Object result = interpreter.compute(getProgram("intent_access"));
        assertThat(result).as("Result is null").isNull();
    }

    @Test
    public void intent_access_event_in_session() {
        /*
         * Should return null, EventInstance is not a RecognizedIntent
         */
        ExecutionContext context = createContextWithEventInstance();
        Object result = interpreter.compute(getProgram("intent_access"), context);
        assertThat(result).as("Result is null").isNull();
    }

    @Test
    public void intent_access_intent_in_session() {
        ExecutionContext context = createContextWithRecognizedIntent();
        Object result = interpreter.compute(getProgram("intent_access"), context);
        assertThat(result).as("Correct result").isEqualTo(TEST_RECOGNIZED_INTENT);
    }

    @Test
    public void intent_access_get_definition() {
        ExecutionContext context = createContextWithRecognizedIntent();
        Object result = interpreter.compute(getProgram("intent_access_get_definition"), context);
        assertThat(result).as("Correct IntentDefinition").isEqualTo(TEST_RECOGNIZED_INTENT.getDefinition());
    }

    @Test
    public void intent_access_get_matched_input() {
        ExecutionContext context = createContextWithRecognizedIntent();
        Object result = interpreter.compute(getProgram("intent_access_get_matched_input"), context);
        assertThat(result).as("Correct matched input").isEqualTo(TEST_RECOGNIZED_INTENT.getMatchedInput());
    }

    @Test
    public void intent_access_get_recognition_confidence() {
        ExecutionContext context = createContextWithRecognizedIntent();
        Object result = interpreter.compute(getProgram("intent_access_get_recognition_confidence"), context);
        assertThat(result).as("Correct recognition confidence").isEqualTo(TEST_RECOGNIZED_INTENT.getRecognitionConfidence());
    }

    @Test
    public void float_literal() {
        Object result = interpreter.compute(getProgram("float_literal"));
        assertThat(result).as("Valid result").isEqualTo(2.5f);
    }

    @Test
    public void float_literal_equals_int_literal_true() {
        Object result = interpreter.compute(getProgram("float_literal_equals_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void float_literal_equals_int_literal_false() {
        Object result = interpreter.compute(getProgram("float_literal_equals_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void float_literal_not_equals_int_literal_true() {
        Object result = interpreter.compute(getProgram("float_literal_not_equals_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void float_literal_not_equals_int_literal_false() {
        Object result = interpreter.compute(getProgram("float_literal_not_equals_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void float_literal_geq_int_literal_true() {
        Object result = interpreter.compute(getProgram("float_literal_geq_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void float_literal_geq_int_literal_false() {
        Object result = interpreter.compute(getProgram("float_literal_geq_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    @Test
    public void float_literal_leq_int_literal_true() {
        Object result = interpreter.compute(getProgram("float_literal_leq_int_literal_true"));
        assertThat(result).as("Result is true").isEqualTo(true);
    }

    @Test
    public void float_literal_leq_int_literal_false() {
        Object result = interpreter.compute(getProgram("float_literal_leq_int_literal_false"));
        assertThat(result).as("Result is false").isEqualTo(false);
    }

    private ExecutionContext createContextWithEventInstance() {
        XatkitSession session = new XatkitSession("sessionID");
        session.store(ExecutionService.MATCHED_EVENT_SESSION_KEY, TEST_EVENT_INSTANCE);
        ExecutionContext context = new ExecutionContext();
        context.setSession(session);
        return context;
    }

    private ExecutionContext createContextWithRecognizedIntent() {
        XatkitSession session = new XatkitSession("sessionID");
        session.store(ExecutionService.MATCHED_EVENT_SESSION_KEY, TEST_RECOGNIZED_INTENT);
        ExecutionContext context = new ExecutionContext();
        context.setSession(session);
        return context;
    }

    private Resource getProgram(String fileName) {
        /*
         * Clear the previously loaded resources, just in case
         */
        rSet.getResources().clear();
        if (!fileName.endsWith(".xmi")) {
            /*
             * Avoid to repeat the ".xmi" in all the test cases
             */
            fileName = fileName + ".xmi";
        }
        URL resourceURL = CommonInterpreterTest.class.getClassLoader().getResource(baseURI + fileName);
        Resource resource = rSet.createResource(URI.createURI(baseURI + fileName));
        try {
            resource.load(resourceURL.openStream(), Collections.emptyMap());
        } catch (IOException e) {
            /*
             * Wrap the Exception to avoid catching it in all the tests.
             */
            throw new RuntimeException(e);
        }
        return resource;
    }
}
