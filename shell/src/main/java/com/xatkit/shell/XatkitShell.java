package com.xatkit.shell;

import com.xatkit.shell.command.ExitCommand;
import com.xatkit.shell.command.HelpCommand;
import com.xatkit.shell.command.InitCommand;
import com.xatkit.shell.command.RunCommand;
import com.xatkit.shell.command.StopCommand;
import com.xatkit.shell.command.XatkitCommand;
import fr.inria.atlanmod.commons.log.Log;

import java.io.Console;

import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Runs the Xatkit shell.
 * <p>
 * The shell listens to user commands and executes them (see {@link com.xatkit.shell.command} package).
 */
public class XatkitShell {

    /**
     * Runs the Xatkit shell.
     * <p>
     * The shell does not expect any parameter, and will log a warning if a parameter is provided.
     * <p>
     * Unsupported commands or unknown inputs trigger the execution of the {@link HelpCommand} that prints additional
     * information to the shell user.
     *
     * @param args the program's arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length - 1; i++) {
                sb.append(args[i])
                        .append(", ");
            }
            sb.append(args[args.length - 1]);
            Log.warn("Xatkit shell does not take any parameters, ignoring {0}", sb.toString());
        }
        CommandContext context = new CommandContext();
        Console console = System.console();
        if (isNull(console)) {
            Log.error("No console provided in the environment, stopping the shell");
            return;
        } else {
            Log.info("Welcome to Xatkit shell!");
        }
        String line = null;
        while (nonNull(line = console.readLine())) {
            String[] command = line.split("\\s");
            if (nonNull(command) && command.length > 0) {
                switch (command[0]) {
                    case "init":
                        executeCommand(new InitCommand(command, context));
                        break;
                    case "run":
                        executeCommand(new RunCommand(command, context));
                        break;
                    case "stop":
                        executeCommand(new StopCommand(command, context));
                        break;
                    case "exit":
                        executeCommand(new ExitCommand(command, context));
                        /*
                         * The ExitCommand should stop the application anyway.
                         */
                        break;
                    case "help":
                        executeCommand(new HelpCommand(command, context));
                        break;
                    default:
                        Log.error("Unknown command {0}", command[0]);
                        executeCommand(new HelpCommand(command, context));
                }
            }
        }
    }

    /**
     * Executes the provided {@link XatkitCommand} and handles its thrown exceptions.
     *
     * @param command the {@link XatkitCommand} to execute
     */
    private static void executeCommand(XatkitCommand command) {
        checkNotNull(command, "Cannot run the provided command {0}", command);
        try {
            command.execute();
        } catch (XatkitShellException e) {
            Log.error(e.getMessage());
        } catch (Throwable e) {
            Log.error(e, "An error occurred when running the command {0}, see attached exception",
                    command.getClass().getSimpleName());
        }
    }

}
