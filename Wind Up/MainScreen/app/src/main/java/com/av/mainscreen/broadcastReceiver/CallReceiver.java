package com.av.mainscreen.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

/**
 * Created by Ankit on 17-11-2018.
 */

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    public static long INCOMING_callStartTime = -1;
    private static long INCOMING_callEndTime = -1;
    private static long OUTGOING_callStartTime = -1;
    private static long OUTGOING_callEndTime = -1;
    private static boolean isIncoming;
    public static String savedNumber;  //because the passed incoming is only valid in ringing
    public static long lastAnsweredCall;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: CALL");
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
            }
            onCallStateChanged(context, state, number);
        }
    }

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state)//No change, debounce extras
            return;

        switch (state) {

            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                INCOMING_callStartTime = System.currentTimeMillis();
                savedNumber = number;
                Log.e(TAG, "onCallStateChanged: Incoming call received " + number);
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    OUTGOING_callStartTime = System.currentTimeMillis();
                    Log.e(TAG, "onCallStateChanged: OutgoingCall Started");
                } else {
                    isIncoming = true;
                    lastAnsweredCall=INCOMING_callStartTime;
                    Log.e(TAG, "onCallStateChanged: Incoming answered");
                }
                break;

            case TelephonyManager.CALL_STATE_IDLE:
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    Log.e(TAG, "onCallStateChanged: MissedCall");
                    INCOMING_callEndTime = System.currentTimeMillis();
                } else if (isIncoming) {
                    INCOMING_callEndTime = System.currentTimeMillis();
                    Log.e(TAG, "onCallStateChanged: IncomingCall Ended");
                } else {
                    OUTGOING_callEndTime = System.currentTimeMillis();
                    Log.e(TAG, "onCallStateChanged: OutgoingCall Ended");
                }
                break;
        }
        lastState = state;
    }

    /*Check outgoing call*/
    public static boolean isOutgoing() {
        long s = OUTGOING_callStartTime, e = OUTGOING_callEndTime, c = System.currentTimeMillis();
        if (s == -1 && e == -1)
            return false;
        else if (c > s && s > e)
            return true;
        return false;
    }

    /*Check incoming call*/
    public static boolean isIncoming() {
        long s = INCOMING_callStartTime, e = INCOMING_callEndTime, c = System.currentTimeMillis();
        if (s == -1 && e == -1)
            return false;
        else if (c > s && s > e)
            return true;
        return false;
    }
}
