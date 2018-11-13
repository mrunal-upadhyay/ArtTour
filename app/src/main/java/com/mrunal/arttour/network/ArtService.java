package com.mrunal.arttour.network;

import com.google.gson.reflect.TypeToken;
import com.mrunal.arttour.BuildConfig;
import com.mrunal.arttour.dto.ArtworkResponse;
import java.util.HashMap;
import java.util.Map;

public class ArtService extends BaseService {

  private static final String BASE_URL = "https://www.rijksmuseum.nl/api/en/collection";

  public void getArtworks(String page, ResponseListener<ArtworkResponse> listener) {
    Map<String, String> params = new HashMap<>();
    params.put("key", BuildConfig.API_KEY);
    params.put("format", "json");
    params.put("imgonly", "true");
    params.put("p", page);
    params.put("ps", "10");
    executeGetRequest(BASE_URL, null, params, new TypeToken<ArtworkResponse>() {
    }, listener);
  }

}
