package com.xatkit.shell.command;

import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitShellException;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;
import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;

/**
 * An abstract command.
 * <p>
 * This class provides basic argument checking methods and an abstract {@link #execute()} method that needs to be
 * implemented by its subclasses.
 * <p>
 * Note that no concrete execution code should be put in the constructor of {@link XatkitCommand}s.
 */
public abstract class XatkitCommand {

    /**
     * The command's arguments.
     */
    protected String[] args;

    /**
     * The {@link CommandContext} associated to this command.
     */
    protected CommandContext context;

    /**
     * Constructs a {@link XatkitCommand} with the provided {@code args} and {@code context}.
     *
     * @param args    the command's arguments
     * @param context the {@link CommandContext} associated to this command
     */
    public XatkitCommand(String[] args, CommandContext context) {
        checkArgument(args.length > 0, "Invalid argument count, expected at least 1, found 0");
        checkNotNull(context, "Cannot construct a %s with the provided %s %s", this.getClass().getSimpleName(),
                CommandContext.class.getSimpleName(), context);
        this.args = args;
        this.context = context;
    }

    /**
     * Executes the command.
     * <p>
     * This method throws a {@link XatkitShellException} if an error occurred during the execution of the command.
     * {@link XatkitShellException}s should contain a clear error message that is displayed to the shell user. Other
     * exceptions are reported with a full stack trace to the user.
     *
     * @throws XatkitShellException if an error occurred during the execution of the command.
     */
    public abstract void execute() throws XatkitShellException;
}
