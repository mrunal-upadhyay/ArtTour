package com.mrunal.arttour.network;

import com.android.volley.Request;
import com.google.gson.reflect.TypeToken;
import com.mrunal.arttour.MainApplication;
import com.mrunal.arttour.utils.NetworkUtils;
import java.util.Map;

public class BaseService {

  //Opens connection port via GET Method
  protected void executeGetRequest(String url, Map<String, String> headers,
      Map<String, String> params, TypeToken typeToken, ResponseListener listener) {
    url = NetworkUtils.getUrl(url, params);
    executeRequest(Request.Method.GET, url, headers, params, typeToken, listener);
  }

  //Opens connection port via POST Method
  protected void executePostRequest(String url, Map<String, String> headers,
      Map<String, String> params, TypeToken typeToken, ResponseListener listener) {
    url = NetworkUtils.getUrl(url, params);
    executeRequest(Request.Method.POST, url, headers, params, typeToken, listener);
  }

  protected void executeRequest(int method, String url, Map<String, String> headers,
      Map<String, String> params, TypeToken typeToken, ResponseListener listener) {
    BaseRequest request = new BaseRequest(method, url, headers, params, typeToken, listener);
    MainApplication.getInstance().addToRequestQueue(request);
  }
}
