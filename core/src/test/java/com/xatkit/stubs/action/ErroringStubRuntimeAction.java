package com.xatkit.stubs.action;

import com.xatkit.core.platform.RuntimePlatform;
import com.xatkit.core.platform.action.RuntimeAction;
import com.xatkit.core.session.XatkitSession;

public class ErroringStubRuntimeAction extends RuntimeAction {

    private boolean actionProcessed;

    public ErroringStubRuntimeAction(RuntimePlatform runtimePlatform) {
        super(runtimePlatform, new XatkitSession("id"));
    }

    public boolean isActionProcessed() {
        return actionProcessed;
    }

    @Override
    public Object compute() {
        this.actionProcessed = true;
        throw new RuntimeException("Error when running the action");
    }
}
