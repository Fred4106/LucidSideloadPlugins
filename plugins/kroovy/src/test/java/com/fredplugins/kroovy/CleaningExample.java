package com.fredplugins.kroovy;

import java.lang.ref.*;

public class CleaningExample implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    static class State implements Runnable {
        final long ctxt;
        State(long ctxt) {
            this.ctxt = ctxt;
        }

        @Override
        public void run() {
        }
    };
    private final State state;
    private final Cleaner.Cleanable cleanable;
    public CleaningExample() {
        this.state = new State(11l);
        this.cleanable = cleaner.register(this, state);
    }
    public void close() {
        cleanable.clean();
    }
}