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
package de.matthiasmann.twlthemeeditor.fontgen;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matthias Mann
 */
public final class FontData {

    private final File fontFile;
    private final Font javaFont;
    private final float size;
    private final int upem;
    private final IntMap<IntMap<Integer>> kerning;
    private final IntMap<int[]> glyphToUnicode;
    private final BitSet defined;
    private final String postScriptName;

    public String getName() {
        return postScriptName;
    }

    public String getFamilyName() {
        return javaFont.getFamily();
    }

    public float getSize() {
        return size;
    }

    public Font getJavaFont() {
        return javaFont;
    }

    public File getFontFile() {
        return fontFile;
    }

    public IntMap<IntMap<Integer>> getRawKerning() {
        return kerning;
    }

    public int[][] getKernings(CharSet charSet) {
        ArrayList<int[]> kernings = new ArrayList<int[]>();
        for(IntMap.Entry<IntMap<Integer>> from : kerning) {
            int[] fromUnicode = glyphToUnicode.get(from.key);
            if(fromUnicode != null && charSet.isIncluded(fromUnicode)) {
                for(IntMap.Entry<Integer> to : from.value) {
                    int[] toUnicode = glyphToUnicode.get(to.key);
                    if(toUnicode != null && charSet.isIncluded(toUnicode)) {
                        int value = convertUnitToEm(to.value);
                        if(value != 0) {
                            expandKerning(kernings, fromUnicode, toUnicode, value, charSet);
                        }
                    }
                }
            }
        }
        return kernings.toArray(new int[kernings.size()][]);
    }
    
    public void expandKerning(ArrayList<int[]> kernings, int leftGlyphIndex, int rightGlyphIndex, int value, CharSet charSet) {
        int[] leftCodePoints = glyphToUnicode.get(leftGlyphIndex);
        int[] rightCodePoints = glyphToUnicode.get(rightGlyphIndex);
        if(leftCodePoints != null && rightCodePoints != null) {
            expandKerning(kernings, leftCodePoints, rightCodePoints, value, charSet);
        }
    }

    public void expandKerning(ArrayList<int[]> kernings, int[] leftCodePoints, int[] rightCodePoints, int value, CharSet charSet) {
        for(int lc : leftCodePoints) {
            if(charSet.isIncluded(lc)) {
                for(int rc : rightCodePoints) {
                    if(charSet.isIncluded(rc)) {
                        kernings.add(new int[] { lc, rc, value});
                    }
                }
            }
        }
    }

    public int getNextCodepoint(int codepoint) {
        return defined.nextSetBit(codepoint + 1);
    }

