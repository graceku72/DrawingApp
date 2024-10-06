package com.example.classwork3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = this.openOrCreateDatabase("db", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS DRAWINGS (ID INTEGER PRIMARY KEY AUTOINCREMENT, IMAGE BLOB, TIME DATETIME DEFAULT CURRENT_TIMESTAMP, TAGS TEXT)");
        displayRecents();
    }

    public void displayRecents() {
        Cursor c;
        c = db.rawQuery("SELECT * FROM DRAWINGS ORDER BY TIME DESC", null);
        displayThumbnail(c);
    }

    public void saveDrawing(View view) {
        DrawingView dv = findViewById(R.id.drawingArea);
        Bitmap b = dv.getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] ba = stream.toByteArray();

        EditText et = findViewById(R.id.setTags);
        String tagsInput = et.getText().toString().trim();
        String[] tagsArray = tagsInput.split("\\s*,\\s*");  // Split by comma and trim whitespace around

        // Join the array back into a single comma-separated string
        String tags = String.join(", ", tagsArray);

        ContentValues cv = new ContentValues();
        cv.put("IMAGE", ba);
        cv.put("TAGS", tags);
        db.insert("DRAWINGS", null, cv);

        clearDrawing(findViewById(R.id.drawingArea));
    }

    public void findDrawings(View view) {
        EditText et = findViewById(R.id.tagsQuery);
        String tagInput = et.getText().toString().trim();

        if (tagInput.contains(",")) {
            et.setError("Please enter only one tag");
            return;
        }

        Cursor c;
        if (tagInput.isEmpty()) {
            c = db.rawQuery("SELECT * FROM DRAWINGS ORDER BY TIME DESC", null);
        } else {
            c = db.rawQuery(
                    "SELECT * FROM DRAWINGS WHERE TAGS LIKE ? OR TAGS LIKE ? OR TAGS LIKE ? OR TAGS = ? ORDER BY TIME DESC",
                    new String[]{tagInput + ",%", "%, " + tagInput + ",%", "%, " + tagInput, tagInput}
            );
        }

        displayThumbnail(c);
    }

    public void displayThumbnail(Cursor c) {
        if (c.moveToFirst()) {
            int[] imageViewIds = {R.id.img1, R.id.img2, R.id.img3};
            int[] tagsTextViews = {R.id.img1Tags, R.id.img2Tags, R.id.img3Tags};
            int[] datetimeTextViews = {R.id.img1DateTime, R.id.img2DateTime, R.id.img3DateTime};
            for (int i = 0; i < 3; i++) {
                TextView tagsTV = findViewById(tagsTextViews[i]);
                TextView datetimeTV = findViewById(datetimeTextViews[i]);
                if (c.getCount() - i > 0) {
                    byte[] ba = c.getBlob(1);
                    Bitmap b = BitmapFactory.decodeByteArray(ba, 0, ba.length);
                    ImageView im = findViewById(imageViewIds[i]);
                    im.setImageBitmap(b);

                    tagsTV.setText(c.getString(3));
                    datetimeTV.setText(c.getString(2));

                    c.moveToNext();
                } else {
                    setPlaceholderImage(findViewById(imageViewIds[i]));
                    tagsTV.setText("Unavailable");
                    datetimeTV.setText("");
                }
            }
        } else {
            setPlaceholderImage(findViewById(R.id.img1));
            TextView img1TagsTV = findViewById(R.id.img1Tags);
            img1TagsTV.setText("Unavailable");
            TextView img1DatetimeTV = findViewById(R.id.img1DateTime);
            img1DatetimeTV.setText("");

            setPlaceholderImage(findViewById(R.id.img2));
            TextView img2TagsTV = findViewById(R.id.img2Tags);
            img2TagsTV.setText("Unavailable");
            TextView img2DatetimeTV = findViewById(R.id.img2DateTime);
            img2DatetimeTV.setText("");

            setPlaceholderImage(findViewById(R.id.img3));
            TextView img3TagsTV = findViewById(R.id.img3Tags);
            img3TagsTV.setText("Unavailable");
            TextView img3DatetimeTV = findViewById(R.id.img3DateTime);
            img3DatetimeTV.setText("");
        }
    }

    private void setPlaceholderImage(ImageView im) {
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder);

        int cropStartX = 0;
        int cropStartY = 0;
        int cropWidth = 117;
        int cropHeight = 78;

        Bitmap croppedPlaceholder = Bitmap.createBitmap(b, cropStartX, cropStartY, cropWidth, cropHeight);
        im.setImageBitmap(croppedPlaceholder);
    }

    public void clearDrawing(View view) {
        DrawingView dv = findViewById(R.id.drawingArea);
        dv.clearDrawing();
    }
}