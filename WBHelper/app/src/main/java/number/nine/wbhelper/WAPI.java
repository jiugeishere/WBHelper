package number.nine.wbhelper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;


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
    private WIFIBroadcastReceiver mBroadcastReceiver=new WIFIBroadcastReceiver();
    // 定义一个WifiLock
    private WifiManager.WifiLock mWifiLock;
    private static boolean wThreadflag=true;
    public WAPI(Context context) {
        this.wcontext=context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();//进入时便开始扫描，减少后续扫描时间
        registerBroadcast(true);//默认注册广播
    }

    /**
     * wifi线程，用于异步不断刷新wifi列表，活性执行wifi列表刷新
     */
    @SuppressLint("HandlerLeak")
    Handler whandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 6562) {
                BroadcastBus.getDefault().postScanlist((List<ScanResult>) msg.obj);
            }
        }
    };

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
     * 关闭线程刷新
     */
    public void closeRefresh(){
        wThreadflag=false;
    }

    /**
     * 开启线程刷新list，默认4秒刷新一轮
     */
    public void startWifiRefresh(){
      startRefreshThreadl(4000);
    }

    /**
     * 开启线程刷新list,可自定义时间
     */
    public void startWifiRefresh(int refreshtime){
        startRefreshThreadl(refreshtime);
    }

    private void startRefreshThreadl(final int refreshtime){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (wThreadflag){
                    Message message = new Message();
                    message.what = 6562;
                    message.obj= getWifiList();
                    whandler.sendMessage(message);
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
     * @return
     */
    public List<ScanResult> getWifiList(){
        List<ScanResult> resultList = new ArrayList<>();
        if (checkPermission((Activity)wcontext)&&wifiManager != null && isWifiEnable()){
            resultList.addAll(wifiManager.getScanResults());
        }
        return resultList;
    }

    // 创建一个WifiLock,避免后台休眠被打断,默认创建就锁定
    public void creatWifiLock(String tag) {
        if (tag==null){
            tag="WifiLock";
        }
        mWifiLock = wifiManager.createWifiLock(tag);
        acquireWifiLock();
    }

    // 创建一个WifiLock,避免后台休眠被打断,默认创建WifiLock
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
