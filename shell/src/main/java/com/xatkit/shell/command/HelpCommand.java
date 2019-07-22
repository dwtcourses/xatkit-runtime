package com.xatkit.shell.command;

import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitShellException;
import fr.inria.atlanmod.commons.log.Log;

/**
 * Prints a help message to the shell user.
 * <p>
 * This command lists all the commands supported by the shell.
 * <p>
 * Usage: {@code help}.
 */
public class HelpCommand extends XatkitCommand {

    /**
     * The help message to print.
     */
    private static String HELP_MESSAGE;

    static {
        StringBuilder builder = new StringBuilder();
        builder.append("Xatkit Help\n")
                .append("# Command List\n")
                .append("\t- init <location>: creates a new sample project at the provided location\n")
                .append("\t- run <.properties file>: runs the Xatkit bot defined in the given .properties file\n")
                .append("\t- stop: stops the Xatkit bot currently running\n")
                .append("\t- exit: exits the current shell (and stops the running Xatkit bot)\n")
                .append("\t- help: prints this help message\n");
        HELP_MESSAGE = builder.toString();
    }

    /**
     * Constructs a {@link HelpCommand} with the provided {@code args} and {@code context}.
     *
     * @param args    the command's arguments
     * @param context the {@link CommandContext} associated to this command
     */
    public HelpCommand(String[] args, CommandContext context) {
        super(args, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws XatkitShellException {
        Log.info(HELP_MESSAGE);
    }
}
