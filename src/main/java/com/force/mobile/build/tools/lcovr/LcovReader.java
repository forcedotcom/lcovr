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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import com.force.mobile.build.tools.lcovr.data.SourceFileInfo;

/**
 * Reads <code>.lcov</code> files.
 * @author jason
 *
 */
public class LcovReader {
    /**
     * The input file.
     */
    private final File file;

    /**
     * Default constructor.
     * @param inputFile File to read from.
     */
    public LcovReader(final File inputFile) {
        file = inputFile;
    }

    /**
     * Read the input file.
     * @return parsed objects.
     * @throws IOException for any i/o read error.
     */
    public final Vector<SourceFileInfo> parse() throws IOException {
        Vector<SourceFileInfo> infos = new Vector<SourceFileInfo>();

        SourceFileInfo info = null;

        BufferedReader r = new BufferedReader(new FileReader(file));
        String line = r.readLine();
        while (null != line) {
            line = line.trim();
            if (line.startsWith("SF:")) {
                // the rest is the "Source File"
                info = new SourceFileInfo(line.substring("SF:".length()));
            } else if (line.startsWith("DA:")) {
                // DA:<line number>,<execution count>[,<checksum>]
                String[] tup = line.substring("DA:".length()).split(",");
                int lineNumber = Integer.parseInt(tup[0]);
                int execCount = Integer.parseInt(tup[1]);
                info.getLineInfo().put(lineNumber, execCount);
            } else if (line.startsWith("LH:")) {
                /* LH:<number of lines with non-zero execution count> */
                info.setLh(Integer.parseInt(line.substring("LH:".length())));
            } else if (line.startsWith("LF:")) {
                /* LF:<number of instrumented lines> */
                info.setLf(Integer.parseInt(line.substring("LF:".length())));
            } else if (line.equals("end_of_record")) {
                // new section!
                infos.add(info);
                info = null;
            }
            line = r.readLine();
        }

        return infos;
    }
}
