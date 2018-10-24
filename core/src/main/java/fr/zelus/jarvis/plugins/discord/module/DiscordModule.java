package fr.zelus.jarvis.plugins.discord.module;

import fr.zelus.jarvis.core.JarvisCore;
import fr.zelus.jarvis.core.JarvisModule;
import fr.zelus.jarvis.core.session.JarvisSession;
import fr.zelus.jarvis.plugins.discord.JarvisDiscordUtils;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.apache.commons.configuration2.Configuration;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;
import static fr.zelus.jarvis.plugins.discord.JarvisDiscordUtils.DISCORD_TOKEN_KEY;
import static java.util.Objects.nonNull;

/**
 * A {@link JarvisModule} class that connects and interacts with Discord.
 * <p>
 * This module manages a connection to the Discord API, and provides a set of
 * {@link fr.zelus.jarvis.core.JarvisAction}s to interact with the Discord API:
 * <ul>
 * <li>{@link fr.zelus.jarvis.plugins.discord.module.action.Reply}: reply to a user input</li>
 * <li>{@link fr.zelus.jarvis.plugins.discord.module.action.PostMessage}: post a message to a given Discord
 * channel</li>
 * </ul>
 * <p>
 * This class is part of jarvis' core modules, and can be used in an orchestration model by importing the
 * <i>DiscordModule</i> package.
 */
public class DiscordModule extends JarvisModule {

    /**
     * The {@link JDA} client managing the Discord connection.
     * <p>
     * The {@link JDA} client is created from the Discord bot token stored in this class' {@link Configuration}
     * constructor parameter, and is used to authenticate the bot and post messages through the Discord API.
     *
     * @see #DiscordModule(JarvisCore, Configuration)
     * @see JarvisDiscordUtils
     */
    private JDA jdaClient;

    /**
     * Constructs a new {@link DiscordModule} from the provided {@link JarvisCore} and {@link Configuration}.
     * <p>
     * This constructor initializes the underlying {@link JDA} client with the Discord bot API token retrieved from
     * the {@link Configuration}.
     * <p>
     * <b>Note:</b> {@link DiscordModule} requires a valid Discord bot token to be initialized, and calling the
     * default constructor will throw an {@link IllegalArgumentException} when looking for the Discord bot token.
     *
     * @param jarvisCore    the {@link JarvisCore} instance associated to this module
     * @param configuration the {@link Configuration} used to retrieve the Discord bot token
     * @throws NullPointerException     if the provided {@code jarvisCore} or {@link Configuration} is {@code null}
     * @throws IllegalArgumentException if the provided Discord bot token is {@code null} or empty
     * @see JarvisDiscordUtils
     */
    public DiscordModule(JarvisCore jarvisCore, Configuration configuration) {
        super(jarvisCore, configuration);
        String discordToken = configuration.getString(DISCORD_TOKEN_KEY);
        checkArgument(nonNull(discordToken) && !discordToken.isEmpty(), "Cannot construct a DiscordModule from the " +
                "provided token %s, please ensure that the jarvis configuration contains a valid Discord bot API " +
                "token associated to the key %s", discordToken, DISCORD_TOKEN_KEY);
        jdaClient = JarvisDiscordUtils.getJDA(discordToken);
    }

    /**
     * Returns the {@link JDA} client managing the Discord connection.
     *
     * @return the {@link JDA} client managing the Discord connection
     */
    public JDA getJdaClient() {
        return jdaClient;
    }

    /**
     * Returns the {@link JarvisSession} associated to the provided {@link MessageChannel}.
     *
     * @param messageChannel the {@link MessageChannel} to retrieve the {@link JarvisSession} from
     * @return the {@link JarvisSession} associated to the provided {@link MessageChannel}
     */
    public JarvisSession createSessionFromChannel(MessageChannel messageChannel) {
        return this.jarvisCore.getOrCreateJarvisSession(messageChannel.getId());
    }
}
