package org.fever.filecreator;

import com.jetbrains.python.psi.PyClass;

import java.util.Objects;

public class FQNItem {
    public String fqn;
    public PyClass parentClass;
    public String message;

    public FQNItem(String fqn, PyClass parentClass) {
        this.fqn = fqn;
        this.parentClass = parentClass;
    }

    public FQNItem(String fqn, PyClass parentClass, String message) {
        this.fqn = fqn;
        this.parentClass = parentClass;
        this.message = message;
    }

    public Boolean isNotAClass() {
        return (this.fqn == null && this.parentClass == null);
    }

    public Boolean hasNoImplementations() {
        return (this.fqn == null && this.parentClass != null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FQNItem fqnItem = (FQNItem) o;

        if (!Objects.equals(fqn, fqnItem.fqn)) return false;
        if (!Objects.equals(parentClass, fqnItem.parentClass)) return false;
        return Objects.equals(message, fqnItem.message);
    }

    @Override
    public int hashCode() {
        int result = fqn != null ? fqn.hashCode() : 0;
        result = 31 * result + (parentClass != null ? parentClass.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
