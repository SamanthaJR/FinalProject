package com.example.bankauthenticator;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;

public class GcmBroadcastReceiver extends BroadcastReceiver{
	static final String TAG = "BankAuthenticator";
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context context;
	public static AppClient mAppClient;
	Button acc;
	Button dec;
	static String loginAccepted = "Wait";

	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d("BroadcastReceiver", "onReceive method called");
		
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        this.context = context;
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            sendNotification("Send error: " + intent.getExtras().toString()
            		);
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            sendNotification("Deleted messages on server: " + intent.getExtras().toString());
        } else {
        	
            new connectTask().execute("");
            
            sendNotification("Connected to Server.");

            Intent myIntent = new Intent(context.getApplicationContext(), ButtonActivity.class);
            myIntent.setClassName("com.example.bankauthenticator", "com.example.bankauthenticator.ButtonActivity");
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(myIntent);
//            
//            while(loginAccepted.equalsIgnoreCase("Wait")){} // TODO BUSY WAITING!!! BAD!!!
//            
//            if(loginAccepted.equalsIgnoreCase("Y")){
//            	mAppClient.sendMessage("Accepted");
//            	loginAccepted = "Wait";
//            } else if (loginAccepted.equalsIgnoreCase("N")){
//            	mAppClient.sendMessage("Declined");
//            	loginAccepted = "Wait";
//            }
//            	
            
//            ("Received: " + intent.getExtras().toString() + "Connected to Server.");
         
        }
        setResultCode(Activity.RESULT_OK);
    }


	// Put the GCM message into a notification and post it.
	public void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("GCM Notification")
//				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	
	public static void sendResponseFromClick(boolean response){
		if(response){
			mAppClient.sendMessage("Accepted");
		} else {
			mAppClient.sendMessage("Declined");
		}
	}
	


	
	public class connectTask extends AsyncTask<String,String,AppClient> {
		 
        @Override
        protected AppClient doInBackground(String... message) {
 
            //we create a TCPClient object and
            mAppClient = new AppClient();
            mAppClient.run();
 
            return null;
        }

    }
}