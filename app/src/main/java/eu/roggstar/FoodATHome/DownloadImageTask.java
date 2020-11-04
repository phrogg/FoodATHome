package eu.roggstar.FoodATHome;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ActionMenuView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.in;

class DownloadImageTask extends AsyncTask<String, Void, Bitmap> { //TODO cleanup the global vars do they need to be global?
        ImageView bmImage;
        String barCode;
        Activity activity;

        public DownloadImageTask(ImageView bmImage, String barCode, Activity activity) {
            this.bmImage = bmImage;
            this.barCode = barCode;
            this.activity = activity;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            String urldisplay = urls[0];

            try {
                if(!picExists()) {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                    //Log.d("philz","pic does not exist");
                } else {
                    FileInputStream in = new FileInputStream(createFilePath());
                    mIcon11 = BitmapFactory.decodeStream(in);
                    //Log.d("philz","pic does exist");
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected File createFilePath() {
            return new File(activity.getExternalFilesDir(null) + File.separator, barCode + ".png");
        }

        protected boolean picExists() {
            return createFilePath().exists();
        }

        protected boolean save2Disk(Bitmap result){
            try {
                File folder = new File(activity.getExternalFilesDir(null) + File.separator);
                File image = createFilePath();

                // Encode the file as a PNG image.
                FileOutputStream outStream;

                outStream = new FileOutputStream(image);
                result.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch(Exception ex){
                Log.d("Philz", ex.toString());
            }
            return true;
        }

        protected void onPostExecute(Bitmap result) {
            if(result != null) {
                try {
                    // set the image
                    bmImage.setImageBitmap(result);
                    // save the image
                    if(!save2Disk(result)){
                        Log.d("Philz", "Abgekackt");
                    }
                } catch(Exception ex){
                    Log.d("Philz", ex.toString());
                }
            }
        }
}