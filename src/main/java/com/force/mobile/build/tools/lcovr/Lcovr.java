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
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.force.mobile.build.tools.lcovr.data.SourceFileInfo;


/**
 * Ant task to do all the work.
 * @author Jason Schroeder
 */
public class Lcovr extends Task {
    /**
     * List of FileSet. They describe directories where source code can be
     * found.
     */
    private final List<FileSet> sourceDirSet;

    /**
     * List of FileSet. They describe .lcov input files.
     */
    private final List<FileSet> lcovSources;

    /**
     * The file to export the Cobertura XML report.
     */
    private File outputFile;

    /**
     * Default constructor.
     */
    public Lcovr() {
        sourceDirSet = new Vector<FileSet>();
        lcovSources = new Vector<FileSet>();
    }

    /**
     * Set the output file for the Cobertura XML report.
     * @param f File to save.
     */
    public final void setOutput(final File f) {
        outputFile = f;
    }

    /**
     * Factory for {@link Input} objects.
     * @return a new <code>Input</code> object.
     */
    public final Input createInput() {
        return new Input();
    }

    /**
     * Factory for {@link SourceDirs} objects.
     * @return a new <code>SourceDirs</code> object.
     */
    public final SourceDirs createSourcedirs() {
        return new SourceDirs();
    }

    /**
     * Execute this task.
     */
    @Override
    public final void execute() {
        Vector<SourceFileInfo> allInfos = new Vector<SourceFileInfo>();
        try {
            for (FileSet fs : lcovSources) {
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                for (String includedFile : ds.getIncludedFiles()) {
                    LcovReader reader = new LcovReader(new File(ds.getBasedir(),
                            includedFile));
                    allInfos.addAll(reader.parse());
                }
            }
        } catch (IOException ioe) {
            throw new BuildException("Couldn't read an .lcov file", ioe);
        }
        log("Read information for " + allInfos.size() + " source files.");
        CoberturaCoverageWriter writer = new CoberturaCoverageWriter(allInfos);
        List<File> sources = new Vector<File>();
        for (FileSet dirset : sourceDirSet) {
            DirectoryScanner ds = dirset.getDirectoryScanner(getProject());
            for (String s : ds.getIncludedDirectories()) {
                sources.add(new File(ds.getBasedir(), s));
            }
        }
        writer.setSourceDirectories(sources);
        try {
            writer.process(outputFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException("Could't write cobertura .xml:" + e.getClass().getName() + ':' + e.getMessage(), e);
        }
    }

    /**
     * Handles the &lt;input&gt; sub-node.
     * @author jason
     *
     */
    public final class Input {

        /**
         * Add directories with <code>.lcov</code> files.
         * @param fs FileSet of input files.
         */
        public void addFileSet(final FileSet fs) {
            lcovSources.add(fs);
        }
    }

    /**
     * Handles the &lt;sourcedirs&gt; sub-node.
     * @author jason
     *
     */
    public final class SourceDirs {

        /**
         * Add directories where source code can be found.
         * @param fs FileSet of source code direcories.
         */
        public void addFileSet(final FileSet fs) {
            sourceDirSet.add(fs);
        }
    }
}
