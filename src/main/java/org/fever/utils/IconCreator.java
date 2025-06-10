package org.fever.utils;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class IconCreator {
    public static Icon create(String iconPath) {
        return IconLoader.getIcon(iconPath, IconCreator.class);
    }
}
