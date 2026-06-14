package com.example.plantdetector;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView tvHint;
    private Button btnCapture, btnRetake, btnAnalyze;
    private Bitmap currentPhoto = null;
    private Classifier classifier;

    // Запрос разрешения на камеру
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Разрешение получено, открываем камеру
                    openCamera();
                } else {
                    Toast.makeText(this, "Нужно разрешение на камеру для работы приложения", Toast.LENGTH_LONG).show();
                }
            }
    );

    // Запуск камеры
    private final ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    currentPhoto = (Bitmap) result.getData().getExtras().get("data");
                    imageView.setImageBitmap(currentPhoto);
                    imageView.setVisibility(ImageView.VISIBLE);
                    tvHint.setVisibility(TextView.GONE);
                    btnRetake.setEnabled(true);
                    btnAnalyze.setEnabled(true);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            classifier = new Classifier(this);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка загрузки модели: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        imageView = findViewById(R.id.iv_photo);
        tvHint = findViewById(R.id.tv_hint);
        btnCapture = findViewById(R.id.btn_capture);
        btnRetake = findViewById(R.id.btn_retake);
        btnAnalyze = findViewById(R.id.btn_analyze);

        btnCapture.setOnClickListener(v -> checkCameraPermission());
        btnRetake.setOnClickListener(v -> {
            clearPhoto();
            checkCameraPermission();
        });
        btnAnalyze.setOnClickListener(v -> analyzePhoto());

        btnRetake.setEnabled(false);
        btnAnalyze.setEnabled(false);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка открытия камеры: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void clearPhoto() {
        currentPhoto = null;
        imageView.setImageBitmap(null);
        imageView.setVisibility(ImageView.GONE);
        tvHint.setVisibility(TextView.VISIBLE);
        btnRetake.setEnabled(false);
        btnAnalyze.setEnabled(false);
    }

    private void analyzePhoto() {
        if (currentPhoto == null) {
            Toast.makeText(this, "Сначала сфотографируйте растение", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Classifier.ClassificationResult result = classifier.classify(currentPhoto);

            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("plant_name", result.plantName);
            intent.putExtra("confidence", result.confidence);
            intent.putExtra("is_rare", result.isRare);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка распознавания: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (classifier != null) {
            classifier.close();
        }
    }
}