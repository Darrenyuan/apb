
// Copyright 2008-2009 Emilio Lopez-Gabeiras
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License

package apb;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import apb.metadata.CoverageInfo;
import apb.metadata.TestModule;
import apb.metadata.ProjectElement;

import apb.testrunner.output.TestReport;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Dec 2, 2008
// Time: 5:26:23 PM

//
public class TestModuleHelper
    extends ModuleHelper
{
    //~ Instance fields ......................................................................................

    @NotNull private List<File> classesToTest;

    @NotNull private File coverageDir;

    private ModuleHelper moduleToTest;

    @NotNull private File reportsDir;
    @NotNull private File workingDirectory;

    //~ Constructors .........................................................................................

    public TestModuleHelper(TestModule module, Environment env)
    {
        super(module, env);
    }

    //~ Methods ..............................................................................................

    public TestModule getModule()
    {
        return (TestModule) super.getModule();
    }

    public CoverageInfo getCoverageInfo()
    {
        return getModule().coverage;
    }

    public void setModuleToTest(ModuleHelper module)
    {
        moduleToTest = module;
    }

    @NotNull public ModuleHelper getModuleToTest()
    {
        if (moduleToTest == null) {
            throw new IllegalStateException("'moduleToTest' not initialized");
        }

        return moduleToTest;
    }

    @NotNull public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public List<TestReport> getReports()
    {
        return getModule().reports();
    }

    @NotNull public File getReportsDir()
    {
        return reportsDir;
    }

    @NotNull public File getCoverageDir()
    {
        return coverageDir;
    }

    @NotNull public List<File> getClassesToTest()
    {
        return classesToTest;
    }

    public boolean isCoverageEnabled()
    {
        return getCoverageInfo().enable;
    }

    public List<File> getSourcesToTest()
    {
        return moduleToTest.getSourceDirs();
    }

    public int getMemory()
    {
        return getModule().memory;
    }

    public Map<String, String> getEnvironmentVariables()
    {
        return getModule().environment();
    }

    public String getCreatorClass()
    {
        final TestModule m = getModule();
        return m.testType.creatorClass(m.customCreator);
    }

    void activate(@NotNull ProjectElement activatedTestModule)
    {
        super.activate(activatedTestModule);
        TestModule m = getModule();
        workingDirectory = env.fileFromBase(m.workingDirectory);
        reportsDir = env.fileFromBase(m.reportsDir);
        coverageDir = env.fileFromBase(m.coverage.output);
        classesToTest = Collections.singletonList(moduleToTest.getOutput());
    }
}
