package com.xatkit.shell.command;

import com.xatkit.shell.CommandContext;
import com.xatkit.shell.XatkitCommandType;
import org.junit.Test;

public class HelpCommandTest {

    @Test
    public void execute() {
        /*
         * This test checks that there is no exception thrown when executing the HelpCommand.
         */
        HelpCommand helpCommand = new HelpCommand(new String[]{XatkitCommandType.HELP.label}, new CommandContext());
        helpCommand.execute();
    }
}
