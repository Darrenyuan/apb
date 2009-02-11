

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
//


package apb.compiler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

import apb.Environment;

import apb.utils.FileUtils;
import apb.utils.StringUtils;

import com.sun.tools.javac.util.JCDiagnostic;

import static apb.utils.StringUtils.nChars;

public class DiagnosticReporter
    implements DiagnosticListener<JavaFileObject>
{
    //~ Instance fields ......................................................................................

    List<Diagnostic<? extends JavaFileObject>> ds;
    String                                     lastFile;
    private Environment                        env;
    private List<String>                       excludes;
    private int                                warns, errors;

    //~ Constructors .........................................................................................

    public DiagnosticReporter(Environment env)
    {
        this.env = env;
        lastFile = null;
        ds = new LinkedList<Diagnostic<? extends JavaFileObject>>();
        excludes = Collections.emptyList();
        errors = warns = 0;
    }

    //~ Methods ..............................................................................................

    public void setExcludes(List<String> excludes)
    {
        this.excludes = StringUtils.normalizePaths(excludes);
    }

    public void report(Diagnostic<? extends JavaFileObject> diagnostic)
    {
        final String source = diagnostic.getSource().toString();

        if (!isExcluded(source)) {
            count(diagnostic);

            if (!source.equals(lastFile)) {
                doReport();
                lastFile = source;
            }

            ds.add(diagnostic);
        }
    }

    public void flush()
    {
        doReport();
    }

    public void reportError(boolean failOnWarning)
    {
        if (errors > 0 || failOnWarning && warns > 0) {
            StringBuilder msg = new StringBuilder();
            msg.append("Compilation failed: ");

            if (errors != 0) {
                if (errors == 1) {
                    msg.append("1 error");
                }
                else {
                    msg.append(errors).append(" errors");
                }

                if (warns > 0) {
                    msg.append(" and ");
                }
            }

            if (warns == 1) {
                msg.append("1 warning");
            }
            else {
                if (warns > 1) {
                    msg.append(warns).append(" warnings");
                }
            }

            msg.append(".");

            env.handle(msg.toString());
        }
    }

    private static String format(Diagnostic d)
    {
        String result;

        if (d instanceof JCDiagnostic) {
            JCDiagnostic  jd = (JCDiagnostic) d;
            StringBuilder msg = new StringBuilder(nChars(8, ' '));
            final String  prefix = jd.getPrefix();
            msg.append(prefix.substring(0, 1).toUpperCase());
            msg.append(prefix.substring(1));
            msg.append('(');
            msg.append(d.getLineNumber());
            msg.append(',');
            msg.append(d.getColumnNumber());
            msg.append(") ");
            appendLines(msg, jd.getText());
            result = msg.toString();
        }
        else {
            result = d.toString();
        }

        return result;
    }

    private static void appendLines(StringBuilder result, String msg)
    {
        String indent = nChars(result.length(), ' ');
        int    nl;

        while ((nl = msg.indexOf('\n')) >= 0) {
            result.append(msg.substring(0, ++nl));
            msg = msg.substring(nl);
            result.append(indent);
        }

        if (!msg.isEmpty()) {
            result.append(msg);
        }
    }

    private void count(Diagnostic<? extends JavaFileObject> diagnostic)
    {
        switch (diagnostic.getKind()) {
        case ERROR:
            errors++;
            break;
        case MANDATORY_WARNING:
        case WARNING:
            warns++;
            break;
        }
    }

    private void doReport()
    {
        if (lastFile != null) {
            env.logSevere("%s:\n", lastFile);

            for (Diagnostic d : ds) {
                env.logSevere("%s\n", format(d));
            }

            ds.clear();
        }
    }

    private boolean isExcluded(String name)
    {
        name = FileUtils.makeRelative(env.getBaseDir(), name);

        for (String exclude : excludes) {
            if (StringUtils.matchPath(exclude, name, true)) {
                return true;
            }
        }

        return false;
    }
}