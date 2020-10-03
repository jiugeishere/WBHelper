package number.nine.wbhelper.wadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wbhelper.R;

import java.util.List;

public class Wadapter extends BaseAdapter {
    private List<ScanResult> wdata;
    private Context wcontext;
    public Wadapter(List<ScanResult> scanResults, Context context){
        this.wdata=scanResults;
        this.wcontext=context;
    }

    @Override
    public int getCount() {
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
            holder.connect_img=(ImageView)convertView.findViewById(R.id.wifi_connect_icon);
            holder.wifiname_text=(TextView)convertView.findViewById(R.id.wifi_name);
            holder.wifiMac_text=(TextView)convertView.findViewById(R.id.wifi_mac);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        ScanResult showscanResult=wdata.get(position);
        holder.wifiname_text.setText(showscanResult.SSID);
        holder.wifiMac_text.setText(showscanResult.BSSID);
        return null;
    }
  static class ViewHolder{
        protected ImageView connect_img;
        protected TextView wifiname_text;
        protected TextView wifiMac_text;
    }


}
