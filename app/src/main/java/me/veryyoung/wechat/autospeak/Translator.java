package me.veryyoung.wechat.autospeak;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.text.TextUtils.isEmpty;
import static me.veryyoung.wechat.autospeak.SecretConfig.TRANS_APIID;
import static me.veryyoung.wechat.autospeak.SecretConfig.TRANS_SECURITY_KEY;

/**
 * Created by veryyoung on 2016/12/3.
 */

public class Translator {

    private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";
    private static final String FROM_LANGUAGE = "auto";
    private static final String TO_LANGUAGE = "zh";

    public static String translate(String query) {
        OkHttpClient client = new OkHttpClient();

        String salt = String.valueOf(System.currentTimeMillis());
        String url = new StringBuffer(TRANS_API_HOST)
                .append("?q=").append(query)
                .append("&from=").append(FROM_LANGUAGE)
                .append("&to=").append(TO_LANGUAGE)
                .append("&appid=").append(TRANS_APIID)
                .append("&salt=").append(salt)
                .append("&sign=").append(getMD5(TRANS_APIID + query + salt + TRANS_SECURITY_KEY))
                .toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String zhText = response.body().string();
            if (!isEmpty(zhText)) {
                JSONObject zhTextObject = new JSONObject(zhText);
                JSONArray trans_results = zhTextObject.getJSONArray("trans_result");

                if (trans_results != null && trans_results.length() > 0) {
                    JSONObject trans_result = trans_results.getJSONObject(0);
                    return trans_result.getString("dst");
                }
            }
            return zhText;
        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return query;
    }


    private static String getMD5(String sourceStr) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                int i = b[offset];
                if (i < 0) {
                    i += 256;
                } else if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


}
