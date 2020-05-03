package eu.roggstar.FoodATHome;

import android.database.sqlite.SQLiteDatabase;

class DBHelper {

    public DBHelper(SQLiteDatabase db){
        onCreate(db);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS products(" +
                        "id integer primary key autoincrement," +
                        "barcode text," +
                        "name text not null," +
                        "image text," +
                        "company text," +
                        "expiring text," +
                        "storage integer" +
                        ")"
        );

    }
}
