package com.xatkit.shell;

/**
 * List the {@link com.xatkit.shell.command.XatkitCommand} types supported by the shell.
 */
public enum XatkitCommandType {

    INIT("init"),
    RUN("run"),
    STOP("stop"),
    HELP("help"),
    EXIT("exit");

    /**
     * The {@link XatkitCommandType} String label.
     */
    public final String label;

    /**
     * Constructs a new {@link XatkitCommandType} with the provided {@code label}.
     *
     * @param label the {@link XatkitCommandType}'s label
     */
    XatkitCommandType(String label) {
        this.label = label;
    }
}
