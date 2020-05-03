package eu.roggstar.FoodATHome;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NotSoSimpleAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<Product> data;
    private static LayoutInflater inflater = null;
    private SharedPreferences mPrefs;

    public NotSoSimpleAdapter(Activity a, ArrayList<Product> d) {
        activity = a;
        data = d;
        mPrefs = a.getSharedPreferences("prefs",0);
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView == null)
            vi = inflater.inflate(R.layout.custom_listview_item, null);

        TextView name = vi.findViewById(R.id.tx_name);
        TextView company = vi.findViewById(R.id.tx_company);
        TextView expiring = vi.findViewById(R.id.tx_expiring);
        ImageView imgv = vi.findViewById(R.id.imgv);
        ProgressBar pg = vi.findViewById(R.id.progressBar);

        Product product;
        product = data.get(position);

        // Setting all values in listview
        name.setText(product.name);
        if(product.name.length() > 29) {
            name.setText(product.name.substring(0,26)+"...");
        }
        company.setText(product.company);

        //calculate daysLeft
        if(Integer.parseInt(product.expiring) != 0) {
            int daysLeft = 0;
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            Date d = null;
            try {
                d = df.parse(product.expiring);
            } catch (Exception ex) {
            }

            if (d != null) {
                long diff = d.getTime() - Calendar.getInstance().getTime().getTime();
                daysLeft = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            }

            //standard color
            expiring.setTextColor(Color.parseColor("#10bcc9"));

            if (daysLeft > 14 && daysLeft < 31) {
                int tmp = daysLeft / 7;
                expiring.setText("expiring in >" + tmp + " weeks");
                expiring.setTextColor(Color.parseColor("#10bcc9"));
            } else if (daysLeft > 31 && daysLeft < 365) {
                int tmp = daysLeft / 30;
                expiring.setText("expiring in >" + tmp + " months");
                expiring.setTextColor(Color.parseColor("#10bcc9"));
            } else if (daysLeft > 365) {
                int tmp = daysLeft / 360;
                expiring.setText("expiring in >" + tmp + " years");
                expiring.setTextColor(Color.parseColor("#10bcc9"));
            } else {
                switch (daysLeft) {
                    case (0):
                        expiring.setText("expiring today!");
                        expiring.setTextColor(Color.parseColor("#FF7F00"));
                        break;
                    case (1):
                    case (2):
                    case (3):
                        expiring.setTextColor(Color.parseColor("#FF7F00"));
                        break;
                }
            }

            //progressbar
            pg.setMax(Integer.parseInt(mPrefs.getString("progressbarmax","14")));
            pg.setProgress(pg.getMax()-daysLeft);

            if(pg.getMax() < daysLeft){
                pg.setVisibility(View.INVISIBLE);
            }


            //minus days
            if(daysLeft < 0){
                daysLeft *= -1;
                if(daysLeft == 1) {
                    expiring.setText("expired " + daysLeft + " day ago");
                    pg.setVisibility(View.INVISIBLE);
                } else {
                    expiring.setText("expired " + daysLeft + " days ago");
                    pg.setVisibility(View.INVISIBLE);
                }
                expiring.setTextColor(Color.RED);
                pg.setProgress(pg.getMax());
            }
        } else {
            //expiring.setVisibility(View.INVISIBLE);
            expiring.setText("non set");
        }


        //new DownloadImageTask((ImageView) vi.findViewById(R.id.imgv))
        //        .execute(product.image);

        //new DownloadImageTask(imgv).execute(product.image);

        return vi;
    }
}