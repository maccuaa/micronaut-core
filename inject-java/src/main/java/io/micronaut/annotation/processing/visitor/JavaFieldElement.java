/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.annotation.processing.visitor;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.ast.FieldElement;
import io.micronaut.inject.ast.MemberElement;
import io.micronaut.inject.ast.annotation.ElementAnnotationMetadataFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.Map;

/**
 * A field element returning data from a {@link VariableElement}.
 *
 * @author James Kleeh
 * @since 1.0
 */
@Internal
class JavaFieldElement extends AbstractJavaElement implements FieldElement {

    private final VariableElement variableElement;
    private JavaClassElement owningType;
    private ClassElement typeElement;
    private ClassElement genericType;
    private ClassElement resolvedDeclaringClass;

    /**
     * @param variableElement           The {@link VariableElement}
     * @param annotationMetadataFactory The annotation metadata factory
     * @param visitorContext            The visitor context
     */
    JavaFieldElement(VariableElement variableElement,
                     ElementAnnotationMetadataFactory annotationMetadataFactory,
                     JavaVisitorContext visitorContext) {
        super(variableElement, annotationMetadataFactory, visitorContext);
        this.variableElement = variableElement;
    }

    /**
     * @param owningType                The declaring element
     * @param variableElement           The {@link VariableElement}
     * @param annotationMetadataFactory The annotation metadata factory
     * @param visitorContext            The visitor context
     */
    JavaFieldElement(JavaClassElement owningType,
                     VariableElement variableElement,
                     ElementAnnotationMetadataFactory annotationMetadataFactory,
                     JavaVisitorContext visitorContext) {
        this(variableElement, annotationMetadataFactory, visitorContext);
        this.owningType = owningType;
    }

    @Override
    protected AbstractJavaElement copyThis() {
        return new JavaFieldElement(owningType, variableElement, elementAnnotationMetadataFactory, visitorContext);
    }

    @Override
    public FieldElement withAnnotationMetadata(AnnotationMetadata annotationMetadata) {
        return (FieldElement) super.withAnnotationMetadata(annotationMetadata);
    }

    @Override
    public ClassElement getGenericType() {
        if (this.genericType == null) {
            if (owningType == null) {
                this.genericType = getType();
            } else {
                ClassElement declaringType = getDeclaringType();
                this.genericType = newClassElement(variableElement.asType(), declaringType.getTypeArguments());
            }
        }
        return this.genericType;
    }

    @Override
    public boolean isPrimitive() {
        return getType().isPrimitive();
    }

    @Override
    public boolean isArray() {
        return getType().isArray();
    }

    @Override
    public int getArrayDimensions() {
        return getType().getArrayDimensions();
    }

    @NonNull
    @Override
    public ClassElement getType() {
        if (this.typeElement == null) {
            this.typeElement = newClassElement(variableElement.asType(), Collections.emptyMap());
        }
        return this.typeElement;
    }

    @Override
    public ClassElement getDeclaringType() {
        if (resolvedDeclaringClass == null) {
            Element enclosingElement = variableElement.getEnclosingElement();
            if (enclosingElement instanceof TypeElement te) {
                String typeName = te.getQualifiedName().toString();
                if (owningType.getName().equals(typeName)) {
                    resolvedDeclaringClass = owningType;
                } else {
                    TypeMirror returnType = te.asType();
                    Map<String, ClassElement> genericsInfo = owningType.getTypeArguments(typeName);
                    resolvedDeclaringClass = newClassElement(returnType, genericsInfo);
                }
            } else {
                return owningType;
            }
        }
        return resolvedDeclaringClass;
    }

    @Override
    public boolean hides(MemberElement hidden) {
        if (isStatic() && getDeclaringType().isInterface()) {
            return false;
        }
        return visitorContext.getElements().hides((Element) getNativeType(), (Element) hidden.getNativeType());
    }

    @Override
    public ClassElement getOwningType() {
        return owningType;
    }
}
