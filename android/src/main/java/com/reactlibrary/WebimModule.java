package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import com.webimapp.android.sdk.Webim;
import com.webimapp.android.sdk.WebimSession;
import com.webimapp.android.sdk.MessageListener;
import com.webimapp.android.sdk.Message;

public class WebimModule extends ReactContextBaseJavaModule implements MessageListener {

    private final ReactApplicationContext reactContext;

    private WebimSession session;

    public WebimModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "Webim";
    }

    private void build(String accountName, String location) {
        WebimSession webimSession = Webim.newSessionBuilder()
        .setAccountName(ACCOUNT_NAME)
        .setLocation(LOCATION_NAME)
        .build();
    }

    @ReactMethod
    public void resume(ReadableMap builderData, Promise promise) {
        try {
            String accountName = builderData.getString("accountName");
            String location = builderData.getString("location");
    
            if (session == null) {
                build(accountName, location);
            }
    
            if (session == null) {
                promise.reject("Unable to build session");
            }
    
            session.resume();
            session.getStream().setChatRead();
            promise.resolve();
        } catch (Exception e) {
            promise.reject("Error when building session");
        }
       
    }

    @ReactMethod
    public void sendMessage(String message, Promise promise) {
        try {
            session.getStream().sendMessage(message);
            promise.resolve();
        } catch (Exception e) {
            promise.reject("Send message error");
        }
        
    }

    //@ReactMethod
    //public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
     //   callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    //}

    @Override
    public void messageAdded(@Nullable Message before, @NonNull Message message) {
        WritableMap msg = Arguments.createMap();
        msg.putMap("msg", messageToJson(message));
        sendEvent("messageAdded", msg);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        this.reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
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
        Message.Attachment attach = msg.getAttachment();
        if (attach != null) {
            WritableMap _att = Arguments.createMap();
            _att.putString("contentType", attach.getContentType());
            _att.putString("name", attach.getFileName());
            _att.putString("info", "attach.getImageInfo().toString()");
            _att.putDouble("size", attach.getSize());
            _att.putString("url", attach.getUrl());
            map.putMap("attachment", _att);
        }
        return map;
    }

}
