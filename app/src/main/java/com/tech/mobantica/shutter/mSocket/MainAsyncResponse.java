package com.tech.mobantica.shutter.mSocket;

import java.util.concurrent.atomic.AtomicInteger;

public interface MainAsyncResponse extends ErrorAsyncResponse {
    void processFinish(Host h, AtomicInteger i);
    void processFinish(int output);
    void processFinish(String output);
    void processFinish(boolean output);

}
