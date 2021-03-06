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
package de.matthiasmann.twlthemeeditor.datamodel.images;

import de.matthiasmann.twlthemeeditor.datamodel.Images;
import de.matthiasmann.twlthemeeditor.datamodel.ThemeTreeNode;
import de.matthiasmann.twlthemeeditor.dom.Element;
import de.matthiasmann.twlthemeeditor.properties.AttributeProperty;
import de.matthiasmann.twlthemeeditor.properties.ColorProperty;
import de.matthiasmann.twlthemeeditor.properties.FloatProperty;
import de.matthiasmann.twlthemeeditor.properties.IntegerProperty;

/**
 *
 * @author Matthias Mann
 */
public class Frame extends Alias {

    Frame(Images textures, ThemeTreeNode parent, Element node) {
        super(textures, parent, node);
        addProperty(new IntegerProperty(new AttributeProperty(element, "duration"), 0, Short.MAX_VALUE));
        addProperty(new ColorProperty(new AttributeProperty(element, "tint", "Tint color", true), parent));
        addProperty(new FloatProperty(new AttributeProperty(element, "zoom", "Zoom X/Y", true), 0f, 4f, 1f));
        addProperty(new FloatProperty(new AttributeProperty(element, "zoomX", "Zoom X", true), 0f, 4f, 1f));
        addProperty(new FloatProperty(new AttributeProperty(element, "zoomY", "Zoom Y", true), 0f, 4f, 1f));
        addProperty(new FloatProperty(new AttributeProperty(element, "zoomCenterX", "Zoom center X", true), -1f, 2f, 0.5f));
        addProperty(new FloatProperty(new AttributeProperty(element, "zoomCenterY", "Zoom center Y", true), -1f, 2f, 0.5f));
    }
}
