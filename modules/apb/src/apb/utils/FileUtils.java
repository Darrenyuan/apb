
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

package apb.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import apb.BuildException;
import apb.Environment;
import apb.Os;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Sep 8, 2008
// Time: 6:42:15 PM

//
public class FileUtils
{
    //~ Methods ..............................................................................................

    public static Set<File> listDirsWithFiles(@NotNull File dir, @NotNull String ext)
    {
        Set<File> result = new TreeSet<File>();

        if (dir.exists()) {
            addDirsWithFiles(result, dir, ext);
        }
        return result;
    }

    public static List<File> listAllFilesWithExt(@NotNull File dir, @NotNull String ext)
    {
        List<File> result = new ArrayList<File>();

        if (dir.exists()) {
            addAll(result, dir, ext);
        }

        return result;
    }

    public static List<File> listAllFilesWithExt(@NotNull Collection<File> dirs, @NotNull String ext)
    {
        List<File> result = new ArrayList<File>();

        for (File dir : dirs) {
            if (dir != null && dir.exists()) {
                addAll(result, dir, ext);
            }
        }

        return result;
    }

    public static List<File> listAllFiles(@NotNull File dir)
    {
        List<File> result = new ArrayList<File>();

        if (dir.exists()) {
            addAll(result, dir, null);
        }

        return result;
    }

    public static List<File> filterByTimestamp(final List<File> files, final List<File> sourceDirs,
                                               final File targetDir, final String targetExt)
    {
        String       targetPrefix = targetDir.getAbsolutePath();
        List<String> sourcePrefixes = absolutePaths(sourceDirs);

        List<File> result = new ArrayList<File>();

        for (File file : files) {
            String path = file.getAbsolutePath();

            int prefixLen = -1;

            for (String prefix : sourcePrefixes) {
                if (path.startsWith(prefix)) {
                    prefixLen = prefix.length();
                    break;
                }
            }

            if (prefixLen == -1) {
                throw new IllegalStateException(file + " not in any source directory.");
            }

            File target = new File(targetPrefix + changeExtension(path.substring(prefixLen), targetExt));

            if (!target.exists() || target.lastModified() < file.lastModified()) {
                result.add(file);
            }
        }

        return result;
    }

    /**
     * Change a filename extension to a new one
     * @param fileName  The filename to change the extension
     * @param ext the new extension
     * @return the filename with a new extension
     */
    @NotNull public static String changeExtension(@NotNull String fileName, @NotNull String ext)
    {
        int    dot = fileName.lastIndexOf('.');
        String baseName = dot == -1 ? fileName : fileName.substring(0, dot);
        return baseName + (ext.charAt(0) == '.' ? ext : '.' + ext);
    }

    public static List<File> removePrefix(List<File> filePrefixes, List<File> files)
    {
        List<String> prefixes = absolutePaths(filePrefixes);

        List<File> result = new ArrayList<File>();

        for (File file : files) {
            String path = file.getAbsolutePath();

            for (String prefix : prefixes) {
                if (path.startsWith(prefix)) {
                    result.add(new File(path.substring(prefix.length() + 1)));
                    break;
                }
            }
        }

        return result;
    }

    public static File removePrefix(File filePrefix, File file)
    {
        String prefix = filePrefix.getAbsolutePath();

        String path = file.getAbsolutePath();

        if (path.startsWith(prefix)) {
            throw new IllegalStateException();
        }

        return new File(path.substring(prefix.length() + 1));
    }

    public static String makePath(File... files)
    {
        return makePath(Arrays.asList(files));
    }

    public static String makePath(Collection<File> files)
    {
        return makePath(files, File.pathSeparator);
    }

    public static String makePath(Collection<File> files, String pathSeparator)
    {
        StringBuilder result = new StringBuilder();

        for (File file : files) {
            if (result.length() != 0) {
                result.append(pathSeparator);
            }

            result.append(file.getPath());
        }

        return result.toString();
    }

    public static String makePathFromStrings(List<String> files)
    {
        StringBuilder result = new StringBuilder();

        for (String file : files) {
            if (result.length() != 0) {
                result.append(File.pathSeparator);
            }

            result.append(file);
        }

        return result.toString();
    }

