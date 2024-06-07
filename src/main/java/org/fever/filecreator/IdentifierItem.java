package org.fever.filecreator;

import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyParameter;

import java.util.Objects;

public class IdentifierItem {
    public String identifier;
    public PyClass parentClass;
    public PyParameter parameter;

    public IdentifierItem(String identifier, PyClass parentClass, PyParameter parameter) {
        this.identifier = identifier;
        this.parentClass = parentClass;
        this.parameter = parameter;
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
        return Objects.equals(parameter, identifierItem.parameter);
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (parentClass != null ? parentClass.hashCode() : 0);
        result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
        return result;
    }

    public PyParameter getParameter() {
        return parameter;
    }
}
