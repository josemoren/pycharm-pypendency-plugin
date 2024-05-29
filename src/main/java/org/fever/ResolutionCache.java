package org.fever;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@State(
        name = "ReferenceCache",
        storages = {@Storage(StoragePathMacros.CACHE_FILE)}
)
public final class ResolutionCache implements PersistentStateComponent<ResolutionCache.State> {
    public static class State {
        public Map<String, Map<String, String>> resolutionCache;

        public State() {
            resolutionCache = new HashMap<>();
        }

        private Map<String, String> getResolutionCacheForProject(String projectName) {
            return resolutionCache.computeIfAbsent(projectName, k -> new HashMap<>());
        }

        public String getCachedResolution(String projectName, String identifier) {
            return this.getResolutionCacheForProject(projectName).get(identifier);
        }

        public void setCachedResolution(String projectName, String identifier, String filePath) {
            this.getResolutionCacheForProject(projectName).put(identifier, filePath);
        }

        public void removeCachedResolution(String projectName, String identifier) {
            this.getResolutionCacheForProject(projectName).remove(identifier);
        }

        // This returns a Collection<String> because there may be multiple identifiers for the same className in the same project.
        public Collection<String> getCachedIdentifiersByClassName(String projectName, String className) {
            return this.getResolutionCacheForProject(projectName).keySet().stream()
                    .filter(identifier -> identifier != null && identifier.endsWith("." + className))
                    .collect(Collectors.toList());
        }

        public int countIdentifiers(String projectName) {
            return this.getResolutionCacheForProject(projectName).keySet().size();
        }

        public List<String> fuzzyFindIdentifiersMatching(String projectName, String identifier) {
            return this.getResolutionCacheForProject(projectName).keySet().stream()
                    .filter(key -> key.toLowerCase().contains(identifier.toLowerCase()))
                    .toList();
        }
    }

    private State myState = new State();

    public State getState() {
        return myState;
    }

    public void loadState(@NotNull State state) {
        myState = state;
    }

    public static State getInstance() {
        return ApplicationManager.getApplication().getService(ResolutionCache.class).getState();
    }
}
