package number.nine.wbhelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.example.wbhelper.R;

import java.util.List;

import number.nine.wbhelper.WifiEvent.BroadcastBus;
import number.nine.wbhelper.WifiEvent.BroadcastListener;
import number.nine.wbhelper.wadapter.Wadapter;

public class MainActivity extends AppCompatActivity implements BroadcastListener {

    private Button button;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.wifi_button);
        listView=findViewById(R.id.list);
        Wadapter wadapter=new Wadapter(this);
        listView.setAdapter(wadapter);
        WAPI.getInstance(this).openWifi();
        BroadcastBus.getDefault().register(this);
        WAPI.getInstance(this).startWifiRefresh();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ScanResult> list= WAPI.getInstance(MainActivity.this).getWifiList();
                Log.e("Wifi","Size"+list.size());
                for (ScanResult scanResult:list){
                    Log.e("Wifi","BSSID"+scanResult.BSSID+"SSID"+scanResult.SSID+"  level"+scanResult.level);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastBus.getDefault().unregister(this);
    }

    @Override
    public void getMessage(String message) {
        Log.e("Wifi",message);
    }

    @Override
    public void getConnection(String connection) {
        Log.e("WifiConnection",connection);
    }

    @Override
    public void getRefreshWifiList(List<ScanResult> scanResults) {
        Log.e("Wifi","Size"+scanResults.size());
        for (ScanResult scanResult:scanResults){
            Log.e("WifiRefreshWifiList","BSSID"+scanResult.BSSID+"SSID"+scanResult.SSID+"capabilities"+scanResult.capabilities);
        }
    }
}