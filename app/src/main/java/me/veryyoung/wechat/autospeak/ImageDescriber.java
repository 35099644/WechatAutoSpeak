package me.veryyoung.wechat.autospeak;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

    private static final String APPLICATION_JSON = "application/json; charset=utf-8";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private static final OkHttpClient client = new OkHttpClient();


    public static String describe(String url) {
        Request request = new Request.Builder()
                .url("https://api.projectoxford.ai/vision/v1.0/describe")
                .header("Ocp-Apim-Subscription-Key", MS_KEY)
                .header("Content-Type", APPLICATION_JSON)
                .post(create(MediaType.parse(APPLICATION_JSON), "{\"url\":\"" + url + "\"}"))
                .build();
        return getDesc(request);
    }

    public static String describe(File file) {
        Request request = new Request.Builder()
                .url("https://api.projectoxford.ai/vision/v1.0/describe")
                .header("Ocp-Apim-Subscription-Key", MS_KEY)
                .header("Content-Type", APPLICATION_OCTET_STREAM)
                .post(create(MediaType.parse(APPLICATION_OCTET_STREAM), file))
                .build();
        return getDesc(request);
    }

    private static String getDesc(Request request) {
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
}
