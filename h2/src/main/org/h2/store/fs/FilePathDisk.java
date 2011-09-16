/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.store.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.h2.constant.ErrorCode;
import org.h2.constant.SysProperties;
import org.h2.message.DbException;
import org.h2.util.IOUtils;
import org.h2.util.New;
import org.h2.util.Utils;

/**
 * This file system stores files on disk.
 * This is the most common file system.
 */
public class FilePathDisk extends FilePath {

    private static final String CLASSPATH_PREFIX = "classpath:";

    public FilePathDisk getPath(String path) {
        FilePathDisk p = new FilePathDisk();
        p.name = translateFileName(path);
        return p;
    }

    public long size() {
        return new File(name).length();
    }

    /**
     * Translate the file name to the native format.
     * This will expand the home directory (~).
     *
     * @param fileName the file name
     * @return the native file name
     */
    protected static String translateFileName(String fileName) {
        return expandUserHomeDirectory(fileName);
    }

    /**
     * Expand '~' to the user home directory. It is only be expanded if the ~
     * stands alone, or is followed by / or \.
     *
     * @param fileName the file name
     * @return the native file name
     */
    public static String expandUserHomeDirectory(String fileName) {
        if (fileName == null) {
            return null;
        }
        boolean prefix = false;
        if (fileName.startsWith("file:")) {
            prefix = true;
            fileName = fileName.substring("file:".length());
        }
        if (fileName.startsWith("~") && (fileName.length() == 1 || fileName.startsWith("~/") || fileName.startsWith("~\\"))) {
            String userDir = SysProperties.USER_HOME;
            fileName = userDir + fileName.substring(1);
        }
        return prefix ? "file:" + fileName : fileName;
    }

    public void moveTo(FilePath newName) {
        File oldFile = new File(name);
        File newFile = new File(newName.name);
        if (oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            DbException.throwInternalError("rename file old=new");
        }
        if (!oldFile.exists()) {
            throw DbException.get(ErrorCode.FILE_RENAME_FAILED_2,
                    name + " (not found)",
                    newName.name);
        }
        if (newFile.exists()) {
            throw DbException.get(ErrorCode.FILE_RENAME_FAILED_2,
                    new String[] { name, newName + " (exists)" });
        }
        for (int i = 0; i < SysProperties.MAX_FILE_RETRY; i++) {
            IOUtils.trace("rename", name + " >" + newName, null);
            boolean ok = oldFile.renameTo(newFile);
            if (ok) {
                return;
            }
            wait(i);
        }
        throw DbException.get(ErrorCode.FILE_RENAME_FAILED_2, new String[]{name, newName.name});
    }

