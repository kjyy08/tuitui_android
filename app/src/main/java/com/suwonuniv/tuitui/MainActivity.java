package com.suwonuniv.tuitui;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.suwonuniv.tuitui.common.activity.BaseActivity;
import com.suwonuniv.tuitui.signup.SignUpActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class MainActivity extends BaseActivity {
    private final String KAKAO_PREFS_NAME = "kakaoAccessToken";
    private RelativeLayout kakaoLoginBtn, naverLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //  getHashKey();

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //  카카오 콜백 함수
        Function2<OAuthToken, Throwable, Unit> kakaoCallback = (oAuthToken, throwable) -> {
            if (oAuthToken != null) {
                Log.i("user", "AccessToken: " + oAuthToken.getAccessToken());

                // 로그인 성공 시 accessToken을 SharedPreferences에 저장
                SharedPreferences prefs = getSharedPreferences(KAKAO_PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("accessToken", oAuthToken.getAccessToken());
                editor.apply();

                // UI 업데이트
                updateKakaoLoginUi();
            }
            if (throwable != null) {
                Log.w(TAG, "invoke: " + throwable.getLocalizedMessage());
                // 로그인 실패 시 AlertDialog로 안내 메시지
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("로그인 실패")  // 다이얼로그 제목
                        .setMessage("카카오 로그인에 실패했습니다\n" + throwable.getLocalizedMessage())  // 실패 메시지
                        .setPositiveButton("확인", (dialog, which) -> {
                            dialog.dismiss();  // 확인 버튼 클릭 시 다이얼로그 닫기
                        })
                        .setCancelable(false)  // 다이얼로그 외부를 클릭해도 닫히지 않도록 설정
                        .show();  // 다이얼로그 표시
            }

            return null;
        };

        initializeButtons();

        // 카카오 로그인
        kakaoLoginBtn.setOnClickListener(v -> {
            // 카카오톡이 설치되었는지 확인
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)) {
                // 카카오톡을 통한 로그인 시도
                UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this, (oAuthToken, error) -> {
                    if (error != null) {
                        // 카카오톡 로그인 실패 시 웹 로그인 유도
                        loginWithKakaoAccount(kakaoCallback);
                    } else if (oAuthToken != null) {
                        // 로그인 성공
                        saveKakaoAccessToken(oAuthToken.getAccessToken());
                        updateKakaoLoginUi();
                    }
                    return null;
                });
            } else {
                // 웹 로그인
                loginWithKakaoAccount(kakaoCallback);
            }
        });


        // 네이버 로그인
        naverLoginBtn.setOnClickListener(v -> {
            
        });
    }

    private void loginWithKakaoAccount(Function2<OAuthToken, Throwable, Unit> kakaoCallback) {
        UserApiClient.getInstance().loginWithKakaoAccount(MainActivity.this, kakaoCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 카카오 웹 로그인 취소 시 처리
        if (resultCode == RESULT_CANCELED) {
            // 사용자가 웹 로그인 중 뒤로 가기 등으로 취소했을 경우
            clearKakaoAccessToken();
            Log.i(TAG, "User canceled Kakao login.");
        }
    }

    // 액세스 토큰을 SharedPreferences에서 삭제
    private void clearKakaoAccessToken() {
        SharedPreferences prefs = getSharedPreferences(KAKAO_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("accessToken");
        editor.apply();
    }

    // 액세스 토큰을 저장
    private void saveKakaoAccessToken(String accessToken) {
        SharedPreferences prefs = getSharedPreferences(KAKAO_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("accessToken", accessToken);
        editor.apply();
    }

    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }

    public void initializeButtons(){
        kakaoLoginBtn = findViewById(R.id.kakaoLoginButton);
        naverLoginBtn = findViewById(R.id.naverLoginButton);
    }

    public void updateKakaoLoginUi() {
        // 카카오 UI 가져오는 메소드 (로그인 핵심 기능)
        UserApiClient.getInstance().me((user, throwable) -> {
            if (user != null) {
                // 유저 정보가 정상 전달 되었을 경우
                Log.i(TAG, "id " + user.getId());   // 유저의 고유 아이디를 불러옵니다.
                Log.i(TAG, "invoke: nickname=" + user.getKakaoAccount().getProfile().getNickname());  // 유저의 닉네임을 불러옵니다.
                Log.i(TAG, "userimage " + user.getKakaoAccount().getProfile().getProfileImageUrl());    // 유저의 이미지 URL을 불러옵니다.

                // 이 부분에는 로그인이 정상적으로 되었을 경우 어떤 일을 수행할 지 적으면 됩니다.
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
            }
            if (throwable != null) {
                // 로그인 시 오류 났을 때
                // 키해시가 등록 안 되어 있으면 오류 납니다.
                Log.w(TAG, "invoke: " + throwable.getLocalizedMessage());
            }
            return null;
        });
    }

    //  카카오 엑세스 토큰을 저장소에서 읽어 옵니다.
    private String getStoredKakaoAccessToken() {
        SharedPreferences prefs = getSharedPreferences(KAKAO_PREFS_NAME, MODE_PRIVATE);
        return prefs.getString("accessToken", null);
    }
}