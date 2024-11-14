package com.suwonuniv.tuitui.login.kakao;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KakaoApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
        KakaoSdk.init(this, "56372e49a12d1a4267d174d8d5bf3f5b");
    }
}
