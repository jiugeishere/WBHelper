package number.nine.wbhelper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;


import androidx.core.app.ActivityCompat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class WAPI {

    private static WAPI wapis = null;
    private Context wcontext;

    private WifiManager wifiManager;
    private WIFIBroadcastReceiver mBroadcastReceiver=new WIFIBroadcastReceiver();

    public WAPI(Context context) {
        this.wcontext=context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();//进入时便开始扫描，减少后续扫描时间
        registerBroadcast(true);//默认注册广播
    }


    /**
     * wifi单例模式
     * @param context
     * @return
     */
    public static WAPI getInstance(Context context) {
        if (wapis == null) {
            synchronized (WAPI.class) {
                if (wapis == null) {
                    wapis = new WAPI(context);
                }
            }
        }
        return wapis;
    }

    /**
     * 监听wifi状态的广播
     * SCAN_RESULTS_AVAILABLE_ACTION为扫描wifi动作
     */
    private void registerBroadcast(boolean on){
        if (on){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            wcontext.registerReceiver(mBroadcastReceiver,intentFilter);
        }else {
            wcontext.unregisterReceiver(mBroadcastReceiver);
        }
    }

    /**
     * wifi是否打开
     * @return
     */
    public boolean isWifiEnable() {
        boolean isEnable = false;
        if (wifiManager != null) {
            if (wifiManager.isWifiEnabled()) {
                isEnable = true;
            }
        }
        return isEnable;
    }

    /**
     * 打开WiFi
     */
    public void openWifi() {
        if (wifiManager != null && !isWifiEnable()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭WiFi
     */
    public void closeWifi() {
        if (wifiManager != null && isWifiEnable()) {
            wifiManager.setWifiEnabled(false);
        }
    }


    /**
     * 适配Andoroid获取wifi列表需要定位权限问题
     * @param activity
     */
    private boolean checkPermission(Activity activity){
        //动态获取定位权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && wcontext.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    100);
            return false;
        } else {
            return true;
        }
    }
    /**
     * 获取WiFi列表,如果用户不给定位权限
     * @return
     */
    public List<ScanResult> getWifiList(){
        List<ScanResult> resultList = new ArrayList<>();
        if (checkPermission((Activity)wcontext)){
            if (wifiManager != null && isWifiEnable()) {
                resultList.addAll(wifiManager.getScanResults());
            }
        }
        return resultList;
    }

    /**
     * 有密码连接
     * @param ssid
     * @param pws
     */
    public void connectWifiPws(String ssid, String pws) {
        wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        int netId = wifiManager.addNetwork(getWifiConfig(ssid, pws, true));
        wifiManager.enableNetwork(netId, true);
    }

    /**
     * 无密码连接
     * @param ssid
     */
    public void connectWifiNoPws(String ssid) {
        wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        int netId = wifiManager.addNetwork(getWifiConfig(ssid, "", false));
        wifiManager.enableNetwork(netId, true);
    }

    /**
     * wifi设置,用以wifi连接
     * @param ssid
     * @param pws
     * @param isHasPws
     */
    private WifiConfiguration getWifiConfig(String ssid, String pws, boolean isHasPws) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = isExist(ssid);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }
        if (isHasPws) {
            config.preSharedKey = "\"" + pws + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }



    /**
     * 得到配置好的网络连接(连接前确认wifi权限
     * @param ssid
     * @return
     */
    private WifiConfiguration isExist(String ssid) {
        if (ActivityCompat.checkSelfPermission(wcontext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\""+ssid+"\"")) {
                return config;
            }
        }
        return null;
    }

}