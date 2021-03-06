/*
 * Copyright (c) 2008-2012, Matthias Mann
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
package de.matthiasmann.twlthemeeditor.gui.editors;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.DialogLayout.Group;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.IntegerModel;
import de.matthiasmann.twl.model.Property;
import de.matthiasmann.twlthemeeditor.gui.PropertyEditorFactory;
import de.matthiasmann.twlthemeeditor.gui.SpecialPropertyEditorFactory;

/**
 *
 * @author Matthias Mann
 */
public class IntegerEditorFactory implements
        PropertyEditorFactory<Integer>,
        SpecialPropertyEditorFactory<Integer>
{
    public Widget create(Property<Integer> property, ExternalFetaures ef) {
        ValueAdjusterInt va = new ValueAdjusterInt(asIntegerModel(property));
        ef.disableOnNotPresent(va);
        return va;
    }

    public boolean createSpecial(Group horz, Group vert, Property<Integer> property) {
        if(property.isReadOnly()) {
            final IntegerModel integerModel = asIntegerModel(property);
            final Label valueLabel = new Label();
            final Runnable callback = new Runnable() {
                public void run() {
                    valueLabel.setText(Integer.toString(integerModel.getValue()));
                }
            };
            integerModel.addCallback(callback);
            valueLabel.setTheme("value");
            callback.run();

            Label nameLabel = new Label(property.getName());
            nameLabel.setLabelFor(valueLabel);

            DialogLayout layout = new DialogLayout();
            layout.setTheme("readOnlyInteger");
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addWidget(nameLabel)
                    .addGap()
                    .addWidget(valueLabel));
            layout.setVerticalGroup(layout.createParallelGroup()
                    .addWidget(nameLabel)
                    .addWidget(valueLabel));

            horz.addWidget(layout);
            vert.addWidget(layout);
            return true;
        }
        return false;
    }

    private static IntegerModel asIntegerModel(Property<Integer> property) {
        return (property instanceof IntegerModel)
                ? (IntegerModel)property
                : new PropertyIntegerModel(property);
    }
    
    static class PropertyIntegerModel implements IntegerModel {
        final Property<Integer> property;
        public PropertyIntegerModel(Property<Integer> property) {
            this.property = property;
        }
        public void addCallback(Runnable callback) {
            property.addValueChangedCallback(callback);
        }
        public void removeCallback(Runnable callback) {
            property.removeValueChangedCallback(callback);
        }
        public int getValue() {
            return property.getPropertyValue();
        }
        public void setValue(int value) {
            property.setPropertyValue(value);
        }
        public int getMaxValue() {
            return Short.MAX_VALUE;
        }
        public int getMinValue() {
            return Short.MIN_VALUE;
        }
    }
}
