package org.fever.filecreator;

import com.jetbrains.python.psi.PyClass;

import java.util.Objects;

public class IdentifierItem {
    public String identifier;
    public PyClass parentClass;
    public String message;

    public IdentifierItem(String identifier, PyClass parentClass) {
        this.identifier = identifier;
        this.parentClass = parentClass;
    }

    public IdentifierItem(String identifier, PyClass parentClass, String message) {
        this.identifier = identifier;
        this.parentClass = parentClass;
        this.message = message;
    }

    public Boolean isNotAClass() {
        return (this.identifier == null && this.parentClass == null);
    }

    public Boolean hasNoImplementations() {
        return (this.identifier == null && this.parentClass != null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdentifierItem identifierItem = (IdentifierItem) o;

        if (!Objects.equals(identifier, identifierItem.identifier)) return false;
        if (!Objects.equals(parentClass, identifierItem.parentClass)) return false;
        return Objects.equals(message, identifierItem.message);
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (parentClass != null ? parentClass.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
