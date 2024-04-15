package org.fever.filecreator;

import java.util.Collection;

public interface FileCreator {
    static Integer countImplementations(FQNItem initArgument, Collection<FQNItem> initArguments) {
        return (int)(initArguments.stream()
                .filter(item -> item.parentClass != null && item.parentClass.equals(initArgument.parentClass))
                .count());
    }
}
