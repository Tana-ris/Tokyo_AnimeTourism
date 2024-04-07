package com.example.tokyo2;

import static com.google.android.gms.common.internal.service.Common.API;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.content.Intent;
import android.widget.TextView;

public class animation_select extends AppCompatActivity {

    private LinearLayout circularLayout;
    private String[] optionNames;

    private float initialX;
    private float centerX;
    private float centerY;
    private float radius;

    private double currentAngle = Math.PI / 2; // 初期角度 (90 degrees in radians)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animeselect); // レイアウトをセット

        circularLayout = findViewById(R.id.circularLayout); // 円形のレイアウトを取得
        optionNames = getResources().getStringArray(R.array.option_names); // 文字列リソースを取得

        ViewTreeObserver viewTreeObserver = circularLayout.getViewTreeObserver(); // レイアウトの監視を行うためのオブザーバーを取得
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    circularLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this); // レイアウト変更の監視を解除
                    createCircularButtons(); // 円形ボタンを生成
                    setupSwipeAnimation(); // スワイプアニメーションをセットアップ
                }
            });
        }
    }


    private void createCircularButtons() {
        centerX = circularLayout.getWidth() * 3/2;
        centerY = circularLayout.getHeight() / 1;
        float extractedCenterX = centerX;
        radius = extractedCenterX * 5/6;

        int diameter = (int) (600); // 円の直径

        for (String optionName : optionNames) {
            final Button button = new Button(this);
            button.setLayoutParams(new ViewGroup.LayoutParams(diameter, diameter));
            button.setBackgroundResource(R.drawable.rounded_button_background);
            button.setText(optionName);

            // テキストの大きさを設定
            float textSizeInSp = 25; // テキストの大きさを設定したいサイズ（sp単位で指定）
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);


            button.setLayoutParams(new ViewGroup.LayoutParams(diameter, diameter));
            button.setBackgroundResource(R.drawable.rounded_button_background);
            button.setText(optionName);

            // テキストのフォントを変更
            Typeface customTypeface = Typeface.create("sans-serif", Typeface.BOLD); // 例: フォントをsans-serifに変更
            button.setTypeface(customTypeface);


            button.setLayoutParams(new ViewGroup.LayoutParams(diameter, diameter));
            button.setBackgroundResource(R.drawable.rounded_button_background);
            button.setText(optionName);

            // テキストの色を変更
            button.setTextColor(Color.WHITE); //


            circularLayout.addView(button);

            // ボタンのクリックリスナーを追加
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ボタンがクリックされたときのアクションを処理
                    showMapLocation(optionName);
                }
            });
        }
    }



    private void updateAngle(float deltaX) {
        double angleIncrement = (2 * Math.PI) / optionNames.length;
        currentAngle -= (deltaX / radius); // 角度を減算して逆方向に回転

        // 角度を360度未満に制限
        if (currentAngle < 0) {
            currentAngle += 2 * Math.PI;
        }
    }

    private void rotateOptions() {
        double angleIncrement = (2 * Math.PI) / optionNames.length;

        // Calculate the maximum distance from the center for scaling
        float maxDistance = radius;

        for (int i = 0; i < optionNames.length; i++) {
            View button = circularLayout.getChildAt(i);
            double angle = currentAngle + angleIncrement * i;

            // Calculate button position
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));

            // Log button position
            Log.d("ButtonPosition", "Button " + i + " - X: " + x + ", Y: " + y);

            // Calculate scaling factor based on y-coordinate with exponential scaling

            float scale = 1.0f + (float) Math.exp((y * centerX / x - centerY) / maxDistance) - 0.9f;

            // ボタンの最大サイズを900に制限
            if (scale > 1.0f) {
                scale = 1.0f;
            } else if (scale < 0.5f) {
                scale = 0.5f; // ボタンの最小サイズを制限（任意の値に調整可能）
            }

            // Apply scaling to button dimensions
            int diameter = (int) (600 * scale); // ベースの直径を調整（最大サイズは900）
            ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
            layoutParams.width = diameter;
            layoutParams.height = diameter;
            button.setLayoutParams(layoutParams);

            // Apply other transformations
            button.setTranslationZ(-radius * (float) Math.sin(angle));
            button.setRotation((float) Math.toDegrees(angle) - 220);
            button.setX(x - button.getWidth() / 2);
            button.setY(y - button.getHeight() / 2);
        }
    }


    private long lastRotationTime = 0;
    private static final long ROTATION_INTERVAL = 0; // ローテーションを行う間隔（ミリ秒）

    private void setupSwipeAnimation() {
        circularLayout.setOnTouchListener(new View.OnTouchListener() {
            private float previousX = 0; // 前回のX座標

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 初期位置のX座標を保持
                        initialX = event.getX();
                        previousX = initialX; // 前回のX座標を初期位置に設定
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 現在のX座標を取得
                        float currentX = event.getX();

                        // 前回のX座標との差を計算
                        float deltaX = previousX - currentX;

                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastRotationTime >= ROTATION_INTERVAL) {
                            // オプションを回転させるアニメーションを適用
                            updateAngle(deltaX);
                            rotateOptions();
                            lastRotationTime = currentTime;
                        }

                        previousX = currentX; // 前回のX座標を更新
                        break;

                }
                return true;
            }
        });
    }

    private void showMapLocation(String optionName) {
        // MainActivity に遷移する Intent を作成
        Intent intent = new Intent(animation_select.this, MainActivity.class);
        if (optionName.equals(getString(R.string.button_text_weather))) {
            intent.putExtra("id", "1");
        } else if (optionName.equals(getString(R.string.button_text_yourname))){
            intent.putExtra("id","2");
        } else if (optionName.equals(getString(R.string.button_text_suzume))){
            intent.putExtra("id","3");
        }
            startActivity(intent);
    }
}
