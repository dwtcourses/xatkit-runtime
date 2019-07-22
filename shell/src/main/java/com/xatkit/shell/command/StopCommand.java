package com.xatkit.shell.command;

import com.xatkit.core.XatkitCore;
import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitShellException;

import java.text.MessageFormat;

import static java.util.Objects.nonNull;

/**
 * Stops the running Xatkit bot.
 * <p>
 * Usage: {@code stop}.
 */
public class StopCommand extends XatkitCommand {

    /**
     * Constructs a {@link StopCommand} with the provided {@code args} and {@code context}.
     *
     * @param args    the command's arguments
     * @param context the {@link CommandContext} associated to this command
     */
    public StopCommand(String[] args, CommandContext context) {
        super(args, context);
    }

    /**
     * {@inheritDoc}
     *
     * @throws XatkitShellException  if there is no running instance of Xatkit to stop
     * @throws IllegalStateException if the stored running instance is not a {@link XatkitCore} instance
     */
    @Override
    public void execute() throws XatkitShellException {
        Object runningInstance = context.get(RunCommand.RUNNING_INSTANCE);
        if (nonNull(runningInstance)) {
            if (runningInstance instanceof XatkitCore) {
                XatkitCore xatkitCore = (XatkitCore) runningInstance;
                xatkitCore.shutdown();
                /*
                 * The instance is shutdown, we can remove it from the context.
                 */
                context.remove(RunCommand.RUNNING_INSTANCE);
            } else {
                throw new IllegalStateException(MessageFormat.format("The command context value {0} contains an " +
                                "instance of {1} instead of {2}", RunCommand.RUNNING_INSTANCE,
                        runningInstance.getClass().getSimpleName(), XatkitCore.class.getSimpleName()));
            }
        } else {
            throw new XatkitShellException("There is no running instance of Xatkit to stop");
        }
    }
}
