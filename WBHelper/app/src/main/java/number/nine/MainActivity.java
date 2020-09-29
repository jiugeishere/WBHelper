package number.nine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.wbhelper.R;

import java.util.List;

import number.nine.wbhelper.WifiEvent.BroadcastBus;
import number.nine.wbhelper.WifiEvent.BroadcastListener;

public class MainActivity extends AppCompatActivity implements BroadcastListener {

    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.wifi_button);
        WAPI.getInstance(this).openWifi();
        BroadcastBus.getDefault().register(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerPermission();
            }
        });

    }

    private void registerPermission(){
        //动态获取定位权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    100);

        } else {
            List<ScanResult> list= WAPI.getInstance(MainActivity.this).getWifiList();
            Log.e("Test","Size"+list.size());
            for (ScanResult scanResult:list){
                Log.e("Test","BSSID"+scanResult.BSSID+"SSID"+scanResult.SSID+"capabilities"+scanResult.capabilities);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            List<ScanResult> list= WAPI.getInstance(MainActivity.this).getWifiList();
            Log.e("Test","Size"+list.size());
            for (ScanResult scanResult:list){
                Log.e("Test","BSSID"+scanResult.BSSID+"\n        SSID"+scanResult.SSID+"\n           capabilities"+scanResult.capabilities);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastBus.getDefault().unregister(this);
    }

    @Override
    public void getMessage(String message) {
        Log.e("Test",message);
    }

    @Override
    public void getConnection(String connection) {
        Log.e("TestConnection",connection);
    }
}