package com.fred4106.improvedCharges.item.triggers;

import com.fred4106.improvedCharges.store.utils.DynamicReplaceTarget;
import com.fred4106.improvedCharges.store.utils.ReplaceTarget;
import com.fred4106.improvedCharges.store.utils.*;

import java.util.*;
import java.util.concurrent.*;

public class OnMenuEntryAdded extends TriggerBase {
    public Optional<String> menuEntryOption;
    public Optional<Boolean> hide = Optional.empty();
    public Optional<String> replaceOption = Optional.empty();
    public Optional<Callable<String>> replaceOptionConsumer = Optional.empty();

    public OnMenuEntryAdded() {
        this.menuEntryOption = Optional.empty();
    }

    public OnMenuEntryAdded(String option) {
        this.menuEntryOption = Optional.of(option);
    }

    public OnMenuEntryAdded hide() {
        this.hide = Optional.of(true);
        return this;
    }

    public OnMenuEntryAdded replaceOption(String option) {
        this.replaceOption = Optional.of(option);
        return this;
    }

    public OnMenuEntryAdded replaceOptionConsumer(Callable<String> consumer) {
        this.replaceOptionConsumer = Optional.of(consumer);
        return this;
    }
}
