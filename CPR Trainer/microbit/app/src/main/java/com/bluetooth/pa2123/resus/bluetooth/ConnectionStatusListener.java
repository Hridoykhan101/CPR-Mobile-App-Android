package com.bluetooth.pa2123.resus.bluetooth;
public interface ConnectionStatusListener {

    public void connectionStatusChanged(boolean new_state);
    public void serviceDiscoveryStatusChanged(boolean new_state);

}
