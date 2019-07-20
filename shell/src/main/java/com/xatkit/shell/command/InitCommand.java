package com.xatkit.shell.command;

import com.xatkit.core.XatkitException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;

public class InitCommand {

    private String folder;

    private static String TEMPLATE_PREFIX = "templates/";

    private static String[] TEMPLATE_FILES = {
            "ChatExampleExecution.execution",
            "ChatExampleExecution.xmi",
            "ChatExampleLibrary.intent",
            "ChatExampleLibrary.xmi",
            "deploy-react.properties"
    };

    // args[0] = init
    // args[1] = folder
    public InitCommand(String[] args) {
        checkArgument(args.length == 2, "Cannot compute %s the command expects a single argument (folder location), " +
                "found %s", InitCommand.class.getSimpleName(), args.length);
        this.folder = args[1];
        File f = new File(folder);
        f.mkdirs();
        for(String templateFile : TEMPLATE_FILES) {
            copyTemplateFile(templateFile, f);
        }
        createProjectFile(f);
    }

    private void copyTemplateFile(String templateFile, File parentFolder) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(TEMPLATE_PREFIX + templateFile);
        File file = new File(parentFolder, templateFile);
        // check if it already exists
        if(file.exists()) {
            throw new XatkitException(MessageFormat.format("Cannot copy the file {0}, the file already exists in the " +
                    "folder {1}", templateFile, parentFolder.getName()));
        }
        try {
            FileUtils.copyInputStreamToFile(is, file);
        } catch(IOException e) {
            throw new XatkitException(e);
        }
    }

    private void createProjectFile(File parentFolder) {
        File projectFile = new File(parentFolder, ".project");
        if(projectFile.exists()) {
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
        } catch(IOException e) {
            throw new XatkitException(e);
        }
    }
}
