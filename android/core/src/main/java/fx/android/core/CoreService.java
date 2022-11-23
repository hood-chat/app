package fx.android.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import bridge.Bridge;
import bridge.Bridge_;
import bridge.HostConfig;


public class CoreService extends Service {
    private final String NAME = "CoreService";
    private final String VERSION = "0.0.2";
    Bridge_ core = null;
    String appDir;
    String storeDirPath;
    String path;
    final RemoteCallbackList<ICoreServiceCallback> mCallbacks
            = new RemoteCallbackList<ICoreServiceCallback>();

    int mValue = 0;
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("IsolatedService", "Task removed in " + this + ": " + rootIntent);
        stopSelf();
    }

    private void broadcastMessageStatus(String id, String status) {
        // Broadcast to all clients the new value.
        final int N = mCallbacks.beginBroadcast();
        for (int i=0; i<N; i++) {
            try {
                mCallbacks.getBroadcastItem(i).msgChanged(id, status);
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
            }
        }
        mCallbacks.finishBroadcast();
    }

    public class LocalBinder extends Binder {
        CoreService getService() {
            return CoreService.this;
        }
    }

    synchronized void createHost(){
        Context context = getApplicationContext();
        appDir = context.getFilesDir().toString();
        storeDirPath = appDir + "/bee/received/";
        path = appDir + "/bee";
        File storeDir = new File(storeDirPath);
        boolean success = false;
        if (!storeDir.exists()) {
            success = storeDir.mkdirs();
        }
        if(success){
            Log.d(NAME,"store folder created");
        }else{
            Log.d(NAME,"store exist");
        }
        try {
            Log.i(NAME, "core service stared");
            HostConfig hostConfig = Bridge.newHostConfig();
            // set net driver
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                NetDriver inet = new NetDriver();
                hostConfig.setNetDriver(inet);
            }
            this.core = Bridge.newBridge(path, hostConfig);
            String s1 = this.core.getInterfaces();
            Log.d(NAME, "driver" + s1);
        } catch (Exception e) {
            Log.e(NAME,"failed to start core service", e);
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        if (core == null){
            createHost();
        }
        Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_LONG).show();
        // Display a notification about us starting.  We put an icon in the status bar.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (core == null){
            createHost();
        }
//        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Log.i(NAME, "Received start id " + startId + ": " + intent);
//        Toast.makeText(this, "Background Service Created", Toast.LENGTH_LONG).show();
//        showNotification();
        return START_REDELIVER_INTENT;

    }
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
//        mNM.cancel(NOTIFICATION);
        // Tell the user we stopped.
        Log.i(NAME, "core service destroyed");
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(NAME, "client requested for bind");
        if (core == null){
            createHost();
        }
        return binder;
    }
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private final ICoreService.Stub binder = new ICoreService.Stub() {
        public void registerCallback(ICoreServiceCallback cb) {
            if (cb != null) mCallbacks.register(cb);
        }
        public void unregisterCallback(ICoreServiceCallback cb) {
            if (cb != null) mCallbacks.unregister(cb);
        }

        public boolean addContact(String id, String name) throws RemoteException {
            try {
                core.addContact(id, name);
                return true;
            } catch (Exception e) {
                Log.e("CoreService", "can not add contact", e);
                return false;
            }
        }

        public String getChat(String id) throws RemoteException {
            try {
                String res = core.getChat(id);
                return res;
            } catch (Exception e) {
                Log.e(NAME, "can not retrieve chat", e);
                return null;
            }
        }

        public String getChats() throws RemoteException {
            try {
                Log.d(NAME, core.getChats());
                broadcastMessageStatus("1","new");
                return core.getChats();
            } catch (Exception e) {
                Log.e(NAME, "can not retrieve chat list", e);
                return null;
            }
        }

        public String getContact(String id) throws RemoteException {
            try {
                return core.getContact(id);
            } catch (Exception e) {
                Log.e(NAME, "can not retrieve contact", e);
                return null;
            }
        }

        public String getContacts() throws RemoteException {
            try {
                return core.getContacts();
            } catch (Exception e) {
                Log.e(NAME, "can not retrieve contact list", e);
                throw new RemoteException(e.getMessage());
            }
        }

        public String getIdentity() throws RemoteException {
            try {
                return core.getIdentity();
            } catch (Exception e) {
                Log.e(NAME, "can not retrieve identity", e);
                return null;
            }
        }

        public String newIdentity(String name) throws RemoteException {
            try {
                return core.newIdentity(name);
            } catch (Exception e) {
                Log.e(NAME, "can not retrieve identity", e);
                return null;
            }
        }

        public String getMessages(String id) throws RemoteException {
            try {
                return core.getMessages(id);
            } catch (Exception e) {
                Log.e(NAME, "can not retrieve messages", e);
                return null;
            }
        }

        public boolean isLogin() throws RemoteException {
            try {
                return core.isLogin();
            } catch (Exception e) {
                Log.e(NAME, "can not call is login", e);
                return false;
            }
        }

        public String newPMChat(String contactID) throws RemoteException {
            try {
                return core.newPMChat(contactID);
            } catch (Exception e) {
                Log.e(NAME, "failed to create new chat", e);
                return null;
            }
        }

        public String getPMChat(String contactID) throws RemoteException {
            try {
                return core.getPMChat(contactID);
            } catch (Exception e) {
                Log.e(NAME, "failed to create new chat", e);
                return null;
            }
        }

        public String sendMessage(String chatID, String text) throws RemoteException {
            Log.d(NAME, "send called");
            try {
                return core.sendMessage(chatID, text);
            } catch (Exception e) {
                Log.e(NAME, "can not send message", e);
                return null;
            }
        }
    };

}