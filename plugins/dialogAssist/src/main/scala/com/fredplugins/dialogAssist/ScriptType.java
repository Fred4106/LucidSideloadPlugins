package com.fredplugins.dialogAssist;

import java.util.Optional;

public enum ScriptType {
    CLIENT("clientscript"),
    PROCEDURAL("proc");

    private final String frag;

    private ScriptType(String frag) {
        this.frag = frag;
    }

    public String getFrag() {
        return this.frag;
    }


    public static Optional<ScriptType> fromString(String s) {
        ScriptType toRet = null;
        for (ScriptType v : values()) {
            if (toRet != null)
                continue;
            if (v.getFrag().equals(s)) {
                toRet = v;
            }
        }
        return Optional.ofNullable(toRet);
    }
}
