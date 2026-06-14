package com.example.plantdetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Classifier {
    private Interpreter interpreter;

    private final String[] classNames = {
            "Кандык сибирский",
            "Родиола розовая",
            "Лилия мартагон",
            "Башмачок крупноцветковый",
            "Калипсо луковичная",
            "Пион уклоняющийся",
            "Башмачок настоящий",
            "Лилия карликовая",
            "Кизильник блестящий",
            "Обычное растение"
    };

    private final boolean[] isRare = {
            true, true, true, true, true,
            true, true, true, true, false
    };

    public Classifier(Context context) {
        try {
            MappedByteBuffer modelBuffer = loadModelFile(context);
            interpreter = new Interpreter(modelBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws Exception {
        var assetFileDescriptor = context.getAssets().openFd("plant_classifier.tflite");
        var inputStream = new FileInputStream(assetFileDescriptor.getFileDescriptor());
        var fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,
                assetFileDescriptor.getStartOffset(),
                assetFileDescriptor.getDeclaredLength());
    }

    public ClassificationResult classify(Bitmap bitmap) {
        if (interpreter == null) {
            return new ClassificationResult("Ошибка", false, 0);
        }

        // Поворот фото
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Изменяем размер до 224x224 (простой способ)
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 224, 224, true);

        // Нормализуем пиксели
        float[][][][] input = new float[1][224][224][3];
        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                int pixel = resizedBitmap.getPixel(x, y);
                input[0][y][x][0] = ((pixel >> 16) & 0xFF) / 255.0f;
                input[0][y][x][1] = ((pixel >> 8) & 0xFF) / 255.0f;
                input[0][y][x][2] = (pixel & 0xFF) / 255.0f;
            }
        }

        // Предсказание
        float[][] output = new float[1][classNames.length];
        interpreter.run(input, output);

        // Поиск лучшего результата
        float[] probabilities = output[0];
        int maxIndex = 0;
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxIndex]) {
                maxIndex = i;
            }
        }

        int confidence = (int) (probabilities[maxIndex] * 100);
        return new ClassificationResult(classNames[maxIndex], isRare[maxIndex], confidence);
    }

    public void close() {
        if (interpreter != null) interpreter.close();
    }

    public static class ClassificationResult {
        public final String plantName;
        public final boolean isRare;
        public final int confidence;

        public ClassificationResult(String plantName, boolean isRare, int confidence) {
            this.plantName = plantName;
            this.isRare = isRare;
            this.confidence = confidence;
        }
    }
}