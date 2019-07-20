package com.xatkit.shell.command;

import com.xatkit.shell.XatkitCommand;
import fr.inria.atlanmod.commons.log.Log;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class InitCommandTest {

    private static String TEST_FOLDER_LOCATION = "initCommandTest";

    @AfterClass
    public static void tearDownAfterClass() {
        deleteTestFolder();
    }

    private static void deleteTestFolder() {
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
    public void initRelativePath() {
        new InitCommand(new String[]{XatkitCommand.INIT.label, TEST_FOLDER_LOCATION});
        File f = new File(TEST_FOLDER_LOCATION);
        checkCreatedFile(f);
    }

    @Test
    public void initRelativePathEndingSeparator() {
        new InitCommand(new String[]{XatkitCommand.INIT.label, TEST_FOLDER_LOCATION + "/"});
        File f = new File(TEST_FOLDER_LOCATION);
        checkCreatedFile(f);
    }

    @Test
    public void initAbsolutePath() {
        File f = new File(TEST_FOLDER_LOCATION);
        new InitCommand(new String[]{XatkitCommand.INIT.label, f.getAbsolutePath()});
        checkCreatedFile(f);
    }

    private void checkCreatedFile(File f) {
        assertThat(f).as("The file exists").exists();
        assertThat(f).as("The file is a directory").isDirectory();
    }
}