    public static File makeRelative(@NotNull File baseDir, @NotNull File file)
    {
        List<String> base = getParts(baseDir.getAbsoluteFile());
        List<String> f = getParts(file.getAbsoluteFile());
        int          i = 0;

        while (i < base.size() && i < f.size()) {
            if (!base.get(i).equals(f.get(i))) {
                break;
            }

            i++;
        }

        File result = null;

        for (int j = i; j < base.size(); j++) {
            result = new File(result, "..");
        }

        for (int j = i; j < f.size(); j++) {
            result = new File(result, f.get(j));
        }

        return result;
    }

    public static String makeRelative(@NotNull File baseDir, @NotNull String filePath)
    {
        return makeRelative(baseDir, new File(filePath)).getPath();
    }

    public static List<String> getParts(final File file)
    {
        LinkedList<String> result = new LinkedList<String>();

        for (File f = file; f != null; f = f.getParentFile()) {
            final String name = f.getName();
            if ("..".equals(name))
                f = f.getParentFile();
            else
            
            result.addFirst(name.isEmpty() ? "/" : name);
        }

        return result;
    }

    /**
     * Returns the extension portion of a file specification string.
     * This everything after the last dot '.' in the filename (NOT including
     * the dot). If not dot it returns the empty String
     * @param name the filename
     * @return The extension portion of it
     */
    public static String extension(File name)
    {
        String nm = name.getName();
        int    lastDot = nm.lastIndexOf('.');
        return lastDot == -1 ? "" : nm.substring(lastDot + 1);
    }

