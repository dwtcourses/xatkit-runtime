package com.xatkit.shell;

import com.xatkit.shell.command.InitCommand;

import static java.util.Objects.nonNull;

public class XatkitShell {

    public static void main(String[] args) {
        if(nonNull(args)) {
            switch (args[0]) {
                case "init":
                    new InitCommand(args);

            }
        } else {
            // help command
        }
    }
}
