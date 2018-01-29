package com.tech.mobantica.shutter.mSocket;

import android.database.sqlite.SQLiteException;

import java.io.IOException;
import java.io.Serializable;

public class Host implements Serializable {

    private String hostname;
    private String ip;
    private String mac;
    private String vendor;

    public Host(String ip, String mac, Database db) throws IOException {
        this(ip, mac);
        setVendor(db);
    }

    public Host(String ip, String mac) {
        this.ip = ip;
        this.mac = mac;
    }

    public String getHostname() {
        return hostname;
    }

    public Host setHostname(String hostname) {
        this.hostname = hostname;

        return this;
    }

    private Host setVendor(Database db) throws IOException {
        vendor = findMacVendor(mac, db);

        return this;
    }

    public String getVendor() {
        return vendor;
    }


    public String getIp() {
        return ip;
    }

    public String getMac() {
        return mac;
    }

    public static String findMacVendor(String mac, Database db) throws IOException, SQLiteException {
        String prefix = mac.substring(0, 8);
        return db.selectVendor(prefix);
    }

}
