package number.nine.wbhelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import number.nine.wbhelper.WifiEvent.BroadcastBus;

public class WAPI {

    private static WAPI wapis = null;
    private Context wcontext;

    private WifiManager wifiManager;
    private WIFIBroadcastReceiver mBroadcastReceiver = new WIFIBroadcastReceiver();
    // 定义一个WifiLock
    private WifiManager.WifiLock mWifiLock;
    //自动刷新控制值
    private static boolean wThreadflag = true;
    //没有打开定位服务是否自动调转
    private static boolean autoOpenLocation=false;
    //当前定位服务状态
    private  boolean LocationState;

    public WAPI(Context context) {
        this.wcontext = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();//进入时便开始扫描，减少后续扫描时间,此方法在高版本已被废弃
        registerBroadcast(true);//默认注册广播
    }

    /**
     * wifi线程，用于异步不断刷新wifi列表，活性执行wifi列表刷新
     */
    @SuppressLint("HandlerLeak")
    Handler whandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 6562) {
                BroadcastBus.getDefault().postScanlist((List<ScanResult>) msg.obj);
            }
        }
    };

    /**
     * wifiAPI单例模式
     *
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
    private void registerBroadcast(boolean on) {
        if (on) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            wcontext.registerReceiver(mBroadcastReceiver, intentFilter);
        } else {
            wcontext.unregisterReceiver(mBroadcastReceiver);
        }
    }

    /**
     * wifi是否打开
     *
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
     * 打开自动跳转定位服务
     */
    public void autocheckLocation(){
        autoOpenLocation=true;
    }

    /**
     * 获取当前定位服务状态
     * @return
     */
    public boolean getLocationState(){
        LocationManager manager = (LocationManager) wcontext.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGPS || isNetwork || !autoOpenLocation;
    }
    /**
     * 适配Andoroid6.0以上获取wifi列表需要定位权限问题
     *
     * @param activity
     */
    private boolean checkLocationPermission(Activity activity) {
        //动态获取定位权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && wcontext.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);

            if (getLocationState()&&autoOpenLocation){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                wcontext.startActivity(intent);
            }
            return false;
        } else {
            LocationManager manager = (LocationManager) wcontext.getSystemService(Context.LOCATION_SERVICE);
            boolean isGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetwork = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPS && !isNetwork) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                wcontext.startActivity(intent);
            }
            return true;
        }
    }

    /**
     * 关闭线程刷新
     */
    public void closeRefresh() {
        wThreadflag = false;
    }

    /**
     * 开启线程刷新list，默认4秒刷新一轮
     */
    public void startWifiRefresh() {
        startRefreshThreadl(4000);
    }

    /**
     * 开启线程刷新list,可自定义时间
     *
     */
    public void startWifiRefresh(int refreshtime) {
        startRefreshThreadl(refreshtime);
    }

    /**
     * 系统wifi会在10分钟或者5分钟内进行扫描，
     * 一超出就会停止扫描服务，
     * 所以可以通过判断为0（亮屏这些操作也可）的次数来进行wifi扫描
     * 这里默认四次为空就会再次启动扫描服务     * @param refreshtime
     */
    private void startRefreshThreadl(final int refreshtime) {
        wThreadflag = true;
        checkLocationPermission((Activity) wcontext);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int scanWatcher=0;
                while (wThreadflag) {
                    if (getRefreshWifiList().size()==0){
                        scanWatcher++;
                    }else {
                        Message message = new Message();
                        message.what = 6562;
                        message.obj = getRefreshWifiList();
                        whandler.sendMessage(message);
                    }
                    if (scanWatcher>4){
                        wifiManager.startScan();
                        scanWatcher=0;
                    }
                    try {
                        Thread.sleep(refreshtime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    /**
     * 获取WiFi列表,如果用户不给定位权限就返回null
     *
     * @return
     */
    public List<ScanResult> getWifiList() {
        List<ScanResult> resultList = new ArrayList<>();
        if (checkLocationPermission((Activity) wcontext) && wifiManager != null && isWifiEnable()) {
            resultList.addAll(wifiManager.getScanResults());
        }
        return resultList;
    }

    /**
     * 获取线程自动刷新wifi列表，
     * 权限判断在线程执行前判断，
     *
     * @return
     */
    public List<ScanResult> getRefreshWifiList() {
        List<ScanResult> resultList = new ArrayList<>();
        if (wifiManager != null && isWifiEnable()) {
            resultList.addAll(wifiManager.getScanResults());
        }
        return resultList;
    }

    // 创建一个WifiLock,避免后台休眠被打断,默认创建就锁定
    public void creatWifiLock(String tag) {
        if (tag == null) {
            tag = "WifiLock";
        }
        mWifiLock = wifiManager.createWifiLock(tag);
        acquireWifiLock();
    }

    // 创建一个WifiLock,避免后台休眠被打断,默认创建就锁定
    public void creatWifiLock() {
        mWifiLock = wifiManager.createWifiLock("WifiLock");
        acquireWifiLock();
    }

    // 锁定WifiLock
    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    // 解锁WifiLock
    public void releaseWifiLock() {
        // 判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
    }

    /**
     * 有密码连接
     *
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
     *
     * @param ssid
     */
    public void connectWifiNoPws(String ssid) {
        wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        int netId = wifiManager.addNetwork(getWifiConfig(ssid, "", false));
        wifiManager.enableNetwork(netId, true);
    }

    /**
     * wifi设置,用以wifi连接
     *
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
     * 得到配置好的网络连接，连接前确认wifi权限
     *
     * @param ssid
     * @return
     */
    private WifiConfiguration isExist(String ssid) {
        if (ActivityCompat.checkSelfPermission(wcontext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                return config;
            }
        }
        return null;
    }
}
