package com.xatkit.stubs.io;

import com.xatkit.core.platform.io.RuntimeEventProvider;
import com.xatkit.stubs.StubRuntimePlatform;
import org.apache.commons.configuration2.Configuration;

public class StubInputProvider extends RuntimeEventProvider<StubRuntimePlatform> {

    public StubInputProvider(StubRuntimePlatform runtimePlatform) {
        super(runtimePlatform);
    }

    public StubInputProvider(StubRuntimePlatform runtimePlatform, Configuration configuration) {
        this(runtimePlatform);
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
    }

}