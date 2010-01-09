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

import de.matthiasmann.twl.model.TreeTableNode;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twlthemeeditor.TestEnv;
import de.matthiasmann.twlthemeeditor.VirtualFile;
import de.matthiasmann.twlthemeeditor.XMLWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;

/**
 *
 * @author Matthias Mann
 */
public class ThemeFile implements VirtualFile {

    private final TestEnv env;
    private final URL url;
    private final Document document;

    private ModifyableTreeTableNode node;
    private Runnable[] callbacks;

    public ThemeFile(TestEnv env, URL url) throws IOException {
        this.env = env;
        this.url = url;
        
        try {
            SAXBuilder saxb = new SAXBuilder(false);
            saxb.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    return new InputSource(new StringReader(""));
                }
            });
            document = saxb.build(url);
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

    public TestEnv getEnv() {
        return env;
    }

    public URL getURL(String file) throws MalformedURLException {
        return new URL(url, file);
    }

    protected void addChildren(ModifyableTreeTableNode parent) throws IOException {
        this.node = parent;
        Utils.addChildren(this, parent, document.getRootElement(), new DomWrapper() {
            public TreeTableNode wrap(ThemeFile themeFile, ModifyableTreeTableNode parent, Element element) throws IOException {
                String tagName = element.getName();

                if("textures".equals(tagName)) {
                    return new Textures(parent, element, ThemeFile.this);
                }
                if("include".equals(tagName)) {
                    return new Include(parent, element, ThemeFile.this);
                }
                return null;
            }
        });
    }

    public void registerAs(String filename) {
        env.registerFile(filename, this);
    }

    void fireCallbacks() {
        CallbackSupport.fireCallbacks(callbacks);
    }

    @SuppressWarnings("unchecked")
    public Object getContent(Class<?> type) throws IOException {
        if(type == XmlPullParser.class) {
            DomXPPParser xpp = new DomXPPParser();
            Element rootElement = document.getRootElement();
            Utils.addToXPP(xpp, rootElement.getName(), node, rootElement.getAttributes());
            return xpp;
        }
        return null;
    }

    public InputStream openStream() throws IOException {
        throw new IOException("Call getContent().");
    }

}
