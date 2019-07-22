package com.xatkit.shell;

import java.util.HashMap;

/**
 * Stores command values and allows their access.
 * <p>
 * This class is used, for example, by {@link com.xatkit.shell.command.StopCommand} to retrieve the
 * {@link com.xatkit.core.XatkitCore} instance started by a previous {@link com.xatkit.shell.command.RunCommand}.
 */
public class CommandContext extends HashMap<String, Object> {

}
