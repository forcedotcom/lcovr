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
package com.force.mobile.build.tools.lcovr.data;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Plain Java object to hold information about a Source file.
 *
 * @author Jason Schroeder
 *
 */
public class SourceFileInfo implements Comparable<SourceFileInfo> {

    /**
     * The file name, from the perspective of the instrumentation.
     */
    private final String fileName;

    /**
     * A map. The key is the source line number, and the value is the number of
     * times that line was executed.
     */
    private final Map<Integer, Integer> lineInfo;

    /**
     * The Numerator. Number of lines executed at least once.
     */
    private int lh;

    /**
     * The Denominator. Number of lines instrumented.
     */
    private int lf;

    /**
     * Constructor. Create a new <code>SourceFileInfo</code> with a given source
     * code filename.
     *
     * @param sourceFilename
     *            source filename, from the perspective of the instrumentation.
     */
    public SourceFileInfo(final String sourceFilename) {
        fileName = sourceFilename;
        lineInfo = new HashMap<Integer, Integer>();
    }

    /**
     * Get the source filename of this coverage information.
     *
     * This probably has OS-specific separators, and it is not an absolute path.
     *
     * @return The filename
     * @see File#separator
     */
    public final String getFileName() {
        return fileName;
    }

    /**
     * Get the line information. The key is the line number, and the value is
     * the number of times that line was executed.
     *
     * @return A Map that describes number of times a line was executed.
     */
    public final Map<Integer, Integer> getLineInfo() {
        return lineInfo;
    }

    /**
     * Get the number of lines that were executed at least once.
     *
     * @return Number of lines that were executed at least once.
     */
    public final int getLh() {
        return lh;
    }

    /**
     * Set the number of lines that were executed at least once.
     *
     * @param linesExecuted
     *            Number of lines that were executed at least once.
     */
    public final void setLh(final int linesExecuted) {
        this.lh = linesExecuted;
    }

    /**
     * Get the number of lines of code that were instrumented.
     *
     * @return Number of lines of code that were instrumented.
     */
    public final int getLf() {
        return lf;
    }

    /**
     * Set the number of lines of code that were instrumented.
     *
     * @param linesInstrumented
     *            Number of lines of code that were instrumented.
     */
    public final void setLf(final int linesInstrumented) {
        this.lf = linesInstrumented;
    }

    /**
     * Get the source file's full class name. This includes the package.
     *
     * @return The full class name, including the package.
     */
    public final String getSourceFullClassName() {
        return getSourcePackageName() + '.' + getSourceClassName();
    }

    /**
     * Get the source file's class name, without the package.
     *
     * @return The source file's class name, without the package name.
     */
    public final String getSourceClassName() {
        String className = fileName.replace('/', '.');
        if (className.endsWith(".java")) {
            className = className.substring(0, className.length()
                    - ".java".length());
        }
        className = className.substring(className.lastIndexOf('.') + 1);
        return className;
    }

    /**
     * Get the source file's package name.
     *
     * @return The source file's package name.
     */
    public final String getSourcePackageName() {
        String packageName = fileName.replace('/', '.');
        if (packageName.endsWith(".java")) {
            packageName = packageName.substring(0, packageName.length()
                    - ".java".length());
        }
        packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        return packageName;
    }

    /**
     * Get the branch rate.
     *
     * NOTE: This is not implemented, and always returns <code>0.0</code>
     *
     * @return The branch rate, as a percent. (In the range [0.0, 1.0])
     */
    public final double getBranchRate() {
        // TODO Not implemented.
        return 0.0f;
    }

    /**
     * Get the algorithmic complexity of the source file.
     *
     * NOTE: This is not implemented, and always returns <code>0.0</code>
     *
     * @return The algorithmic complexity, as a percent (In the range [0.0,
     *         1.0])
     */
    public final double getComplexity() {
        // TODO Not implemented.
        return 0.0f;
    }

    /**
     * Returns the percent of lines that were executed at least once.
     *
     * @return The percent of lines that were executed at least once.
     */
    public final double getLineRate() {
        if (lf == 0) {
            return 0.0f;
        }
        return (double) lh / (double) lf;
    }

    /**
     * Split a list of {@link SourceFileInfo}s into a map, keyed by the package
     * name.
     *
     * @param infos
     *            Objects to split up.
     * @return A Map, with keys being the package name, and the value being a
     *         set of {@link SourceFileInfo}s.
     * @see SourceFileInfo#getSourcePackageName()
     */
    public static final Map<String, Set<SourceFileInfo>> splitIntoPackages(
            final List<SourceFileInfo> infos) {
        Map<String, Set<SourceFileInfo>> dictionary = new HashMap<String,
            Set<SourceFileInfo>>();
        for (SourceFileInfo info : infos) {
            String packageName = info.getSourcePackageName();
            Set<SourceFileInfo> s = dictionary.get(packageName);
            if (null == s) {
                s = new HashSet<SourceFileInfo>();
                dictionary.put(packageName, s);
            }
            s.add(info);
        }
        return dictionary;
    }

    /**
     * Compare a <code>SourceFileInfo</code> to <code>this</code>. Objects are
     * sorted based on file name, to provide a stable sort.
     *
     * @param otherObject
     *            Object to compare to.
     * @return sort order
     * @see Comparable#compareTo(Object)
     */
    public final int compareTo(final SourceFileInfo otherObject) {
        return fileName.compareTo(otherObject.fileName);
    }

}
