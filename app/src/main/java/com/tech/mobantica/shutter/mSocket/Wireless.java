package com.tech.mobantica.shutter.mSocket;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class Wireless {

    private Context context;

    public static class NoWifiManagerException extends Exception {
    }

    public static class NoConnectivityManagerException extends Exception {
    }


    public Wireless(Context context) {
        this.context = context;
    }

    private InetAddress getWifiInetAddress() throws UnknownHostException, NoWifiManagerException {
        String ipAddress = getInternalWifiIpAddress(String.class);
        return InetAddress.getByName(ipAddress);
    }

    public <T> T getInternalWifiIpAddress(Class<T> type) throws UnknownHostException, NoWifiManagerException {
        int ip = getWifiInfo().getIpAddress();

        //Endianness can be a potential issue on some hardware
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }

        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();


        if (type.isInstance("")) {
            return type.cast(InetAddress.getByAddress(ipByteArray).getHostAddress());
        } else {
            return type.cast(new BigInteger(InetAddress.getByAddress(ipByteArray).getAddress()).intValue());
        }

    }

    public int getInternalWifiSubnet() throws NoWifiManagerException {
        WifiManager wifiManager = getWifiManager();
        if (wifiManager == null) {
            return 0;
        }

        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo == null) {
            return 0;
        }

        int netmask = Integer.bitCount(dhcpInfo.netmask);

        if (netmask < 4 || netmask > 32) {
            try {
                InetAddress inetAddress = getWifiInetAddress();
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
                if (networkInterface == null) {
                    return 0;
                }

                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                    if (inetAddress != null && inetAddress.equals(address.getAddress())) {
                        return address.getNetworkPrefixLength(); // This returns a short of the CIDR notation.
                    }
                }
            } catch (SocketException | UnknownHostException ignored) {
            }
        }

        return netmask;
    }



    public int getNumberOfHostsInWifiSubnet() throws NoWifiManagerException {
        Double subnet = (double) getInternalWifiSubnet();
        double hosts;
        double bitsLeft = 32.0d - subnet;
        hosts = Math.pow(2.0d, bitsLeft) - 2.0d;

        return (int) hosts;
    }

    public boolean isConnectedWifi() throws NoConnectivityManagerException {
        NetworkInfo info = getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.isConnectedOrConnecting();
    }

    public boolean isEnabled() throws NoWifiManagerException {
        return getWifiManager().isWifiEnabled();
    }

    private WifiManager getWifiManager() throws NoWifiManagerException {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager == null) {
            throw new NoWifiManagerException();
        }

        return manager;
    }


    private WifiInfo getWifiInfo() throws NoWifiManagerException {
        return getWifiManager().getConnectionInfo();
    }

    private ConnectivityManager getConnectivityManager() throws NoConnectivityManagerException {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            throw new NoConnectivityManagerException();
        }

        return manager;
    }

    private NetworkInfo getNetworkInfo(int type) throws NoConnectivityManagerException {
        return getConnectivityManager().getNetworkInfo(type);
    }

}
