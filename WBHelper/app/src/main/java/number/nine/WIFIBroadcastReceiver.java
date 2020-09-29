package number.nine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;

import number.nine.wbhelper.WifiEvent.BroadcastBus;

/**
 * WifiBroadcastReceiver广播接收器用于接收WiFi状态为主
 * 可用来监听目前系统的一个WiFi状态和情况
 */
public class WIFIBroadcastReceiver extends BroadcastReceiver {

    public static final int CONNECTEDSTATE=1;//已连接状态
    public static final int PSDWRONGSTATE=2;//密码错误状态
    public static final int FAILEDSTATE=3;//连接失败状态
    public static final int SUCCESSSTATE=4;//成功连接状态

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            //密码错误广播,是不是正在获得IP地址
            int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                //密码错误
                sendNetworkStateChange(PSDWRONGSTATE);
            }
            SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(supplicantState);
            if (state == NetworkInfo.DetailedState.CONNECTING) {
                BroadcastBus.getDefault().postConnection("connecting");
                //正在连接
            } else if (state == NetworkInfo.DetailedState.FAILED
                    || state == NetworkInfo.DetailedState.DISCONNECTING) {
                BroadcastBus.getDefault().postConnection("fail to connect");
                sendNetworkStateChange(FAILEDSTATE);
                //连接失败
            } else if (state == NetworkInfo.DetailedState.CONNECTED) {
                //连接成功
                BroadcastBus.getDefault().postConnection("success to connect");
                sendNetworkStateChange(SUCCESSSTATE);
            } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                //正在获取ip地址
                BroadcastBus.getDefault().postConnection("getting ipaddress");
            }
        } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            // 监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLING://正在停止0
                    BroadcastBus.getDefault().postConnection("stoping");
                    break;
                case WifiManager.WIFI_STATE_DISABLED://已停止1
                    BroadcastBus.getDefault().postConnection("stoped");
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN://未知4
                    BroadcastBus.getDefault().postConnection("unkonwn");
                    break;
                case WifiManager.WIFI_STATE_ENABLING://正在打开2
                    BroadcastBus.getDefault().postConnection("opening");
                    break;
                case WifiManager.WIFI_STATE_ENABLED://已开启3
                    BroadcastBus.getDefault().postConnection("opened");
                    break;
                default:
                    break;
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            // 监听wifi的连接状态即是否连上了一个有效无线路由
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                // 获取联网状态的NetWorkInfo对象
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                //获取的State对象则代表着连接成功与否等状态
                NetworkInfo.State state = networkInfo.getState();
                //判断网络是否已经连接
                boolean isConnected = state == NetworkInfo.State.CONNECTED;
                if (isConnected) {
                    BroadcastBus.getDefault().postConnection("connected");
                    sendNetworkStateChange(CONNECTEDSTATE);
                }
            }
        }
    }

    /**
     * 发送网络状态eventBus.
     *
     * @param state
     */
    private <T extends Serializable> void sendNetworkStateChange(T state) {
        BroadcastBus.getDefault().postMessage(state);
        Log.e("TestConnection",String.valueOf(state));
    }


}