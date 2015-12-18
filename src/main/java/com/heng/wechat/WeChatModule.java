package com.heng.wechat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by heng on 2015/12/10.
 * <p>
 * Edited by heng on 15/12/18
 * Added WeChat share
 */
public class WeChatModule extends ReactContextBaseJavaModule {

    public static final String REACT_MODULE_NAME = "WeChatAndroid";

    public static IWXAPI wxApi = null;
    public static String appId ;
    public static ReactApplicationContext reactApplicationContext;


    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_SHARE = "share";
    public static final String ACTION_DEFAULT = "default";
    public static String currentAction = ACTION_DEFAULT;

    /** ============== WeChat login ============== */
    public static final String OPTIONS_SCOPE = "scope";
    public static final String OPTIONS_STATE = "state";
    /** ============== WeChat login ============== */

    /** ============== WeChat share ============== */
    public static final String OPTIONS_LINK = "link";
    public static final String OPTIONS_TAG_NAME = "tagName";
    public static final String OPTIONS_TITLE = "title";
    public static final String OPTIONS_DESC = "desc";
    public static final String OPTIONS_THUMB_IMAGE = "thumbImage";
    public static final String OPTIONS_SCENE = "scene";
    /** ============== WeChat share ============== */

    /** ============== WeChat pay ============== */
    public static final String OPTIONS_APP_ID = "appId";
    public static final String OPTIONS_NONCE_STR = "nonceStr";
    public static final String OPTIONS_PACKAGE_VALUE = "packageValue";
    public static final String OPTIONS_PARTNER_ID = "partnerId";
    public static final String OPTIONS_PREPAY_ID = "prepayId";
    public static final String OPTIONS_TIME_STAMP = "timeStamp";
    public static final String OPTIONS_SIGN = "sign";
    /** ============== WeChat pay ============== */

    public WeChatModule(ReactApplicationContext reactContext) {
        super(reactContext);
        WeChatModule.reactApplicationContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_MODULE_NAME;
    }

    @ReactMethod
    public void registerApp(String appId, Callback callback) {
        WeChatModule.appId = appId;
        WeChatModule.wxApi = WXAPIFactory.createWXAPI(getReactApplicationContext(), appId, true);
        callback.invoke(WeChatModule.wxApi.registerApp(appId));
    }

    /**
     * Edited by heng on 2015/12/18
     * <p>
     * add errCallback;
     */
    @ReactMethod
    public void isWXAppInstalled(Callback callback,Callback errCallback) {
        if (WeChatModule.wxApi == null) {
            if(errCallback != null) {
                errCallback.invoke("please registerApp before this !");
            }
            return;
        }
        if(callback != null) {
            callback.invoke(WeChatModule.wxApi.isWXAppInstalled());
        }
    }

