/*
 * Copyright (c) 2008-2010, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.matthiasmann.twlthemeeditor.properties;

import de.matthiasmann.twl.model.AbstractProperty;
import de.matthiasmann.twl.model.StringModel;
import de.matthiasmann.twlthemeeditor.datamodel.Utils;
import org.jdom.Element;

/**
 *
 * @author Matthias Mann
 */
public class AttributeProperty extends AbstractProperty<String> implements StringModel{

    private final Element element;
    private final String attribute;
    private final String name;
    private final boolean canBeNull;

    public AttributeProperty(Element element, String attribute) {
        this.element = element;
        this.attribute = attribute;
        this.name = Utils.capitalize(attribute);
        this.canBeNull = false;
    }

    public AttributeProperty(Element element, String attribute, String name, boolean canBeNull) {
        this.element = element;
        this.attribute = attribute;
        this.name = name;
        this.canBeNull = canBeNull;
    }

    public String getName() {
        return name;
    }

    public boolean isReadOnly() {
        return false;
    }

    public boolean canBeNull() {
        return canBeNull;
    }

    public Class<String> getType() {
        return String.class;
    }

    public String getValue() {
        return element.getAttributeValue(attribute);
    }

    public void setValue(String value) throws IllegalArgumentException {
        if(!canBeNull && value == null) {
            throw new NullPointerException("value");
        }
        String curValue = element.getAttributeValue(attribute);
        if(!Utils.equals(curValue, value)) {
            if(value == null) {
                element.removeAttribute(attribute);
            } else {
                element.setAttribute(attribute, value);
            }
            fireValueChangedCallback();
        }
    }

    public String getPropertyValue() {
        return getValue();
    }

    public void setPropertyValue(String value) throws IllegalArgumentException {
        setValue(value);
    }

    public void addCallback(Runnable callback) {
        addValueChangedCallback(callback);
    }

    public void removeCallback(Runnable callback) {
        removeValueChangedCallback(callback);
    }
}
