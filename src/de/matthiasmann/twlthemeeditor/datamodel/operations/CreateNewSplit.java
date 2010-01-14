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
package de.matthiasmann.twlthemeeditor.datamodel.operations;

import de.matthiasmann.twlthemeeditor.datamodel.ThemeTreeNode;
import java.io.IOException;
import org.jdom.Element;

/**
 *
 * @author Matthias Mann
 */
public class CreateNewSplit extends CreateChildOperation {

    private final String tagName;
    private final boolean splitx;
    private final boolean splity;

    public CreateNewSplit(ThemeTreeNode parent, String tagName, boolean splitx, boolean splity) {
        super("opNewNode" + Character.toUpperCase(tagName.charAt(0)) + tagName.substring(1), parent);
        this.tagName = tagName;
        this.splitx = splitx;
        this.splity = splity;
    }

    @Override
    public void execute() throws IOException {
        Element e = new Element(tagName);
        e.setAttribute("name", "new" + System.nanoTime());
        e.setAttribute("x", "0");
        e.setAttribute("y", "0");
        e.setAttribute("width", "1");
        e.setAttribute("height", "1");
        if(splitx) {
            e.setAttribute("splitx", "0,0");
        }
        if(splity) {
            e.setAttribute("splity", "0,0");
        }
        addChild(e);
    }

}