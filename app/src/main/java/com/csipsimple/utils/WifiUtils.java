package com.csipsimple.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.WIFI_SERVICE;

public class WifiUtils {
    public static final String TAG = "WifiUtils";
    public static int WIFI_CIPHER_NOPASS = 0;
    public static int WIFI_CIPHER_WEP = 1;
    public static int WIFI_CIPHER_WPA_OR_WPA2 = 2;

    /**
     * 创建 WifiConfiguration，这里创建的是wpa2加密方式的wifi
     *
     * @param ssid     wifi账号
     * @param password wifi密码
     * @return
     */
    public static WifiConfiguration createWifiInfo(Context context, String ssid, String password, int type) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        WifiConfiguration tempConfig = isExsits(context, ssid);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";
        if (type == WIFI_CIPHER_NOPASS) {//WIFICIPHER_NOPASS
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == WIFI_CIPHER_WEP) {//WIFI_CIPHER_WEP
            config.wepKeys[0] = password;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFI_CIPHER_WPA_OR_WPA2) { //WIFI_CIPHER_WPA_OR_WPA2
            config.preSharedKey = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    public static int connectWifiApByName(Context context, String wifiApName, String password, int type) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiConfiguration wifiNewConfiguration = createWifiInfo(context, wifiApName, password, type);//使用wpa2的wifi加密方式
        int newNetworkId = mWifiManager.addNetwork(wifiNewConfiguration);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        if (newNetworkId == -1) {
            Log.e(TAG, "操作失败,需要您到手机wifi列表中取消对设备连接的保存");
            return -1;
        }
        Log.i(TAG, "newNetworkId is:" + newNetworkId);
        // 如果wifi权限没打开（1、先打开wifi，2，使用指定的wifi
        boolean enableNetwork = mWifiManager.enableNetwork(newNetworkId, true);
        if (!enableNetwork) {
            Log.e(TAG, "切换到指定wifi失败");
            return -1;
        } else {
            Log.e(TAG, "切换到指定wifi成功");
            return newNetworkId;
        }
    }

    // 打开WIFI
    public static void openWifi(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        } else if (mWifiManager.getWifiState() == 2) {
            Toast.makeText(context, "亲，Wifi正在开启，不用再开了", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "亲，Wifi已经开启,不用再开了", Toast.LENGTH_SHORT).show();
        }
    }

    // 关闭wifi
    public static void closeWifi(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        } else if (mWifiManager.getWifiState() == 1) {
            Toast.makeText(context, "亲，Wifi已经关闭，不用再关了", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == 0) {
            Toast.makeText(context, "亲，Wifi正在关闭，不用再关了", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "请重新关闭", Toast.LENGTH_SHORT).show();
        }
    }


    private static WifiConfiguration isExsits(Context context, String SSID) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        java.util.List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if(existingConfigs!=null){
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                    return existingConfig;
                }
            }
        }

        return null;
    }
}