package com.chatroom.task;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.TaskStackBuilder;

import com.chatroom.R;
import com.chatroom.activity.SplashScreen;
import com.chatroom.model.Message;
import com.chatroom.util.Helper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackgroundService extends Service {

    FirebaseUser mUser;
    HashMap<String, Set<String>> map;

    public BackgroundService() {
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        map = new HashMap<String, Set<String>>() {
            @Override
            public Set<String> get(Object key) {
                Set<String> set = super.get(key);
                return set == null ? new HashSet<String>() : set;
            }

        };

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mUser != null) {

            DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(mUser.getUid());
            chatsRef.keepSynced(true);

            chatsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String uid = dataSnapshot.getRef().getKey();
                    getUnreadMessages(uid);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        return START_STICKY;
    }

    private void getUnreadMessages(final String uid) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("Messages")
                .child(mUser.getUid())
                .child(uid);
        messagesRef.keepSynced(true);

        Query currentUserMessages = messagesRef.orderByChild("recipientUID").equalTo(mUser.getUid());
        currentUserMessages.keepSynced(true);

        currentUserMessages.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);
                if (message != null && message.getStatus().equals("sent")) {

                    Set<String> set = map.get(uid);
                    set.add(dataSnapshot.getRef().getKey());
                    map.put(uid, set);

                    createNotification();
                    message.setStatus("delivered");
                    Helper.setMessageStatus(dataSnapshot.getKey(), message);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);
                if (message != null && message.getStatus().equals("seen")) {
                    Set<String> set = map.get(uid);
                    set.remove(dataSnapshot.getRef().getKey());
                    map.put(uid, set);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    Set<String> set = map.get(uid);
                    set.remove(dataSnapshot.getRef().getKey());
                    map.put(uid, set);
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void createNotification() {
        Intent intent = new Intent(this, SplashScreen.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(SplashScreen.class);
        taskStackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 2727, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);



        builder.setContentTitle(getResources().getString(R.string.app_name));
        int size = 0;
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            size += entry.getValue().size();
        }
        builder.setContentText(size + " messages from " + map.size() + " chats");
        builder.setSmallIcon(R.drawable.ic_stat_name);
        builder.setColor(getResources().getColor(R.color.colorPrimaryDark));

        Notification notification = new Notification();

        if (!isApplicationInForeground()) {
            builder.setPriority(Notification.PRIORITY_HIGH);
            notification.defaults |= Notification.DEFAULT_SOUND;
        }

        notification.defaults |= Notification.DEFAULT_VIBRATE;

        builder.setDefaults(notification.defaults);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(27, builder.build());
        }
    }

    public boolean isApplicationInForeground() {

        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        if (am != null) {
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
            PackageManager pm = this.getPackageManager();
            PackageInfo foregroundAppPackageInfo;
            try {
                foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
            String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();
            return foregroundTaskAppName.equals(getString(R.string.app_name));
        }
        return false;
    }
}
