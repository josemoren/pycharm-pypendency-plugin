package org.fever;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class YamlCompletionContributor extends CompletionContributor {
    private List<String> completions;

    public YamlCompletionContributor() {
    }

    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        String currentProjectBasePath = context.getProject().getBasePath();
        if (currentProjectBasePath == null) {
            Notifications.Bus.notify(new Notification(
                    "pypendency",
                    "Pypendency",
                    "Current project path not available.",
                    NotificationType.WARNING
            ));
            return;
        }

        this.completions = this.generateCompletion(currentProjectBasePath);
    }

    private List<String> generateCompletion(String currentProjectBasePath) {
        List<String> output = new ArrayList<>();
        try {
            String[] cmd = new String[]{
                    "/bin/sh", "-c", "~/.pypendency/list_container_keys.py " + currentProjectBasePath
            };
            Process process = Runtime.getRuntime().exec(cmd);

            if (notifyErrors(process)) return output;

            String s;
            BufferedReader stdInput = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            while ((s = stdInput.readLine()) != null) {
                output.add(s);
            }

            process.waitFor();
        } catch (Exception e) {
            Notifications.Bus.notify(new Notification(
                    "pypendency",
                    "Pypendency",
                    e.toString(),
                    NotificationType.WARNING
            ));
        }

        return output;
    }

    private boolean notifyErrors(Process process) throws IOException {
        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(process.getErrorStream()));

        String s;
        StringBuilder error = new StringBuilder();
        while ((s = stdError.readLine()) != null) {
            error.append(s);
        }
        if (!error.toString().equals("")) {
            Notifications.Bus.notify(new Notification(
                    "pypendency",
                    "Pypendency",
                    error.toString(),
                    NotificationType.WARNING
            ));
            return true;
        }
        return false;
    }

    public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull CompletionResultSet result) {
        for (String c : this.completions) {
            result.addElement(LookupElementBuilder.create(c));
        }

    }
}