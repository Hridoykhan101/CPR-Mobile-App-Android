package com.bluetooth.pa2123.resus;

import java.util.concurrent.Callable;

public abstract class CallableParam<T> implements Callable<Void> {
    public T param = null;
    public int responseCode = 0;

    public void setParam (T param) {
        this.param = param;
    }
    public void setResponseCode (int code) {this.responseCode = code;}

    public abstract Void call () throws Exception;
}