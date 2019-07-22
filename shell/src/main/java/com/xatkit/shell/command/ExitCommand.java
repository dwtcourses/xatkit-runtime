package com.xatkit.shell.command;

import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitCommandType;
import com.xatkit.shell.XatkitShellException;
import fr.inria.atlanmod.commons.log.Log;

/**
 * Stops the running bot instance and exit the shell.
 * <p>
 * Usage: {@code exit}.
 */
public class ExitCommand extends XatkitCommand {

    /**
     * Constructs an {@link ExitCommand} with the provided {@code args} and {@code context}.
     *
     * @param args    the command's arguments
     * @param context the {@link CommandContext} associated to this command
     */
    public ExitCommand(String[] args, CommandContext context) {
        super(args, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws XatkitShellException {
        StopCommand stopCommand = new StopCommand(new String[]{XatkitCommandType.STOP.label}, context);
        try {
            stopCommand.execute();
        } catch (XatkitShellException e) {
            Log.info("There is no running bot to stop");
        }
        System.exit(0);
    }
}
