package me.veryyoung.wechat.autospeak;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static android.text.TextUtils.isEmpty;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static me.veryyoung.wechat.autospeak.ImageDescriber.describe;


public class Main implements IXposedHookLoadPackage {

    public static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    private static final String NOTIFICATION_CLASS_NAME = WECHAT_PACKAGE_NAME + ".booter.notification.b";
    private static final String STORAGE_CLASS_NAME = WECHAT_PACKAGE_NAME + ".storage.j";
    private static final String STORAGE_METHOD_NAME = WECHAT_PACKAGE_NAME + ".bh.g";
    private static final String IMAGE_CLASS_NAME = WECHAT_PACKAGE_NAME + ".ag.n";
    private static final String IMAGE_METHOD_NAME1 = "Gj";
    private static final String IMAGE_METHOD_NAME2 = "iJ";
    private static final String ContextGetterClass = WECHAT_PACKAGE_NAME + ".sdk.platformtools.aa";

    private WechatMainDBHelper mDb;

    private Class<?> mImgClss;

    private Context context;
    private TTS tts;


    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) {
        new HideModule().hide(lpparam);
        if (lpparam.packageName.equals(WECHAT_PACKAGE_NAME)) {
            findAndHookMethod(NOTIFICATION_CLASS_NAME, lpparam.classLoader, "a", NOTIFICATION_CLASS_NAME, String.class, String.class, int.class, int.class, boolean.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            int messageType = (int) param.args[3];
                            String content = (String) param.args[2];

                            String sender = (String) param.args[1];
                            sender = mDb.getNickname(sender);
                            log(sender + "给你发了一条消息");
                            if (null == tts) {
                                tts = new TTS(context);
                            }
                            tts.speak(sender + "给你发了一条消息");
                            switch (messageType) {
                                case 1:
                                    log("文字消息：" + content);
                                    break;
                                case 3:
                                    // 图片消息
                                    String imagePath = getImagePath();
                                    log("图片消息:" + imagePath);
                                    new DescriberTask().execute(imagePath);
                                    break;
                                case 47:
                                    // 表情；
                                    String expressionUrl = getExpressionUrl(content);
                                    log("表情消息:" + expressionUrl);
                                    new DescriberTask().execute(expressionUrl);
                                    break;
                                default:
                                    //do nothing;
                                    return;
                            }

                        }
                    }

            );


            findAndHookConstructor(STORAGE_CLASS_NAME, lpparam.classLoader, STORAGE_METHOD_NAME, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (null == mDb) {
                        mDb = new WechatMainDBHelper(param.args[0]);
                    }
                    if (null == mImgClss) {
                        mImgClss = findClass(IMAGE_CLASS_NAME, lpparam.classLoader);
                    }
                    if (null == context) {
                        context = (Context) callStaticMethod(findClass(ContextGetterClass, lpparam.classLoader), "getContext");
                    }
                }
            });


        }


    }


    private String getImagePath() {
        Cursor cursor = mDb.getLastMsg();
        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }
        String imgPath = cursor.getString(cursor.getColumnIndex("imgPath"));
        String locationInSDCard = (String) callMethod(callStaticMethod(mImgClss, IMAGE_METHOD_NAME1), IMAGE_METHOD_NAME2, imgPath);
        if (isEmpty(locationInSDCard)) {
            return null;
        }
        return locationInSDCard.replace("emulated/0", "emulated/legacy");
    }

    private String getExpressionUrl(String content) {
        content = "http:" + content.substring(content.indexOf("//"));
        return content.substring(0, content.indexOf("\""));
    }

    private class DescriberTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            String image = args[0];
            String desc = image.startsWith("http") ? describe(image) : describe(new File(image));
            return Translator.translate(desc);
        }


        //这个是在后台执行完毕之后执行
        @Override
        protected void onPostExecute(String result) {
            log("图片转换结果：" + result);
        }
    }


}
