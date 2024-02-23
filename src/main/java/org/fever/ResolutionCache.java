package org.fever;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Service
@State(
        name = "ReferenceCache",
        storages = {@Storage(StoragePathMacros.CACHE_FILE)}
)
final class ResolutionCache implements PersistentStateComponent<ResolutionCache.State> {
    static class State {
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
    }

    private State myState = new State();

    public State getState() {
        return myState;
    }

    public void loadState(@NotNull State state) {
        myState = state;
    }
}
