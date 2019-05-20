package edu.uoc.som.jarvis.plugins.react.platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import edu.uoc.som.jarvis.core.JarvisCore;
import edu.uoc.som.jarvis.core.session.JarvisSession;
import edu.uoc.som.jarvis.plugins.chat.platform.ChatPlatform;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.configuration2.Configuration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * A {@link ChatPlatform} class that interacts with the
 * <a href="https://github.com/jarvis-platform/jarvis-react">Jarvis React component</a>.
 * <p>
 * This {@link ChatPlatform} manages a list of pending messages that can be queried by the jarvis-react application
 * to display them. It initializes a REST endpoint and registers it to the Jarvis server, allowing to reply to
 * jarvis-react REST queries.
 * <p>
 * This platform provides the following actions:
 * <ul>
 * <li>{@link edu.uoc.som.jarvis.plugins.react.platform.action.Reply}: replies to a user input</li>
 * <li>{@link edu.uoc.som.jarvis.plugins.react.platform.action.PostMessage}: post a message to a given channel
 * (i.e. window running a jarvis-react instance)</li>
 * </ul>
 * <p>
 * This platform registers a webhook at {@code /react/getAnswer}, that provides the last answer associated to a given
 * channel.
 * <p>
 * This class is part of jarvis" core paltform, and can be used in an execution model by importing the
 * <i>ReactPlatform</i> package.
 */
public class ReactPlatform extends ChatPlatform {

    /**
     * The {@link Map} containing the pending messages that can be queried by the jarvis-react application.
     */
    private Map<String, Queue<String>> storedMessages;

    /**
     * Constructs a new {@link ReactPlatform} from the provided {@link JarvisCore} and {@link Configuration}.
     * <p>
     * This constructor initializes the underlying REST endpoint at {@code /react/getAnswers}, allowing to retrieve
     * the stored answers associated to a channel. The stored message are returned as a {@code json} object with the
     * following structure:
     * <pre>
     * {@code
     * {
     *  messages: [
     *      {value: 'Message 1'},
     *      {value: 'Message 2"}
     *  ]
     * }
     * }
     * </pre>
     * Note that client applications can iterate the {@code messages} array to display the messages in the same order
     * they have been produced.
     * <p>
     * A REST request on {@code /rest/getAnswers} must contain a JSON object with a {@code channel} field containing
     * the channel to retrieve the answer for.
     *
     * @param jarvisCore    the {@link JarvisCore} instance associated to this runtimePlatform
     * @param configuration the platform's {@link Configuration}
     * @throws NullPointerException if the provided {@code jarvisCore} or {@code configuration} is {@code null}
     */
    public ReactPlatform(JarvisCore jarvisCore, Configuration configuration) {
        super(jarvisCore, configuration);
        this.storedMessages = new HashMap<>();
        /*
         * Register the REST endpoint at /react/getAnswers. Note that the received request must contain a JSON object
         * defining the channel field.
         */
        this.getJarvisCore().getJarvisServer().registerRestEndpoint("/react/getAnswers",
                (headers, param, content) -> {
                    JsonObject contentObject = content.getAsJsonObject();
                    String channel = contentObject.get("channel").getAsString();
                    Queue<String> messageQueue = this.getMessagesFor(channel);
                    if (isNull(messageQueue)) {
                        Log.info("No messages awaiting for {0}", channel);
                        return null;
                    }
                    JsonObject result = new JsonObject();
                    JsonArray array = new JsonArray();
                    result.add("messages", array);
                    String message = messageQueue.poll();
                    /*
                     * Use a while loop here, Queue#iterator() does not guarantees the order of the elements.
                     */
                    while (nonNull(message)) {
                        JsonObject messageObject = new JsonObject();
                        messageObject.add("value", new JsonPrimitive(message));
                        array.add(messageObject);
                        message = messageQueue.poll();
                    }
                    return result;
                });
    }

    /**
     * Stores the provided {@code message} for the given {@code channel}.
     * <p>
     * The stored message can be accessed by jarvis-react through the {@code /react/getAnswers} endpoint.
     *
     * @param channel the channel associated to the message to store
     * @param message the message to store
     */
    public void storeMessage(String channel, String message) {
        Queue<String> messageQueue = this.storedMessages.get(channel);
        if (isNull(messageQueue)) {
            messageQueue = new LinkedList<>();
            this.storedMessages.put(channel, messageQueue);
        }
        messageQueue.add(message);
    }

    /**
     * Returns the messages stored for the provided {@code channel}.
     * <p>
     * This method returns a {@link Queue} containing the stored messages. Note that calling {@link Queue#poll()} on
     * the returned object will remove the message.
     *
     * @param channel the channel to retrieve the messages for
     * @return a {@link Queue} containing the messages stored for the provided {@code channel}
     */
    public Queue<String> getMessagesFor(String channel) {
        return this.storedMessages.get(channel);
    }

    /**
     * Creates a {@link JarvisSession} from the provided {@code channel}.
     * <p>
     * This method ensures that the same {@link JarvisSession} is returned for the same {@code channel}.
     *
     * @param channel the channel to create a {@link JarvisSession} from
     * @return the created {@link JarvisSession}
     */
    public JarvisSession createSessionFromChannel(String channel) {
        return this.jarvisCore.getOrCreateJarvisSession(channel);
    }

}
