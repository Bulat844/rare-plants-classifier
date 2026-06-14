package com.example.plantdetector;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView tvPlantName = findViewById(R.id.tv_plant_name);
        TextView tvConfidence = findViewById(R.id.tv_confidence);
        TextView tvStatus = findViewById(R.id.tv_status);
        TextView tvDescription = findViewById(R.id.tv_description);
        Button btnNewPhoto = findViewById(R.id.btn_new_photo);
        MaterialCardView cardResult = findViewById(R.id.card_result);

        String plantName = getIntent().getStringExtra("plant_name");
        int confidence = getIntent().getIntExtra("confidence", 0);
        boolean isRare = getIntent().getBooleanExtra("is_rare", false);

        if (plantName == null) plantName = "Неизвестно";

        tvPlantName.setText(plantName);
        tvConfidence.setText("Уверенность: " + confidence + "%");

        if (isRare) {
            tvStatus.setText("🟢 РЕДКОЕ РАСТЕНИЕ!");
            tvDescription.setText("Это растение внесено в Красную книгу Иркутской области. Не срывайте и не вытаптывайте его!");
            cardResult.setCardBackgroundColor(getResources().getColor(R.color.rare_green, null));
        } else {
            tvStatus.setText("🟡 ОБЫЧНОЕ РАСТЕНИЕ");
            tvDescription.setText("Это растение не требует особой охраны, но помните о бережном отношении к природе.");
            cardResult.setCardBackgroundColor(getResources().getColor(R.color.common_yellow, null));
        }

        btnNewPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}