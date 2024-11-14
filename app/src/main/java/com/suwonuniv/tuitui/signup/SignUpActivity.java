package com.suwonuniv.tuitui.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.suwonuniv.tuitui.MainActivity;
import com.suwonuniv.tuitui.R;
import com.suwonuniv.tuitui.common.activity.BaseActivity;
import com.suwonuniv.tuitui.home.HomeActivity;

public class SignUpActivity extends BaseActivity {
    private ImageButton backButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        initializeButtons();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                SignUpActivity.this.startActivity(intent);
                SignUpActivity.this.finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 프로필 등록 성공 후 홈으로 화면 전환
                Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                SignUpActivity.this.startActivity(intent);
                SignUpActivity.this.finish();
            }
        });
    }

    public void initializeButtons(){
        backButton = findViewById(R.id.backButton);
        registerButton = findViewById(R.id.registerButton);
    }
}