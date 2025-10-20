package org.fever.utils;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class Icons {
    public static final Icon CREATE_DI_ICON = IconCreator.create("icons/createDI.svg");
    public static final Icon GO_TO_DI_ICON = IconCreator.create("icons/goToDI.svg");
    public static final Icon GO_TO_SOURCE_ICON = IconCreator.create("icons/goToSource.svg");

    private static class IconCreator {
        private static Icon create(String iconPath) {
            return IconLoader.getIcon(iconPath, IconCreator.class);
        }
    }
}
