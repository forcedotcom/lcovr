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

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

import com.force.mobile.build.tools.lcovr.data.SourceFileInfo;

/**
 * A Writer to format {@link SourceFileInfo} metrics into a Cobertura XML
 * report.
 *
 * @author Jason Schroeder
 * @see http://cobertura.sourceforge.net/xml/coverage-03.dtd
 */
public class CoberturaCoverageWriter {

    /**
     * List of Infos to process. Set in the constructor.
     */
    private final List<SourceFileInfo> infos;

    /**
     * List of sources.
     *
     * @see #setSourceDirectories(List)
     */
    private List<File> sources;

    /**
     * Constructor.
     *
     * @param information
     *            A list of {@link SourceFileInfo} objects to be written out to
     *            XML format.
     * @see #process(File)
     */
    public CoberturaCoverageWriter(final List<SourceFileInfo> information) {
        this.infos = information;
    }

    /**
     * Set a list of source directories. Your filenames in your
     * {@link SourceFileInfo} will be relative to one of these directories.
     *
     * @param dirs
     *            Source directories.
     */
    public final void setSourceDirectories(final List<File> dirs) {
        this.sources = dirs;
    }

    /**
     * Create the XML <code>Document</code> with the correct DTD and DocType.
     *
     * @return a newly-created <code>Document</code>
     * @throws Exception
     *             on any XML Factory error.
     */
    protected static Document createDocument() throws Exception {
        // We need a Document
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        DocumentType docType = docBuilder.getDOMImplementation().
                createDocumentType("coverage", null,
                "http://cobertura.sourceforge.net/xml/coverage-03.dtd");
        Document doc = docBuilder.getDOMImplementation().createDocument(null,
                "coverage", docType);
        return doc;
    }

    /**
     * Process the information and write it to the given {@link File}.
     *
     * @param outputFile
     *            File to write to.
     * @throws Exception
     *             Any IOExceptions.
     */
    public final void process(final File outputFile) throws Exception {
        writeDocument(populateXmlDocument(createDocument()), outputFile);
    }

    /**
     * Transform the <code>infos</code> into the XML DOM.
     *
     * @param doc
     *            Document to populate.
     * @return a populated Document.
     */
    protected final Document populateXmlDocument(final Document doc) {
        Element root = doc.getDocumentElement();
        root.setAttribute("timestamp",
                Long.toString(System.currentTimeMillis()));
        root.setAttribute("branch-rate", "0.0");
        root.setAttribute("version",
                "lcovr " + getClass().getPackage().getImplementationVersion());

        Element sourcesXml = doc.createElement("sources");
        root.appendChild(sourcesXml);

        if (null != sources) {
            for (File sourceDirectory : sources) {
                Element sourceXml = doc.createElement("source");
                sourceXml.appendChild(
                        doc.createTextNode(sourceDirectory.getAbsolutePath()));
                sourcesXml.appendChild(sourceXml);
            }
        }

        Element packagesXml = doc.createElement("packages");
        root.appendChild(packagesXml);

        int totalLineCount = 0;
        int totalCoveredLineCount = 0;
        Map<String, Set<SourceFileInfo>> allPackages =
            SourceFileInfo.splitIntoPackages(infos);
        Vector<String> packageNames = new Vector<String>(allPackages.keySet());
        Collections.sort(packageNames);
        for (String packageName : packageNames) {
            Element packageXml = doc.createElement("package");
            packagesXml.appendChild(packageXml);
            packageXml.setAttribute("name", packageName);
            packageXml.setAttribute("branch-rate", "0.0");
            packageXml.setAttribute("complexity", "0.0");
            Element classesXml = doc.createElement("classes");
            packageXml.appendChild(classesXml);
            Vector<SourceFileInfo> childSources =
                new Vector<SourceFileInfo>(allPackages.get(packageName));
            Collections.sort(childSources);
            int packageLineCount = 0;
            int coveredPackageLineCount = 0;
            for (SourceFileInfo info : childSources) {
                addClass(info, classesXml);
                packageLineCount += info.getLf();
                coveredPackageLineCount += info.getLh();
            }
            double lineRate = 0;
            if (packageLineCount > 0) {
                lineRate = (double) coveredPackageLineCount / (double) packageLineCount;
            }
            packageXml.setAttribute("line-rate", Double.toString(lineRate));

            totalLineCount += packageLineCount;
            totalCoveredLineCount += coveredPackageLineCount;
        }
        double totalLineRate = 0;
        if (totalLineCount > 0) {
            totalLineRate = (double) totalCoveredLineCount / (double) totalLineCount;
        }
        root.setAttribute("line-rate", Double.toString(totalLineRate));

        return doc;
    }

    /**
     * Populate a new <code>&lt;class&gt;</code> node, and append it to
     * <code>parentNode</code>.
     *
     * @param info
     *            Information about a source file (class)
     * @param parentNode
     *            Node to append to.
     */
    private void addClass(final SourceFileInfo info, final Element parentNode) {
        Document doc = parentNode.getOwnerDocument();
        Element classXml = doc.createElement("class");
        parentNode.appendChild(classXml);
        classXml.setAttribute("branch-rate",
                Double.toString(info.getBranchRate()));
        classXml.setAttribute("complexity",
                Double.toString(info.getComplexity()));
        classXml.setAttribute("line-rate",
                Double.toString(info.getLineRate()));
        classXml.setAttribute("filename", info.getFileName());
        classXml.setAttribute("name", info.getSourceFullClassName());
        Element methodsXml = doc.createElement("methods");
        classXml.appendChild(methodsXml);

        Element linesXml = doc.createElement("lines");
        classXml.appendChild(linesXml);
        addLines(info, linesXml);
    }

    /**
     * Create new <code>&lt;line&gt;</code> nodes and append to the
     * <code>parentNode</code>.
     *
     * @param info
     *            Information about a source file (class)
     * @param parentNode
     *            Node to append the newly created nodes.
     */
    private void addLines(final SourceFileInfo info, final Element parentNode) {
        Document doc = parentNode.getOwnerDocument();
        for (Integer lineNumber : info.getLineInfo().keySet()) {
            Element line = doc.createElement("line");
            parentNode.appendChild(line);
            line.setAttribute("hits",
                    Integer.toString(info.getLineInfo().get(lineNumber)));
            line.setAttribute("number", Integer.toString(lineNumber));
        }
    }

    /**
     * Write the provided <code>Document</code> to the given <code>File</code>.
     *
     * @param doc
     *            Document to transform
     * @param file
     *            File to write
     * @throws Exception
     *             on any I/O error.
     */
    private static void writeDocument(final Document doc, final File file)
    throws Exception {
        // Set up a transformer
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        DocumentType doctype = doc.getDoctype();
        if (doctype != null) {
            trans.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                    doctype.getSystemId());
        }

        // Write file from xml tree
        FileWriter fw = new FileWriter(file);
        StreamResult result = new StreamResult(fw);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, result);
    }
}
