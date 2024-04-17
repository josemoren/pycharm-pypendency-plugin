package org.fever.filecreator;

import java.util.Collection;

public interface FileCreator {
    static Integer countImplementations(IdentifierItem initArgument, Collection<IdentifierItem> initArguments) {
        return (int)(initArguments.stream()
                .filter(item -> item.parentClass != null && item.parentClass.equals(initArgument.parentClass))
                .count());
    }
}
