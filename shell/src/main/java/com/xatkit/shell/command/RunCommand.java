package com.xatkit.shell.command;

import com.xatkit.Xatkit;
import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitShellException;

import java.io.File;
import java.text.MessageFormat;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;

/**
 * Runs the Xatkit bot from the provided {@code .properties} file.
 * <p>
 * Usage: {@code run <.properties file>}.
 */
public class RunCommand extends XatkitCommand {

    /**
     * The {@link CommandContext} key used to store the running {@link com.xatkit.core.XatkitCore} instance.
     */
    public static String RUNNING_INSTANCE = "running.instance";

    /**
     * The path of the {@code .properties} file to run.
     */
    private String propertiesFilePath;

    /**
     * Constructs a {@link RunCommand} with the provided {@code args} and {@code context}.
     * <p>
     * The {@code args} array must contain two values: the name of the command ({@code "run"}) and the location of
     * the {@code .properties} file to run.
     *
     * @param args    the command's arguments
     * @param context the {@link CommandContext} associated to this command
     */
    public RunCommand(String[] args, CommandContext context) {
        super(args, context);
    }

    /**
     * {@inheritDoc}
     *
     * @throws XatkitShellException if the given {@code .properties} file does not exist
     */
    @Override
    public void execute() throws XatkitShellException {
        checkArgument(args.length == 2, "Cannot compute %s, the command expects a single argument (.properties file " +
                "location), but found %s", RunCommand.class.getSimpleName(), args.length);
        this.propertiesFilePath = args[1];
        File f = new File(propertiesFilePath);
        if (f.exists()) {
            Xatkit.main(new String[]{this.propertiesFilePath});
        } else {
            throw new XatkitShellException(MessageFormat.format("Cannot find {0}", this.propertiesFilePath));
        }
        /*
         * Store it in the context, so we can stop it later by retrieving it from there.
         */
        context.put(RUNNING_INSTANCE, Xatkit.getXatkitCore());
    }
}
