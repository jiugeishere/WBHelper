package number.nine.wbhelper.WifiEvent;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BroadcastBus {
    static BroadcastBus broadcastBus;//异步全局消息回传，模仿RxBus
    static List<BroadcastListener> listeners = new ArrayList<>();//添加监听器到列表中

    private BroadcastBus() {

    }
    //获取广播单例
    public static BroadcastBus getDefault() {
        if (broadcastBus == null) {
            return new BroadcastBus();
        } else {
            return broadcastBus;
        }
    }
    //注册广播
    public void register(BroadcastListener eventListener) {
        listeners.add(eventListener);
    }
    //取消广播注册
    public void unregister(BroadcastListener eventListener) {
        listeners.remove(eventListener);
    }
    //以序列化的方式传过去，泛型范围为Serializable的子类，传递信息状态
    public <T extends Serializable> void postMessage(T message){
        for (BroadcastListener eventListener : listeners) {
            eventListener.getMessage(String.valueOf(message));
        }
    }
    //传递连接状态
    public void postConnection(String connection){
        Log.e("TestConnection",connection);
        for (BroadcastListener eventListener : listeners) {
            eventListener.getConnection(connection);
        }
    }

}
