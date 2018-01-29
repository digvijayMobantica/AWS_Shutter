package com.tech.mobantica.shutter.mSocket;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ScanHostsRunnable implements Runnable {
    private int start;
    private int stop;
    private int timeout;
    private final WeakReference<MainAsyncResponse> delegate;
    public ScanHostsRunnable(int start, int stop, int timeout, WeakReference<MainAsyncResponse> delegate) {
        this.start = start;
        this.stop = stop;
        this.timeout = timeout;
        this.delegate = delegate;
    }

    @Override
    public void run() {
        for (int i = start; i <= stop; i++) {
            Socket socket = new Socket();
            try {
                socket.setTcpNoDelay(true);
                byte[] bytes = BigInteger.valueOf(i).toByteArray();
                socket.connect(new InetSocketAddress(InetAddress.getByAddress(bytes), 7), timeout);
            } catch (IOException ignored) {
                // Connection failures aren't errors in this case.
                // We want to fill up the ARP table with our connection attempts.
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Something's really wrong if we can't close the socket...
                }

                MainAsyncResponse activity = delegate.get();
                if (activity != null) {
                    activity.processFinish(1);
                }
            }
        }
    }
}