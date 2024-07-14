package com.fredplugins.common;

public class Utils {
    public static String PLUGIN_NAME_PREFIX = "<html><font color=\"#32C8CD\">Freds</font> ";
    public static String PLUGIN_NAME_POSTFIX = "</html>";
    public static String createPluginName(String name) {
        return PLUGIN_NAME_PREFIX + name + PLUGIN_NAME_POSTFIX;
    }
}
