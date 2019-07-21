package com.example.gpstracker.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * Created by Uri Robinov on 20/7/2019.
 */

//getting the url of the photo making a requset to get it from the url.
//making the rounded bitmap
public class GetPhoto extends AsyncTask<String, Void, Bitmap> {

    Bitmap bitmap1;
    @Override
    protected Bitmap doInBackground(String... voids) {
        try {
            URL url = new URL(voids[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            Bitmap output = Bitmap.createBitmap(myBitmap.getWidth(),
                    myBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);


            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, myBitmap.getWidth(), myBitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(output.getWidth() / 2, output.getHeight() / 2,
                    output.getWidth() / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(myBitmap, rect, rect, paint);
            bitmap1 = output;

        } catch(IOException e) {
            System.out.println(e);
        }
        return bitmap1;
    }

    @Override
    protected void onPostExecute(Bitmap aVoid) {
    }
}