    public static void copyFileFiltering(@NotNull File from, @NotNull File to, @NotNull String encoding,
                                         @NotNull List<Filter> filters)
        throws IOException
    {
        BufferedReader reader = null;
        Writer         writer = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(from), encoding));
            writer = new OutputStreamWriter(createOutputStream(to), encoding);

            String line;

            while ((line = reader.readLine()) != null) {
                for (Filter filter : filters) {
                    line = filter.filter(line);
                }

                writer.write(line);
            }
        }
        finally {
            close(reader);
            close(writer);
        }
    }

    public static void copyFile(@NotNull File from, @NotNull File to)
        throws IOException
    {
        FileInputStream  in = null;
        FileOutputStream out = null;

        try {
            out = createOutputStream(to);

            in = new FileInputStream(from);

            FileChannel readChannel = in.getChannel();
            FileChannel writeChannel = out.getChannel();

            long size = readChannel.size();

            for (long position = 0; position < size;) {
                position += readChannel.transferTo(position, MB, writeChannel);
            }

            if (from.length() != to.length()) {
                throw new IOException("Failed to copy full contents from " + from + " to " + to);
            }
        }
        finally {
            close(in);
            close(out);
        }
    }

    /**
     * Create a FileOutputStream, creates the intermediate directories if necessary
     * @param file The file to open
     * @return A FileOutputStream
     * @throws FileNotFoundException
     */
    public static FileOutputStream createOutputStream(File file)
        throws FileNotFoundException
    {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return new FileOutputStream(file);
    }

    /**
     * Create a FileWriter, creates the intermediate directories if necessary
     * @param file The file to open
     * @return A FileWriter
     * @throws IOException
     */
    public static FileWriter createWriter(File file)
        throws IOException
    {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return new FileWriter(file);
    }

    public static void copyFile(@NotNull InputStream from, @NotNull File to)
        throws IOException
    {
        FileOutputStream writer = null;

        try {
            writer = createOutputStream(to);

            byte[] buffer = new byte[4092];

            int n;

            while ((n = from.read(buffer)) > 0) {
                writer.write(buffer, 0, n);
            }
        }
        finally {
            close(writer);
        }
    }

    public static void validateDirectory(File dir)
    {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BuildException("Cannot create directory: " + dir);
        }

        if (!dir.isDirectory()) {
            throw new BuildException(dir + " is not a directory.");
        }
    }

    /**
     * List all Java Sources under a given set of directories
     * Return the RELATIVE file name
     * @param sourceDirs The directories that can contain java sources
     * @return The relative list of file names
     */
    public static List<File> listJavaSources(Collection<File> sourceDirs)
    {
        List<File> result = new ArrayList<File>();

        for (File dir : sourceDirs) {
            if (dir != null && dir.exists()) {
                List<File> abs = new ArrayList<File>();
                addAll(abs, dir, JAVA_EXT);
                for (File ab : abs) {
                    result.add(makeRelative(dir, ab));
                }
            }
        }

        return result;
    }

    public static void close(Closeable closeable)
    {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException e) {
                // Ignore
            }
        }
    }

    @NotNull public static URL[] toURLArray(@NotNull Iterable<File> urls)
        throws MalformedURLException
    {
        Set<URL> result = new HashSet<URL>();

        for (File url : urls) {
            if (url != null) {
                result.add(new URL(url.toURI().toASCIIString()));
            }
        }

        return result.toArray(new URL[result.size()]);
    }

    public static PrintStream nullOutputStream()
    {
        return new PrintStream(new OutputStream() {
                public void write(int b) {}
            });
    }

    public static String addExecutableExtension(String cmd)
    {
        final Os os = Os.getInstance();
        return os.isWindows() || os.isOs2() ? cmd + ".exe" : cmd;
    }

    public static String findJavaExecutable(@NotNull final String cmd, @NotNull final Environment env)
    {
        final String javaCmd = addExecutableExtension(cmd);

        // Try with '$java.home/bin

        String result = findCmdInDir(new File(java_home), javaCmd);

        if (result == null) {
            // Try with '$java.home/../bin
            result = findCmdInDir(new File(java_home, ".."), javaCmd);
        }

        if (result == null) {
            // Try with environment JAVA_HOME
            if (JAVA_HOME == null) {
                env.logInfo("JAVA_HOME environment variable not set.\n");
            }
            else {
                result = findCmdInDir(new File(JAVA_HOME), cmd);

                if (result == null) {
                    env.logInfo("Invalid value for JAVA_HOME environment variable: %s\n", JAVA_HOME);
                }
            }
        }

        if (result == null) {
            env.logInfo("Looking for '%s' in the PATH.\n", cmd);
            result = javaCmd;
        }

        return result;
    }

    public static String findCmdInDir(@NotNull File dir, @NotNull String javaCmd)
    {
        dir = new File(dir, "bin");
        String result = null;

        if (dir.exists()) {
            File java = new File(dir, javaCmd);

            if (java.exists()) {
                result = java.getPath();
            }
        }

        return result;
    }

    static boolean isSymbolicLink(File file)
        throws IOException
    {
        return !file.getAbsolutePath().equals(file.getCanonicalPath());
    }

    private static List<String> absolutePaths(List<File> sourceDirs)
    {
        List<String> sourcePrefixes = new ArrayList<String>();

        for (File sourceDir : sourceDirs) {
            sourcePrefixes.add(sourceDir.getAbsolutePath());
        }

        return sourcePrefixes;
    }

    private static void addAll(final List<File> files, File dir, final String ext)
    {
        dir.listFiles(new FileFilter() {
                public boolean accept(File file)
                {
                    if (file.isDirectory()) {
                        addAll(files, file, ext);
                    }
                    else if (ext == null || file.getName().endsWith(ext)) {
                        files.add(file);
                    }

                    return true;
                }
            });
    }

    private static void addDirsWithFiles(final Set<File> files, File dir, final String ext)
    {
        dir.listFiles(new FileFilter() {
                public boolean accept(File file)
                {
                    if (file.isDirectory()) {
                        addDirsWithFiles(files, file, ext);
                    }
                    else if (ext == null || file.getName().endsWith(ext)) {
                        files.add(file.getParentFile());
                    }

                    return true;
                }
            });
    }

    //~ Static fields/initializers ...........................................................................

    public static final String JAVA_HOME = System.getenv("JAVA_HOME");
    public static final String java_home = System.getProperty("java.home");

    public static final String JAVA_EXT = ".java";

    private static final int MB = 1024 * 1024;

    //~ Inner Interfaces .....................................................................................

    public static interface Filter
    {
        String filter(String str);
    }
}