    /**
     * Edited by heng on 2015/12/18
     * <p>
     * update params and add errCallback;
     *
     * save current action
     */
    @ReactMethod
    public void sendAuthReq(ReadableMap options, Callback errCallback) {
        if (WeChatModule.wxApi == null) {
            if(errCallback != null) {
                errCallback.invoke("please registerApp before this !");
            }
            return;
        }
        String scope = "snsapi_userinfo";
        String state = "SECRET";
        if (options != null) {
            if (options.hasKey(OPTIONS_SCOPE)){
                scope = options.getString(OPTIONS_SCOPE);
            }
            if (options.hasKey(OPTIONS_STATE)){
                state = options.getString(OPTIONS_STATE);
            }
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = scope;
        req.state = state;
        WeChatModule.currentAction = ACTION_LOGIN;
        WeChatModule.wxApi.sendReq(req);
    }


    /**
     * Added by heng on 2015/12/18
     * <p>
     *     if not register WeChat appId , errCallback return error
     *     else callback return true ? is support : not support
     */
    @ReactMethod
    public void isWXAppSupportAPI(Callback callback,Callback errCallback) {
        if (WeChatModule.wxApi == null) {
            if(errCallback != null) {
                errCallback.invoke("please registerApp before this !");
            }
            return;
        }
        if(callback != null){
            callback.invoke(WeChatModule.wxApi.isWXAppSupportAPI());
        }
    }

    /**
     * Added by heng on 2015/12/18
     *
     * this method is used to be sharing to WeChat
     *
     * errCallback return error
     */
    @ReactMethod
    public void sendLinkURL(ReadableMap options, Callback errCallback) {
        if (WeChatModule.wxApi == null) {
            if(errCallback != null) {
                errCallback.invoke("please registerApp before this !");
            }
            return;
        }
        String link = "";          //待分享的链接
        String tagName = "";       //标签
        String title = "";         //标题
        String desc = "";          //描述内容
        String thumbImage = "";    //缩略图地址
        int scene = 0;             //场景(0:聊天界面，1:朋友圈，2:收藏)
        if (options != null) {
            if (options.hasKey(OPTIONS_LINK)) {
                link = options.getString(OPTIONS_LINK);
            }
            if (options.hasKey(OPTIONS_TAG_NAME)) {
                tagName = options.getString(OPTIONS_TAG_NAME);
            }
            if (options.hasKey(OPTIONS_TITLE)) {
                title = options.getString(OPTIONS_TITLE);
            }
            if (options.hasKey(OPTIONS_DESC)) {
                desc = options.getString(OPTIONS_DESC);
            }
            if (options.hasKey(OPTIONS_THUMB_IMAGE)) {
                thumbImage = options.getString(OPTIONS_THUMB_IMAGE);
            }
            if (options.hasKey(OPTIONS_SCENE)) {
                scene = options.getInt(OPTIONS_SCENE);
            }
        }
        WXWebpageObject webPage = new WXWebpageObject();
        webPage.webpageUrl = link;
        WXMediaMessage msg = new WXMediaMessage(webPage);
        msg.mediaTagName = tagName;
        msg.title = title;
        msg.description = desc;
        Bitmap bitmap = getBmFromUrl(thumbImage);
        if (bitmap != null) {
            msg.setThumbImage(bitmap);
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        switch (scene) {
            case 0:
                req.scene = SendMessageToWX.Req.WXSceneSession;
                break;
            case 1:
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                break;
            case 2:
                req.scene = SendMessageToWX.Req.WXSceneFavorite;
                break;
        }
        WeChatModule.currentAction = ACTION_SHARE;
        WeChatModule.wxApi.sendReq(req);
    }

    /**
     * Added by heng on 2015/12/18
     * <p>
     *    get bitmap from url
     */
    Bitmap getBmFromUrl(String sourceUrl) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(sourceUrl);
            InputStream is = url.openStream();
            bitmap = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Added by heng on 2015/12/18
     * <p>
     *     WeChat pay method
     *     if not register WeChat appId , errCallback return error
     */
    @ReactMethod
    public void weChatPay(ReadableMap options,Callback errCallback) {
        if (WeChatModule.wxApi == null) {
            if(errCallback != null) {
                errCallback.invoke("please registerApp before this !");
            }
            return;
        }
        String appId = WeChatModule.appId;
        String nonceStr = "";
        String packageValue = "";
        String partnerId = "";
        String prepayId = "";
        String timeStamp = "";
        String sign = "";
        if(options != null){
            if(options.hasKey(OPTIONS_APP_ID)){
                appId = options.getString(OPTIONS_APP_ID);
            }
            if(options.hasKey(OPTIONS_NONCE_STR)){
                nonceStr = options.getString(OPTIONS_NONCE_STR);
            }
            if(options.hasKey(OPTIONS_PACKAGE_VALUE)){
                packageValue = options.getString(OPTIONS_PACKAGE_VALUE);
            }
            if(options.hasKey(OPTIONS_PARTNER_ID)){
                partnerId = options.getString(OPTIONS_PARTNER_ID);
            }
            if(options.hasKey(OPTIONS_PREPAY_ID)){
                prepayId = options.getString(OPTIONS_PREPAY_ID);
            }
            if(options.hasKey(OPTIONS_TIME_STAMP)){
                timeStamp = options.getString(OPTIONS_TIME_STAMP);
            }
            if(options.hasKey(OPTIONS_SIGN)){
                sign = options.getString(OPTIONS_SIGN);
            }
        }

        PayReq request = new PayReq();
        request.appId = appId;
        request.nonceStr = nonceStr;
        request.packageValue = packageValue;
        request.partnerId = partnerId;
        request.prepayId = prepayId;
        request.timeStamp = timeStamp;
        request.sign = sign;
        WeChatModule.wxApi.sendReq(request);
    }
}
