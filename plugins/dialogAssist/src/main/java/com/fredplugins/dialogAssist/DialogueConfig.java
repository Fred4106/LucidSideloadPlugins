package com.fredplugins.dialogAssist;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DialogueConfig {
    @Getter
    private final int targetId;

    @Override
    public String toString() {
        String opsString = optionMap.entrySet().stream().map(x -> {
            return "\"" + x.getKey() + "\" = " + x.getValue() + "";
        }).collect(Collectors.joining(",", "{", "}"));
        return "DialogueConfig{" +
            "targetId=" + targetId +
            ", optionMap=" + opsString +
            '}';
    }

    private Map<String, OptionStatus> optionMap = new HashMap<>();

    public DialogueConfig(int id)
    {
        this.targetId = id;
    }

    public boolean isHighlighted(String option)
    {
        return optionMap.containsKey(option) && optionMap.get(option) == OptionStatus.HIGHLIGHTED;
    }

    public boolean isLocked(String option)
    {
        return optionMap.containsKey(option) && optionMap.get(option) == OptionStatus.LOCKED;
    }

    public void setHighlighted(String option)
    {
        optionMap.put(option, OptionStatus.HIGHLIGHTED);
    }

    public void setLocked(String option)
    {
        optionMap.put(option, OptionStatus.LOCKED);
    }

    public void resetOption(String option)
    {
        optionMap.remove(option);
    }

}
