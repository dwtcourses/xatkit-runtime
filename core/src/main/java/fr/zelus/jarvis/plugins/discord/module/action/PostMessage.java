package fr.zelus.jarvis.plugins.discord.module.action;

import fr.inria.atlanmod.commons.log.Log;
import fr.zelus.jarvis.core.JarvisAction;
import fr.zelus.jarvis.core.JarvisException;
import fr.zelus.jarvis.core.JarvisMessageAction;
import fr.zelus.jarvis.core.session.JarvisSession;
import fr.zelus.jarvis.plugins.discord.module.DiscordModule;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.text.MessageFormat;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * A {@link JarvisAction} that posts a {@code message} to a given Discord {@code channel}.
 * <p>
 * This class relies on the {@link DiscordModule}'s {@link net.dv8tion.jda.core.JDA} client to connect to the Discord
 * API and post messages.
 * <p>
 * <b>Note:</b> this class requires that its containing {@link DiscordModule} has been loaded with a valid Discord
 * bot token in order to authenticate the bot and post messages.
 */
public class PostMessage extends JarvisMessageAction<DiscordModule> {

    /**
     * The Discord channel to post the message to.
     */
    private MessageChannel channel;

    /**
     * Constructs a new {@link PostMessage} with the provided {@code containingModule}, {@code session}, {@code
     * message} and {@code channel}.
     *
     * @param containingModule the {@link DiscordModule} containing this action
     * @param session          the {@link JarvisSession} associated to this action
     * @param message          the message to post
     * @param channel          the Discord channel to post the message to
     * @throws NullPointerException     if the provided {@code containingModule} or {@code session} is {@code null}
     * @throws IllegalArgumentException if the provided {@code message} or {@code channel} is {@code null} or empty
     */
    public PostMessage(DiscordModule containingModule, JarvisSession session, String message, String channel) {
        super(containingModule, session, message);
        checkArgument(nonNull(channel) && !channel.isEmpty(), "Cannot construct a {0} action with the provided " +
                "channel {1}, expected a non-null and not empty String", this.getClass().getSimpleName(), channel);
        this.channel = module.getJdaClient().getPrivateChannelById(channel);
        if (isNull(this.channel)) {
            /*
             * The channel may be null if the provided ID corresponds to a user ID, in that case we can open a new
             * private channel by first retrieving the Discord User associated to the provided ID.
             */
            User user = module.getJdaClient().getUserById(channel);
            if (nonNull(user)) {
                Log.info("Opening a private channel with user {0} (id: {1})", user.getName(), user.getId());
                this.channel = user.openPrivateChannel().complete();
            } else {
                throw new JarvisException(MessageFormat.format("Cannot construct the %s action: the provided channel " +
                        "ID ({0}) does not correspond to an existing private channel or a valid Discord user",
                        channel));
            }
        }
    }

    /**
     * Posts the provided {@code message} to the given {@code channel}.
     * <p>
     * This method relies on the containing {@link DiscordModule}'s Discord {@link net.dv8tion.jda.core.JDA} client
     * to authenticate the bot and post the {@code message} to the given {@code channel}.
     *
     * @return {@code null}
     */
    @Override
    public Object compute() {
        channel.sendMessage(message).queue();
        return null;
    }

    @Override
    protected JarvisSession getClientSession() {
        return this.module.createSessionFromChannel(channel);
    }
}
