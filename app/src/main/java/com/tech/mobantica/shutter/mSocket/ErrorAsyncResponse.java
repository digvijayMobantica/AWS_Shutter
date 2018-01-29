package com.tech.mobantica.shutter.mSocket;

interface ErrorAsyncResponse {
    <T extends Throwable> void processFinish(T output);
}
