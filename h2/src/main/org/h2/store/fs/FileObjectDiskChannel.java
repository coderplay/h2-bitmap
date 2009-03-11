/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: Jan Kotek
 */
package org.h2.store.fs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * File which uses NIO FileChannel.
 */
public class FileObjectDiskChannel implements FileObject {

    private final String name;
    private FileChannel channel;

    FileObjectDiskChannel(String fileName, String mode) throws FileNotFoundException {
        this.name = fileName;
        RandomAccessFile raf = new RandomAccessFile(fileName, mode);
        channel = raf.getChannel();
    }

    public void close() throws IOException {
        channel.close();
    }

    public long getFilePointer() throws IOException {
        return channel.position();
    }

    public String getName() {
        return name;
    }

    public long length() throws IOException {
        return channel.size();
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        if (channel.size() <= off + len) {
            // TODO get size can degrade performance
            throw new java.io.EOFException();
        }
        ByteBuffer buf = ByteBuffer.wrap(b);
        buf.position(off);
        buf.limit(off + len);
        channel.read(buf);
    }

    public void seek(long pos) throws IOException {
        // System.out.println("seek");
        channel.position(pos);
    }

    public void setFileLength(long newLength) throws IOException {
        // System.out.println("setFileLength");
        // System.out.println(" "+channel.size()+" - "+channel.position());
        if (newLength <= channel.size()) {
            long oldPos = channel.position();
            channel.truncate(newLength);
            if (oldPos > newLength) {
                oldPos = newLength;
            }
            channel.position(oldPos);
        } else {
            // extend by writting to new location
            ByteBuffer b = ByteBuffer.allocate(1);
            channel.write(b, newLength - 1);
        }
        // System.out.println(" "+channel.size()+" - "+channel.position());
    }

    public void sync() throws IOException {
        // System.out.println("sync");
        channel.force(true);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        // System.out.println("write");
        ByteBuffer buf = ByteBuffer.wrap(b);
        buf.position(off);
        buf.limit(off + len);
        channel.write(buf);
    }

}
