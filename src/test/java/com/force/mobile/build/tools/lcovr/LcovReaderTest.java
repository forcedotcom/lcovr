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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Vector;

import org.junit.Test;

import com.force.mobile.build.tools.lcovr.data.SourceFileInfo;

public class LcovReaderTest {

    @Test
    public void testLcovReader() throws Exception {
        File f = new File("src/test/resources/coverage.lcov");
        assertNotNull("sample lcov file is null", f);
        assertTrue("sample lcov file does not exist: " + f.getAbsolutePath(), f.exists());
        LcovReader r = new LcovReader(f);
        Vector<SourceFileInfo> sfi = r.parse();
        assertNotNull("parse() returned null.", sfi);

        assertEquals("wrong set size from parse()", 3, sfi.size());
        SourceFileInfo file1 = sfi.elementAt(0);
        assertEquals("com/mycompany/MyClass1.java", file1.getFileName());
        assertEquals(0, file1.getLh());
        assertEquals(40, file1.getLf());
        assertEquals(40, file1.getLineInfo().size());
        assertTrue(file1.getLineInfo().containsKey(49));
        assertFalse(file1.getLineInfo().containsKey(2));
    }
}
