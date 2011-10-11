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

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.junit.Test;

import com.force.mobile.build.tools.lcovr.data.SourceFileInfo;

public class SourceFileInfoTest {

    @Test
    public void testSplitIntoPackages() {
        Vector<SourceFileInfo> infos = new Vector<SourceFileInfo>(4);
        infos.add(new SourceFileInfo("com" + File.separator + "mycompany" + File.separator + "MyClass1.java"));
        infos.add(new SourceFileInfo("com" + File.separator + "mycompany" + File.separator + "MyClass2.java"));
        infos.add(new SourceFileInfo("com" + File.separator + "mycompany" + File.separator + "mypackage" + File.separator + "MyClass3.java"));

        Map<String, Set<SourceFileInfo>> allPackages = SourceFileInfo.splitIntoPackages(infos);
        assertEquals("expected two unique packages", 2, allPackages.size());
        Set<SourceFileInfo> myCompanyPackages = allPackages.get("com.mycompany");
        assertNotNull("com.mycompany package had no sources", myCompanyPackages);
        assertEquals("expected two classes in com.mycompany", 2, myCompanyPackages.size());

        Set<SourceFileInfo> myPackagePackages = allPackages.get("com.mycompany.mypackage");
        assertNotNull("com.mycompany.mypackage had no sources", myPackagePackages);
        assertEquals("expected one class in com.mycompany.mypackage", 1, myPackagePackages.size());
        SourceFileInfo only = (SourceFileInfo)myPackagePackages.toArray()[0];
        assertEquals("wrong class name!", "MyClass3", only.getSourceClassName());
    }

    @Test
    public void testSourceFileInfo() {
        String fileName = "com" + File.separator + "mycompany" + File.separator + "MyClass.java";
        SourceFileInfo sfi = new SourceFileInfo(fileName);
        assertEquals("com.mycompany.MyClass", sfi.getSourceFullClassName());
        assertEquals("com.mycompany", sfi.getSourcePackageName());
        assertEquals("MyClass", sfi.getSourceClassName());
    }
}
