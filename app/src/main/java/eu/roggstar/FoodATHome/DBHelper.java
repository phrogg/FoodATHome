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
                        "barcode text not null," +
                        "name text not null," +
                        "image text not null," +
                        "company text not null," +
                        "expiring text," +
                        "storage integer" +
                        ")"
        );

    }
}
