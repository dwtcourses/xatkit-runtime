package com.xatkit.shell.command;

import com.xatkit.Xatkit;
import com.xatkit.core.XatkitCore;
import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitCommandType;
import com.xatkit.shell.XatkitShellException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class RunCommandTest {

    public static String TEST_PROPERTIES_FILE = InitCommandTest.TEST_FOLDER_LOCATION + "/deploy-react.properties";

    @AfterClass
    public static void tearDownAfterClass() {
        shutdownXatkitCore();
        InitCommandTest.tearDownAfterClass();
    }

    public static void shutdownXatkitCore() {
        XatkitCore xatkitCore = Xatkit.getXatkitCore();
        if (nonNull(xatkitCore)) {
            if(!xatkitCore.isShutdown()) {
                xatkitCore.shutdown();
            }
        }
    }

    private CommandContext context;

    @Before
    public void setUp() {
        /*
         * Shutdown the XatkitCore instance to avoid lock issues in the underlying databases.
         */
        shutdownXatkitCore();
        InitCommandTest.deleteTestFolder();
        /*
         * Do not check it, it is already tested in InitCommandTest
         */
        InitCommand command = new InitCommand(new String[]{XatkitCommandType.INIT.label,
                InitCommandTest.TEST_FOLDER_LOCATION}, new CommandContext());
        command.execute();
        context = new CommandContext();
    }

    @Test
    public void executeRelativePath() {
        RunCommand command = new RunCommand(new String[]{XatkitCommandType.RUN.label, TEST_PROPERTIES_FILE}, context);
        command.execute();
        checkContext();
    }

    @Test
    public void executeAbsolutePath() {
        File f = new File(TEST_PROPERTIES_FILE);
        RunCommand command = new RunCommand(new String[]{XatkitCommandType.RUN.label, f.getAbsolutePath()}, context);
        command.execute();
        checkContext();
    }

    @Test
    public void executeDirectoryPathWithSinglePropertiesFile() {
        RunCommand command = new RunCommand(new String[]{XatkitCommandType.RUN.label,
                InitCommandTest.TEST_FOLDER_LOCATION}, context);
        command.execute();
        checkContext();
    }

    /*
     * Cannot test executeDirectoryPathWithMultiplePropertiesFile because it waits for input in the console.
     */

    @Test(expected = XatkitShellException.class)
    public void executeDirectoryPathWithNoPropertiesFile() {
        File directory = new File(InitCommandTest.TEST_FOLDER_LOCATION);
        for(File f : directory.listFiles()) {
            f.delete();
        }
        RunCommand command = new RunCommand(new String[]{XatkitCommandType.RUN.label,
                InitCommandTest.TEST_FOLDER_LOCATION}, context);
        command.execute();
    }

    @Test(expected = XatkitShellException.class)
    public void executeInvalidPath() {
        RunCommand command = new RunCommand(new String[]{XatkitCommandType.RUN.label,
                InitCommandTest.TEST_FOLDER_LOCATION + "/test"}, context);
        command.execute();
    }

    private void checkContext() {
        assertThat(context.get(RunCommand.RUNNING_INSTANCE)).as("There is a running instance").isNotNull();
    }

}