    private static void wait(int i) {
        if (i == 8) {
            System.gc();
        }
        try {
            // sleep at most 256 ms
            long sleep = Math.min(256, i * i);
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public boolean createFile() {
        File file = new File(name);
        for (int i = 0; i < SysProperties.MAX_FILE_RETRY; i++) {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                // 'access denied' is really a concurrent access problem
                wait(i);
            }
        }
        return false;
    }

    public boolean exists() {
        return new File(name).exists();
    }

    public void delete() {
        File file = new File(name);
        for (int i = 0; i < SysProperties.MAX_FILE_RETRY; i++) {
            IOUtils.trace("delete", name, null);
            boolean ok = file.delete();
            if (ok || !file.exists()) {
                return;
            }
            wait(i);
        }
        throw DbException.get(ErrorCode.FILE_DELETE_FAILED_1, name);
    }

    public List<FilePath> listFiles() {
        ArrayList<FilePath> list = New.arrayList();
        File f = new File(name);
        try {
            String[] files = f.list();
            if (files != null) {
                String base = f.getCanonicalPath();
                if (!base.endsWith(SysProperties.FILE_SEPARATOR)) {
                    base += SysProperties.FILE_SEPARATOR;
                }
                for (int i = 0, len = files.length; i < len; i++) {
                    list.add(getPath(base + files[i]));
                }
            }
            return list;
        } catch (IOException e) {
            throw DbException.convertIOException(e, name);
        }
    }

    public boolean canWrite() {
        return canWriteInternal(new File(name));
    }

    public boolean setReadOnly() {
        File f = new File(name);
        return f.setReadOnly();
    }

    public FilePathDisk getCanonicalPath() {
        try {
            String fileName = new File(name).getCanonicalPath();
            return getPath(fileName);
        } catch (IOException e) {
            throw DbException.convertIOException(e, name);
        }
    }

    public FilePath getParent() {
        String p = new File(name).getParent();
        return p == null ? null : getPath(p);
    }

    public boolean isDirectory() {
        return new File(name).isDirectory();
    }

    public boolean isAbsolute() {
        return new File(name).isAbsolute();
    }

    public long lastModified() {
        return new File(name).lastModified();
    }

    private static boolean canWriteInternal(File file) {
        try {
            if (!file.canWrite()) {
                return false;
            }
        } catch (Exception e) {
            // workaround for GAE which throws a
            // java.security.AccessControlException
            return false;
        }
        // File.canWrite() does not respect windows user permissions,
        // so we must try to open it using the mode "rw".
        // See also http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
        RandomAccessFile r = null;
        try {
            r = new RandomAccessFile(file, "rw");
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public void createDirectory() {
        File f = new File(name);
        if (!f.exists()) {
            File dir = new File(name);
            for (int i = 0; i < SysProperties.MAX_FILE_RETRY; i++) {
                if ((dir.exists() && dir.isDirectory()) || dir.mkdir()) {
                    return;
                }
                wait(i);
            }
            throw DbException.get(ErrorCode.FILE_CREATION_FAILED_1, name);
        }
    }

    public OutputStream newOutputStream(boolean append) {
        try {
            File file = new File(name);
            File parent = file.getParentFile();
            if (parent != null) {
                FileUtils.createDirectories(parent.getAbsolutePath());
            }
            FileOutputStream out = new FileOutputStream(name, append);
            IOUtils.trace("openFileOutputStream", name, out);
            return out;
        } catch (IOException e) {
            freeMemoryAndFinalize();
            try {
                return new FileOutputStream(name);
            } catch (IOException e2) {
                throw DbException.convertIOException(e, name);
            }
        }
    }

    public InputStream newInputStream() throws IOException {
        if (name.indexOf(':') > 1) {
            // if the : is in position 1, a windows file access is assumed: C:.. or D:
            if (name.startsWith(CLASSPATH_PREFIX)) {
                String fileName = name.substring(CLASSPATH_PREFIX.length());
                if (!fileName.startsWith("/")) {
                    fileName = "/" + fileName;
                }
                InputStream in = getClass().getResourceAsStream(fileName);
                if (in == null) {
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
                }
                if (in == null) {
                    throw new FileNotFoundException("resource " + fileName);
                }
                return in;
            }
            // otherwise an URL is assumed
            URL url = new URL(name);
            InputStream in = url.openStream();
            return in;
        }
        FileInputStream in = new FileInputStream(name);
        IOUtils.trace("openFileInputStream", name, in);
        return in;
    }

    /**
     * Call the garbage collection and run finalization. This close all files that
     * were not closed, and are no longer referenced.
     */
    static void freeMemoryAndFinalize() {
        IOUtils.trace("freeMemoryAndFinalize", null, null);
        Runtime rt = Runtime.getRuntime();
        long mem = rt.freeMemory();
        for (int i = 0; i < 16; i++) {
            rt.gc();
            long now = rt.freeMemory();
            rt.runFinalization();
            if (now == mem) {
                break;
            }
            mem = now;
        }
    }

    public FileObject openFileObject(String mode) throws IOException {
        FileObjectDisk f;
        try {
            f = new FileObjectDisk(name, mode);
            IOUtils.trace("openFileObject", name, f);
        } catch (IOException e) {
            freeMemoryAndFinalize();
            try {
                f = new FileObjectDisk(name, mode);
            } catch (IOException e2) {
                throw e;
            }
        }
        return f;
    }

    public String getScheme() {
        return "file";
    }

    public FilePath createTempFile(String suffix, boolean deleteOnExit, boolean inTempDir)
            throws IOException {
        String fileName = name + ".";
        String prefix = new File(fileName).getName();
        File dir;
        if (inTempDir) {
            dir = new File(Utils.getProperty("java.io.tmpdir", "."));
        } else {
            dir = new File(fileName).getAbsoluteFile().getParentFile();
        }
        FileUtils.createDirectories(dir.getAbsolutePath());
        while (true) {
            File f = new File(dir, prefix + getNextTempFileNamePart(false) + suffix);
            if (f.exists() || !f.createNewFile()) {
                // in theory, the random number could collide
                getNextTempFileNamePart(true);
                continue;
            }
            if (deleteOnExit) {
                try {
                    f.deleteOnExit();
                } catch (Throwable e) {
                    // sometimes this throws a NullPointerException
                    // at java.io.DeleteOnExitHook.add(DeleteOnExitHook.java:33)
                    // we can ignore it
                }
            }
            return get(f.getCanonicalPath());
        }
    }

}
