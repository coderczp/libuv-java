/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package net.java.libuv.handles;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import net.java.libuv.cb.StreamCloseCallback;
import net.java.libuv.cb.StreamConnectCallback;
import net.java.libuv.cb.StreamConnectionCallback;
import net.java.libuv.cb.StreamRead2Callback;
import net.java.libuv.cb.StreamReadCallback;
import net.java.libuv.cb.StreamShutdownCallback;
import net.java.libuv.cb.StreamWriteCallback;

public class StreamHandle extends Handle {

    protected boolean closed;
    private boolean readStarted;

    private StreamReadCallback onRead = null;
    private StreamRead2Callback onRead2 = null;
    private StreamWriteCallback onWrite = null;
    private StreamConnectCallback onConnect = null;
    private StreamConnectionCallback onConnection = null;
    private StreamCloseCallback onClose = null;
    private StreamShutdownCallback onShutdown = null;

    static {
        _static_initialize();
    }

    public void setReadCallback(final StreamReadCallback callback) {
        onRead = callback;
    }

    public void setRead2Callback(final StreamRead2Callback callback) {
        onRead2 = callback;
    }

    public void setWriteCallback(final StreamWriteCallback callback) {
        onWrite = callback;
    }

    public void setConnectCallback(final StreamConnectCallback callback) {
        onConnect = callback;
    }

    public void setConnectionCallback(final StreamConnectionCallback callback) {
        onConnection = callback;
    }

    public void setCloseCallback(final StreamCloseCallback callback) {
        onClose = callback;
    }

    public void setShutdownCallback(final StreamShutdownCallback callback) {
        onShutdown = callback;
    }

    public void readStart() {
        if (!readStarted) {
            _read_start(pointer);
        }
        readStarted = true;
    }

    public void read2Start() {
        if (!readStarted) {
            _read2_start(pointer);
        }
        readStarted = true;
    }

    public void readStop() {
        _read_stop(pointer);
        readStarted = false;
    }

    public int write2(final String str, final StreamHandle handle) {
        assert handle != null;
        final byte[] data;
        try {
            data = str.getBytes("utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e); // "utf-8" is always supported
        }
        return _write2(pointer, data, 0, data.length, handle.pointer);
    }

    public int write(final String str) {
        final byte[] data;
        try {
            data = str.getBytes("utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e); // "utf-8" is always supported
        }
        return write(data, 0, data.length);
    }

    public int write(final String str, final String encoding) throws UnsupportedEncodingException {
        final byte[] data = str.getBytes(encoding);
        return write(data, 0, data.length);
    }

    public int write(final ByteBuffer data) {
        return write(data.array(), data.position(), data.remaining());
    }

    public int write(final byte[] data, final int offset, final int length) {
        return _write(pointer, data, offset, length);
    }

    public int write(final byte[] data) {
        return write(data, 0, data.length);
    }

    public int closeWrite() {
        return _close_write(pointer);
    }

    public void close() {
        if (!closed) {
            _close(pointer);
        }
        closed = true;
    }

    public int listen(final int backlog) {
        return _listen(pointer, backlog);
    }

    public int accept(final StreamHandle client) {
        return _accept(pointer, client.pointer);
    }

    public boolean isReadable() {
        return _readable(pointer);
    }

    public boolean isWritable() {
        return _writable(pointer);
    }

    public long writeQueueSize() {
        return _write_queue_size(pointer);
    }

    protected StreamHandle(final long pointer, final LoopHandle loop) {
        super(pointer, loop);
        this.closed = false;
        this.readStarted = false;
        _initialize(pointer);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    private void callRead(final ByteBuffer data) {
        if (onRead != null) {
            loop.callbackHandler.handleStreamReadCallback(onRead, data);
        }
    }

    private void callRead2(final ByteBuffer data, long handle, int type) {
        if (onRead2 != null) {
            loop.callbackHandler.handleStreamRead2Callback(onRead2, data, handle, type);
        }
    }

    private void callWrite(final int status, final Exception error) {
        if (onWrite != null) {
            loop.callbackHandler.handleStreamWriteCallback(onWrite, status, error);
        }
    }

    private void callConnect(final int status, final Exception error) {
        if (onConnect != null) {
            loop.callbackHandler.handleStreamConnectCallback(onConnect, status, error);
        }
    }

    private void callConnection(final int status, final Exception error) {
        if (onConnection != null) {
            loop.callbackHandler.handleStreamConnectionCallback(onConnection, status, error);
        }
    }

    private void callClose() {
        if (onClose != null) {
            loop.callbackHandler.handleStreamCloseCallback(onClose);
        }
    }

    private void callShutdown(final int status, final Exception error) {
        if (onShutdown != null) {
            loop.callbackHandler.handleStreamShutdownCallback(onShutdown, status, error);
        }
    }

    private static native void _static_initialize();

    private native void _initialize(final long ptr);

    private native void _read_start(final long ptr);

    private native void _read2_start(final long ptr);

    private native void _read_stop(final long ptr);

    private native boolean _readable(final long ptr);

    private native boolean _writable(final long ptr);

    private native int _write(final long ptr,
                              final byte[] data,
                              final int offset,
                              final int length);

    private native int _write2(final long ptr,
                               final byte[] data,
                               final int offset,
                               final int length,
                               final long handlePointer);

    private native long _write_queue_size(final long ptr);

    private native void _close(final long ptr);

    private native int _close_write(final long ptr);

    private native int _listen(final long ptr, final int backlog);

    private native int _accept(final long ptr, final long client);

}
