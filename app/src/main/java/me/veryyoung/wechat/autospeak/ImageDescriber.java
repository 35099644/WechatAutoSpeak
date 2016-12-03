package me.veryyoung.wechat.autospeak;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static me.veryyoung.wechat.autospeak.SecretConfig.MS_KEY;
import static okhttp3.RequestBody.create;

/**
 * Created by veryyoung on 2016/12/3.
 */

public class ImageDescriber {

    private static final String MEDIA_TYPE = "application/json; charset=utf-8";

    public static String describeNetworkImage(String url) {
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.projectoxford.ai/vision/v1.0/describe")
                .header("Ocp-Apim-Subscription-Key", MS_KEY)
                .header("Content-Type", MEDIA_TYPE)
                .post(create(MediaType.parse(MEDIA_TYPE), "{\"url\":\"" + url + "\"}"))
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return null;
            }
            JSONObject jsonObject = new JSONObject(response.body().string());
            JSONObject desc = jsonObject.getJSONObject("description");
            if (desc != null) {
                JSONArray captions = desc.getJSONArray("captions");
                if (captions != null && captions.length() > 0) {
                    JSONObject caption = captions.getJSONObject(0);
                    String text = caption.getString("text");
                    return text;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String describeLocalImage(String path) {
        return null;
    }
}
