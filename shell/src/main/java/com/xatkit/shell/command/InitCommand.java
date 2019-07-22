package com.xatkit.shell.command;

import com.xatkit.core.XatkitException;
import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitShellException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;

/**
 * Creates a sample Xatkit project is the provided directory.
 * <p>
 * Usage: {@code init <directory>}.
 */
public class InitCommand extends XatkitCommand {

    /**
     * The directory to create the sample project in.
     */
    private String folder;

    /**
     * The prefix of the template resources used to create the sample project.
     */
    private static String TEMPLATE_PREFIX = "templates/";

    /**
     * The files to add in the created project.
     */
    private static String[] TEMPLATE_FILES = {
            "ChatExampleExecution.execution",
            "ChatExampleExecution.xmi",
            "ChatExampleLibrary.intent",
            "ChatExampleLibrary.xmi",
            "deploy-react.properties"
    };

    /**
     * Constructs an {@link InitCommand} with the provided {@code args} and {@code context}.
     * <p>
     * The {@code args} array must contain two values: the name of the command ({@code "init"}) and the directory
     * location to create the sample project in.
     *
     * @param args    the command's arguments
     * @param context the {@link CommandContext} associated to this command
     */
    public InitCommand(String[] args, CommandContext context) {
        super(args, context);
    }

    /**
     * {@inheritDoc}
     *
     * @throws XatkitShellException if the directory already contains a file that will be erased by the command
     * @throws RuntimeException     if an error occurred when reading the templates/copying them to the destination
     *                              directory
     */
    @Override
    public void execute() throws XatkitShellException {
        checkArgument(args.length == 2, "Cannot compute %s, the command expects a single argument (folder location), " +
                "found %s", InitCommand.class.getSimpleName(), args.length);
        this.folder = args[1];
        File f = new File(folder);
        f.mkdirs();
        for (String templateFile : TEMPLATE_FILES) {
            copyTemplateFile(templateFile, f);
        }
        createProjectFile(f);
    }

    /**
     * Copies the provided {@code templateFile} to the given {@code parentFolder}.
     *
     * @param templateFile the file to copy
     * @param parentFolder the directory to copy the template file to
     * @throws XatkitShellException if the directory already contains a file that will be erased by the command
     * @throws RuntimeException     if an error occurred when reading the template/copying them to the destination
     *                              directory
     */
    private void copyTemplateFile(String templateFile, File parentFolder) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE_PREFIX + templateFile);
        File file = new File(parentFolder, templateFile);
        /*
         * Check if the file already exists in the target directory.
         */
        if (file.exists()) {
            throw new XatkitShellException(MessageFormat.format("Cannot copy the file {0}, the file already exists in" +
                    " the folder {1}", templateFile, parentFolder.getName()));
        }
        try {
            FileUtils.copyInputStreamToFile(is, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the {@code .project} file of the sample project and sets its name.
     *
     * @param parentFolder the directory to create the {@code .project} in
     */
    private void createProjectFile(File parentFolder) {
        File projectFile = new File(parentFolder, ".project");
        if (projectFile.exists()) {
            throw new XatkitException("Cannot create .project file, the file already exists");
        }
        try {
            FileWriter fileWriter = new FileWriter(projectFile);
            fileWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<projectDescription>\n" +
                    "\t<name>" + parentFolder.getName() + "</name>\n" +
                    "\t<comment></comment>\n" +
                    "\t<projects>\n" +
                    "\t</projects>\n" +
                    "\t<buildSpec>\n" +
                    "\t\t<buildCommand>\n" +
                    "\t\t\t<name>org.eclipse.xtext.ui.shared.xtextBuilder</name>\n" +
                    "\t\t\t<arguments>\n" +
                    "\t\t\t</arguments>\n" +
                    "\t\t</buildCommand>\n" +
                    "\t</buildSpec>\n" +
                    "\t<natures>\n" +
                    "\t\t<nature>org.eclipse.xtext.ui.shared.xtextNature</nature>\n" +
                    "\t</natures>\n" +
                    "</projectDescription>\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new XatkitException(e);
        }
    }
}
