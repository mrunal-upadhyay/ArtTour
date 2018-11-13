package com.mrunal.arttour;

import android.app.Application;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.mrunal.arttour.network.OkHttpStack;

public class MainApplication extends Application {

  public static final String TAG = MainApplication.class.getSimpleName();

  private static MainApplication _instance;
  private RequestQueue mRequestQueue;

  public static MainApplication getInstance() {
    return _instance;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    Fresco.initialize(this);

    _instance = this;
  }

  public RequestQueue getRequestQueue() {
    if (mRequestQueue == null) {
      mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new OkHttpStack());
    }
    return mRequestQueue;
  }

  public <T> void addToRequestQueue(Request<T> req) {
    req.setTag(TAG);
    getRequestQueue().add(req);
  }
}
