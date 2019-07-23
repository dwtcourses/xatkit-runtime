package com.xatkit.shell.command;

import com.xatkit.Xatkit;
import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitShellException;
import fr.inria.atlanmod.commons.log.Log;

import java.io.Console;
import java.io.File;
import java.text.MessageFormat;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

/**
 * Runs the Xatkit bot from the provided {@code file}.
 * <p>
 * The provided {@link File} can be the {@code .properties} file to execute, or a directory. If the directory
 * contains a single {@code .properties} file it is directly executed, otherwise the list of files is printed to the
 * user who selects the one to use.
 * <p>
 * Usage: {@code run <file path>}.
 */
public class RunCommand extends XatkitCommand {

    /**
     * The {@link CommandContext} key used to store the running {@link com.xatkit.core.XatkitCore} instance.
     */
    public static String RUNNING_INSTANCE = "running.instance";

    /**
     * The path of the {@code .properties} file to run.
     */
    private String filePath;

    /**
     * Constructs a {@link RunCommand} with the provided {@code args} and {@code context}.
     * <p>
     * The {@code args} array must contain two values: the name of the command ({@code "run"}) and the location of
     * the {@code file} to run. This {@link File} can be either the {@code .properties} file to execute or a directory.
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
     * @throws XatkitShellException if the given {@code file} does not exist, or if the given directory does not
     *                              contain any {@code .properties} file, or if an error occurred when listing the
     *                              content of the directory
     */
    @Override
    public void execute() throws XatkitShellException {
        checkArgument(args.length == 2, "Cannot compute %s, the command expects a single argument (.properties file " +
                "location), but found %s", RunCommand.class.getSimpleName(), args.length);
        this.filePath = args[1];
        File file = new File(filePath);
        /*
         * Assume the .properties file to use is the one provided by the user.
         */
        File propertiesFile = file;
        if (file.exists()) {
            if (file.isDirectory()) {
                /*
                 * The user provided a directory, we need to check if it contains a .properties file and execute it.
                 * If multiple .properties file are found we need to ask which one to execute.
                 */
                File[] propertiesFiles = file.listFiles((dir, name) -> name.endsWith(".properties"));
                if (nonNull(propertiesFiles)) {
                    if (propertiesFiles.length == 1) {
                        propertiesFile = propertiesFiles[0];
                        Log.info("Executing .properties file {0}/{1}", filePath, propertiesFile.getName());
                    } else if (propertiesFiles.length > 1) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Available files:\n");
                        for (int i = 0; i < propertiesFiles.length; i++) {
                            sb.append("\t[")
                                    .append(i)
                                    .append("] ")
                                    .append(propertiesFiles[i].getName())
                                    .append("\n");
                        }
                        Log.info("Directory {0} contains multiple .properties files, please select the one to execute");
                        Log.info(sb.toString());
                        Console console = System.console();
                        Log.info("File to use >");
                        String line = console.readLine();
                        /*
                         * Do not deal with the exception, if there is an error it will be printed in the console.
                         */
                        int i = Integer.parseInt(line);
                        propertiesFile = propertiesFiles[i];
                    } else {
                        throw new XatkitShellException(MessageFormat.format("The directory {0} does not contain any " +
                                ".properties file", filePath));
                    }
                } else {
                    throw new XatkitShellException(MessageFormat.format("Cannot list the files in directory {0}",
                            file.getName()));
                }
            }
        } else {
            throw new XatkitShellException(MessageFormat.format("Cannot find {0}", this.filePath));
        }
        /*
         * Run the .properties file we found (this may be the given file, or a contained file if the provided file
         * was a directory).
         */
        Xatkit.main(new String[]{propertiesFile.getAbsolutePath()});
        /*
         * Store it in the context, so we can stop it later by retrieving it from there.
         */
        context.put(RUNNING_INSTANCE, Xatkit.getXatkitCore());
    }
}
