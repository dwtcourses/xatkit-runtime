package com.xatkit.stubs.action;

import com.xatkit.core.platform.RuntimePlatform;
import com.xatkit.core.session.XatkitSession;

import java.io.IOException;

public class StubRuntimeMessageActionIOException extends StubRuntimeMessageAction {

    public StubRuntimeMessageActionIOException(RuntimePlatform runtimePlatform, XatkitSession session, String
            rawMessage) {
        super(runtimePlatform, session, rawMessage);
    }

    @Override
    protected Object compute() throws IOException {
        attempts++;
        throw new IOException("StubRuntimeMessageActionIOException");
    }
}
