package number.nine.wbhelper.wadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wbhelper.R;

import java.util.List;

import number.nine.wbhelper.WAPI;
import number.nine.wbhelper.WifiEvent.BroadcastBus;
import number.nine.wbhelper.WifiEvent.BroadcastListener;

/**
 * 适配器模板
 * 可用来参考wifi列表的自动刷新
 * 也可直接使用
 */
public class Wadapter extends BaseAdapter implements BroadcastListener {
    private List<ScanResult> wdata;
    private Context wcontext;

    public Wadapter(List<ScanResult> scanResults, Context context){
        this.wdata=scanResults;
        this.wcontext=context;
        initListener();
    }

    public Wadapter(Context context){
        this.wcontext=context;
        initListener();
    }

    private void initListener(){
        BroadcastBus.getDefault().register(this);
        if (WAPI.getInstance(wcontext).getRefreshflag()){
            WAPI.getInstance(wcontext).startWifiRefresh();
        }
    }

    @Override
    public int getCount() {
        if (wdata==null){
            return 0;
        }
        return wdata.size();
    }

    @Override
    public Object getItem(int position) {
        return wdata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if (convertView==null){
            LayoutInflater layoutInflater=LayoutInflater.from(wcontext);
            convertView=layoutInflater.inflate(R.layout.wifi_item,null);
            holder=new ViewHolder();
            holder.connect_img=(ImageView)convertView.findViewById(R.id.wifi_connect_icon);
            holder.show_img=(ImageView)convertView.findViewById(R.id.wifi_icon);
            holder.wifiname_text=(TextView)convertView.findViewById(R.id.wifi_name);
            holder.wifiMac_text=(TextView)convertView.findViewById(R.id.wifi_mac);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        ScanResult showscanResult=wdata.get(position);
        holder.wifiname_text.setText(showscanResult.SSID);
        holder.wifiMac_text.setText(showscanResult.BSSID);
        if (showscanResult.level>-40){
            holder.show_img.setImageResource(R.drawable.wifi_5);
        }else if (showscanResult.level>-60){
            holder.show_img.setImageResource(R.drawable.wifi_4);
        }else if (showscanResult.level>-70){
            holder.show_img.setImageResource(R.drawable.wifi_3);
        }else if (showscanResult.level>-80){
            holder.show_img.setImageResource(R.drawable.wifi_2);
        }else if (showscanResult.level>-90){
            holder.show_img.setImageResource(R.drawable.wifi_1);
        }
        return convertView;
    }

    @Override
    public void getMessage(String message) {

    }

    @Override
    public void getConnection(String connection) {

    }

    /**
     * 刷新wifi列表方法
     * @param scanResults
     */
    @Override
    public void getRefreshWifiList(List<ScanResult> scanResults) {
        wdata=scanResults;
        notifyDataSetChanged();
    }

    static class ViewHolder{
        protected ImageView connect_img;
        protected ImageView show_img;
        protected TextView wifiname_text;
        protected TextView wifiMac_text;
    }
}
