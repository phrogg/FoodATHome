package eu.roggstar.FoodATHome;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements onCompleted {

    private NotSoSimpleAdapter adapter;
    private ArrayList<Product> productList = new ArrayList <>();
    private ListView productListView;
    private SQLiteDatabase db;
    private Calendar calendar;
    private int year, month, day, currID = -1;
    private SharedPreferences mPrefs, mStatistics;
    private SharedPreferences.Editor mEStatistics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Activity activity = this;

        //Set Title
        setTitle("Food@Home");

        //getShared preferences
        mPrefs = getSharedPreferences("prefs",0);

        mStatistics = getSharedPreferences("statistics",0);
        mEStatistics = mStatistics.edit();

        if(mPrefs.getInt("wizard",0) == 0) {
            //start first opening wizard //TODO add the wirzard function to the settings
        }

        //Set Adapter
        productListView = findViewById(R.id.foodList);
        adapter = new NotSoSimpleAdapter(this, productList);
        productListView.setAdapter(adapter);

        //setup vars
        calendar = Calendar.getInstance();

        //PreBoot stuff
        //createNoMedia();
        fetchDatabase();

        // Scan Barcode FaB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES);
                integrator.setPrompt("Scan");
                int camId = 0;
                if(mPrefs.getBoolean("camera",false)){
                    camId = 1;
                } //TODO add orientation option
                integrator.setCameraId(camId);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
            }
        });

        // Manually enter product FaB
        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newProductDialog(null);
            }
        });

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                diaRemProd(position);
            }
        });

        productListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                           long id) {
                diaLongClickDecision(position);
                return true;
            }
        });

        //new getImage(this,"http://files.gamebanana.com/img/ico/sprays/_1539-.png","handsome");
    }

    @Override
    protected void onResume(){
        super.onResume();
        fetchDatabase(); // noice
    }

    private void cloneProduct(final int pos){
        db = openOrCreateDatabase("products.db",MODE_PRIVATE,null);
        try{
            db.execSQL("insert into products(barcode,name,image,company) select barcode,name,image,company from products where id = "+productList.get(pos).id);
            //removeUnusedImages(productList.get(pos).image); TODO FIX
        } catch (Exception e){
            Log.d("PHILZ",e.toString());
        }
        openDatePicker(lastId());
        db.close();
    }


    private void diaLongClickDecision(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("What would you like to do?");

        builder.setMessage(productList.get(pos).name);

        builder.setPositiveButton("Change MHD", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                openDatePicker(productList.get(pos).id);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Clone Product", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cloneProduct(pos);
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void diaRemProd(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Remove from list?");


        if(mPrefs.getBoolean("statistics",false)) {
            builder.setMessage(productList.get(pos).name+" became ...");


            builder.setPositiveButton("rotten", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mEStatistics.putInt("rotten",mStatistics.getInt("rotten",0)+1).commit();
                    removeProduct(pos);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("eaten", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mEStatistics.putInt("eaten",mStatistics.getInt("eaten",0)+1).commit();
                    removeProduct(pos);
                    dialog.dismiss();
                }
            });

            builder.setNeutralButton("skip statistics", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeProduct(pos);
                    dialog.dismiss();
                }
            });
        } else {
            builder.setMessage(productList.get(pos).name);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    removeProduct(pos);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void removeProduct(final int pos){
        db = openOrCreateDatabase("products.db",MODE_PRIVATE,null);
        try{
            db.execSQL("DELETE FROM products WHERE id = "+productList.get(pos).id);
            //removeUnusedImages(productList.get(pos).image); TODO FIX
        } catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show(); //TODO remove
            Log.d("PHILZ",e.toString());
        }
        productList.remove(pos);
        adapter.notifyDataSetChanged();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void openDatePicker(int currID){
        if(currID != -1){ this.currID = currID; }
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDialog(999);
    }

    private void removeUnusedImages(String imageLink){
        db = openOrCreateDatabase("products.db",MODE_PRIVATE,null);

        try{
            Cursor resultSet = db.rawQuery("SELECT image FROM products WHERE image = '"+imageLink+"'",null);
            resultSet.moveToFirst();
            while(resultSet.isAfterLast() == false){
                //is duplicated
            }
        } catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show(); //TODO remove
            Log.d("PHILZ",e.toString()); //TODO remove
        }
        db.close();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    String year = String.valueOf(arg1), month = String.valueOf(++arg2), day = String.valueOf(arg3), date = "";

                    if(day.length() == 1){ day = "0" + day; }
                    if(month.length() == 1){ month = "0" + month; }

                    date = year+month+day;

                    if(currID == -1){
                        currID = lastId();
                    }

                    db = openOrCreateDatabase("products.db",MODE_PRIVATE,null);
                    db.execSQL("UPDATE products SET expiring = '"+date+"' WHERE id ='"+currID+"'");

                    currID = -1;

                    fetchDatabase();
                }
            };

    private void sortAllItemsInList(){
        Product currItem;
        Product nextItem;
        for(int i = 0;i<productList.size();i++){
            if(i < productList.size()-1) {
                currItem = productList.get(i);
                nextItem = productList.get(i + 1);

                if (Integer.parseInt(nextItem.expiring) < Integer.parseInt(currItem.expiring)) {
                    productList.get(i);
                    productList.set(i + 1, currItem);
                    productList.set(i,nextItem);
                    i = -1;
                }
            }
        }
        adapter.notifyDataSetChanged();
        productListView.setAdapter(adapter);
    }

    private int lastId(){
        db = openOrCreateDatabase("products.db",MODE_PRIVATE,null);
        new DBHelper(db); //initialize db

        int lastId = -1;

        try{
            Cursor resultSet = db.rawQuery("SELECT MAX(id) FROM products",null);
            resultSet.moveToFirst();
            lastId = resultSet.getInt(0);
        } catch (Exception e){
            Log.d("lastId",e.toString());
        }

        db.close();
        return lastId;
    }

    private void fetchDatabase(){
        productList.clear();
        productListView.setAdapter(null);

        db = openOrCreateDatabase("products.db",MODE_PRIVATE,null);
        new DBHelper(db); //initialize db

        try{
            Cursor resultSet = db.rawQuery("SELECT * FROM products",null);
            resultSet.moveToFirst();
            while(resultSet.isAfterLast() == false){
                String tmpExpir = resultSet.getString(5);
                if(tmpExpir == null){
                    tmpExpir = "00000000";
                }
                productList.add(new Product(resultSet.getInt(0),resultSet.getString(1),resultSet.getString(2),resultSet.getString(3),resultSet.getString(4),tmpExpir));
                resultSet.moveToNext();
            }
        } catch (Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show(); //TODO remove
            Log.d("PHILZ",e.toString());
        }

        sortAllItemsInList();
    }

    public void xml(String xml) {
        db = openOrCreateDatabase("products.db",MODE_PRIVATE,null);

        try{
            JSONObject reader = new JSONObject(xml);
            JSONObject product  = reader.getJSONObject("product");

            String pname = "", pbrand = "", pimage = "", pcode = "";

            if(product.has("product_name")) {
                pname += product.getString("product_name");
                if(product.has("brands")) {
                    pbrand += product.getString("brands");
                }
                if(product.has("image_front_url")) {
                    pimage += product.getString("image_front_url");
                }
                if(product.has("code")) {
                    pcode += product.getString("code");
                }

                db.execSQL("INSERT INTO products(barcode,name,image,company) " +
                        "VALUES('" + pcode + "','" + pname + "','" + pimage + "','" + pbrand + "')");

                //new DownloadImageTask((ImageView) findViewById(R.id.imgv)).execute(product.getString("image_front_url"));
            } else {
                Toast.makeText(this, "Error in Application", Toast.LENGTH_SHORT).show();
            }

        } catch(Exception e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            Log.d("PHILZ",e.toString());
        }

        fetchDatabase();
    }

    private void newProductDialog(final String bar){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add new product manually");
        builder.setCancelable(true); //changed this

        final EditText one = new EditText(this);
        one.setHint("Product Name");
        final EditText two = new EditText(this);
        two.setHint("Product Brand");

        // build dialog with edittexts
        one.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        two.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(one);
        lay.addView(two);

        builder.setView(lay);

        two.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return true;
                }
                return false;
            }
        });

        builder.setNeutralButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Boolean boxFilled = (one.getText().toString().trim().isEmpty());
                //Boolean boxFilled2 = (two.getText().toString().trim().isEmpty()); // it is not necessary to putin a company name, e.g. an apple


                //if (!boxFilled && !boxFilled2) {
                if (!boxFilled) {
                    if(bar != null) {
                        db.execSQL("INSERT INTO products(barcode,name,company) " +
                                "VALUES('" + bar + "','" + one.getText() + "','" + two.getText() + "')");
                    } else {
                        db.execSQL("INSERT INTO products(name,company) " +
                                "VALUES('" + one.getText() + "','" + two.getText() + "')");
                    }

                    openDatePicker(-1);
                }

                /*
                if (!wantToCloseDialog && !wantToCloseDialog2) {
                    UploadInfo2OpenFoodFacts uploadInfo2OpenFoodFacts = new UploadInfo2OpenFoodFacts(MainActivity.this,bar,one.getText().toString(),two.getText().toString());
                }
                */
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        builder.show();
    }

    //used for the media folder coming later
    private void createNoMedia(){
        try {
            FileOutputStream fOut = openFileOutput(".nomedia",
                    MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            osw.write("");

            osw.flush();
            osw.close();

        } catch (IOException ioe)
        {ioe.printStackTrace();}
    }


    //persistent images

    private File getOutputMediaFile(String bar){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        File mediaFile;
        String mImageName = bar;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    private void storeImage(Bitmap image, String bar) {
        File pictureFile = getOutputMediaFile(bar);
        if (pictureFile == null) {
            Log.d("philo", "Error creating media file, check storage permissions: ");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("philo", "File not found: " + e.getMessage());
            Log.d("PHILZ",e.toString());
        } catch (IOException e) {
            Log.d("philo", "Error accessing file: " + e.getMessage());
            Log.d("PHILZ",e.toString());
        }
        /*
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/Android/data/eu.roggstar.roggenbuck.homefinder/files/h"+house_id+".jpg", options);
        imageV.setImageBitmap(bitmap);
         */
    }


    //Overrides

    //photo for upload
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_SHORT).show();
                getJSON getJSON = new getJSON(this,result.getContents());
                getJSON.execute();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if(false){
            Uri image = null;
            String path = null;

            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    image = data.getData();
                    //iv.setImageURI(image);
                    //iv.setVisibility(View.VISIBLE);
                }
                if (image == null && path != null) {
                    image = Uri.fromFile(new File(path));
                    //iv.setImageURI(image);
                    //iv.setVisibility(View.VISIBLE);
                }
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }
    }

    @Override
    public void onUploadTaskCompleted(String result){
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        Log.d("Philz",result);
    }

    @Override
    public void onGetJSONCompleted(String result, String bar) {
        if(result.contains("not found")){
            Toast.makeText(this, "Product not found in OpenFoodFacts", Toast.LENGTH_SHORT).show();

            newProductDialog(bar);
        } else {
            xml(result);
            openDatePicker(-1);
        }
    }


    //TODO
    @Override
    public void onPicDownloaded(String result, String bar) {
        byte [] encodeByte= Base64.decode(result,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        storeImage(bitmap,bar);
        Toast.makeText(this, "Got Pic!", Toast.LENGTH_SHORT).show();
    }

}