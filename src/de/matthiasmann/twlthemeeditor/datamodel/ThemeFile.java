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
package de.matthiasmann.twlthemeeditor.datamodel;

import de.matthiasmann.twl.model.AbstractTreeTableModel;
import de.matthiasmann.twl.model.TreeTableNode;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twlthemeeditor.TestEnv;
import de.matthiasmann.twlthemeeditor.VirtualFile;
import de.matthiasmann.twlthemeeditor.XMLWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Matthias Mann
 */
public class ThemeFile extends AbstractTreeTableModel {

    private final Document document;

    private Runnable[] callbacks;

    public ThemeFile(TestEnv env, URL url) throws IOException {
        try {
            SAXBuilder saxb = new SAXBuilder(false);
            saxb.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return new InputSource(new StringReader(""));
                }
            });
            document = saxb.build(url);
            findTextures(env, url);
        } catch(IOException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new IOException(ex);
        }
    }

    public void writeTo(OutputStream out) throws IOException {
        Writer w = new XMLWriter(new OutputStreamWriter(out, "UTF8"));
        new XMLOutputter().output(document, w);
        w.flush();
    }

    public void addCallback(Runnable cb) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, cb, Runnable.class);
    }

    public void removeCallbacks(Runnable cb) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
    }

    public List<Textures> getTextures() {
        ArrayList<Textures> result = new ArrayList<Textures>();
        for(int i=0,n=getNumChildren() ; i<n ; i++) {
            TreeTableNode child = getChild(i);
            if(child instanceof Textures) {
                result.add((Textures)child);
            }
        }
        return result;
    }

    private static final String COLUMN_HEADER[] = {"Name", "Type"};

    public String getColumnHeaderText(int column) {
        return COLUMN_HEADER[column];
    }

    public int getNumColumns() {
        return COLUMN_HEADER.length;
    }

    private void findTextures(TestEnv env, URL baseUrl) throws IOException {
        for(Object node : getRoot().getChildren()) {
            if(node instanceof Element) {
                Element element = (Element)node;
                String tagName = element.getName();
                NodeWrapper entry = null;
                
                if("textures".equals(tagName)) {
                    entry = new Textures(this, element, baseUrl, env);
                } else if("include".equals(tagName)) {
                    entry = new Include(this, element, baseUrl, env);
                }
                
                if(entry != null) {
                    insertChild(entry, getNumChildren());
                }
            }
        }
    }

    public VirtualFile createVirtualFile() {
        return new VirtualXMLFile(document);
    }
    
    private Element getRoot() {
        return document.getRootElement();
    }

    void fireCallbacks() {
        CallbackSupport.fireCallbacks(callbacks);
    }

    @Override
    protected void fireNodesAdded(TreeTableNode parent, int idx, int count) {
        super.fireNodesAdded(parent, idx, count);
    }

    @Override
    protected void fireNodesChanged(TreeTableNode parent, int idx, int count) {
        super.fireNodesChanged(parent, idx, count);
    }

    @Override
    protected void fireNodesRemoved(TreeTableNode parent, int idx, int count) {
        super.fireNodesRemoved(parent, idx, count);
    }

}
