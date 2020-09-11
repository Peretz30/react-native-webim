package com.reactlibrary;

import java.util.List;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.webimapp.android.sdk.Webim;
import com.webimapp.android.sdk.WebimSession;
import com.webimapp.android.sdk.MessageListener;
import com.webimapp.android.sdk.Message;
import com.webimapp.android.sdk.MessageTracker;
import com.webimapp.android.sdk.WebimLog;

public class WebimModule extends ReactContextBaseJavaModule implements MessageListener {

    private final ReactApplicationContext reactContext;

    private WebimSession session;
    private MessageTracker tracker;

    public WebimModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "Webim";
    }

    private void build(String accountName, String location) {
        session = Webim.newSessionBuilder().setContext(this.reactContext).setAccountName(accountName)
                .setLocation(location).setLogger(new WebimLog() {
                    @Override
                    public void log(String log) {
                        Log.i("WEBIM LOG", log);
                    }
                }, Webim.SessionBuilder.WebimLogVerbosityLevel.VERBOSE).build();
    }

    @ReactMethod
    public void resume(ReadableMap builderData, Promise promise) {

        String accountName = builderData.getString("accountName");
        String location = builderData.getString("location");

        if (session == null) {
            build(accountName, location);
        }

        if (session == null) {
            promise.reject("Unable to build session");
        }
        session.resume();
        session.getStream().startChat();
        session.getStream().setChatRead();
        tracker = session.getStream().newMessageTracker(this);
        promise.resolve("success");

    }

    @ReactMethod
    public void sendMessage(String message, Promise promise) {
        try {
            session.getStream().sendMessage(message);
            promise.resolve("success");
        } catch (Exception e) {
            promise.reject("Send message error");
        }

    }

    @Override
    public void messageAdded(@Nullable Message before, @NonNull Message message) {
        WritableMap msg = Arguments.createMap();
        msg.putMap("msg", messageToJson(message));
        sendEvent("messageAdded", msg);
    }

    @Override
    public void allMessagesRemoved() {
        final WritableMap map = Arguments.createMap();
        sendEvent("allMessagesRemoved", map);
    }

    @Override
    public void messageChanged(@NonNull Message from, @NonNull Message to) {
        final WritableMap map = Arguments.createMap();
        map.putMap("to", messageToJson(to));
        map.putMap("from", messageToJson(from));
        sendEvent("messageChanged", map);
    }

    @Override
    public void messageRemoved(@NonNull Message message) {
        final WritableMap msg = Arguments.createMap();
        msg.putMap("msg", messageToJson(message));
        sendEvent("messageRemoved", msg);
    }

    private MessageTracker.GetMessagesCallback getMessagesCallback(final Callback successCallback) {
        return new MessageTracker.GetMessagesCallback() {
            @Override
            public void receive(@NonNull List<? extends Message> messages) {
                WritableMap response = messagesToJson(messages);
                successCallback.invoke(response);
            }
        };
    }

    @ReactMethod
    public void getLastMessages(int limit, final Callback errorCallback, final Callback successCallback) {
        try {
            tracker.getLastMessages(limit, getMessagesCallback(successCallback));
        } catch (Exception e) {
            // errorCallback.invoke("Error when getting last messages");
        }

    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    private WritableMap messageToJson(Message msg) {
        final WritableMap map = Arguments.createMap();
        map.putString("id", msg.getId().toString());
        map.putDouble("time", msg.getTime());
        map.putString("type", msg.getType().toString());
        map.putString("text", msg.getText());
        map.putString("name", msg.getSenderName());
        map.putString("status", msg.getSendStatus().toString());
        map.putString("avatar", msg.getSenderAvatarUrl());
        map.putBoolean("read", msg.isReadByOperator());
        map.putBoolean("canEdit", msg.canBeEdited());
        return map;
    }

    private WritableMap messagesToJson(@NonNull List<? extends Message> messages) {
        WritableMap response = Arguments.createMap();
        WritableArray jsonMessages = Arguments.createArray();
        for (Message message : messages) {
            jsonMessages.pushMap(messageToJson(message));
        }
        response.putArray("messages", jsonMessages);
        return response;
    }

}
