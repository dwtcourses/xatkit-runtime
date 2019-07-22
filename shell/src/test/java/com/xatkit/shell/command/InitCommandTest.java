package com.xatkit.shell.command;

import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitCommandType;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class InitCommandTest {

    public static String TEST_FOLDER_LOCATION = "initCommandTest";

    @AfterClass
    public static void tearDownAfterClass() {
        deleteTestFolder();
    }

    public static void deleteTestFolder() {
        try {
            FileUtils.deleteDirectory(new File(TEST_FOLDER_LOCATION));
        } catch (IOException e) {
            Log.error("An error occurred when deleting the test folder {0}, see the attached exception for more " +
                    "details", TEST_FOLDER_LOCATION, e);
        }
    }

    @Before
    public void setUp() {
        deleteTestFolder();
    }

    @Test
    public void executeRelativePath() {
        InitCommand command = new InitCommand(new String[]{XatkitCommandType.INIT.label, TEST_FOLDER_LOCATION},
                new CommandContext());
        command.execute();
        File f = new File(TEST_FOLDER_LOCATION);
        checkCreatedFile(f);
    }

    @Test
    public void executeRelativePathEndingSeparator() {
        InitCommand command = new InitCommand(new String[]{XatkitCommandType.INIT.label, TEST_FOLDER_LOCATION + "/"},
                new CommandContext());
        command.execute();
        File f = new File(TEST_FOLDER_LOCATION);
        checkCreatedFile(f);
    }

    @Test
    public void executeAbsolutePath() {
        File f = new File(TEST_FOLDER_LOCATION);
        InitCommand command = new InitCommand(new String[]{XatkitCommandType.INIT.label, f.getAbsolutePath()},
                new CommandContext());
        command.execute();
        checkCreatedFile(f);
    }

    private void checkCreatedFile(File f) {
        assertThat(f).as("The file exists").exists();
        assertThat(f).as("The file is a directory").isDirectory();
    }
}
