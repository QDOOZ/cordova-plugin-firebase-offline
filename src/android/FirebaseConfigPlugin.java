package by.chemerisuk.cordova.firebase;

import java.util.Collections;

import android.content.Context;
import android.util.Log;
import android.util.Base64;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class FirebaseConfigPlugin extends ReflectiveCordovaPlugin {
    private static final String TAG = "FirebaseConfigPlugin";

    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase Remote Config plugin");

        this.firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        String filename = preferences.getString("FirebaseRemoteConfigDefaults", "");
        if (!filename.isEmpty()) {
            Context ctx = cordova.getActivity().getApplicationContext();
            int resourceId = ctx.getResources().getIdentifier(filename, "xml", ctx.getPackageName());
            this.firebaseRemoteConfig.setDefaults(resourceId);
        }
    }

    @CordovaMethod
    protected void update(long ttlSeconds, final CallbackContext callbackContext) {
        if (ttlSeconds == 0) {
            // App should use developer mode to fetch values from the service
            this.firebaseRemoteConfig.setConfigSettings(
                new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(true)
                    .build()
            );
        }

        this.firebaseRemoteConfig.fetch(ttlSeconds)
            .addOnCompleteListener(cordova.getActivity(), new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        firebaseRemoteConfig.activateFetched();

                        callbackContext.success();
                    } else {
                        callbackContext.error(task.getException().getMessage());
                    }
                }
            });
    }

    private static Map<String, Object> defaultsToMap(JSONObject object) throws JSONException {
        final Map<String, Object> map = new HashMap<String, Object>();

        for (Iterator<String> keys = object.keys(); keys.hasNext(); ) {
            String key = keys.next();
            Object value = object.get(key);

            if (value instanceof Integer) {
                //setDefaults() should take Longs
                value = new Long((Integer) value);
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                if (array.length() == 1 && array.get(0) instanceof String) {
                    //parse byte[] as Base64 String
                    value = Base64.decode(array.getString(0), Base64.DEFAULT);
                } else {
                    //parse byte[] as numeric array
                    byte[] bytes = new byte[array.length()];
                    for (int i = 0; i < array.length(); i++)
                        bytes[i] = (byte) array.getInt(i);
                    value = bytes;
                }
            }

            map.put(key, value);
        }
        return map;
    }

    private void setDefaults(final CallbackContext callbackContext, final JSONObject defaults, final String namespace) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    if (namespace == null)
                        firebaseRemoteConfig.setDefaults(defaultsToMap(defaults));
                    else
                        firebaseRemoteConfig.setDefaults(defaultsToMap(defaults), namespace);
                    callbackContext.success();
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    @CordovaMethod
    protected void setDefaults(JSONObject defaults, String namespace, CallbackContext callbackContext) {
        if (namespace != null && namespace.length() > 0) {
            this.setDefaults(callbackContext, defaults, namespace);
        } else {
            this.setDefaults(callbackContext, defaults, null);
        }
    }

    @CordovaMethod
    protected void getBoolean(String key, String namespace, CallbackContext callbackContext) {
        boolean value = getValue(key, namespace).asBoolean();

        callbackContext.sendPluginResult(
            new PluginResult(PluginResult.Status.OK, value));
    }

    @CordovaMethod
    protected void getBytes(String key, String namespace, CallbackContext callbackContext) {
        callbackContext.success(getValue(key, namespace).asByteArray());
    }

    @CordovaMethod
    protected void getNumber(String key, String namespace, CallbackContext callbackContext) {
        double value = getValue(key, namespace).asDouble();

        callbackContext.sendPluginResult(
            new PluginResult(PluginResult.Status.OK, (float)value));
    }

    @CordovaMethod
    protected void getString(String key, String namespace, CallbackContext callbackContext) {
        callbackContext.success(getValue(key, namespace).asString());
    }

    private FirebaseRemoteConfigValue getValue(String key, String namespace) {
        if (namespace.isEmpty()) {
            return this.firebaseRemoteConfig.getValue(key);
        } else {
            return this.firebaseRemoteConfig.getValue(key, namespace);
        }
    }
}