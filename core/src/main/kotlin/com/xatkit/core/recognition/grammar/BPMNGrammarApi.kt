package com.xatkit.core.recognition.grammar

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.request.BaseRequest
import com.xatkit.core.XatkitCore
import com.xatkit.core.XatkitException
import com.xatkit.core.platform.action.RestAction
import com.xatkit.core.recognition.IntentRecognitionProvider
import com.xatkit.core.recognition.RecognitionMonitor
import com.xatkit.core.session.XatkitSession
import com.xatkit.intent.Context
import com.xatkit.intent.ContextInstance
import com.xatkit.intent.ContextParameter
import com.xatkit.intent.ContextParameterValue
import com.xatkit.intent.EntityDefinition
import com.xatkit.intent.IntentDefinition
import com.xatkit.intent.IntentFactory
import com.xatkit.intent.RecognizedIntent
import fr.inria.atlanmod.commons.log.Log
import org.apache.commons.configuration2.Configuration
import java.io.InputStream


class BPMNGrammarApi(private val xatkitCore: XatkitCore, private val configuration: Configuration,
                     private val recognitionMonitor: RecognitionMonitor?) : IntentRecognitionProvider {

    companion object {
        private val factory = IntentFactory.eINSTANCE;

        const val GRAMMAR_PROVIDER_KEY: String = "xatkit.grammar.provider"
        const val BPMN_GRAMMAR_VALUE: String = "bpmn"
        const val BPMN_GRAMMAR_SERVER_URL_KEY = "xatkit.grammar.bpmn.server";
        const val DEFAULT_BPMN_GRAMMAR_SERVER_URL = "http://localhost:8084/parse"

        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val DEFAULT_FALLBACK_INTENT: IntentDefinition = factory.createIntentDefinition()

        init {
            DEFAULT_FALLBACK_INTENT.name = "Default_Fallback_Intent"
        }
    }

    private var isShutdown: Boolean = false

    private val bpmnGrammarServerUrl: String = configuration.getString(BPMN_GRAMMAR_SERVER_URL_KEY,
            DEFAULT_BPMN_GRAMMAR_SERVER_URL)

    override fun getIntent(input: String, session: XatkitSession): RecognizedIntent {
        val request: BaseRequest = Unirest.post(bpmnGrammarServerUrl)
                .header("Content-Type", "application/json")
                .body(buildJsonRequest(input, session))

        val response: HttpResponse<InputStream> = request.asBinary()
        val status: Int = response.status
        if (status != 200) {
            Log.error("Cannot retrieve the intent from the provided input, the grammar server returned a status code " +
                    "$status")
            return createDefaultFallbackRecognizedIntent()
        }
        val body: JsonObject = RestAction.getJsonBody(response.body).asJsonObject
        val result: String = body.get("result").asString
        if (result != "OK") {
            Log.error("Cannot retrieve the intent from the provided input, the grammar server returned the following " +
                    "result: $result")
            return createDefaultFallbackRecognizedIntent()
        }
        val recognizedIntent = convertGrammarIntentToRecognizedIntent(body)
        Log.info("status code: {0}", status)
        Log.info("Response: {0}", gson.toJson(body))

        setSessionValues(body, session)

        return recognizedIntent
    }

    private fun createDefaultFallbackRecognizedIntent(): RecognizedIntent {
        val recognizedIntent: RecognizedIntent = factory.createRecognizedIntent()
        recognizedIntent.definition = DEFAULT_FALLBACK_INTENT
        return recognizedIntent
    }

    private fun setSessionValues(jsonObject: JsonObject, session: XatkitSession) {
        val intentStyle: JsonObject = jsonObject.getAsJsonObject("intent-style")!!
        // convert to a map<string,boolean> to get rid of GSON types
        val intentStyleMap: Map<String, Boolean> = intentStyle.entrySet().map { entry ->
            entry!!.key to entry.value.asBoolean
        }.toMap()
        session.store("intent-style", intentStyleMap)
    }

    private fun buildJsonRequest(message: String, session: XatkitSession): String {
        var activityLabels: String? = (session.get("activity-labels") as List<String>?)?.joinToString(prefix = "[",
                postfix = "]", separator = ",", transform = { "\"$it\"" })
        if (activityLabels == null) {
            activityLabels = """["A", "B", "C"]"""
            Log.warn("The session doesn't contain the activity-labels entry, using default value $activityLabels")
        }
        var activityAliases: String? = (session.get("activity-aliases") as List<Int>?)?.joinToString(prefix =
        "[", postfix = "]", separator = ",", transform = { it.toString() })
        if(activityAliases == null) {
            activityAliases = """[1,2,3]"""
            Log.warn("The session doesn't contain the activity-labels entry, using default value $activityAliases")
        }
        val jsonRequest: String = """
            { 
                "message": "$message", 
                "context": {
                    "language": "EN",
                    "activity-labels": $activityLabels,
                    "activity-aliases": $activityAliases,
                    "session-id": "${session.sessionId}"
                }
            }
        """.trimIndent()
        Log.info("JSON Request : {0}", jsonRequest)
        return jsonRequest
    }

    private fun convertGrammarIntentToRecognizedIntent(jsonObject: JsonObject): RecognizedIntent {
        var intentName: String = jsonObject.get("intent")!!.asString
        if (intentName == "RelabelActivity") {
            intentName = "RenameActivity"
        } else if (intentName == "AddConstraint") {
            val constraintName: String = getConstraintName(jsonObject)
            intentName = when (constraintName) {
                "INVERSE-BACKWARDS-PATH" -> "ActivityBackpath"
                "BETWEEN" -> "ActivityBetween"
                "PRECEDENCE" -> "ActivityBefore"
                "PRECEDENCE-PRE" -> "ActivityBefore"
                "INVERSE-PRECEDENCE" -> "ActivityAfter"
                "REPEATS-PRE" -> "ActivityRepeats"
                "REPEATS-SUF" -> "ActivityRepeats"
                "CONFLICT-PRE" -> "ActivitiesNoCoOccurs"
                "CONFLICT" -> "ActivitiesNoCoOccurs"
                else -> throw XatkitException("Cannot find the constraint $constraintName")
            }
        }
        val intentDefinition: IntentDefinition = xatkitCore.eventDefinitionRegistry.getIntentDefinition(intentName)
                ?: DEFAULT_FALLBACK_INTENT
        val recognizedIntent: RecognizedIntent = factory.createRecognizedIntent()
        recognizedIntent.definition = intentDefinition
        recognizedIntent.recognitionConfidence = 1f
        when (intentName) {
            "AddActivity" -> setAddActivityContext(recognizedIntent, intentDefinition, jsonObject.parameters)
            "LoadModel" -> setLoadModelContext(recognizedIntent, intentDefinition, jsonObject.parameters)
            "SaveModel" -> setSaveModelContext(recognizedIntent, intentDefinition, jsonObject.parameters)
            "RenameActivity" -> setRenameActivityContext(recognizedIntent, intentDefinition, jsonObject.parameters)
            "ActivityBackpath" -> setActivityBackpathContext(recognizedIntent, intentDefinition, jsonObject.parameters)
            "ActivityBefore" -> setActivityBeforeContext(recognizedIntent, intentDefinition, jsonObject.parameters)
            "ActivityAfter" -> setActivityAfterContext(recognizedIntent, intentDefinition, jsonObject.parameters)
            "ActivityBetween" -> setActivityBetweenContext(recognizedIntent, intentDefinition, jsonObject.parameters)
            "ActivityRepeats" -> setActivityRepeatsContext(recognizedIntent, intentDefinition, jsonObject.parameters)
            "ActivitiesNoCoOccurs" -> setActivitiesNoCoOccurs(recognizedIntent, intentDefinition, jsonObject.parameters)
        }
        return recognizedIntent
    }

    private fun getConstraintName(jsonObject: JsonObject): String {
        val constraintArray: JsonArray? = jsonObject.parameters.getInnerListWithKey("CONSTRAINT-TYPE")
                ?: jsonObject.parameters.getInnerListWithKey("CONSTRAINT-TYPE-PRE")
                ?: jsonObject.parameters.getInnerListWithKey("TERNARY-CONSTRAINT-TYPE")
                ?: jsonObject.parameters.getInnerListWithKey("UNARY-CONSTRAINT-TYPE-PRE")
                ?: jsonObject.parameters.getInnerListWithKey("UNARY-CONSTRAINT-TYPE-SUF")
        return constraintArray!!.flattenListWrapper()[0].asString
    }

    private fun setAddActivityContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                      jsonParams: JsonArray) {
        val contextDefinition: Context = intentDefinition.getOutContext("Activity")!!
        val contextInstance: ContextInstance = contextDefinition.createInstance()

        // [ "ACT", [ ... ] ]
        val jsonActValues: List<String> = jsonParams.getInnerListWithKey("ACT")!!.map { p -> p.asString }
        val joinedParameters: String = jsonActValues.joinToString(",")

        val contextParameter: ContextParameter = contextDefinition.getContextParameter("name")
        val contextParameterValue: ContextParameterValue = contextParameter.createValue(joinedParameters)
        contextInstance.values.add(contextParameterValue)
        recognizedIntent.outContextInstances.add(contextInstance)
    }

    private fun setLoadModelContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                    jsonParams: JsonArray) {
        this.setModelNameContext(recognizedIntent, intentDefinition, jsonParams)
    }

    private fun setSaveModelContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                    jsonParams: JsonArray) {
        this.setModelNameContext(recognizedIntent, intentDefinition, jsonParams)
    }

    private fun setRenameActivityContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                         jsonParams: JsonArray) {
        val contextDefinition: Context = intentDefinition.getOutContext("Activity")!!
        val contextInstance: ContextInstance = contextDefinition.createInstance()
        // [0][0][1] -> [0] wrapping list, [0] wrapping list2, [1] = concrete list
        // [0] = DEF-ACT-LABEL || DEF-ACT-ALIAS
        val activityIdentifier: String = jsonParams.getInnerListWithKey("DEF-ACT")!!.flattenListWrapper()[1].asString
        val activityContextParameterValue: ContextParameterValue = contextDefinition.getContextParameter("activity")
                .createValue(activityIdentifier)

        val newLabel: String = jsonParams.getInnerListWithKey("ACT")!![0].asString
        val newLabelContextParameterValue: ContextParameterValue = contextDefinition.getContextParameter("newLabel")
                .createValue(newLabel)

        contextInstance.values.add(activityContextParameterValue)
        contextInstance.values.add(newLabelContextParameterValue)
        recognizedIntent.outContextInstances.add(contextInstance)
    }

    private fun setActivityBackpathContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                           jsonParams: JsonArray) {
        val contextDefinition: Context = intentDefinition.getOutContext("Backpath")!!
        val contextInstance: ContextInstance = contextDefinition.createInstance()

        val scopes: List<Pair<String, String>> = getScopes(jsonParams)
        val scope1Value = contextDefinition.getContextParameter("backpathScope").createValue(scopes[0].first)
        val backpathActivity = contextDefinition.getContextParameter("backpathActivity").createValue(scopes[0].second)
        val scope2Value = contextDefinition.getContextParameter("loopScope").createValue(scopes[1].first)
        val loopActivity = contextDefinition.getContextParameter("loopActivity").createValue(scopes[1].second)

        contextInstance.values.addAll(listOf(scope1Value, backpathActivity, scope2Value, loopActivity))
        recognizedIntent.outContextInstances.add(contextInstance)
    }

    private fun setActivityBeforeContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                         jsonParams: JsonArray) {
        val contextDefinition = intentDefinition.getOutContext("Precedence")!!
        val contextInstance = contextDefinition.createInstance()
        val scopes = getScopes(jsonParams)
        setBeforeAndAfterActivities(contextInstance, contextDefinition, scopes)
        recognizedIntent.outContextInstances.add(contextInstance)
    }

    private fun setActivityAfterContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                        jsonParams: JsonArray) {
        val contextDefinition = intentDefinition.getOutContext("Succession")!!
        val contextInstance = contextDefinition.createInstance()

        val scopes = getScopes(jsonParams)
        setBeforeAndAfterActivities(contextInstance, contextDefinition, scopes)
        recognizedIntent.outContextInstances.add(contextInstance)
    }

    // set the values before/afterScope and before/afterActivity of the provided ContextInstance from the provided
    // Context
    private fun setBeforeAndAfterActivities(contextInstance: ContextInstance, contextDefinition: Context, scopes: List<Pair<String, String>>) {
        val beforeScope = contextDefinition.getContextParameter("beforeScope").createValue(scopes[0].first)
        val beforeActivity = contextDefinition.getContextParameter("beforeActivity").createValue(scopes[0].second)
        val afterScope = contextDefinition.getContextParameter("afterScope").createValue(scopes[1].first)
        val afterActivity = contextDefinition.getContextParameter("afterActivity").createValue(scopes[1].second)

        contextInstance.values.addAll(listOf(beforeScope, beforeActivity, afterScope, afterActivity))
    }

    private fun setActivityBetweenContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                          jsonParams: JsonArray) {
        val contextDefinition: Context = intentDefinition.getOutContext("Between")!!
        val contextInstance: ContextInstance = contextDefinition.createInstance()

        val scopes: List<Pair<String, String>> = getScopes(jsonParams)
        val betweenScope = contextDefinition.getContextParameter("scopeBetween").createValue(scopes[0].first)
        val betweenActivity = contextDefinition.getContextParameter("between").createValue(scopes[0].second)
        val leftScope = contextDefinition.getContextParameter("scopeLeft").createValue(scopes[1].first)
        val leftActivity = contextDefinition.getContextParameter("left").createValue(scopes[1].second)
        val rightScope = contextDefinition.getContextParameter("scopeRight").createValue(scopes[2].first)
        val rightActivity = contextDefinition.getContextParameter("right").createValue(scopes[2].second)

        contextInstance.values.addAll(listOf(betweenScope, betweenActivity, leftScope, leftActivity, rightScope, rightActivity))
        recognizedIntent.outContextInstances.add(contextInstance)
    }

    private fun setActivityRepeatsContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                          jsonParams: JsonArray) {
        val contextDefinition: Context = intentDefinition.getOutContext("Repeat")!!
        val contextInstance: ContextInstance = contextDefinition.createInstance()
        val scopes = getScopes(jsonParams)
        val scope = contextDefinition.getContextParameter("scope").createValue(scopes[0].first)
        val activity = contextDefinition.getContextParameter("activity").createValue(scopes[0].second)

        contextInstance.values.addAll(listOf(scope, activity))
        recognizedIntent.outContextInstances.add(contextInstance)
    }

    private fun setActivitiesNoCoOccurs(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                        jsonParams: JsonArray) {
        val contextDefinition = intentDefinition.getOutContext("NoCoOccurs")!!
        val contextInstance = contextDefinition.createInstance()
        val scopes = getScopes(jsonParams)
        val scope1 = contextDefinition.getContextParameter("leftScope").createValue(scopes[0].first)
        val left = contextDefinition.getContextParameter("left").createValue(scopes[0].second)
        val scope2 = contextDefinition.getContextParameter("rightScope").createValue(scopes[1].first)
        val right = contextDefinition.getContextParameter("right").createValue(scopes[1].second)

        contextInstance.values.addAll(listOf(scope1, left, scope2, right))
        recognizedIntent.outContextInstances.add(contextInstance)
    }

    // returns a list [scope1 -> actId1, scope2 -> actId2 ...]
    private fun getScopes(jsonParams: JsonArray): List<Pair<String, String>> {
        val res: MutableList<Pair<String, String>> = ArrayList()
        val simpleSelectorList: JsonArray? = jsonParams.getInnerListWithKey("SELECTOR")
        if (simpleSelectorList != null) {
            simpleSelectorList.forEach { s ->
                val selector: String = (s as JsonArray).flattenListWrapper().getInnerListWithKey("SCOPE")!![0].asString
                // Mapping needed, selectors don't have the same names in the grammar server and the intent file
                val xatkitSelector: String = when (selector) {
                    "BRANCH-SEL" -> "Branch of"
                    "ACT-SEL" -> "Activity"
                    "BLOCK-SEL" -> "Block of"
                    else -> throw XatkitException("Unknown selector $selector")
                }
                val activityIdentifier: String = s.flattenListWrapper().getInnerListWithKey("DEF-ACT")!!
                        .flattenListWrapper()[1].asString
                res.add(xatkitSelector to activityIdentifier)
            }
        } else {
            val selectorPluralType: String = jsonParams.getInnerListWithKey("SELECTOR-PLURAL")!!.flattenListWrapper()[0].asString
            val xatkitSelector = when {
                selectorPluralType.contains("ACT") -> "Activity"
                selectorPluralType.contains("BRANCH") -> "Branch of"
                selectorPluralType.contains("BLOCK") -> "Block of"
                else -> throw XatkitException("Unknown selector $selectorPluralType")
            }
            jsonParams.getInnerListWithKey("DEF-ACT")!!.flattenListWrapper().forEach { a ->
                val activityIdentifier: String = (a as JsonArray).flattenListWrapper()[1].asString
                res.add(xatkitSelector to activityIdentifier)
            }
        }
        return res
    }

    /*
     * Flattens
     * [
                [
                    [
                        "DEF-ACT-LABEL",
                        "A"
                    ]
                ]
            ]
     * into
     * [ "DEF-ACT-LABEL", "A"]
     * Only works for empty wrappers
     * Always returns a JsonArray
     */
    private fun JsonArray.flattenListWrapper(): JsonArray {
        return if (this.size() == 1) {
            val content: JsonElement = this[0]
            if (content is JsonArray) {
                content.flattenListWrapper()
            } else {
                this
            }
        } else {
            this
        }
    }

    private val JsonObject.parameters: JsonArray
        get() = this.getAsJsonArray("parameters")!!

    private fun setModelNameContext(recognizedIntent: RecognizedIntent, intentDefinition: IntentDefinition,
                                    jsonParams: JsonArray) {
        val contextDefinition: Context = intentDefinition.getOutContext("Model")!!
        val contextInstance: ContextInstance = contextDefinition.createInstance()

        val jsonNameValue: String = jsonParams.getInnerListWithKey("NAME")!![0].asString
        val contextParameterValue: ContextParameterValue = contextDefinition.getContextParameter("name").createValue(jsonNameValue)
        contextInstance.values.add(contextParameterValue)
        recognizedIntent.outContextInstances.add(contextInstance)
    }

    private fun Context.createInstance(lifespanCount: Int = 5): ContextInstance {
        val contextInstance: ContextInstance = factory.createContextInstance()
        contextInstance.definition = this
        contextInstance.lifespanCount = lifespanCount
        return contextInstance
    }

    private fun ContextParameter.createValue(value: String): ContextParameterValue {
        val contextParameterValue: ContextParameterValue = factory.createContextParameterValue()
        contextParameterValue.contextParameter = this
        contextParameterValue.value = value
        return contextParameterValue
    }

    /*
     * in:
     * [
     *  [
     *      "key",
     *      [
     *          xxx
     *      ]
     *  ]
     *      bbb
     * ]
     * out: [ xxx ]
     */
    private fun JsonArray.getInnerListWithKey(key: String): JsonArray? {
        val keyList: JsonArray? = this.firstOrNull { p -> p is JsonArray && p[0]!!.asString == key } as? JsonArray
        return keyList?.get(1) as? JsonArray
    }


    override fun getRecognitionMonitor(): RecognitionMonitor? {
        return this.recognitionMonitor
    }

    override fun shutdown() {
        this.isShutdown = true
    }

    override fun isShutdown(): Boolean {
        return this.isShutdown
    }

    override fun createSession(sessionId: String?): XatkitSession {
        return XatkitSession(sessionId)
    }

    // Not implemented, the provider is specific to a grammar


    override fun registerEntityDefinition(entityDefinition: EntityDefinition?) {
    }

    override fun registerIntentDefinition(intentDefinition: IntentDefinition?) {
    }

    override fun deleteEntityDefinition(entityDefinition: EntityDefinition?) {
    }

    override fun deleteIntentDefinition(intentDefinition: IntentDefinition?) {
    }

    override fun trainMLEngine() {
    }

}