/*
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 * following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.force.mobile.build.tools.lcovr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.force.mobile.build.tools.lcovr.data.SourceFileInfo;

public class CoberturaCoverageWriterTest {

    @Test
    public void testCreateDocument() throws Exception {
        Document d = CoberturaCoverageWriter.createDocument();
        assertNotNull("Document is null", d);
        //TODO assert correct namespace and DTD
    }

    @Test
    public void testCreateXmlDocument() throws Exception {
        SourceFileInfo info1 = new SourceFileInfo("com" + File.separator + "mycompany" + File.separator + "MyClass.java");
        info1.setLh(3);
        info1.setLf(4);
        info1.getLineInfo().put(3, 1);
        info1.getLineInfo().put(4, 1);
        info1.getLineInfo().put(5, 1);
        info1.getLineInfo().put(6, 0);


        SourceFileInfo info2 = new SourceFileInfo("com" + File.separator + "mycompany" + File.separator + "MyOtherClass.java");
        info2.setLh(3);
        info2.setLf(6);
        info2.getLineInfo().put(3, 0);
        info2.getLineInfo().put(4, 1);
        info2.getLineInfo().put(5, 1);
        info2.getLineInfo().put(6, 1);
        info2.getLineInfo().put(7, 0);
        info2.getLineInfo().put(8, 0);


        Vector<SourceFileInfo> infos = new Vector<SourceFileInfo>(2);
        infos.add(info1);
        infos.add(info2);

        File sourceFile1 = new File("src" + File.separator + "test" + File.separator + "java");
        File sourceFile2 = new File("src" + File.separator + "main" + File.separator + "java");
        List<File> sources = new Vector<File>(2);

        sources.add(sourceFile1);
        sources.add(sourceFile2);
        CoberturaCoverageWriter writer = new CoberturaCoverageWriter(infos);
        writer.setSourceDirectories(sources);

        Document d = writer.populateXmlDocument(CoberturaCoverageWriter.createDocument());

        assertNotNull("document is null", d);
        Element rootElement = d.getDocumentElement();
        assertEquals("Root node name is wrong", "coverage", rootElement.getNodeName());
//        assertEquals("0.6", rootElement.getAttribute("line-rate"));

        /* Validate <sources/> */
        NodeList sourcesList = d.getElementsByTagName("sources");
        assertEquals(1, sourcesList.getLength());
        Element sourcesElement = (Element)sourcesList.item(0);
        NodeList sourcesChildren = sourcesElement.getChildNodes();
        assertEquals(sources.size(), sourcesChildren.getLength());
        Element firstSource = (Element)sourcesChildren.item(0);
        assertEquals("source", firstSource.getNodeName());
        assertEquals(sourceFile1.getAbsolutePath(), firstSource.getTextContent());

        Element secondSource = (Element)sourcesChildren.item(1);
        assertEquals("source", secondSource.getNodeName());
        assertEquals(sourceFile2.getAbsolutePath(), secondSource.getTextContent());

        /* Validate <packages/> */
        NodeList packagesList = d.getElementsByTagName("packages");
        assertEquals(1, packagesList.getLength());
        NodeList packagesChildren = packagesList.item(0).getChildNodes();
        assertEquals(1, packagesChildren.getLength());

        Element packageXml = (Element)packagesChildren.item(0);
        assertEquals("com.mycompany", packageXml.getAttribute("name"));
        assertEquals("0.6", packageXml.getAttribute("line-rate"));
        assertEquals("0.0", packageXml.getAttribute("complexity"));
        assertEquals("0.0", packageXml.getAttribute("branch-rate"));

        NodeList packageChildren = packageXml.getChildNodes();
        assertEquals(1, packageChildren.getLength());
        Element classesElement = (Element)packageChildren.item(0);
        assertEquals("classes", classesElement.getNodeName());

        NodeList classesChildren = classesElement.getChildNodes();
        assertEquals(2, classesChildren.getLength());
        Element myClassElement = (Element)classesChildren.item(0);
        Element myOtherClassElement = (Element)classesChildren.item(1);

        assertEquals("com.mycompany.MyClass", myClassElement.getAttribute("name"));
        assertEquals(info1.getFileName(), myClassElement.getAttribute("filename"));
        assertEquals(Double.toString(info1.getComplexity()), myClassElement.getAttribute("complexity"));
        assertEquals(Double.toString(info1.getBranchRate()), myClassElement.getAttribute("branch-rate"));
        assertEquals(Double.toString(info1.getLineRate()), myClassElement.getAttribute("line-rate"));

        assertEquals("com.mycompany.MyOtherClass", myOtherClassElement.getAttribute("name"));
        assertEquals(info2.getFileName(), myOtherClassElement.getAttribute("filename"));
        assertEquals(Double.toString(info2.getComplexity()), myOtherClassElement.getAttribute("complexity"));
        assertEquals(Double.toString(info2.getBranchRate()), myOtherClassElement.getAttribute("branch-rate"));
        assertEquals(Double.toString(info2.getLineRate()), myOtherClassElement.getAttribute("line-rate"));

        validateLineElements(info1, myClassElement);
        validateLineElements(info2, myOtherClassElement);

    }

    private void validateLineElements(SourceFileInfo info, Element classElement) {
        /* There should be two sets of children: <methods/> and <lines/> */
        assertEquals(2, classElement.getChildNodes().getLength());
        assertEquals(1, classElement.getElementsByTagName("methods").getLength());
        assertEquals(1, classElement.getElementsByTagName("lines").getLength());
        Element linesElement = (Element)classElement.getElementsByTagName("lines").item(0);
        NodeList linesChildren = linesElement.getChildNodes();


        assertEquals(info.getLineInfo().size(), linesChildren.getLength());
        for (int i = 0; i < linesChildren.getLength(); i++) {
            Node n = linesChildren.item(i);
            assertTrue(n instanceof Element);
            Element lineElement = (Element)n;
            String lineNumberString = lineElement.getAttribute("number");
            assertNotNull(lineNumberString);
            int lineNumber = Integer.parseInt(lineNumberString);
            assertTrue(info.getLineInfo().containsKey(lineNumber));
            String hitsString = lineElement.getAttribute("hits");
            assertNotNull(lineNumberString);
            int hits = Integer.parseInt(hitsString);
            assertEquals((int)info.getLineInfo().get(lineNumber), hits);
        }
    }

}
