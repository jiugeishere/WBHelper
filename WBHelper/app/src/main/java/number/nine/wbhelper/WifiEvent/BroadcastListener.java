package number.nine.wbhelper.WifiEvent;

import android.net.wifi.ScanResult;

import java.util.List;

public interface BroadcastListener {
    void getMessage(String message);
    void getConnection(String connection);
    void getRefreshWifiList(List<ScanResult> scanResults);
}