    public HashSet<Character.UnicodeBlock> getDefinedBlocks() {
        HashSet<Character.UnicodeBlock> result = new HashSet<Character.UnicodeBlock>();
        int codepoint = -1;
        while((codepoint=getNextCodepoint(codepoint)) >= 0) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(codepoint);
            if(block != null) {
                result.add(block);
            }
        }
        return result;
    }
    
    public FontData(File file, float size) throws IOException {
        this.fontFile = file;
        this.size = size;
        this.defined = new BitSet();
        this.kerning = new IntMap<IntMap<Integer>>();
        this.glyphToUnicode = new IntMap<int[]>();
        
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            try {
                byte[] dirTable = readDirTable(raf);
                byte[] headSection = readSection(raf, dirTable, "head");
                byte[] cmapSection = readSection(raf, dirTable, "cmap");
                byte[] nameSection = readSection(raf, dirTable, "name");
                byte[] kernSection = readSectionOptional(raf, dirTable, "kern");

                upem = readUPEM(headSection);
                postScriptName = readNAME(nameSection);
                readCMAP(cmapSection);

                if(kernSection != null) {
                    readKERN(kernSection);
                }
            } finally {
                raf.close();
            }

            Font font = Font.createFont(Font.TRUETYPE_FONT, file);
            
            String name = getName();
            System.err.println("Loaded: " + name);
            
            int style = 0;
            int comma = name.indexOf(',');
            if (comma >= 0) {
                name = name.substring(comma + 1);
                if (name.indexOf("Bold") >= 0) {
                    style |= Font.BOLD;
                }
                if (name.indexOf("Italic") >= 0) {
                    style |= Font.ITALIC;
                }
            }

            javaFont = font.deriveFont(style, size);
        } catch (FontFormatException e) {
            throw (IOException)(new IOException("Failed to read font").initCause(e));
        }
    }

    private FontData(FontData src, float size, int style) {
        this.fontFile = src.fontFile;
        this.size = size;
        this.javaFont = src.javaFont.deriveFont(style, size);
        this.upem = src.upem;
        this.kerning = src.kerning;
        this.defined = src.defined;
        this.postScriptName = src.postScriptName;
        this.glyphToUnicode = src.glyphToUnicode;
    }

    public FontData deriveFont(float size) {
        return deriveFont(size, javaFont.getStyle());
    }

    public FontData deriveFont(float size, int style) {
        return new FontData(this, size, style);
    }

    private int convertUnitToEm(int units) {
        return Math.round((units * size) / upem);
    }

    private void addGlyphCodePoint(int glyphIdx, int unicode) {
        int[] codepoints = glyphToUnicode.get(glyphIdx);
        if(codepoints == null) {
            codepoints = new int[] { unicode };
        } else {
            int len = codepoints.length;
            codepoints = Arrays.copyOf(codepoints, len+1);
            codepoints[len] = unicode;
        }
        glyphToUnicode.put(glyphIdx, codepoints);
        defined.set(unicode);
    }
    
    private void readCMAP(byte[] cmapSection) throws IOException {
        int numCMap = readUShort(cmapSection, 2);

        for(int i=0 ; i<numCMap ; i++) {
            int cmapPID = readUShort(cmapSection, i*8 + 4);
            int cmapEID = readUShort(cmapSection, i*8 + 6);

            if(cmapPID == 3 && cmapEID == 1) {
                readCMAP_USC2(cmapSection, readInt(cmapSection, i*8 + 8));
                return;
            }
            if(cmapPID == 3 && cmapEID == 10) {
                readCMAP_USC4(cmapSection, readInt(cmapSection, i*8 + 8));
                return;
            }
        }
        
        throw new IOException("No unicode mapping table found");
    }
    
    private void readCMAP_USC2(byte[] cmapSection, int cmapUniOffset) throws IOException {
        int cmapFormat = readUShort(cmapSection, cmapUniOffset);
        if (cmapFormat != 4) {
            throw new IOException("Unsupported unicode table format: " + cmapFormat);
        }

        int cmapSegCountX2 = readUShort(cmapSection, cmapUniOffset + 6);

        for (int segX2=0 ; segX2<cmapSegCountX2 ; segX2+=2) {
            int cmapEndCount   = readUShort(cmapSection, cmapUniOffset + 14 + segX2);
            int cmapStartCount = readUShort(cmapSection, cmapUniOffset + 16 + segX2 + cmapSegCountX2);
            int cmapDelta      = readShort (cmapSection, cmapUniOffset + 16 + segX2 + cmapSegCountX2*2);

            int cmapROO         = cmapUniOffset + 16 + segX2 + cmapSegCountX2*3;
            int cmapRangeOffset = readUShort(cmapSection, cmapROO);
            int glyphOffset     = cmapRangeOffset + cmapROO;

            if(cmapEndCount == 65535) {
                // exclude the last character 65535 = .notdef
                cmapEndCount--;
            }

            for (int unicode=cmapStartCount ; unicode<=cmapEndCount ; unicode++) {
                int glyphIdx = unicode;
                
                if (cmapRangeOffset != 0) {
                    glyphIdx = readUShort(cmapSection, glyphOffset);
                    glyphOffset += 2;
                }

                if (cmapRangeOffset == 0 || glyphIdx != 0) {
                    glyphIdx = (glyphIdx + cmapDelta) & 0xffff;
                }

                if (glyphIdx != 0) {
                    addGlyphCodePoint(glyphIdx, unicode);
                }
            }
        }
    }
    
    private void readCMAP_USC4(byte[] cmapSection, int cmapUniOffset) throws IOException {
        int cmapFormat = readUShort(cmapSection, cmapUniOffset);
        if (cmapFormat != 12) {
            throw new IOException("Unsupported unicode table format: " + cmapFormat);
        }

        int nGroups = readInt(cmapSection, cmapUniOffset + 12);

        for (int group=0 ; group<nGroups ; group++) {
            int startCharCode = readUShort(cmapSection, cmapUniOffset + 16 + group*12);
            int endCharCode   = readUShort(cmapSection, cmapUniOffset + 16 + group*12 + 4);
            int startGlyphID  = readShort (cmapSection, cmapUniOffset + 16 + group*12 + 8);

            for(int i=startCharCode ; i<=endCharCode ; i++) {
                addGlyphCodePoint(startGlyphID+(i-startCharCode), i);
            }
        }
    }

    private void readKERN(byte[] kernSection) {
        int version = readUShort(kernSection, 0);
        int nTables = readUShort(kernSection, 2);
        //System.out.println("version="+version+" nTables="+nTables);

        int tableOffset = 4;
        for(int table=0 ; table<nTables ; table++) {
            int tableLength = readInt(kernSection, tableOffset);
            int coverage = readUShort(kernSection, tableOffset + 4);
            
            if ((coverage & 3) == 1) {  // only horizontal
                int format = coverage >> 8;
                switch(format) {
                    case 0: {
                        int numPairs = readUShort(kernSection, tableOffset + 6);
                        int offset = tableOffset + 14;

                        for(int pair=0 ; pair<numPairs ; pair++,offset+=6) {
                            int from = readUShort(kernSection, offset);
                            int to   = readUShort(kernSection, offset + 2);
                            int kpx  = readShort (kernSection, offset + 4);
                            if (kpx != 0) {
                                addKerning(from, to, kpx);
                            }
                        }
                        break;
                    }
                    default:
                        Logger.getLogger(FontData.class.getName()).log(Level.WARNING,
                                "Unsupported kerning subtable format: {0} (kern table version: {1})",
                                new Object[]{format, version});
                }
            }

            tableOffset += tableLength;
        }
    }

    private void addKerning(int fromGlyph, int toGlyph, int kpx) {
        IntMap<Integer> adjTab = kerning.get(fromGlyph);
        if (adjTab == null) {
            adjTab = new IntMap<Integer>();
            kerning.put(fromGlyph, adjTab);
        }
        adjTab.put(toGlyph, kpx);
    }

    private static byte[] readDirTable(RandomAccessFile raf) throws IOException {
        raf.seek(4);
        int ntabs = raf.readUnsignedShort();
        raf.seek(12);

        byte[] dirTable = new byte[ntabs * 16];
        raf.readFully(dirTable);

        return dirTable;
    }

    private static byte[] readSectionOptional(RandomAccessFile raf, byte[] dirTable, String sectionName) throws IOException {
        assert sectionName.length() == 4;

        for(int i=0 ; i<dirTable.length ; i+=16) {
            boolean match = true;
            for(int j=0 ; j<4 ; j++) {
                if(dirTable[i + j] != sectionName.charAt(j)) {
                    match = false;
                    break;
                }
            }

            if(match) {
                int offset = readInt(dirTable, i + 8);
                int length = readInt(dirTable, i + 12);

                byte[] section = new byte[length];
                raf.seek(offset);
                raf.readFully(section);
                return section;
            }
        }

        return null;
    }

    private static byte[] readSection(RandomAccessFile raf, byte[] dirTable, String sectionName) throws IOException {
        byte[] section = readSectionOptional(raf, dirTable, sectionName);
        if(section == null) {
            throw new IOException("Missing '"+sectionName+"' section");
        }
        return section;
    }

    private static int readUPEM(byte[] headSection) {
        return readUShort(headSection, 18);
    }

    private static String readNAME(byte[] nameSection) {
        int numStrings = readUShort(nameSection, 2);
        int strOffset = readUShort(nameSection, 4);

        String familyName = "";
        String subFamilyName = "";

        for(int i=0 ; i<numStrings ; i++) {
            int platformID = readUShort(nameSection, i*12 + 6);
            int encodingID = readUShort(nameSection, i*12 + 8);

            if ((platformID == 1 || platformID == 3) && (encodingID == 0 || encodingID == 1)) {
                int nameID = readUShort(nameSection, i*12 + 12);
                int length = readUShort(nameSection, i*12 + 14);
                int offset = readUShort(nameSection, i*12 + 16);

                switch (nameID) {
                    case 1:
                        familyName = readString(nameSection, strOffset + offset, length);
                        break;
                    case 2:
                        subFamilyName = readString(nameSection, strOffset + offset, length);
                        break;
                }
            }
        }

        if (subFamilyName.length() == 0 || "Regular".equals(subFamilyName) || "Roman".equals(subFamilyName)) {
            return familyName;
        } else {
            return familyName + "," + subFamilyName;
        }
    }
    
    private static int readUShort(byte[] a, int off) {
        return ((a[off+0] & 0xFF) << 8) |
               ((a[off+1] & 0xFF)     );
    }

    private static short readShort(byte[] a, int off) {
        return (short)readUShort(a, off);
    }

    private static int readInt(byte[] a, int off) {
        return ((a[off+0] & 0xFF) << 24) |
               ((a[off+1] & 0xFF) << 16) |
               ((a[off+2] & 0xFF) <<  8) |
               ((a[off+3] & 0xFF)      );
    }

    private static String readString(byte[] a, int off, int len) {
        try {
            if (len > 0) {
                String encoding = (a[off] == 0) ? "UTF-16BE" : "ISO-8859-1";
                return new String(a, off, len, encoding);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FontData.class.getName()).log(Level.SEVERE, "Can't decode string", ex);
        }
        return "";
    }
}
