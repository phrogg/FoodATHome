package eu.roggstar.FoodATHome;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class UploadInfo2OpenFoodFacts extends AsyncTask<Void, Void, String> {

    private String rsponse,url,bar,name,brand;
    private onCompleted listener;

    public UploadInfo2OpenFoodFacts(onCompleted listener,String bar, String name, String brand){
        this.listener=listener;
        this.bar = bar;
        this.name = name;
        this.brand = brand;
        this.url = "https://world.openfoodfacts.net/cgi/product_jqm2.pl";
    }

    @Override
    protected String doInBackground(Void... voids) {
        //OkHttpClient client = new OkHttpClient();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                //.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
                .addFormDataPart("some-field", "some-value")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("code", bar)
                .addHeader("User-id", "off")
                .addHeader("Password", "off")
                .addHeader("product_name", name)
                .addHeader("brands", brand)
                //.post(requestBody)
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
        listener.onUploadTaskCompleted(result);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}