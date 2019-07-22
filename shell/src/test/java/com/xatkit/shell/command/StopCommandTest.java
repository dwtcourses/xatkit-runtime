package com.xatkit.shell.command;

import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitCommandType;
import com.xatkit.shell.XatkitShellException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StopCommandTest {

    @AfterClass
    public static void tearDownAfterClass() {
        RunCommandTest.tearDownAfterClass();
    }

    private CommandContext context;

    @Before
    public void setUp() {
        RunCommandTest.shutdownXatkitCore();
        InitCommandTest.deleteTestFolder();
        context = new CommandContext();
        InitCommand initCommand = new InitCommand(new String[]{XatkitCommandType.INIT.label,
                InitCommandTest.TEST_FOLDER_LOCATION}, context);
        initCommand.execute();
        RunCommand runCommand = new RunCommand(new String[]{XatkitCommandType.RUN.label,
                RunCommandTest.TEST_PROPERTIES_FILE}, context);
        runCommand.execute();
    }

    @Test(expected = XatkitShellException.class)
    public void executeEmptyContext() {
        StopCommand command = new StopCommand(new String[]{XatkitCommandType.STOP.label}, new CommandContext());
        command.execute();
    }

    @Test
    public void executeRunningInstanceInContext() {
        StopCommand command = new StopCommand(new String[]{XatkitCommandType.STOP.label}, context);
        command.execute();
        assertThat(context).as("Context doesn't contain the running instance").doesNotContainKey(RunCommand.RUNNING_INSTANCE);
    }
}
