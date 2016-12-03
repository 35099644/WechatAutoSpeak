package me.veryyoung.wechat.autospeak;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.Locale;

import static de.robv.android.xposed.XposedBridge.log;


/**
 * Created by veryyoung on 2016/12/3.
 */

public class TTS implements OnInitListener {

    private Context context;

    public TTS(Context context) {
        this.context = context;
    }

    private TextToSpeech mTts;

    public void speak(String words) {
        //检查TTS数据是否已经安装并且可用
        mTts = new TextToSpeech(context, this);
        if (null == mTts) {
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        } else {
            mTts.speak(words, TextToSpeech.QUEUE_ADD, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.CHINA);
            // 设置发音语言
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                log("Language is not available");
            }
        } else {
            Intent installIntent = new Intent();
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            context.startActivity(installIntent);
        }
    }
}
