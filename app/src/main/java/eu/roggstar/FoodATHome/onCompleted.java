package eu.roggstar.FoodATHome;

/**
 * Created by Phil on 12/19/2017.
 */
public interface onCompleted {
    void onGetJSONCompleted(String result, String bar);
    void onUploadTaskCompleted(String result);
    void onPicDownloaded(String name,String bar);
}