package eu.roggstar.FoodATHome;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class getJSON extends AsyncTask<Void, Void, String> {

    private String rsponse,url,bar;
    private onCompleted listener;

    public getJSON(onCompleted listener, String bar){
        this.listener=listener;
        this.bar = bar;
        this.url = "https://world.openfoodfacts.org/api/v0/produit/"+bar+".json";
    }

    @Override
    protected String doInBackground(Void... voids) {
        //OkHttpClient client = new OkHttpClient();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response;

        try {
            response = client.newCall(request).execute();
            if (response.body() != null) {
                rsponse = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rsponse;
    }


    @Override
    protected void onPostExecute(String result) {
        listener.onGetJSONCompleted(result,bar);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}